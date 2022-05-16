package me.danwi.kato.apt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import com.github.chhorz.javadoc.tags.ParamTag;
import com.github.chhorz.javadoc.tags.ReturnTag;
import com.github.chhorz.javadoc.tags.ThrowsTag;
import me.danwi.kato.apt.model.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
        Types typesUtil = processingEnv.getTypeUtils();
        elementsUtil = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        super.init(processingEnv);
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
        //解析java doc
        String javadocStr = elementsUtil.getDocComment(classElement);
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(javadocStr);
        //构造ClassDoc
        ClassDoc classDoc = new ClassDoc();
        //描述
        classDoc.setDescription(javaDoc.getDescription());
        //方法
        MethodDoc[] methodDocs = classElement.getEnclosedElements().stream()
                .filter(it -> it instanceof ExecutableElement)
                .filter(it -> it.getKind() == ElementKind.METHOD)
                .filter(it -> !util.isGetter(it))
                .map(it -> generateMethodDoc((ExecutableElement) it))
                .toArray(MethodDoc[]::new);
        classDoc.setMethodDocs(methodDocs);
        //字段Getter TODO: 规范化处理,并添加lombok的支持
        GetterDoc[] getterDocs = classElement.getEnclosedElements().stream()
                .filter(it -> util.isGetter(it))
                .map(it -> generateGetterDoc((ExecutableElement) it))
                .toArray(GetterDoc[]::new);
        classDoc.setGetterDocs(getterDocs);
        return classDoc;
    }

    private MethodDoc generateMethodDoc(ExecutableElement methodElement) {
        //解析java doc
        String javadocStr = elementsUtil.getDocComment(methodElement);
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(javadocStr);
        //构造MethodDoc
        MethodDoc methodDoc = new MethodDoc();
        //名称
        methodDoc.setName(methodElement.getSimpleName().toString());
        //描述
        methodDoc.setDescription(javaDoc.getDescription());
        //参数
        ParamDoc[] paramDocs = javaDoc.getTags(ParamTag.class).stream()
                .map(it -> {
                    ParamDoc paramDoc = new ParamDoc();
                    paramDoc.setName(it.getParamName());
                    paramDoc.setDescription(it.getParamDescription());
                    return paramDoc;
                })
                .toArray(ParamDoc[]::new);
        methodDoc.setParamDocs(paramDocs);
        //返回值
        List<ReturnTag> returnTags = javaDoc.getTags(ReturnTag.class);
        if (!returnTags.isEmpty()) {
            methodDoc.setReturnDoc(returnTags.get(0).getDescription());
        }
        //异常
        ThrowDoc[] throwDocs = javaDoc.getTags(ThrowsTag.class).stream()
                .map(it -> {
                    ThrowDoc throwDoc = new ThrowDoc();
                    throwDoc.setClassName(it.getClassName());
                    throwDoc.setDescription(it.getDescription());
                    return throwDoc;
                })
                .toArray(ThrowDoc[]::new);
        methodDoc.setThrowDocs(throwDocs);
        return methodDoc;
    }

    private GetterDoc generateGetterDoc(ExecutableElement element) {
        //解析java doc
        String javadocStr = elementsUtil.getDocComment(element);
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(javadocStr);
        //构造PropertyDoc
        GetterDoc getterDoc = new GetterDoc();
        getterDoc.setName(element.getSimpleName().toString());
        getterDoc.setDescription(javaDoc.getDescription());
        return getterDoc;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
