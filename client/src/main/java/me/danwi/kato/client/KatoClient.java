package me.danwi.kato.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FeignClient(configuration = KatoClientConfig.class)
public @interface KatoClient {
    @AliasFor(annotation = FeignClient.class)
    String value();

    @AliasFor(annotation = FeignClient.class)
    String url();
}
