package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.QualifiedStub;

import java.util.LinkedList;
import java.util.List;

/**
 * 构造类型
 */
@Getter
@Setter
public class StructType extends QualifiedStub implements Type {
    /**
     * 泛型参数
     */
    private List<Type> genericArgument = new LinkedList<>();

    @Override
    public Kind getKind() {
        return Kind.Struct;
    }
}
