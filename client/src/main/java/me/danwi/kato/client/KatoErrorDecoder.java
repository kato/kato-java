package me.danwi.kato.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.ErrorDecoder;
import me.danwi.kato.client.exception.KatoClientException;
import me.danwi.kato.common.ExceptionResult;
import me.danwi.kato.common.exception.ExceptionExtraDataHolder;
import me.danwi.kato.common.exception.ExceptionIdentify;
import me.danwi.kato.common.exception.KatoException;
import org.springframework.http.HttpStatus;

public class KatoErrorDecoder implements ErrorDecoder {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        //默认处理
        if (response.status() != HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return new Default().decode(methodKey, response);
        }
        //空body
        if (response.body() == null)
            throw new DecodeException(response.status(), "无法获取异常的详细信息", response.request());

        //500状态为kato特定异常状态
        try {
            //读取数据
            String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
            //反序列化ErrorResult结果
            ExceptionResult exceptionResult = mapper.readValue(bodyStr, ExceptionResult.class);
            //尝试构造异常
            Exception exception = constructException(exceptionResult);
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

    //尝试反序列化并构造异常
    private Exception constructException(ExceptionResult result) {
        try {
            //加载异常类
            Class<?> exceptionClass = Class.forName(result.getId());
            //判断其是否能被定位
            if (exceptionClass.getAnnotation(ExceptionIdentify.class) != null) {
                //实例化异常
                Exception exceptionInstance = null;
                //以message构造
                try {
                    exceptionInstance = (Exception) exceptionClass.getConstructor(String.class).newInstance(result.getMessage());
                } catch (Exception ignored) {
                }
                //以无参构造
                if (exceptionInstance == null)
                    try {
                        exceptionInstance = (Exception) exceptionClass.getConstructor().newInstance();
                    } catch (Exception ignored) {
                    }
                //构造异常实例失败
                if (exceptionInstance == null)
                    return null;
                //如果附带的异常数据
                if (exceptionInstance instanceof ExceptionExtraDataHolder) {
                    //加载附带的数据
                    ((ExceptionExtraDataHolder) exceptionInstance).loadFromMap(result.getData());
                }
                //返回异常
                return exceptionInstance;
            }
        } catch (Exception ignored) {
            //构造异常的过程中发生异常,返回null
        }
        return null;
    }
}
