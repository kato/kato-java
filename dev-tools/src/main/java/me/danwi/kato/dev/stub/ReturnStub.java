package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.type.Type;

/**
 * 返回值存根
 */
@Getter
@Setter
public class ReturnStub extends Stub {
    /**
     * 类型
     */
    private Type type = null;
}
