package me.danwi.kato.common.exception;

/**
 * 权限不足
 */
public class KatoAccessDeniedException extends KatoCommonException {
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

}
