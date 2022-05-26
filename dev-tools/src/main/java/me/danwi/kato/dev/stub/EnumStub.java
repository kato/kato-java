package me.danwi.kato.dev.stub;

import lombok.Getter;
import lombok.Setter;
import me.danwi.kato.dev.stub.type.EnumType;

import java.util.LinkedList;
import java.util.List;

/**
 * 枚举存根
 */
@Getter
@Setter
public class EnumStub extends EnumType {
    private List<String> constantDescriptions = new LinkedList<>();
}
