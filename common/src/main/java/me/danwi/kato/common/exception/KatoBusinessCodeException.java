package me.danwi.kato.common.exception;

import java.util.Map;
import java.util.Optional;

/**
 * 根据code定义不同逻辑处理异常
 */
public class KatoBusinessCodeException extends KatoBusinessException implements ExceptionExtraDataHolder {
    public static final String CODE_KEY = "code";
    private Integer code;

    public KatoBusinessCodeException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of(CODE_KEY, code);
    }

    @Override
    public void loadFromMap(Map<String, Object> map) {
        if (map != null)
            Optional.ofNullable((String) map.get(CODE_KEY)).ifPresent(it -> this.code = Integer.valueOf(it));
    }

}
