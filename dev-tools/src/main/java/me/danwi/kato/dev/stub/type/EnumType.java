package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.QualifiedStub;

import java.util.LinkedList;
import java.util.List;

/**
 * 枚举类型
 */
@Getter
@Setter
public class EnumType extends QualifiedStub implements Type {
    private List<String> elements = new LinkedList<>();

    @Override
    public Kind getKind() {
        return Kind.Enum;
    }
}
