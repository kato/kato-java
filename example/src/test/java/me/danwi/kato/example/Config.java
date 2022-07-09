package me.danwi.kato.example;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public Logger.Level level() {
        return Logger.Level.FULL;
    }
}
