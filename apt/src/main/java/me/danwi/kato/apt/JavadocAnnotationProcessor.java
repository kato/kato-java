package me.danwi.kato.apt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import me.danwi.kato.apt.model.ClassDoc;
import me.danwi.kato.apt.model.MethodDoc;
import me.danwi.kato.apt.model.PropertyDoc;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.util.Collections;
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
        //构造ClassDoc
        ClassDoc classDoc = new ClassDoc(elementsUtil.getDocComment(classElement));
        //方法
        MethodDoc[] methodDocs = classElement.getEnclosedElements().stream()
                .filter(it -> it instanceof ExecutableElement)
                .filter(it -> it.getKind() == ElementKind.METHOD)
                .filter(it -> ProcessorUtil.isGetter(it) == null)
                .map(it -> generateMethodDoc((ExecutableElement) it))
                .toArray(MethodDoc[]::new);
        classDoc.setMethodDocs(methodDocs);
        //字段Getter TODO: 添加lombok的支持
        PropertyDoc[] propertyDocs = classElement.getEnclosedElements().stream()
                .filter(it -> ProcessorUtil.isGetter(it) != null)
                .map(it -> generatePropertyDoc((ExecutableElement) it))
                .toArray(PropertyDoc[]::new);
        classDoc.setPropertyDocs(propertyDocs);
        return classDoc;
    }

    private MethodDoc generateMethodDoc(ExecutableElement methodElement) {
        MethodDoc methodDoc = new MethodDoc(elementsUtil.getDocComment(methodElement));
        methodDoc.setName(methodElement.getSimpleName().toString());
        return methodDoc;
    }

    private PropertyDoc generatePropertyDoc(ExecutableElement element) {
        //解析java doc
        String javadocStr = elementsUtil.getDocComment(element);
        JavaDocParser javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build();
        JavaDoc javaDoc = javaDocParser.parse(javadocStr);
        //构造PropertyDoc
        return new PropertyDoc(
                ProcessorUtil.getterNameToPropertyName(element.getSimpleName().toString()),
                javaDoc.getDescription()
        );
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
