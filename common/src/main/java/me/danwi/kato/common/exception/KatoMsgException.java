package me.danwi.kato.common.exception;

/**
 * 语义化提醒异常，message 内容可以直接给用户看
 */
public class KatoMsgException extends KatoBusinessException {

    public KatoMsgException(String message) {
        super(message);
    }

    public KatoMsgException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoMsgException(Throwable cause) {
        super(cause);
    }
}
