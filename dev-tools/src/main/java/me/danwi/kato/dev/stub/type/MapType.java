package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;

/**
 * 字典类型
 */
@Getter
@Setter
public class MapType implements Type {
    /**
     * key的类型
     */
    private Type key = null;
    /**
     * value的类型
     */
    private Type value = null;

    @Override
    public Kind getKind() {
        return Kind.Map;
    }
}
