package me.danwi.kato.apt.model;

import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import com.github.chhorz.javadoc.tags.ExceptionTag;
import com.github.chhorz.javadoc.tags.ParamTag;
import com.github.chhorz.javadoc.tags.ReturnTag;
import com.github.chhorz.javadoc.tags.ThrowsTag;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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

    public MethodDoc(String doc) {
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(doc);
        //描述
        description = javaDoc.getDescription();
        //参数
        parameters = javaDoc.getTags(ParamTag.class).stream()
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
            returns = returnTags.get(0).getDescription();
        }
        //异常
        List<ThrowDoc> exceptions = javaDoc.getTags(ThrowsTag.class).stream()
                .map(it -> {
                    ThrowDoc throwDoc = new ThrowDoc();
                    throwDoc.setClassName(it.getClassName());
                    throwDoc.setDescription(it.getDescription());
                    return throwDoc;
                })
                .collect(Collectors.toList());
        exceptions.addAll(
                javaDoc.getTags(ExceptionTag.class).stream()
                        .map(it -> {
                            ThrowDoc throwDoc = new ThrowDoc();
                            throwDoc.setClassName(it.getClassName());
                            throwDoc.setDescription(it.getDescription());
                            return throwDoc;
                        }).collect(Collectors.toList())
        );
        this.exceptions = exceptions.toArray(new ThrowDoc[0]);
    }
}
