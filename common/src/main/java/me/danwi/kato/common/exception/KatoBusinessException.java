package me.danwi.kato.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务异常，正常参与业务逻辑异常，通常用于：
 * 1. 消息提醒
 * 2. 自定义业务操作
 * HTTP Response 处理后 状态码为 200
 */
public class KatoBusinessException extends KatoException {

    public KatoBusinessException() {
        super("未定义业务");
    }

    public KatoBusinessException(String message) {
        super(message);
    }

    public KatoBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoBusinessException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        // TODO HttpStatus.SERVER_ERROR
        return HttpStatus.OK;
    }
}