package me.danwi.kato.dev.stub.type;

/**
 * 空类型(Nothing)
 */
public class UnitType implements Type {
    @Override
    public Kind getKind() {
        return Kind.Unit;
    }
}
