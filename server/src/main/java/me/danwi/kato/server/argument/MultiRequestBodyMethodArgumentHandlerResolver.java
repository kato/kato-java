package me.danwi.kato.server.argument;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import kotlin.reflect.KFunction;
import kotlin.reflect.KType;
import kotlin.reflect.jvm.ReflectJvmMapping;
import me.danwi.kato.common.argument.MultiRequestBody;
import me.danwi.kato.common.exception.KatoBadRequestException;
import me.danwi.kato.server.PassByKato;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 解析请求：body
 * <p> 支持特性：
 * <ul>
 *  <li>  不限制请求类型，支持GET及其他类型请求
 * </ul>
 * <p> 如果JSON是Object：
 * <ul>
 *  <li>  支持通过注解value，指定JSON的key来解析对象
 *  <li>  支持注解无value，直接根据参数名来解析对象
 *  <li>  支持注解无value且参数名不匹配JSON串key时，整个body作为参数解析
 *  <li>  支持参数“共用”（不指定value时，参数名不为JSON串的key时，解析整个body）
 *  <li>  支持多余属性不报错（需配置{@link ObjectMapper#configure(DeserializationFeature, boolean)}为FAIL_ON_UNKNOWN_PROPERTIES(false)）
 * </ul>
 * <p> 如果JSON不是Object：
 * <ul>
 *  <li>  默认整个body作为参数解析
 * </ul>
 */
public class MultiRequestBodyMethodArgumentHandlerResolver implements HandlerMethodArgumentResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiRequestBodyMethodArgumentHandlerResolver.class);

    private static final String KATO_JSON_NODE_KEY = "_KATO_JSON_NODE_KEY_";

    private final ObjectMapper mapper;

    public MultiRequestBodyMethodArgumentHandlerResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        final Method method = methodParameter.getMethod();
        if (method == null) {
            return false;
        }

        //  TODO 类（katoService）
        return methodParameter.getDeclaringClass().getAnnotation(PassByKato.class) == null
                && method.getAnnotation(PassByKato.class) == null
                && !methodParameter.hasParameterAnnotation(PassByKato.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, @NotNull NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
        // 定义结果
        Object result = null;
        // 错误信息暂存
        Exception temp = null;

        // 获取参数配置信息
        final ParamInfo paramInfo = getParamInfo(methodParameter);

        try {
            // 获取请求body字符串
            final JsonNode rootNode = getJsonNode(nativeWebRequest);

            if (!ObjectUtils.isEmpty(rootNode)) {
                // 尝试获取field
                JsonNode node = rootNode.get(paramInfo.key);
                // 如果json中不存在与参数名称相对应的field,且开启了全body映射
                if ((node == null || node.isNull()) && paramInfo.parseBodyIfMissKey) {
                    try {
                        result = readValue(methodParameter, rootNode);
                    } catch (Exception e) {
                        // 忽略 此时result=null，后续进行是否必填验证
                        temp = e;
                    }
                } else {
                    result = readValue(methodParameter, node);
                }
            }
        } catch (Exception e) {
            throw new KatoBadRequestException(e.getMessage(), e);
        }

        boolean isJavaCode = true;
        KType type = null;
        final Method method = methodParameter.getMethod();
        if (method != null) {
            final KFunction<?> kotlinFunction = ReflectJvmMapping.getKotlinFunction(method);
            if (kotlinFunction != null) {
                type = kotlinFunction
                        .getParameters()
                        .get(methodParameter.getParameterIndex() + 1)
                        .getType();
                isJavaCode = type.toString().endsWith("!");
            }
        }

        // 是否必填验证
        if (Objects.isNull(result) && (isJavaCode ? paramInfo.required : !type.isMarkedNullable())) {
            throw new KatoBadRequestException(String.format("resolve [%s] error: %s", paramInfo.key, temp != null ? temp.getMessage() : "need key"), temp);
        }
        LOGGER.debug("解析参数：key={}，value={}", paramInfo.key, result);
        return result;
    }

    private Object readValue(MethodParameter methodParameter, JsonNode rootNode) throws IOException {
        return mapper.readValue(mapper.treeAsTokens(rootNode), new TypeReference<>() {
            @Override
            public Type getType() {
                return methodParameter.getGenericParameterType();
            }
        });
    }

    private ParamInfo getParamInfo(MethodParameter methodParameter) {
        final ParamInfo paramInfo = new ParamInfo();
        MultiRequestBody parameterAnnotation = methodParameter.getParameterAnnotation(MultiRequestBody.class);
        // 获取key
        if (parameterAnnotation == null) {
            paramInfo.key = methodParameter.getParameterName();
        } else {
            paramInfo.key = parameterAnnotation.value();
            if (ObjectUtils.isEmpty(paramInfo.key)) {
                paramInfo.key = methodParameter.getParameterName();
            }
            paramInfo.required = parameterAnnotation.required();
            paramInfo.parseBodyIfMissKey = parameterAnnotation.parseBodyIfMissKey();
        }
        // 校验是否获取到 key
        if (ObjectUtils.isEmpty(paramInfo.key)) {
            throw new IllegalStateException("JVM 版本不支持自动获取参数名，请手动使用 MultiRequestBody 注解指定value作为key");
        }
        return paramInfo;
    }

    /**
     * 多个参数解析时，从attribute中获取数据
     *
     * @param nativeWebRequest
     * @return JsonNode
     * @throws IOException
     */
    private JsonNode getJsonNode(NativeWebRequest nativeWebRequest) throws IOException {
        Object attribute = nativeWebRequest.getAttribute(KATO_JSON_NODE_KEY, WebRequest.SCOPE_REQUEST);
        if (ObjectUtils.isEmpty(attribute)) {
            HttpServletRequest servletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
            Assert.state(servletRequest != null, "No HttpServletRequest");
            attribute = mapper.readTree(servletRequest.getInputStream());
            nativeWebRequest.setAttribute(KATO_JSON_NODE_KEY, attribute, WebRequest.SCOPE_REQUEST);

            LOGGER.debug("解析请求数据 [{}] 为 [{}]", nativeWebRequest.getHeader("accept"), attribute);
        }
        return (JsonNode) attribute;
    }

    private static class ParamInfo {
        String key = "";
        boolean required = true;
        boolean parseBodyIfMissKey = true;
    }
}
