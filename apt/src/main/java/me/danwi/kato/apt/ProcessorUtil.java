package me.danwi.kato.apt;

import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

/**
 * 注解处理器辅助类
 */
public class ProcessorUtil {
    /**
     * javadoc解析器
     */
    public static final JavaDocParser JavadocParser = JavaDocParserBuilder
            .withBasicTags().withCustomTag(new PropertyTag())
            .withOutputType(OutputType.PLAIN).build();
    private final Types types;

    public ProcessorUtil(ProcessingEnvironment env) {
        this.types = env.getTypeUtils();
        Elements elements = env.getElementUtils();
    }

    /**
     * 获取类型全限定名
     *
     * @param type 类型
     * @return 全限定名
     */
    public String getQualifiedName(TypeMirror type) {
        return getQualifiedName(types.asElement(type));
    }

    /**
     * 获取元素的全限定名
     *
     * @param element 元素
     * @return 全限定名
     */
    public String getQualifiedName(Element element) {
        if (element instanceof QualifiedNameable) {
            return ((QualifiedNameable) element).getQualifiedName().toString();
        }
        return element.toString();
    }

    /**
     * 获取类型的父类
     *
     * @param type 类型
     * @return 父类
     */
    public Element getSuperClass(TypeMirror type) {
        return getSuperClass(types.asElement(type));
    }

    /**
     * 获取元素的父类
     *
     * @param element 元素
     * @return 父类
     */
    public Element getSuperClass(Element element) {
        List<? extends TypeMirror> supertypes = this.types.directSupertypes(element.asType());
        if (supertypes.isEmpty())
            return null;
        return this.types.asElement(supertypes.get(0));
    }

    /**
     * 获取元素所在的包
     *
     * @param element 元素
     * @return 所在的包元素
     */
    public PackageElement getPackageElement(Element element) {
        if (element == null)
            return null;
        if (element instanceof PackageElement)
            return (PackageElement) element;
        return getPackageElement(element.getEnclosingElement());
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

    /**
     * 元素是否是一个getter函数
     *
     * @param element 元素
     * @return 如果是一个getter函数, 则返回对于的属性名, 如果不是则返回空
     */
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
        if (getterName.startsWith("get")) {
            getterName = getterName.substring(3);
        } else if (getterName.startsWith("is")) {
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
