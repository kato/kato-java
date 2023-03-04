package me.danwi.kato.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.ErrorDecoder;
import me.danwi.kato.client.exception.KatoClientException;
import me.danwi.kato.common.ExceptionResult;
import me.danwi.kato.common.exception.KatoAuthenticationException;
import me.danwi.kato.common.exception.KatoException;
import org.springframework.http.HttpStatus;

public class KatoErrorDecoder implements ErrorDecoder {
    private final ObjectMapper mapper;
    private final Default defaultErrorDecode = new Default();

    {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    }

    @Override
    public Exception decode(String methodKey, Response response) {

        // 不是Kato-Error则默认处理
        if (!response.headers().containsKey(ExceptionResult.HEADER_KEY)) {
            return defaultErrorDecode.decode(methodKey, response);
        }

        // 401 处理（由于401读取不到body内容，特殊处理）
        if (response.status() == HttpStatus.UNAUTHORIZED.value()) {
            return new KatoAuthenticationException(methodKey);
        }

        // 空body
        if (response.body() == null)
            throw new DecodeException(response.status(), "无法获取异常的详细信息", response.request());

        // kato特定异常
        try {
            //读取数据
            String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
            //反序列化ErrorResult结果
            ExceptionResult exceptionResult = mapper.readValue(bodyStr, ExceptionResult.class);
            //尝试构造异常
            Exception exception = exceptionResult.toException(this.getClass(), methodKey);
            if (exception == null) {
                //如果构造失败,则使用kato异常包裹
                return new KatoException(exceptionResult.getMessage());
            }
            if (!(exception instanceof RuntimeException)) {
                //如果不是运行时异常,则使用kato异常包裹
                return new KatoException(exception);
            }
            //正常返回
            return exception;
        } catch (Exception e) {
            //处理异常的过程中发生异常
            return new KatoClientException(e);
        }
    }
}
