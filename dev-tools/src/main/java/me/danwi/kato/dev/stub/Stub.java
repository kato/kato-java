package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * 存根基类
 */
@Getter
@Setter
public abstract class Stub {
    /**
     * 描述
     */
    private List<String> description = new LinkedList<>();
}
