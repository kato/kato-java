package me.danwi.kato.apt.model;

import lombok.Data;

@Data
public class ParamDoc {
    /**
     * 参数名
     */
    private String name;
    /**
     * 描述
     */
    private String description;
}
