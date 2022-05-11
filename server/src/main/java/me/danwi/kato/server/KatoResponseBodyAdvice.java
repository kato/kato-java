package me.danwi.kato.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.common.Result;
import me.danwi.kato.common.exception.ExceptionExtraDataHolder;
import me.danwi.kato.common.exception.KatoException;
import me.danwi.kato.common.exception.KatoUndeclaredException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.Map;

@RestControllerAdvice
public class KatoResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = returnType.getMethod();
        if (method == null)
            return false;
        return method.getAnnotation(PassByKato.class) == null && method.getDeclaringClass().getAnnotation(PassByKato.class) == null;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //已经是Result
        if (body instanceof Result) return body;

        //如果是一个异常
        if (body instanceof KatoException) {
            Result<Map<String, Object>> exceptionResult = new Result<>();
            //设置异常定位ID
            exceptionResult.setException(body.getClass().getName());
            //message填充
            exceptionResult.setMessage(((KatoException) body).getMessage());
            //如果有附加数据,则需要填充附加数据
            if (body instanceof ExceptionExtraDataHolder) {
                exceptionResult.setData(((ExceptionExtraDataHolder) body).toMap());
            }
            return exceptionResult;
        }

        //针对String做特殊的处理
        if (body instanceof String) {
            try {
                response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                return new ObjectMapper().writeValueAsString(new Result<>((String) body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("json序列化失败", e);
            }
        }

        //通用结果
        return new Result<>(body);
    }

    @ExceptionHandler(KatoException.class)
    KatoException katoExceptionHandler(KatoException exception) {
        return exception;
    }

    @ExceptionHandler(Exception.class)
    KatoUndeclaredException katoUndeclaredExceptionHandler(Exception exception) {
        return new KatoUndeclaredException(exception);
    }
}
