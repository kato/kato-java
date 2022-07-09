package me.danwi.kato.server;

import me.danwi.kato.server.argument.MethodArgumentHandlerConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({KatoConfig.class, KatoConfigWithSecurity.class, MethodArgumentHandlerConfig.class})
public @interface EnableKatoServer {
}

