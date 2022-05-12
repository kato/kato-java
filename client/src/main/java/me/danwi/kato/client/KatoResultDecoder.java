package me.danwi.kato.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import me.danwi.kato.common.Result;
import me.danwi.kato.common.exception.KatoException;

import java.io.IOException;
import java.lang.reflect.Type;

public class KatoResultDecoder implements Decoder {
    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        //空body
        if (response.body() == null)
            throw new DecodeException(response.status(), "请求返回空Body", response.request());
        //反序列化Result类
        String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
        JavaType dataType = TypeFactory.defaultInstance().constructType(type);
        JavaType resultType = TypeFactory.defaultInstance().constructParametricType(Result.class, dataType);
        Result<Object> result = new ObjectMapper().readValue(bodyStr, resultType);
        //TODO: 暂时不做异常处理
        if (result.getException() != null) {
            throw new KatoException(result.getMessage());
        }
        //返回结果
        return result.getData();
    }
}
