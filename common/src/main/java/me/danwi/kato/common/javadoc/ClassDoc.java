package me.danwi.kato.common.javadoc;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClassDoc {
    /**
     * 描述
     */
    private String description;
    /**
     * 方法文档
     */
    private MethodDoc[] methods;
    /**
     * Getter方法文档
     */
    private PropertyDoc[] properties;
    /**
     * 常量文档(枚举)
     */
    private ConstantDoc[] constants;
}
