package me.danwi.kato.client;

import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("me.danwi.kato.client.Config")
public class KatoClientConfig {
    @Bean("me.danwi.kato.client.ResultDecoder")
    Decoder resultDecoder() {
        return new KatoResultDecoder();
    }
}
