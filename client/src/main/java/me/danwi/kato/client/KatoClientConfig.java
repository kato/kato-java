package me.danwi.kato.client;

import feign.Contract;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.MultipartFormContentProcessor;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.cloud.openfeign.support.AbstractFormWriter;
import org.springframework.cloud.openfeign.support.FeignEncoderProperties;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static feign.form.ContentType.MULTIPART;

public class KatoClientConfig {

    private final ObjectFactory<HttpMessageConverters> messageConverters;
    private final FeignEncoderProperties encoderProperties;

    public KatoClientConfig(ObjectFactory<HttpMessageConverters> messageConverters, FeignEncoderProperties encoderProperties) {
        this.messageConverters = messageConverters;
        this.encoderProperties = encoderProperties;
    }

    @Bean("me.danwi.kato.client.default.katoErrorDecoder")
    ErrorDecoder katoErrorDecoder() {
        return new KatoErrorDecoder();
    }

    @Bean("me.danwi.kato.client.default.katoEncoder")
    public Encoder katoEncoder(
            ObjectProvider<AbstractFormWriter> formWriterProvider,
            ObjectProvider<HttpMessageConverterCustomizer> customizer
    ) {
        return new KatoEncoder(springEncoder(formWriterProvider, encoderProperties, customizer));
    }

    @Bean("me.danwi.kato.client.default.katoContract")
    public Contract katoContract(ObjectProvider<List<AnnotatedParameterProcessor>> parameterProcessors, ObjectProvider<FeignClientProperties> feignClientProperties, ConversionService feignConversionService) {
        AtomicBoolean decodeSlash = new AtomicBoolean(true);
        feignClientProperties.ifAvailable(fc -> decodeSlash.set(fc.isDecodeSlash()));

        List<AnnotatedParameterProcessor> processors = new ArrayList<>();
        parameterProcessors.ifAvailable(processors::addAll);

        return new KatoContract(processors, feignConversionService, decodeSlash.get());
    }

    private Encoder springEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider,
            FeignEncoderProperties encoderProperties, ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        AbstractFormWriter formWriter = formWriterProvider.getIfAvailable();

        if (formWriter != null) {
            return new SpringEncoder(new SpringPojoFormEncoder(formWriter), messageConverters, encoderProperties,
                    customizers);
        } else {
            return new SpringEncoder(new SpringFormEncoder(), messageConverters, encoderProperties, customizers);
        }
    }

    private static class SpringPojoFormEncoder extends SpringFormEncoder {

        SpringPojoFormEncoder(AbstractFormWriter formWriter) {
            super();
            MultipartFormContentProcessor processor = (MultipartFormContentProcessor) getContentProcessor(MULTIPART);
            processor.addFirstWriter(formWriter);
        }

    }
}
