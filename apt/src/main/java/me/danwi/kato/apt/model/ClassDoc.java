package me.danwi.kato.apt.model;

import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.danwi.kato.apt.PropertyTag;

@Data
@NoArgsConstructor
public class ClassDoc {
    /**
     * 描述
     */
    private String description;
    /**
     * 方法文档
     */
    private MethodDoc[] methods;
    /**
     * Getter方法文档
     */
    private PropertyDoc[] properties;

    public ClassDoc(String doc) {
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags()
                .withCustomTag(new PropertyTag())
                .withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(doc);
        description = javaDoc.getDescription();
        //兼容Kotlin
        properties = javaDoc.getTags(PropertyTag.class).stream()
                .map(it -> new PropertyDoc(it.getPropertyName(), it.getDescription()))
                .toArray(PropertyDoc[]::new);
    }
}
