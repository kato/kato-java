package me.danwi.kato.common.javadoc;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.lang.model.element.ExecutableElement;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class JavaDoc {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Map<String, ClassDoc> docCache = new HashMap<>();

    public static ClassDoc forClass(Class<?> clazz) {
        String qualifiedName = clazz.getName();
        if (docCache.get(qualifiedName) == null) {
            synchronized (docCache) {
                if (docCache.get(qualifiedName) == null) {
                    //获取目录名称
                    String packageName = clazz.getPackage().getName();
                    String directory = packageName.replace('.', '/');
                    String className = clazz.getName();
                    if (!packageName.equals(""))
                        className = className.replaceFirst(packageName + ".", "");
                    String fileName = className + ".javadoc.json";
                    //获取获取对应的资源,并解析
                    try (InputStream stream = clazz.getClassLoader().getResourceAsStream(directory + "/" + fileName)) {
                        docCache.put(qualifiedName, mapper.readValue(stream, ClassDoc.class));
                    } catch (Exception e) {
                        docCache.put(qualifiedName, null);
                    }
                }
            }
        }
        return docCache.get(qualifiedName);
    }

    public static ClassDoc forName(String className) {
        try {
            return forClass(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static MethodDoc forMethod(Method method) {
        ClassDoc classDoc = forClass(method.getDeclaringClass());
        String signature = generateMethodSignature(method);
        return Arrays.stream(classDoc.getMethods())
                .filter(it -> it.getSignature().equals(signature))
                .findFirst()
                .orElse(null);
    }

    private static String generateMethodSignature(Method method) {
        String parameterTypes = Arrays.stream(method.getParameterTypes())
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(","));
        return method.getName() + "(" + parameterTypes + ")";
    }
}
