package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.QualifiedStub;

@Getter
@Setter
public class RefType extends QualifiedStub implements Type {
    @Override
    public Kind getKind() {
        return Kind.Ref;
    }
}
