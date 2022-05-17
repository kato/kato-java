package me.danwi.kato.apt.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
