package me.danwi.kato.common.argument;

import java.lang.annotation.*;

/**
 * @see MultiRequestBodyMethodArgumentHandlerResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiRequestBody {

    /**
     * 设置解析key
     */
    String value() default "";

    /**
     * 是否必填参数
     */
    boolean required() default true;

    /**
     * 当body不是JSONObject或者不能匹配到key时
     * 是否将整个body作为参数解析
     */
    boolean parseBodyIfMissKey() default true;

}
