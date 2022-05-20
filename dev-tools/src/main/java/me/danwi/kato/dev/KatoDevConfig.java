package me.danwi.kato.dev;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
public class KatoDevConfig implements WebMvcConfigurer {
    @Bean("me.danwi.kato.dev.StubController")
    public StubController stubController() {
        return new StubController();
    }

    @Bean("me.danwi.kato.dev.IndexerFactory")
    public IndexerFactory indexerFactory() {
        return new IndexerFactory();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/.kato/**")
                .addResourceLocations("classpath:/kato/ui/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/.kato").setViewName("redirect:/.kato/");
        registry.addViewController("/.kato/{path:[^\\\\.]*}").setViewName("forward:/.kato/");
        registry.addViewController("/.kato/").setViewName("forward:/.kato/index.html");
    }
}
