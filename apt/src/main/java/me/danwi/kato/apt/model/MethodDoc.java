package me.danwi.kato.apt.model;

import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import com.github.chhorz.javadoc.tags.ParamTag;
import com.github.chhorz.javadoc.tags.ReturnTag;
import com.github.chhorz.javadoc.tags.ThrowsTag;
import lombok.Data;

import java.util.List;

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

    public MethodDoc(String doc) {
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(doc);
        //描述
        description = javaDoc.getDescription();
        //参数
        paramDocs = javaDoc.getTags(ParamTag.class).stream()
                .map(it -> {
                    ParamDoc paramDoc = new ParamDoc();
                    paramDoc.setName(it.getParamName());
                    paramDoc.setDescription(it.getParamDescription());
                    return paramDoc;
                })
                .toArray(ParamDoc[]::new);
        //返回值
        List<ReturnTag> returnTags = javaDoc.getTags(ReturnTag.class);
        if (!returnTags.isEmpty()) {
            returnDoc = returnTags.get(0).getDescription();
        }
        //异常
        throwDocs = javaDoc.getTags(ThrowsTag.class).stream()
                .map(it -> {
                    ThrowDoc throwDoc = new ThrowDoc();
                    throwDoc.setClassName(it.getClassName());
                    throwDoc.setDescription(it.getDescription());
                    return throwDoc;
                })
                .toArray(ThrowDoc[]::new);
    }
}
