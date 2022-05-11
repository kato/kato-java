package me.danwi.kato.common.exception;

/**
 * Kato异常
 */
@ExceptionIdentify
public class KatoException extends RuntimeException {
    public KatoException(String message) {
        super(message);
    }

    public KatoException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoException(Throwable cause) {
        super(cause);
    }
}
