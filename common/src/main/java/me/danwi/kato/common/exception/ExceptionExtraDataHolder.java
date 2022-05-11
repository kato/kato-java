package me.danwi.kato.common.exception;

import java.util.Map;

/**
 * 异常在引发方和调用方之间可以附带更为丰富的表达信息
 * 类似json的序列化/反序列化
 * 在实现时不要处理异常的message,message已经通过其他的途径表示
 */
public interface ExceptionExtraDataHolder {
    Map<String, Object> toMap();

    void loadFromMap(Map<String, Object> map);
}
