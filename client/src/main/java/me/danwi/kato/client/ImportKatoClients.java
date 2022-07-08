package me.danwi.kato.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableFeignClients
public @interface ImportKatoClients {
    @AliasFor(annotation = EnableFeignClients.class)
    String[] value() default {};

    @AliasFor(annotation = EnableFeignClients.class)
    Class<?>[] clients() default {};

    @AliasFor(annotation = EnableFeignClients.class)
    String[] basePackages() default {};

    @AliasFor(annotation = EnableFeignClients.class)
    Class<?>[] basePackageClasses() default {};

    @AliasFor(annotation = EnableFeignClients.class)
    Class<?>[] defaultConfiguration() default {};

}
