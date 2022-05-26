package me.danwi.kato.dev;

import me.danwi.kato.common.javadoc.*;
import me.danwi.kato.dev.stub.*;
import me.danwi.kato.dev.stub.type.*;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Indexer {
    private final Map<String, Object> controllers;

    private volatile ApplicationStub applicationStubCache;

    private final Map<String, EnumType> enumsContext = new HashMap<>();
    private final Map<String, ModelStub> modelsContext = new HashMap<>();

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
                    applicationStub.getModels().addAll(modelsContext.values());
                    applicationStub.getEnums().addAll(enumsContext.values());

                    //顶级泛型变量重写
                    for (ModuleStub module : applicationStub.getModules()) {
                        for (MethodStub method : module.getMethods()) {
                            List<ParameterStub> parameters = method.getParameters();
                            for (ParameterStub current : parameters) {
                                current.setType(topLevelGenericVariableRewrite(current.getType()));
                            }
                            method.getReturns().setType(topLevelGenericVariableRewrite(method.getReturns().getType()));
                        }
                    }

                    //存入缓存
                    this.applicationStubCache = applicationStub;
                }
            }
        }
        return applicationStubCache;
    }

    private me.danwi.kato.dev.stub.type.Type topLevelGenericVariableRewrite(me.danwi.kato.dev.stub.type.Type type) {
        //如果是一个泛型变量名,则替换为Any
        if (type instanceof VariableType)
            return new AnyType();
        //如果是一个RefType,则继续重写
        if (type instanceof RefType)
            ((RefType) type).getGenericArguments().replaceAll(this::topLevelGenericVariableRewrite);
        //如果是一个数组,则重写其元素的类型
        if (type instanceof ArrayType)
            ((ArrayType) type).setElement(topLevelGenericVariableRewrite(((ArrayType) type).getElement()));
        //如果是一个Map,则重写Key和Value
        if (type instanceof MapType) {
            ((MapType) type).setKey(topLevelGenericVariableRewrite(((MapType) type).getKey()));
            ((MapType) type).setValue(topLevelGenericVariableRewrite(((MapType) type).getValue()));
        }
        //其他情况,保持不变
        return type;
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

        //生成所有的方法存根
        module.getMethods().addAll(
                Arrays.stream(clazz.getMethods())
                        .filter(it -> !it.isBridge()) //去掉bridge方法
                        .map(this::generateMethod)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        return module;
    }

    private MethodStub generateMethod(Method method) {
        //获取注解信息
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (requestMapping == null)
            return null;

        //获取HTTP相关信息
        String httpMethod = Arrays.stream(requestMapping.method()).findFirst().orElse(RequestMethod.GET).toString();
        String httpPath = Arrays.stream(requestMapping.path()).findFirst().orElse("/");

        MethodStub methodStub = new MethodStub();

        methodStub.setName(method.getName());
        methodStub.setHttpMethod(httpMethod);
        methodStub.setHttpPath(httpPath);

        //获取当前方法的文档
        MethodDoc methodDoc = JavaDoc.forMethod(method);
        //找到这个方法所有的super方法
        List<Method> superMethods = getSuperMethods(method);
        //所有super方法对应的文档
        List<MethodDoc> superMethodDocs = superMethods.stream()
                .map(JavaDoc::forMethod)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        //所有文档的集合
        List<MethodDoc> allMethodDocs = new LinkedList<>();
        allMethodDocs.add(methodDoc);
        allMethodDocs.addAll(superMethodDocs);

        //设置描述
        methodStub.setDescription(
                allMethodDocs.stream()
                        .map(MethodDoc::getDescription)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        //设置可以抛出的异常
        methodStub.setExceptions(
                allMethodDocs.stream()
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

        //处理参数
        List<ParameterStub> parameterStubs = new LinkedList<>();
        //获取参数文档
        ParamDoc[] parametersInDoc = methodDoc.getParameters();
        //获取方法的参数实例
        Type[] parameterTypes = method.getGenericParameterTypes();
        //遍历
        for (int i = 0; i < parametersInDoc.length; i++) {
            ParamDoc paramDoc = parametersInDoc[i];
            String name = paramDoc.getName();
            //获取这个参数的所有文档注释
            List<ParamDoc> allParamDocs = allMethodDocs.stream()
                    .map(it -> Arrays.stream(it.getParameters())
                            .filter(p -> p.getName().equals(name))
                            .findAny().orElse(null)
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ParameterStub parameterStub = generateParameter(name, allParamDocs, parameterTypes[i]);
            parameterStubs.add(parameterStub);
        }
        methodStub.setParameters(parameterStubs);

        //处理返回值
        methodStub.setReturns(
                generateReturn(
                        allMethodDocs.stream()
                                .map(MethodDoc::getReturns)
                                .filter(Objects::nonNull)
                                .filter(it -> !it.isEmpty())
                                .collect(Collectors.toList()),
                        method.getGenericReturnType()
                )
        );


        return methodStub;
    }

    private ParameterStub generateParameter(String parameterName, List<ParamDoc> allParamDocs, Type parameterType) {
        ParameterStub parameterStub = new ParameterStub();
        parameterStub.setName(parameterName);
        parameterStub.setDescription(allParamDocs.stream().map(ParamDoc::getDescription).collect(Collectors.toList()));
        parameterStub.setType(generateType(parameterType));
        return parameterStub;
    }

    private ReturnStub generateReturn(List<String> description, Type returnType) {
        ReturnStub returnStub = new ReturnStub();
        returnStub.setDescription(description);
        returnStub.setType(generateType(returnType));
        return returnStub;
    }

    private me.danwi.kato.dev.stub.type.Type generateType(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                return generateArrayType(clazz);
            } else if (clazz.isEnum()) {
                return generateEnumType(clazz);
            } else {
                //先判断是不是预置类型
                me.danwi.kato.dev.stub.type.Type primitiveType = generatePrimitiveType(clazz);
                if (primitiveType != null)
                    return primitiveType;
                //判断是不是void
                if (clazz.isPrimitive() && clazz.getSimpleName().equals("void"))
                    return new UnitType();
                //如果不是则尝试创建构造类型
                return generateStructType(clazz);
            }
        } else if (type instanceof ParameterizedType) {
            //带泛型参数的类型
            ParameterizedType parameterizedType = (ParameterizedType) type;
            //判断是否为List
            ArrayType arrayType = generateListType(parameterizedType);
            if (arrayType != null)
                return arrayType;
            //判断是否为Map
            MapType mapType = generateMapType(parameterizedType);
            if (mapType != null)
                return mapType;
            //尝试构造类型
            return generateStructType(type);
        } else if (type instanceof TypeVariable) {
            String name = ((TypeVariable<?>) type).getName();
            VariableType variableType = new VariableType();
            variableType.setName(name);
            return variableType;
        } else if (type instanceof WildcardType) {
            return new AnyType();
        } else if (type instanceof GenericArrayType) {
            //带泛型带数组
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            ArrayType arrayType = new ArrayType();
            arrayType.setElement(generateType(genericComponentType));
            return arrayType;
        }
        return null;
    }

    //预置类型
    private PrimitiveType generatePrimitiveType(Class<?> primitiveClazz) {
        if (primitiveClazz.isPrimitive()) {
            switch (primitiveClazz.getSimpleName()) {
                case "boolean":
                    return PrimitiveType.Types.Bool.toType();
                case "char":
                    return PrimitiveType.Types.Char.toType();
                case "byte":
                    return PrimitiveType.Types.Byte.toType();
                case "short":
                    return PrimitiveType.Types.Int16.toType();
                case "int":
                    return PrimitiveType.Types.Int32.toType();
                case "long":
                    return PrimitiveType.Types.Int64.toType();
                case "float":
                    return PrimitiveType.Types.Float32.toType();
                case "double":
                    return PrimitiveType.Types.Float64.toType();
            }
        }

        switch (primitiveClazz.getName()) {
            case "java.lang.Boolean":
                return PrimitiveType.Types.Bool.toType();
            case "java.lang.Character":
                return PrimitiveType.Types.Char.toType();
            case "java.lang.Byte":
                return PrimitiveType.Types.Byte.toType();
            case "java.lang.Short":
                return PrimitiveType.Types.Int16.toType();
            case "java.lang.Integer":
                return PrimitiveType.Types.Int32.toType();
            case "java.lang.Long":
                return PrimitiveType.Types.Int64.toType();
            case "java.lang.Float":
                return PrimitiveType.Types.Float32.toType();
            case "java.lang.Double":
                return PrimitiveType.Types.Float64.toType();
            case "java.lang.String":
                return PrimitiveType.Types.String.toType();
            case "java.util.Date":
            case "java.time.LocalDateTime":
            case "java.time.OffsetDateTime":
            case "java.time.ZonedDateTime":
                return PrimitiveType.Types.DateTime.toType();
            case "java.time.LocalDate":
                return PrimitiveType.Types.Date.toType();
            case "java.time.LocalTime":
            case "java.time.OffsetTime":
                return PrimitiveType.Types.Time.toType();
        }

        return null;
    }

    //预置数组
    private ArrayType generateArrayType(Class<?> arrayClazz) {
        Class<?> elementType = arrayClazz.getComponentType();
        ArrayType arrayType = new ArrayType();
        arrayType.setElement(generateType(elementType));
        return arrayType;
    }

    private ArrayType generateListType(ParameterizedType type) {
        Type rawType = type.getRawType();
        if (rawType instanceof Class) {
            List<Class<?>> superClassWithSelf = getSuperClassWithSelf((Class<?>) rawType);
            if (superClassWithSelf.stream().anyMatch(it -> it.getName().equals("java.util.List"))) {
                Type[] actualTypeArguments = type.getActualTypeArguments();
                me.danwi.kato.dev.stub.type.Type elementType = generateType(actualTypeArguments[0]);
                ArrayType arrayType = new ArrayType();
                arrayType.setElement(elementType);
                return arrayType;
            }
        }
        return null;
    }

    private MapType generateMapType(ParameterizedType type) {
        Type rawType = type.getRawType();
        if (rawType instanceof Class) {
            List<Class<?>> superClassWithSelf = getSuperClassWithSelf((Class<?>) rawType);
            if (superClassWithSelf.stream().anyMatch(it -> it.getName().equals("java.util.Map"))) {
                Type[] actualTypeArguments = type.getActualTypeArguments();
                me.danwi.kato.dev.stub.type.Type keyType = generateType(actualTypeArguments[0]);
                me.danwi.kato.dev.stub.type.Type valueType = generateType(actualTypeArguments[1]);
                MapType mapType = new MapType();
                mapType.setKey(keyType);
                mapType.setValue(valueType);
                return mapType;
            }
        }
        return null;
    }

    //枚举
    private RefType generateEnumType(Class<?> enumClazz) {
        //判断上下文是否存在
        String qualifiedName = enumClazz.getName();
        if (!enumsContext.containsKey(qualifiedName)) {
            String namespace = enumClazz.getPackage().getName();
            String secondaryNamespace = getSecondaryNamespace(enumClazz);
            String name = enumClazz.getSimpleName();

            Object[] enumConstants = enumClazz.getEnumConstants();
            EnumType enumType = new EnumType();
            enumType.setNamespace(namespace);
            enumType.setSecondaryNamespace(secondaryNamespace);
            enumType.setName(name);
            enumType.setElements(Arrays.stream(enumConstants).map(Object::toString).collect(Collectors.toList()));
            //添加到Context中
            enumsContext.put(qualifiedName, enumType);
        }

        return RefType.fromQualified(enumsContext.get(qualifiedName));
    }

    //构造类型
    private RefType generateStructType(Type beanType) {
        //泛型参数
        List<me.danwi.kato.dev.stub.type.Type> typeArguments = new LinkedList<>();
        //实际类型
        Class<?> actualType = null;
        //判断其是否为带泛型的类型
        if (beanType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) beanType;
            Type[] actualTypeArguments = ((ParameterizedType) beanType).getActualTypeArguments();
            typeArguments.addAll(Arrays.stream(actualTypeArguments).map(this::generateType).collect(Collectors.toList()));
            //实际类型
            if (parameterizedType.getRawType() instanceof Class)
                actualType = (Class<?>) parameterizedType.getRawType();
        } else if (beanType instanceof Class) {
            actualType = ((Class<?>) beanType);
        }
        //是否正确获取到了实际类型
        if (actualType == null)
            return null;
        //注册model
        ModelStub modelStub = registerModel(actualType);
        //构建引用
        RefType refType = RefType.fromQualified(modelStub);
        refType.setGenericArguments(typeArguments);
        return refType;
    }

    private ModelStub registerModel(Class<?> beanClass) {
        //判断上下文是否存在
        String qualifiedName = beanClass.getName();
        if (!modelsContext.containsKey(qualifiedName)) {
            String namespace = beanClass.getPackage().getName();
            String secondaryNamespace = getSecondaryNamespace(beanClass);
            String name = beanClass.getName();
            ClassDoc classDoc = JavaDoc.forClass(beanClass);

            ModelStub modelStub = new ModelStub();
            modelStub.setNamespace(namespace);
            modelStub.setSecondaryNamespace(secondaryNamespace);
            modelStub.setName(name);
            modelStub.setDescription(
                    getSuperClassWithSelf(beanClass).stream()
                            .map(JavaDoc::forClass)
                            .filter(Objects::nonNull)
                            .map(ClassDoc::getDescription)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
            modelStub.setGenericArgumentNames(Arrays.stream(beanClass.getTypeParameters()).map(Type::getTypeName).collect(Collectors.toList()));
            //添加到Context中
            modelsContext.put(qualifiedName, modelStub);
        }
        return modelsContext.get(qualifiedName);
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
