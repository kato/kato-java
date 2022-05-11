package me.danwi.kato.common.exception;

/**
 * 未被业务代码处理的异常,通常出现这种异常需要引起开发人员的注意
 */
public class KatoUndeclaredException extends KatoException {
    public KatoUndeclaredException(Throwable cause) {
        super(cause);
    }
}
