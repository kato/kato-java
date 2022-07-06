package me.danwi.kato.common.argument;

import cn.hutool.core.io.IoUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 解析请求：body
 * <p> 支持特性：
 * <ul>
 *  <li>  不限制请求类型，支持GET及其他类型请求
 * </ul>
 * <p> 如果JSON是Object：
 * <ul>
 *  <li>  支持通过注解value，指定JSON的key来解析对象
 *  <li>  支持注解无value，直接根据参数名来解析对象
 *  <li>  支持注解无value且参数名不匹配JSON串key时，整个body作为参数解析
 *  <li>  支持参数“共用”（不指定value时，参数名不为JSON串的key时，解析整个body）
 *  <li>  支持多余属性不报错（需配置{@link ObjectMapper#configure(DeserializationFeature, boolean)}为FAIL_ON_UNKNOWN_PROPERTIES(false)）
 * </ul>
 * <p> 如果JSON不是Object：
 * <ul>
 *  <li>  默认整个body作为参数解析
 * </ul>
 */
public class MultiRequestBodyMethodArgumentHandlerResolver implements HandlerMethodArgumentResolver {

    private static final String BODY_KEY = "BODY_KEY";

    private final ObjectMapper mapper;

    public MultiRequestBodyMethodArgumentHandlerResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(MultiRequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        // 定义结果
        Object result = null;

        // 获取注解
        final MultiRequestBody parameterAnnotation = methodParameter.getParameterAnnotation(MultiRequestBody.class);
        // 获取key
        String key = parameterAnnotation.value();
        if (ObjectUtils.isEmpty(key)) {
            key = methodParameter.getParameterName();
        }
        // 获取请求body字符串
        final String body = getBody(nativeWebRequest);

        if (!ObjectUtils.isEmpty(body)) {
            final JsonNode jsonNode = mapper.readTree(body);
            // key解析
            if (jsonNode.has(key)) {
                // String key
                final JsonNode node = jsonNode.get(key);
                result = readValue(methodParameter, node.toString());
            } else {
                // 如果没有指定key，则判断是否可以解析整个body
                if (parameterAnnotation.parseBodyIfMissKey()) {
                    try {
                        result = readValue(methodParameter, body);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        // 是否必填验证
        if (Objects.isNull(result) && parameterAnnotation.required()) {
            throw new IllegalArgumentException(String.format("required param %s is not present", key));
        }

        return result;
    }

    private Object readValue(MethodParameter methodParameter, String content) throws IOException {
        return mapper.readValue(content, new TypeReference<Object>() {
            @Override
            public Type getType() {
                return methodParameter.getGenericParameterType();
            }
        });
    }

    /**
     * 多个参数解析时，从attribute中获取数据
     *
     * @param nativeWebRequest
     * @return
     * @throws IOException
     */
    private String getBody(NativeWebRequest nativeWebRequest) throws IOException {
        Object attribute = nativeWebRequest.getAttribute(BODY_KEY, WebRequest.SCOPE_REQUEST);
        if (ObjectUtils.isEmpty(attribute)) {
            HttpServletRequest servletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
            Assert.state(servletRequest != null, "No HttpServletRequest");
            attribute = IoUtil.read(servletRequest.getInputStream(), StandardCharsets.UTF_8);
            nativeWebRequest.setAttribute(BODY_KEY, attribute, WebRequest.SCOPE_REQUEST);
        }
        return (String) attribute;
    }
}
