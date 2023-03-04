package me.danwi.kato.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FeignClient
public @interface KatoClient {

    @AliasFor(annotation = FeignClient.class)
    String value() default "";

    @AliasFor(annotation = FeignClient.class)
    String contextId() default "";

    @AliasFor(annotation = FeignClient.class)
    String name() default "";

    @AliasFor(annotation = FeignClient.class)
    String[] qualifiers() default {};

    @AliasFor(annotation = FeignClient.class)
    String url() default "";

    @AliasFor(annotation = FeignClient.class)
    boolean dismiss404() default false;

    @AliasFor(annotation = FeignClient.class)
    Class<?>[] configuration() default {KatoClientConfig.class};

    @AliasFor(annotation = FeignClient.class)
    Class<?> fallback() default void.class;

    @AliasFor(annotation = FeignClient.class)
    Class<?> fallbackFactory() default void.class;

    @AliasFor(annotation = FeignClient.class)
    String path() default "";

    @AliasFor(annotation = FeignClient.class)
    boolean primary() default true;
}
