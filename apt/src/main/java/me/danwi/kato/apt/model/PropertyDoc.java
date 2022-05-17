package me.danwi.kato.apt.model;

import lombok.Data;

@Data
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
