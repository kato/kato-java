package me.danwi.kato.apt;

import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import com.github.chhorz.javadoc.tags.ExceptionTag;
import com.github.chhorz.javadoc.tags.ReturnTag;
import com.github.chhorz.javadoc.tags.ThrowsTag;
import me.danwi.kato.common.javadoc.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessorUtil {
    private final ProcessingEnvironment env;
    private final Types types;
    private final Elements elements;

    public ProcessorUtil(ProcessingEnvironment env) {
        this.env = env;
        this.types = env.getTypeUtils();
        this.elements = env.getElementUtils();
    }

    public String getQualifiedName(TypeMirror type) {
        return getQualifiedName(types.asElement(type));
    }

    public String getQualifiedName(Element element) {
        if (element instanceof QualifiedNameable) {
            return ((QualifiedNameable) element).getQualifiedName().toString();
        }
        return element.toString();
    }

    public Element getSuperClass(TypeMirror type) {
        return getSuperClass(types.asElement(type));
    }

    public PackageElement getPackageElement(Element element) {
        if (element == null)
            return null;
        if (element instanceof PackageElement)
            return (PackageElement) element;
        return getPackageElement(element.getEnclosingElement());
    }

    public Element getSuperClass(Element element) {
        List<? extends TypeMirror> supertypes = this.types.directSupertypes(element.asType());
        if (supertypes.isEmpty())
            return null;
        return this.types.asElement(supertypes.get(0));
    }

    public String toCommonTypeName(TypeMirror type) {
        switch (type.getKind()) {
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case CHAR:
            case FLOAT:
            case DOUBLE:
                return types.getPrimitiveType(type.getKind()).toString();
            case ARRAY:
                String elementTypeName = toCommonTypeName(((ArrayType) type).getComponentType());
                return elementTypeName + "[]";
            case VOID:
                return "void";
            case NULL:
                return "null";
            case DECLARED:
                return getQualifiedName(type);
            default:
                return type.toString();
        }
    }

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";

    public static String isGetter(Element element) {
        if (!(element instanceof ExecutableElement))
            return null;
        ExecutableElement executableElement = (ExecutableElement) element;
        if (!executableElement.getParameters().isEmpty())
            return null;
        return getterNameToPropertyName(executableElement.getSimpleName().toString());
    }

    /**
     * getter方法名转换成属性名
     *
     * @param getterName getter方法名
     * @return 属性名, 如果不是合法的getter方法名, 则返回空
     */
    public static String getterNameToPropertyName(String getterName) {
        //去掉get/is前缀
        if (getterName.startsWith(GET_PREFIX)) {
            getterName = getterName.substring(3);
        } else if (getterName.startsWith(IS_PREFIX)) {
            getterName = getterName; //Do nothing
        } else {
            return null;
        }
        //剩余部分为空
        if (getterName.isEmpty())
            return null;
        //一个自负
        if (getterName.length() == 1)
            return getterName.toLowerCase();
        //如果是多个字符,且第二个为小写
        if (Character.isLowerCase(getterName.charAt(1))) {
            return getterName.substring(0, 1).toLowerCase() + getterName.substring(1);
        }
        //如果第二个为大写
        return getterName;
    }

    public static ClassDoc parserClassDoc(String doc) {
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags()
                .withCustomTag(new PropertyTag())
                .withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(doc);
        ClassDoc classDoc = new ClassDoc();
        classDoc.setDescription(javaDoc.getDescription());
        //兼容Kotlin
        classDoc.setProperties(
                javaDoc.getTags(PropertyTag.class).stream()
                        .map(it -> new PropertyDoc(it.getPropertyName(), it.getDescription()))
                        .toArray(PropertyDoc[]::new)
        );
        return classDoc;
    }

    public static MethodDoc parseMethodDoc(String doc) {
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(doc);
        MethodDoc methodDoc = new MethodDoc();
        //描述
        methodDoc.setDescription(javaDoc.getDescription());
        //返回值
        List<ReturnTag> returnTags = javaDoc.getTags(ReturnTag.class);
        if (!returnTags.isEmpty()) {
            methodDoc.setReturns(returnTags.get(0).getDescription());
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
        methodDoc.setExceptions(exceptions.toArray(new ThrowDoc[0]));
        return methodDoc;
    }
}
