package me.danwi.kato.dev.stub.type;

/**
 * 任意类型,类似java中的Object
 */
public class AnyType implements Type {
    @Override
    public Kind getKind() {
        return Kind.Any;
    }
}
