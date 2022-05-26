package me.danwi.kato.common.javadoc;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConstantDoc {
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
}
