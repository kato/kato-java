package me.danwi.kato.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.Type;

public class KatoResultDecoder implements Decoder {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        //空body
        if (response.body() == null)
            return null;
        //反序列化结果
        String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
        return mapper.readValue(bodyStr, TypeFactory.defaultInstance().constructType(type));
    }
}