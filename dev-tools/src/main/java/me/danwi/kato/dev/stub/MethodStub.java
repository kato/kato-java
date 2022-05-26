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
     * HTTP方法
     */
    private String httpMethod = "GET";

    /**
     * HTTP路径
     */
    private String httpPath = "/";

    /**
     * 参数合集
     */
    private List<ParameterStub> parameters = new LinkedList<>();

    /**
     * 返回值
     */
    private ReturnStub returns = null;

    /**
     * 方法可能产生的异常合集
     */
    private List<ExceptionStub> exceptions = new LinkedList<>();
}
