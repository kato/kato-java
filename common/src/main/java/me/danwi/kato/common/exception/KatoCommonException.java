package me.danwi.kato.common.exception;

/**
 * 通用异常,语意化异常
 * 该异常信息可以直接被界面/日志显示
 */
public class KatoCommonException extends KatoException {
    public KatoCommonException(String message) {
        this(message, "未知错误");
    }

    public KatoCommonException(String message, String defaultMessage) {
        super(message == null ? defaultMessage : message);
    }
}