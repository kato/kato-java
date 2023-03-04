package me.danwi.kato.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 请求验证不通过抛出异常
 */
public class KatoBadRequestException extends KatoException {

    public KatoBadRequestException() {
        super("请求验证失败");
    }

    public KatoBadRequestException(String message) {
        super(message);
    }

    public KatoBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public KatoBadRequestException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
