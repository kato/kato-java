package me.danwi.kato.common;

/**
 * kato调用结果表达
 *
 * @param <T> 成功时代表返回的数据,失败时存储着异常的附加数据(如果有的话)
 */
public class Result<T> {
    private String exception;
    private String message;
    private T data;

    public Result() {
    }

    public Result(T data) {
        this.message = "调用成功";
        this.data = data;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
