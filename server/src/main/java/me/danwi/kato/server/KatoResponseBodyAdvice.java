package me.danwi.kato.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.common.ExceptionResult;
import me.danwi.kato.common.exception.ExceptionExtraDataHolder;
import me.danwi.kato.common.exception.KatoException;
import me.danwi.kato.common.exception.KatoUndeclaredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(KatoResponseBodyAdvice.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = returnType.getMethod();
        if (method == null)
            return false;
        return method.getAnnotation(PassByKato.class) == null && method.getDeclaringClass().getAnnotation(PassByKato.class) == null;
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
            LOGGER.debug("捕获异常：", (KatoException) body);
            LOGGER.debug("异常：{} 转换为：{}", body.getClass().getName(), exceptionResult);
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
        LOGGER.debug("katoExceptionHandler 捕获异常", exception);
        return exception;
    }

    @ExceptionHandler(Exception.class)
    KatoUndeclaredException katoUndeclaredExceptionHandler(Exception exception) {
        LOGGER.debug("katoUndeclaredExceptionHandler 捕获异常", exception);
        return new KatoUndeclaredException(exception);
    }
}
