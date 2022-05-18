package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.type.Type;

/**
 * 参数存根
 */
@Getter
@Setter
public class ParameterStub extends Stub {
    /**
     * 类型
     */
    private Type type = null;

    /**
     * 名称
     */
    private String name = "";

    /**
     * 是否为必须
     */
    private boolean required = false;
}
