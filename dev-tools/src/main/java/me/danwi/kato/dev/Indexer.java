package me.danwi.kato.dev;

import me.danwi.kato.common.javadoc.ClassDoc;
import me.danwi.kato.common.javadoc.JavaDoc;
import me.danwi.kato.common.javadoc.MethodDoc;
import me.danwi.kato.common.javadoc.ThrowDoc;
import me.danwi.kato.dev.stub.*;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Indexer {
    private final Map<String, Object> controllers;

    private volatile ApplicationStub applicationStubCache;

    private List<ModelStub> modelsContext = new LinkedList<>();

    public Indexer(Map<String, Object> controllers) {
        this.controllers = controllers;
    }

    public ApplicationStub generateApplicationStub() {
        if (applicationStubCache == null) {
            synchronized (this) {
                if (applicationStubCache == null) {
                    //创建application stub
                    ApplicationStub applicationStub = new ApplicationStub();
                    for (Object controller : controllers.values()) {
                        ModuleStub moduleStub = generateModule(controller);
                        if (!moduleStub.getMethods().isEmpty())
                            applicationStub.getModules().add(moduleStub);
                    }
                    applicationStub.getModels().addAll(modelsContext);
                    this.applicationStubCache = applicationStub;
                }
            }
        }
        return applicationStubCache;
    }

    public ModuleStub generateModule(Object controller) {
        ModuleStub module = new ModuleStub();

        Class<?> clazz = controller.getClass();

        module.setNamespace(clazz.getPackage().getName());
        module.setSecondaryNamespace(getSecondaryNamespace(clazz));
        module.setName(clazz.getSimpleName());
        module.setDescription(
                getSuperClassWithSelf(clazz).stream()
                        .map(JavaDoc::forClass)
                        .filter(Objects::nonNull)
                        .map(ClassDoc::getDescription)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        //找到所有包含了RequestMapping注解的方法
        List<Method> webMethods = Arrays.stream(clazz.getMethods())
                .filter(it -> !it.isBridge()) //去掉bridge方法
                .filter(it -> AnnotationUtils.findAnnotation(it, RequestMapping.class) != null)
                .collect(Collectors.toList());
        for (Method method : webMethods) {
            module.getMethods().add(generateMethod(method));
        }

        return module;
    }

    private MethodStub generateMethod(Method method) {
        MethodStub methodStub = new MethodStub();

        methodStub.setName(method.getName());
        //找到这个方法所有的super方法
        List<Method> superMethods = getSuperMethodsWithSelf(method);
        //所有super方法对应的文档
        List<MethodDoc> methodDocs = superMethods.stream()
                .map(JavaDoc::forMethod)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        //设置描述
        methodStub.setDescription(
                methodDocs.stream()
                        .map(MethodDoc::getDescription)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        //设置可以抛出的异常
        methodStub.setExceptions(
                methodDocs.stream()
                        .flatMap(it -> Stream.of(it.getExceptions()))
                        .collect(Collectors.groupingBy(ThrowDoc::getClassName))
                        .values().stream()
                        .map(it -> {
                            ExceptionStub exceptionStub = new ExceptionStub();
                            exceptionStub.setClassName(it.get(0).getClassName());
                            exceptionStub.setDescription(it.stream().map(ThrowDoc::getDescription).collect(Collectors.toList()));
                            return exceptionStub;
                        })
                        .collect(Collectors.toList())
        );

        return methodStub;
    }

    private List<Class<?>> getSuperClassWithSelf(Class<?> clazz) {
        List<Class<?>> superClasses = new LinkedList<>();
        superClasses.add(clazz);
        superClasses.addAll(getSuperClass(clazz));
        return superClasses;
    }

    private List<Class<?>> getSuperClass(Class<?> clazz) {
        List<Class<?>> superClasses = new LinkedList<>();
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            superClasses.add(superclass);
            superClasses.addAll(getSuperClass(superclass));
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> current : interfaces) {
            superClasses.add(current);
            superClasses.addAll(getSuperClass(current));
        }
        return superClasses;
    }

    private List<Method> getSuperMethodsWithSelf(Method method) {
        List<Method> methods = new LinkedList<>();
        methods.add(method);
        methods.addAll(getSuperMethods(method));
        return methods;
    }

    private List<Method> getSuperMethods(Method method) {
        Method bridgeMethod = findBridgeMethod(method);
        List<Class<?>> superClass = getSuperClass(method.getDeclaringClass());
        return superClass.stream()
                .map(it -> {
                    try {
                        return it.getMethod(bridgeMethod.getName(), bridgeMethod.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Method findBridgeMethod(Method method) {
        Method[] declaredMethods = method.getDeclaringClass().getDeclaredMethods();
        return Arrays.stream(declaredMethods)
                .filter(Method::isBridge)
                .filter(it -> BridgeMethodResolver.findBridgedMethod(it).equals(method))
                .findAny().orElse(method);
    }

    private String getSecondaryNamespace(Class<?> clazz) {
        List<String> names = new LinkedList<>();
        Class<?> parent = clazz.getEnclosingClass();
        while (parent != null) {
            names.add(parent.getSimpleName());
            parent = parent.getEnclosingClass();
        }
        if (names.isEmpty())
            return null;
        Collections.reverse(names);
        return String.join(".", names);
    }
}
