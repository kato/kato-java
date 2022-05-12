package me.danwi.kato.client.exception;

import me.danwi.kato.common.exception.KatoException;

/**
 * 在调用过程中发生在客户端侧的异常
 */
public class KatoClientException extends KatoException {
    public KatoClientException(String message) {
        super(message);
    }

    public KatoClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoClientException(Throwable cause) {
        super(cause);
    }
}
