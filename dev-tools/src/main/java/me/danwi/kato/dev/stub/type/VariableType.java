package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;

/**
 * 泛型变量
 */
@Getter
@Setter
public class VariableType implements Type {
    /**
     * 泛型变量名称,用于唯一定位
     */
    private String name = "";

    @Override
    public Kind getKind() {
        return Kind.Variable;
    }
}
