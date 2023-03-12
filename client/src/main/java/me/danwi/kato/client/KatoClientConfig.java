package me.danwi.kato.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class KatoClientConfig {
    @Bean("me.danwi.kato.client.default.katoErrorDecoder")
    ErrorDecoder katoErrorDecoder() {
        return new KatoErrorDecoder();
    }
}
