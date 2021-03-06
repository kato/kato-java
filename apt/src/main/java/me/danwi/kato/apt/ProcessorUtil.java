package me.danwi.kato.apt;

import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import com.github.chhorz.javadoc.tags.ExceptionTag;
import com.github.chhorz.javadoc.tags.ParamTag;
import com.github.chhorz.javadoc.tags.ReturnTag;
import com.github.chhorz.javadoc.tags.ThrowsTag;
import me.danwi.kato.common.javadoc.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
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
     * getter???????????????????????????
     *
     * @param getterName getter?????????
     * @return ?????????, ?????????????????????getter?????????, ????????????
     */
    public static String getterNameToPropertyName(String getterName) {
        //??????get/is??????
        if (getterName.startsWith(GET_PREFIX)) {
            getterName = getterName.substring(3);
        } else if (getterName.startsWith(IS_PREFIX)) {
            getterName = getterName; //Do nothing
        } else {
            return null;
        }
        //??????????????????
        if (getterName.isEmpty())
            return null;
        //????????????
        if (getterName.length() == 1)
            return getterName.toLowerCase();
        //?????????????????????,?????????????????????
        if (Character.isLowerCase(getterName.charAt(1))) {
            return getterName.substring(0, 1).toLowerCase() + getterName.substring(1);
        }
        //????????????????????????
        return getterName;
    }

    public static ClassDoc parserClassDoc(String doc) {
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags()
                .withCustomTag(new PropertyTag())
                .withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(doc);
        ClassDoc classDoc = new ClassDoc();
        classDoc.setDescription(javaDoc.getDescription());
        //??????Kotlin
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
        //??????
        methodDoc.setDescription(javaDoc.getDescription());
        //??????
        methodDoc.setParameters(
                javaDoc.getTags(ParamTag.class).stream()
                        .map(it -> {
                            ParamDoc paramDoc = new ParamDoc();
                            paramDoc.setName(it.getParamName());
                            paramDoc.setDescription(it.getParamDescription());
                            return paramDoc;
                        })
                        .toArray(ParamDoc[]::new)
        );
        //?????????
        List<ReturnTag> returnTags = javaDoc.getTags(ReturnTag.class);
        if (!returnTags.isEmpty()) {
            methodDoc.setReturns(returnTags.get(0).getDescription());
        }
        //??????
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
