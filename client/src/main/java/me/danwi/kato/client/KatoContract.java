package me.danwi.kato.client;

import feign.AlwaysEncodeBodyContract;
import feign.MethodMetadata;
import feign.Param;
import feign.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.annotation.*;
import org.springframework.cloud.openfeign.encoding.HttpEncoding;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;
import static java.util.Optional.ofNullable;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * 参考 SpringMvcContract 实现
 */
public class KatoContract extends AlwaysEncodeBodyContract implements ResourceLoaderAware {

    private static final Log LOG = LogFactory.getLog(KatoContract.class);

    private static final String ACCEPT = "Accept";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

    private static final TypeDescriptor ITERABLE_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Iterable.class);

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final ConversionService conversionService;

    private final ConvertingExpanderFactory convertingExpanderFactory;

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    private final boolean decodeSlash;


    public KatoContract() {
        this(Collections.emptyList());
    }

    public KatoContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors) {
        this(annotatedParameterProcessors, new DefaultConversionService());
    }

    public KatoContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors,
                        ConversionService conversionService) {
        this(annotatedParameterProcessors, conversionService, true);
    }

    public KatoContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors,
                        ConversionService conversionService, boolean decodeSlash) {
        Assert.notNull(annotatedParameterProcessors, "Parameter processors can not be null.");
        Assert.notNull(conversionService, "ConversionService can not be null.");

        this.conversionService = conversionService;
        convertingExpanderFactory = new KatoContract.ConvertingExpanderFactory(conversionService);
        this.decodeSlash = decodeSlash;

        init(annotatedParameterProcessors);
    }


    public void init(List<AnnotatedParameterProcessor> annotatedParameterProcessors) {
        // 注册类注解处理器
        registerClassAnnotationProcessor();
        // 注册方法注解处理器
        registerMethodAnnotationProcessor();
        // 注册参数注解处理
        registerParameterAnnotationProcessor(annotatedParameterProcessors);
    }

    private void registerClassAnnotationProcessor() {
        registerClassAnnotation(
                RequestMapping.class,
                (annotation, metadata) -> {
                    LOG.error("Cannot process class: " + metadata.targetType().getName()
                            + ". @RequestMapping annotation is not allowed on @FeignClient interfaces.");
                    throw new IllegalArgumentException("@RequestMapping annotation not allowed on @FeignClient interfaces");
                }
        );
        registerClassAnnotation(
                CollectionFormat.class,
                (annotation, metadata) -> metadata.template().collectionFormat(annotation.value())
        );
    }

    private void registerMethodAnnotationProcessor() {
        registerMethodAnnotation(
                CollectionFormat.class,
                (annotation, metadata) -> metadata.template().collectionFormat(annotation.value())
        );
        registerMethodAnnotation(
                (annotation) -> annotation instanceof RequestMapping || annotation.annotationType().isAnnotationPresent(RequestMapping.class),
                (annotation, data) -> {
                    Method method = data.method();
                    RequestMapping methodMapping = findMergedAnnotation(method, RequestMapping.class);
                    // HTTP Method
                    RequestMethod[] methods = methodMapping.method();
                    if (methods.length == 0) {
                        methods = new RequestMethod[]{RequestMethod.GET};
                    }
                    checkOne(method, methods, "method");
                    data.template().method(Request.HttpMethod.valueOf(methods[0].name()));

                    // path
                    checkAtMostOne(method, methodMapping.value(), "value");
                    if (methodMapping.value().length > 0) {
                        String pathValue = emptyToNull(methodMapping.value()[0]);
                        if (pathValue != null) {
                            pathValue = resolve(pathValue);
                            // Append path from @RequestMapping if value is present on method
                            if (!pathValue.startsWith("/") && !data.template().path().endsWith("/")) {
                                pathValue = "/" + pathValue;
                            }
                            data.template().uri(pathValue, true);
                            if (data.template().decodeSlash() != this.decodeSlash) {
                                data.template().decodeSlash(this.decodeSlash);
                            }
                        }
                    }

                    // produces
                    parseProduces(data, method, methodMapping);

                    // consumes
                    parseConsumes(data, method, methodMapping);

                    // headers
                    parseHeaders(data, method, methodMapping);

                    data.indexToExpander(new LinkedHashMap<>());
                }
        );
    }

    private static TypeDescriptor createTypeDescriptor(Method method, int paramIndex) {
        Parameter parameter = method.getParameters()[paramIndex];
        MethodParameter methodParameter = MethodParameter.forParameter(parameter);
        TypeDescriptor typeDescriptor = new TypeDescriptor(methodParameter);

        // Feign applies the Param.Expander to each element of an Iterable, so in those
        // cases we need to provide a TypeDescriptor of the element.
        if (typeDescriptor.isAssignableTo(ITERABLE_TYPE_DESCRIPTOR)) {
            TypeDescriptor elementTypeDescriptor = getElementTypeDescriptor(typeDescriptor);

            checkState(elementTypeDescriptor != null,
                    "Could not resolve element type of Iterable type %s. Not declared?", typeDescriptor);

            typeDescriptor = elementTypeDescriptor;
        }
        return typeDescriptor;
    }

    private static TypeDescriptor getElementTypeDescriptor(TypeDescriptor typeDescriptor) {
        TypeDescriptor elementTypeDescriptor = typeDescriptor.getElementTypeDescriptor();
        // that means it's not a collection but it is iterable, gh-135
        if (elementTypeDescriptor == null && Iterable.class.isAssignableFrom(typeDescriptor.getType())) {
            ResolvableType type = typeDescriptor.getResolvableType().as(Iterable.class).getGeneric(0);
            if (type.resolve() == null) {
                return null;
            }
            return new TypeDescriptor(type, null, typeDescriptor.getAnnotations());
        }
        return elementTypeDescriptor;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        return super.parseAndValidateMetadata(targetType, method);
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value) && resourceLoader instanceof ConfigurableApplicationContext) {
            return ((ConfigurableApplicationContext) resourceLoader).getEnvironment().resolvePlaceholders(value);
        }
        return value;
    }

    private void checkAtMostOne(Method method, Object[] values, String fieldName) {
        checkState(values != null && (values.length == 0 || values.length == 1),
                "Method %s can only contain at most 1 %s field. Found: %s", method.getName(), fieldName,
                values == null ? null : Arrays.asList(values));
    }

    private void checkOne(Method method, Object[] values, String fieldName) {
        checkState(values != null && values.length == 1, "Method %s can only contain 1 %s field. Found: %s",
                method.getName(), fieldName, values == null ? null : Arrays.asList(values));
    }

    private void parseProduces(MethodMetadata md, Method method, RequestMapping annotation) {
        String[] serverProduces = annotation.produces();
        String clientAccepts = serverProduces.length == 0 ? null : emptyToNull(serverProduces[0]);
        if (clientAccepts != null) {
            md.template().header(ACCEPT, clientAccepts);
        }
    }

    private void parseConsumes(MethodMetadata md, Method method, RequestMapping annotation) {
        String[] serverConsumes = annotation.consumes();
        String clientProduces = serverConsumes.length == 0 ? null : emptyToNull(serverConsumes[0]);
        if (clientProduces != null) {
            md.template().header(CONTENT_TYPE, clientProduces);
        }
    }

    private void parseHeaders(MethodMetadata md, Method method, RequestMapping annotation) {
        // TODO: only supports one header value per key
        if (annotation.headers() != null && annotation.headers().length > 0) {
            for (String header : annotation.headers()) {
                int index = header.indexOf('=');
                if (!header.contains("!=") && index >= 0) {
                    md.template().header(resolve(header.substring(0, index)),
                            resolve(header.substring(index + 1).trim()));
                }
            }
        }
    }


    private void registerParameterAnnotationProcessor(List<AnnotatedParameterProcessor> annotatedArgumentResolvers) {

        annotatedArgumentResolvers.add(new MatrixVariableParameterProcessor());
        annotatedArgumentResolvers.add(new PathVariableParameterProcessor());
        annotatedArgumentResolvers.add(new RequestParamParameterProcessor());
        annotatedArgumentResolvers.add(new RequestHeaderParameterProcessor());
        annotatedArgumentResolvers.add(new QueryMapParameterProcessor());
        annotatedArgumentResolvers.add(new RequestPartParameterProcessor());
        annotatedArgumentResolvers.add(new CookieValueParameterProcessor());

        annotatedArgumentResolvers.forEach(annotatedParameterProcessor -> registerParameterAnnotation(
                annotatedParameterProcessor.getAnnotationType(),
                (ParameterAnnotationProcessor) (annotation, metadata, paramIndex) -> {
                    // synthesize, handling @AliasFor, while falling back to parameter name on
                    // missing String #value():
                    Annotation processParameterAnnotation = synthesizeWithMethodParameterNameAsFallbackValue(annotation, metadata.method(), paramIndex);
                    boolean isHttpAnnotation = annotatedParameterProcessor.processArgument(new SimpleAnnotatedParameterContext(metadata, paramIndex), processParameterAnnotation, metadata.method());
                    if (!isMultipartFormData(metadata) && isHttpAnnotation && metadata.indexToExpander().get(paramIndex) == null) {
                        TypeDescriptor typeDescriptor = createTypeDescriptor(metadata.method(), paramIndex);
                        if (conversionService.canConvert(typeDescriptor, STRING_TYPE_DESCRIPTOR)) {
                            Param.Expander expander = convertingExpanderFactory.getExpander(typeDescriptor);
                            metadata.indexToExpander().put(paramIndex, expander);
                        }
                    }
                }
        ));
    }


    private Annotation synthesizeWithMethodParameterNameAsFallbackValue(Annotation parameterAnnotation, Method
            method,
                                                                        int parameterIndex) {
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(parameterAnnotation);
        Object defaultValue = AnnotationUtils.getDefaultValue(parameterAnnotation);
        if (defaultValue instanceof String && defaultValue.equals(annotationAttributes.get(AnnotationUtils.VALUE))) {
            Type[] parameterTypes = method.getGenericParameterTypes();
            String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
            if (shouldAddParameterName(parameterIndex, parameterTypes, parameterNames)) {
                annotationAttributes.put(AnnotationUtils.VALUE, parameterNames[parameterIndex]);
            }
        }
        return AnnotationUtils.synthesizeAnnotation(annotationAttributes, parameterAnnotation.annotationType(), null);
    }

    private boolean shouldAddParameterName(int parameterIndex, Type[] parameterTypes, String[] parameterNames) {
        // has a parameter name
        return parameterNames != null && parameterNames.length > parameterIndex
                // has a type
                && parameterTypes != null && parameterTypes.length > parameterIndex;
    }

    private boolean isMultipartFormData(MethodMetadata data) {
        Collection<String> contentTypes = data.template().headers().get(HttpEncoding.CONTENT_TYPE);

        if (contentTypes != null && !contentTypes.isEmpty()) {
            String type = contentTypes.iterator().next();
            try {
                return Objects.equals(MediaType.valueOf(type), MediaType.MULTIPART_FORM_DATA);
            } catch (InvalidMediaTypeException ignored) {
                return false;
            }
        }

        return false;
    }

    private static class ConvertingExpanderFactory {

        private final ConversionService conversionService;

        ConvertingExpanderFactory(ConversionService conversionService) {
            this.conversionService = conversionService;
        }

        Param.Expander getExpander(TypeDescriptor typeDescriptor) {
            return value -> {
                Object converted = conversionService.convert(value, typeDescriptor, STRING_TYPE_DESCRIPTOR);
                return (String) converted;
            };
        }

    }

    private class SimpleAnnotatedParameterContext implements AnnotatedParameterProcessor.AnnotatedParameterContext {

        private final MethodMetadata methodMetadata;

        private final int parameterIndex;

        SimpleAnnotatedParameterContext(MethodMetadata methodMetadata, int parameterIndex) {
            this.methodMetadata = methodMetadata;
            this.parameterIndex = parameterIndex;
        }

        @Override
        public MethodMetadata getMethodMetadata() {
            return methodMetadata;
        }

        @Override
        public int getParameterIndex() {
            return parameterIndex;
        }

        @Override
        public void setParameterName(String name) {
            nameParam(methodMetadata, name, parameterIndex);
        }

        @Override
        public Collection<String> setTemplateParameter(String name, Collection<String> rest) {
            Collection<String> params = ofNullable(rest).map(ArrayList::new).orElse(new ArrayList<>());
            params.add(String.format("{%s}", name));
            return params;
        }

    }
}
