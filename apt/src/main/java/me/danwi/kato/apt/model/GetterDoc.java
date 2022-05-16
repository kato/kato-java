package me.danwi.kato.apt.model;

import lombok.Data;

@Data
public class GetterDoc {
    /**
     * Getter方法名
     */
    private String name;
    /**
     * 描述
     */
    private String description;
}
