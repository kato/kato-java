package me.danwi.kato.apt.model;

import lombok.Data;

@Data
public class MethodDoc {
    /**
     * 方法名
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 参数文档
     */
    private ParamDoc[] paramDocs;
    /**
     * 返回值
     */
    private String returnDoc;
    /**
     * 异常文档
     */
    private ThrowDoc[] throwDocs;
}
