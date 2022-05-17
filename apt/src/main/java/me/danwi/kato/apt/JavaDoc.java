package me.danwi.kato.apt;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.apt.model.ClassDoc;

import java.io.InputStream;

public class JavaDoc {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ClassDoc forClass(Class<?> clazz) {
        //获取目录名称
        String packageName = clazz.getPackage().getName();
        String directory = packageName.replace('.', '/');
        String className = clazz.getName();
        if (!packageName.equals(""))
            className = className.replaceFirst(packageName + ".", "");
        String fileName = className + ".javadoc.json";
        //获取获取对应的资源,并解析
        try (InputStream stream = clazz.getClassLoader().getResourceAsStream(directory + "/" + fileName)) {
            return mapper.readValue(stream, ClassDoc.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static ClassDoc forName(String className) {
        try {
            return forClass(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
