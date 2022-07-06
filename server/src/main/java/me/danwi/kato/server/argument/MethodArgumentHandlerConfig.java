package me.danwi.kato.server.argument;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 参数处理器自动配置
 */
@Configuration
public class MethodArgumentHandlerConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    public MethodArgumentHandlerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MultiRequestBodyMethodArgumentHandlerResolver(objectMapper));
    }
}
