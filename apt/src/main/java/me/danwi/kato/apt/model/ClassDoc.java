package me.danwi.kato.apt.model;

import lombok.Data;

@Data
public class ClassDoc {
    /**
     * 描述
     */
    private String description;
    /**
     * 方法文档
     */
    private MethodDoc[] methodDocs;
    /**
     * Getter方法文档
     */
    private GetterDoc[] getterDocs;
}
