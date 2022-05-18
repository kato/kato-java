package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;

/**
 * 数组类型
 */
@Getter
@Setter
public class ArrayType implements Type {
    /**
     * 数组元素类型
     */
    private Type element = null;

    @Override
    public Kind getKind() {
        return Kind.Array;
    }
}
