package me.danwi.kato.common.exception;

import java.lang.annotation.*;

/**
 * 代表该异常在调用的传输过程中可以被唯一定位
 * 默认使用类全限定名,机制类似java中的序列化ID
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExceptionIdentify {
}
