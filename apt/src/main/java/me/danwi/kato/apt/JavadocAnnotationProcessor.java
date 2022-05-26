package me.danwi.kato.apt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.tags.ExceptionTag;
import com.github.chhorz.javadoc.tags.ParamTag;
import com.github.chhorz.javadoc.tags.ReturnTag;
import com.github.chhorz.javadoc.tags.ThrowsTag;
import me.danwi.kato.common.javadoc.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JavaDoc注解处理,用于将JavaDoc的内容保存到运行时
 */
public class JavadocAnnotationProcessor extends AbstractProcessor {
    private final ObjectMapper mapper = new ObjectMapper();
    private ProcessorUtil util;
    private Elements elementsUtil;
    private ProcessingEnvironment env;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        env = processingEnv;
        util = new ProcessorUtil(processingEnv);
        elementsUtil = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getRootElements()) {
                if (element instanceof TypeElement)
                    generateJavadoc((TypeElement) element, null);
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return false;
    }

    private void generateJavadoc(TypeElement element, String parent) {
        String className = element.getSimpleName().toString();
        if (parent != null)
            className = parent + "$" + className;
        //获取包元素
        Element packageElement = util.getPackageElement(element);
        //生成class doc
        ClassDoc classDoc = generateClassDoc(element);
        try {
            //写入文件
            FileObject javadocRes = env.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT, util.getQualifiedName(packageElement),
                    className + ".javadoc.json"
            );
            try (Writer writer = javadocRes.openWriter()) {
                writer.write(mapper.writeValueAsString(classDoc));
            }
            //处理内部类
            String finalClassName = className;
            element.getEnclosedElements().stream().filter(it -> it.getKind() == ElementKind.CLASS)
                    .filter(it -> it instanceof TypeElement)
                    .forEach(it -> generateJavadoc((TypeElement) it, finalClassName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ClassDoc generateClassDoc(TypeElement classElement) {
        //解析javadoc
        JavaDoc javaDoc = ProcessorUtil.JavadocParser.parse(elementsUtil.getDocComment(classElement));
        //构造ClassDoc
        ClassDoc classDoc = new ClassDoc();
        if (!javaDoc.getDescription().isEmpty())
            classDoc.setDescription(javaDoc.getDescription());
        //兼容Kotlin
        classDoc.setProperties(
                javaDoc.getTags(PropertyTag.class).stream()
                        .map(it -> new PropertyDoc(it.getPropertyName(), it.getDescription()))
                        .toArray(PropertyDoc[]::new)
        );
        //方法
        MethodDoc[] methodDocs = classElement.getEnclosedElements().stream()
                .filter(it -> it instanceof ExecutableElement)
                .filter(it -> it.getKind() == ElementKind.METHOD)
                .map(it -> generateMethodDoc((ExecutableElement) it))
                .toArray(MethodDoc[]::new);
        classDoc.setMethods(methodDocs);
        //枚举变量的常量值
        ConstantDoc[] constantDocs = classElement.getEnclosedElements().stream()
                .filter(it -> it.getKind() == ElementKind.ENUM_CONSTANT)
                .filter(it -> it instanceof VariableElement)
                .map(it -> generateEnumConstant((VariableElement) it))
                .toArray(ConstantDoc[]::new);
        classDoc.setConstants(constantDocs);
        return classDoc;
    }

    private MethodDoc generateMethodDoc(ExecutableElement methodElement) {
        //解析文档
        JavaDoc javaDoc = ProcessorUtil.JavadocParser.parse(elementsUtil.getDocComment(methodElement));
        //构造MethodDoc
        MethodDoc methodDoc = new MethodDoc();
        //名称
        methodDoc.setName(methodElement.getSimpleName().toString());
        //描述
        if (!javaDoc.getDescription().isEmpty())
            methodDoc.setDescription(javaDoc.getDescription());
        //方法签名,用于在反射是否做重载后的唯一匹配
        methodDoc.setSignature(util.generateMethodSignature(methodElement));
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
        //设置参数的类型和名称,以实际参数为准,不以文档为准
        methodDoc.setParameters(
                methodElement.getParameters().stream()
                        .map(it -> {
                            ParamDoc paramDoc = new ParamDoc();
                            paramDoc.setName(it.getSimpleName().toString());
                            //去文档中查找
                            javaDoc.getTags(ParamTag.class).stream()
                                    .filter(tag -> tag.getParamName().equals(paramDoc.getName())).findAny()
                                    .ifPresent(paramTag -> paramDoc.setDescription(paramTag.getParamDescription()));
                            return paramDoc;
                        })
                        .toArray(ParamDoc[]::new)
        );
        return methodDoc;
    }

    private ConstantDoc generateEnumConstant(VariableElement element) {
        String name = element.getSimpleName().toString();
        JavaDoc javaDoc = ProcessorUtil.JavadocParser.parse(elementsUtil.getDocComment(element));
        ConstantDoc constantDoc = new ConstantDoc();
        constantDoc.setName(name);
        if (!javaDoc.getDescription().isEmpty())
            constantDoc.setDescription(javaDoc.getDescription());
        return constantDoc;
    }
}
