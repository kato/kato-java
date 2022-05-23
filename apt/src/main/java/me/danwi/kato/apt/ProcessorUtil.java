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
import java.util.stream.Collectors;

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

    /**
     * 将类型转换成规范化的字符串
     * 要和运行时的实现保持一致
     *
     * @param type 类型
     * @return 类型字符串表达
     */
    private String getCanonicalName(TypeMirror type) {
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
            case VOID:
                return "void";
            case NULL:
                return "null";
            case DECLARED:
                return getQualifiedName(type);
            case ARRAY:
                return getCanonicalName(((ArrayType) type).getComponentType()) + "[]";
            default:
                return type.toString();
        }
    }

    /**
     * 生成方法签名
     * 用于在运行时做重载方法的匹配
     *
     * @param element 方法元素
     * @return 签名
     */
    public String generateMethodSignature(ExecutableElement element) {
        String parameterTypes = element.getParameters().stream()
                .map(it -> getCanonicalName(types.erasure(it.asType())))
                .collect(Collectors.joining(","));
        return element.getSimpleName().toString() + "(" + parameterTypes + ")";
    }
}
