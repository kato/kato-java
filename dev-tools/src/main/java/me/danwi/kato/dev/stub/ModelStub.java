package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.type.StructType;

import java.util.LinkedList;
import java.util.List;

/**
 * 实体模型存根
 */
@Getter
@Setter
public class ModelStub extends StructType {
    /**
     * 字段合集
     */
    private List<FieldStub> fields = new LinkedList<>();
}
