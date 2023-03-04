package me.danwi.kato.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 权限不足
 */
public class KatoAccessDeniedException extends KatoException {
    public KatoAccessDeniedException() {
        this("权限不足");
    }

    public KatoAccessDeniedException(String message) {
        super(message);
    }

    public KatoAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoAccessDeniedException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
