package me.danwi.kato.common;

import java.util.Map;

/**
 * kato调用异常结果表达
 */
public class ExceptionResult {
     private String id;
     private String message;
     private Map<String, Object> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
