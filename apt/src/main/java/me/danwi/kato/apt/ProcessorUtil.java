package me.danwi.kato.apt;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

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
}
