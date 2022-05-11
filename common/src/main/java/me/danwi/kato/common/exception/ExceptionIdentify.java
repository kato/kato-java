package me.danwi.kato.common.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表该异常在调用的传输过程中可以被唯一定位
 * 默认使用类全限定名,机制类似java中的序列化ID
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionIdentify {
}
