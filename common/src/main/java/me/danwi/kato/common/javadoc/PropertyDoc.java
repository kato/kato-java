package me.danwi.kato.common.javadoc;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropertyDoc {
    /**
     * Getter方法名
     */
    private String name;
    /**
     * 描述
     */
    private String description;

    public PropertyDoc(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
