package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.QualifiedStub;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class RefType extends QualifiedStub implements Type {
    /**
     * 泛型参数
     */
    private List<Type> genericArguments = new LinkedList<>();

    @Override
    public Kind getKind() {
        return Kind.Ref;
    }
}
