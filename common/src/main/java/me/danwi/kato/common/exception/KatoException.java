package me.danwi.kato.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Kato异常
 */
@ExceptionIdentify
public class KatoException extends RuntimeException implements HttpStatusHolder {
    public KatoException(String message) {
        super(message);
    }

    public KatoException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
