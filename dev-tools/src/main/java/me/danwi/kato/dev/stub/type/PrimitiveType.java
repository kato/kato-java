package me.danwi.kato.dev.stub.type;

/**
 * 预置类型
 */
public enum PrimitiveType implements Type {
    Bool,
    Byte,
    Int16,
    Int32,
    Int64,
    Float32,
    Float64,
    String,
    Date,
    Time,
    DateTime;

    @Override
    public Kind getKind() {
        return Kind.Primitive;
    }
}
