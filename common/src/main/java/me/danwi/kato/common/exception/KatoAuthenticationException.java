package me.danwi.kato.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 认证异常
 */
public class KatoAuthenticationException extends KatoException  {
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

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
