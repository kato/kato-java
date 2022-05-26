package me.danwi.kato.dev.stub.type;

import lombok.Getter;
import lombok.Setter;


/**
 * 预置类型
 */
@Setter
@Getter
public class PrimitiveType implements Type {
    public enum Types {
        Bool,
        Char,
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

        public PrimitiveType toType() {
            PrimitiveType primitiveType = new PrimitiveType();
            primitiveType.setType(this);
            return primitiveType;
        }
    }

    private Types type;

    @Override
    public Kind getKind() {
        return Kind.Primitive;
    }
}
