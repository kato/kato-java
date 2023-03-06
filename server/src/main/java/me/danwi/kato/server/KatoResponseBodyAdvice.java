package me.danwi.kato.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import me.danwi.kato.common.ExceptionResult;
import me.danwi.kato.common.exception.ExceptionExtraDataHolder;
import me.danwi.kato.common.exception.KatoBadRequestException;
import me.danwi.kato.common.exception.KatoException;
import me.danwi.kato.common.exception.KatoUndeclaredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

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
            ExceptionResult exceptionResult = ExceptionResult.fromException((Throwable) body);
            exceptionResult.setPath(request.getURI().getPath());

            //如果有附加数据,则需要填充附加数据
            if (body instanceof ExceptionExtraDataHolder) {
                exceptionResult.setData(((ExceptionExtraDataHolder) body).toMap());
            }

            // 设置Kato专有Header（用于客户端判断返回body是否是ExceptionResult结构）
            response.getHeaders().add(ExceptionResult.HEADER_KEY, "true");
            //设置异常状态码
            response.setStatusCode(((KatoException) body).getHttpStatus());
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

    /**
     * 参数校验异常统一转换：@RequestBody 处理后
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    KatoException katoExceptionHandler(MethodArgumentNotValidException exception, HandlerMethod handlerMethod) {
        LOGGER.debug("捕获 MethodArgumentNotValidException，转换为 BadRequestException", exception);
        return new KatoBadRequestException(exception.getMessage(), exception);
    }

    /**
     * 参数校验异常统一转换：MultiRequestBodyMethodArgumentHandlerResolver处理后
     */
    @ExceptionHandler(ConstraintViolationException.class)
    KatoException katoExceptionHandler(ConstraintViolationException exception, HandlerMethod handlerMethod) {
        final Optional<ConstraintViolation<?>> constraintViolationOptional = exception.getConstraintViolations().stream().findFirst();
        if (constraintViolationOptional.isPresent()) {
            final Class<?> rootClass = handlerMethod.getBeanType();
            // Controller层
            if (rootClass.getAnnotation(RestController.class) != null
                    || rootClass.getAnnotation(Controller.class) != null
                    || rootClass.getDeclaredAnnotation(RestController.class) != null
                    || rootClass.getDeclaredAnnotation(Controller.class) != null
            ) {
                // 必须是参数校验抛出异常才转换为KatoBadRequestException
                final ConstraintViolation<?> constraintViolation = constraintViolationOptional.get();
                final Object[] executableParam = constraintViolation.getExecutableParameters();
                if ( // 所有添加@Valid注解的参数 并且是getExecutableParameters类型
                        executableParam != null
                                && Arrays.stream(handlerMethod.getMethodParameters()).anyMatch(it -> it.hasParameterAnnotation(Valid.class)
                                && Arrays.stream(executableParam).anyMatch(param -> it.getParameterType().isInstance(param)))
                ) {
                    LOGGER.debug("捕获Controller 层抛出 ConstraintViolationException，转换为 BadRequestException", exception);
                    return new KatoBadRequestException(exception.getMessage(), exception);
                }
            }
        }
        LOGGER.debug("捕获 ConstraintViolationException，转换为 KatoUndeclaredException", exception);
        return new KatoUndeclaredException(exception.getMessage(), exception);
    }

    @ExceptionHandler(KatoException.class)
    KatoException katoExceptionHandler(KatoException exception) {
        LOGGER.debug("katoExceptionHandler 捕获异常", exception);
        return exception;
    }

    @ExceptionHandler(Exception.class)
    KatoUndeclaredException katoUndeclaredExceptionHandler(Exception exception) {
        LOGGER.debug("katoUndeclaredExceptionHandler 捕获异常", exception);
        return new KatoUndeclaredException(exception.getMessage(), exception);
    }
}
