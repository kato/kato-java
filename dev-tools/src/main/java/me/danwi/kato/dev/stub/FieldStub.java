package me.danwi.kato.dev.stub;


import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.type.Type;

/**
 * 字段存根
 */
@Getter
@Setter
public class FieldStub extends Stub {
    /**
     * 类型
     */
    private Type type = null;

    /**
     * 名称
     */
    private String name = "";

    /**
     * 可空
     */
    private boolean nullable = true;
}
