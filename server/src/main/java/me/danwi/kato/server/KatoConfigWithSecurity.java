package me.danwi.kato.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

/**
 * 当Security被引入的时候,配置能处理AccessDeniedException等异常的ResponseBodyAdvice
 */
@Configuration("me.danwi.kato.ConfigWithSecurity")
@ConditionalOnClass({AccessDeniedException.class, AuthenticationException.class})
public class KatoConfigWithSecurity {
    @Bean("me.danwi.kato.ResponseBodyAdviceWithSecurity")
    KatoResponseBodyAdviceWithSecurity exceptionHandler() {
        return new KatoResponseBodyAdviceWithSecurity();
    }
}
