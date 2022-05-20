package me.danwi.kato.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.common.ExceptionResult;
import me.danwi.kato.common.exception.ExceptionExtraDataHolder;
import me.danwi.kato.common.exception.KatoException;
import me.danwi.kato.common.exception.KatoUndeclaredException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

@RestControllerAdvice
public class KatoResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = returnType.getMethod();
        if (method == null)
            return false;
        Class<?> clazz = method.getDeclaringClass();
        return clazz.getAnnotation(KatoService.class) != null
                && clazz.getAnnotation(PassByKato.class) == null
                && method.getAnnotation(PassByKato.class) == null;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //如果是一个异常
        if (body instanceof KatoException) {
            ExceptionResult exceptionResult = new ExceptionResult();
            //设置异常定位ID
            exceptionResult.setId(body.getClass().getName());
            //message填充
            exceptionResult.setMessage(((KatoException) body).getMessage());
            //如果有附加数据,则需要填充附加数据
            if (body instanceof ExceptionExtraDataHolder) {
                exceptionResult.setData(((ExceptionExtraDataHolder) body).toMap());
            }
            //设置异常状态码
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exceptionResult;
        }

        //针对String做特殊的处理
        if (body instanceof String) {
            try {
                response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                return mapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("json序列化失败", e);
            }
        }

        //正常情况不做处理
        return body;
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
