package me.danwi.kato.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("me.danwi.kato.client.Config")
public class KatoClientConfig {

    @Bean("me.danwi.kato.client.ErrorDecoder")
    ErrorDecoder errorDecoder() {
        return new KatoErrorDecoder();
    }
}
