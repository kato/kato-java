package me.danwi.kato.common.javadoc;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ThrowDoc {
    /**
     * 异常类名
     */
    private String className;
    /**
     * 描述
     */
    private String description;
}
