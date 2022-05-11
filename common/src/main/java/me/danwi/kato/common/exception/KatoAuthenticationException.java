package me.danwi.kato.common.exception;

/**
 * 认证异常
 */
public class KatoAuthenticationException extends KatoCommonException {
    public KatoAuthenticationException() {
        this("认证错误");
    }

    public KatoAuthenticationException(String message) {
        super(message);
    }

    public KatoAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoAuthenticationException(Throwable cause) {
        super(cause);
    }
}
