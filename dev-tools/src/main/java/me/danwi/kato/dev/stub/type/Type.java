package me.danwi.kato.dev.stub.type;

public interface Type {
    enum Kind {
        Variable, //泛型变量占位符
        Primitive, //预置类型
        Struct, //构造类型
        Enum,  //枚举
        Any, //任意类型
        Unit, //空,void

        //特地优化
        Array, //数组
        Map,//字典
        Ref,//存根中使用,用于序列化后的内存引用表达
    }

    Kind getKind();
}
