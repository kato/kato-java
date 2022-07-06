package me.danwi.kato.common.argument;

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

    private static final String KATO_JSON_NODE_KEY = "_KATO_JSON_NODE_KEY_";

    private final ObjectMapper mapper;

    public MultiRequestBodyMethodArgumentHandlerResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        // 类（katoservice），方法，参数没有passby，
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
        final JsonNode rootNode = getJsonNode(nativeWebRequest);

        if (!ObjectUtils.isEmpty(rootNode)) {
            // 尝试获取field
            JsonNode node = rootNode.get(key);
            // 如果json中不存在与参数名称相对应的field,且开启了全body映射
            if (node == null && parameterAnnotation.parseBodyIfMissKey()) {
                node = rootNode;
            }
            result = mapper.treeToValue(node, methodParameter.getParameterType());
        }

        // 是否必填验证
        if (Objects.isNull(result) && parameterAnnotation.required()) {
            throw new IllegalArgumentException(String.format("required param %s is not present", key));
        }

        return result;
    }

    /**
     * 多个参数解析时，从attribute中获取数据
     *
     * @param nativeWebRequest
     * @return JsonNode
     * @throws IOException
     */
    private JsonNode getJsonNode(NativeWebRequest nativeWebRequest) throws IOException {
        Object attribute = nativeWebRequest.getAttribute(KATO_JSON_NODE_KEY, WebRequest.SCOPE_REQUEST);
        if (ObjectUtils.isEmpty(attribute)) {
            HttpServletRequest servletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
            Assert.state(servletRequest != null, "No HttpServletRequest");
            attribute = mapper.readTree(servletRequest.getInputStream());
            nativeWebRequest.setAttribute(KATO_JSON_NODE_KEY, attribute, WebRequest.SCOPE_REQUEST);
        }
        return (JsonNode) attribute;
    }
}
