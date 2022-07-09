package me.danwi.kato.client;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import me.danwi.kato.common.argument.MultiRequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class KatoEncoder implements Encoder {

    private final Encoder delegate;

    public KatoEncoder(Encoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (object.getClass() != Object[].class) {
            this.delegate.encode(object, bodyType, template);
        } else {
            Object[] objects = (Object[]) object;
            if (objects.length == 0) {
                this.delegate.encode(new HashMap<String, Object>(), Map.class, template);
            } else if (objects.length == 1) {
                this.delegate.encode(objects[0], objects[0].getClass(), template);
            } else {
                Map<String, Object> map = converToMap(objects, template.methodMetadata().method());
                this.delegate.encode(map, Map.class, template);
            }
        }
    }

    private Map<String, Object> converToMap(Object[] objects, Method method) {
        HashMap<String, Object> map = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            map.put(getKey(parameters[i]), objects[i]);
        }
        return map;
    }

    private String getKey(Parameter parameters) {
        MultiRequestBody annotation = parameters.getAnnotation(MultiRequestBody.class);
        if (annotation != null && annotation.value() != null && !annotation.value().equals("")) {
            return annotation.value();
        }
        return parameters.getName();
    }
}
