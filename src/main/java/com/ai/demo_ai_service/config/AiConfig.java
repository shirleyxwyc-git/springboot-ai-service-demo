package com.ai.demo_ai_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.Data;

@Data
@Configuration
public class AiConfig {

  @Autowired
  private Dotenv dotenv;

  private String apiKey;
  private String apiUrl;
  
// 讀取 .env 文件

  @PostConstruct
  public void init() {
    apiKey = dotenv.get("AI_API_KEY");
    apiUrl = dotenv.get("AI_API_URL");
  }
}
