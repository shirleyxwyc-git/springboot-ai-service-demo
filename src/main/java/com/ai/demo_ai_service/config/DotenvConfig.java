package com.ai.demo_ai_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;


@Configuration
public class DotenvConfig {
   @Bean
    public Dotenv dotenv() {
        return Dotenv.load();
    }
}
