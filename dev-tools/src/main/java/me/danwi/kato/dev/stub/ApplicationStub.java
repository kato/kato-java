package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * 整个应用存根合集
 * 代表了模块/实体的集合
 */
@Getter
@Setter
public class ApplicationStub extends Stub {
    /**
     * 模块集合
     */
    private List<ModuleStub> modules = new LinkedList<>();

    /**
     * 实体定义集合
     */
    private List<ModelStub> models = new LinkedList<>();
}
