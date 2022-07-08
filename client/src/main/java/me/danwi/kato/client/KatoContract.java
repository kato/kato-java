package me.danwi.kato.client;

import feign.MethodMetadata;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.convert.ConversionService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author wjy
 */
public class KatoContract extends SpringMvcContract {


    public KatoContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors, ConversionService conversionService, boolean decodeSlash) {
        super(annotatedParameterProcessors, conversionService, decodeSlash);
    }

    @Override
    public MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        final MethodMetadata methodMetadata = super.parseAndValidateMetadata(targetType, method);

        return methodMetadata;
    }


    /**
     * 如果没有注解 或者注解是 MultiRequestBody 返回ture
     */
    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        return super.processAnnotationsOnParameter(data, annotations, paramIndex);
    }
}
