package me.danwi.kato.client;

import feign.Contract;
import feign.codec.ErrorDecoder;
import me.danwi.kato.common.argument.MultiRequestBody;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration("me.danwi.kato.client.Config")
public class KatoClientConfig {

    @Autowired(required = false)
    private FeignClientProperties feignClientProperties;


    @Bean("me.danwi.kato.client.ErrorDecoder")
    ErrorDecoder errorDecoder() {
        return new KatoErrorDecoder();
    }

    @Bean
    public Contract katoContract(ObjectProvider<List<AnnotatedParameterProcessor>> parameterProcessors, ObjectProvider<FeignClientProperties> feignClientProperties, ConversionService feignConversionService) {
        AtomicBoolean decodeSlash = new AtomicBoolean(true);
        feignClientProperties.ifAvailable(fc -> decodeSlash.set(fc.isDecodeSlash()));

        List<AnnotatedParameterProcessor> processors = new ArrayList<>();
        processors.add(new AnnotatedParameterProcessor() {
            @Override
            public Class<? extends Annotation> getAnnotationType() {
                return MultiRequestBody.class;
            }

            @Override
            public boolean processArgument(AnnotatedParameterContext context, Annotation annotation, Method method) {
                return true;
            }
        });
        parameterProcessors.ifAvailable(processors::addAll);

        return new KatoContract(processors, feignConversionService, decodeSlash.get());
    }
}
