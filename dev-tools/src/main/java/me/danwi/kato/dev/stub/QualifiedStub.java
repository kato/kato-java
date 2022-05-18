package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;

/**
 * 能全限定定位的存根元素
 */
@Getter
@Setter
public abstract class QualifiedStub extends Stub {
    /**
     * 命名空间
     * <p>
     * 多级结构使用 . 来分割
     * <p>
     * eg. java中package
     */
    private String namespace = "";

    /**
     * 二级命名空间
     * eg. java中内部类才有,其他语言可能没有
     */
    private String secondaryNamespace = "";

    /**
     * 名称
     * java中的类名
     */
    private String name = "";
}
