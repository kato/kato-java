package me.danwi.kato.common.javadoc;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
     * 方法签名
     */
    private String signature;
    /**
     * 参数文档
     */
    private ParamDoc[] parameters;
    /**
     * 返回值
     */
    private String returns;
    /**
     * 异常文档
     */
    private ThrowDoc[] exceptions;
}
