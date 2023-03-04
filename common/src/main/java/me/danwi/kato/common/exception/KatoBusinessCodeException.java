package me.danwi.kato.common.exception;

/**
 * 根据code定义不同逻辑处理异常
 */
public class KatoBusinessCodeException extends KatoBusinessException {
    private final int code;

    public KatoBusinessCodeException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
