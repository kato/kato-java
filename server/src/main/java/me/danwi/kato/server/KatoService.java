package me.danwi.kato.server;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@RestController
public @interface KatoService {
    @AliasFor(annotation = RestController.class)
    String value() default "";
}
