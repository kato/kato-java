package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * 模块存根,是方法存根的集合
 */
@Getter
@Setter
@NoArgsConstructor
public class ModuleStub extends QualifiedStub {
    /**
     * 方法合集
     */
    private List<MethodStub> methods = new LinkedList<>();
}
