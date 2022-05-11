package me.danwi.kato.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置通用的ResponseBodyAdvice
 */
@Configuration("me.danwi.kato.Config")
@ConditionalOnMissingClass({
        "org.springframework.security.access.AccessDeniedException",
        "org.springframework.security.core.AuthenticationException",
})
public class KatoConfig {
    @Bean("me.danwi.kato.ResponseBodyAdvice")
    KatoResponseBodyAdvice exceptionHandler() {
        return new KatoResponseBodyAdvice();
    }
}
