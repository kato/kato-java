package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.type.StructType;
import me.danwi.kato.dev.stub.type.Type;

import java.util.LinkedList;
import java.util.List;

/**
 * 方法存根
 */
@Getter
@Setter
public class MethodStub extends Stub {
    /**
     * 方法名称
     */
    private String name = "";

    /**
     * 参数合集
     */
    private List<ParameterStub> parameters = new LinkedList<>();

    /**
     * 返回值
     */
    private Type returns = null;

    /**
     * 方法可能产生的异常合集
     */
    private List<StructType> exceptions = new LinkedList<>();
}
