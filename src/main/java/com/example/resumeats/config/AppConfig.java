package com.example.resumeats.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${huggingface.api.key:}")
    private String hfApiKey;

    @Bean
    public WebClient huggingFaceWebClient() {
        if (hfApiKey == null || hfApiKey.isBlank()) {
            org.slf4j.LoggerFactory.getLogger(AppConfig.class).warn(
                    "HF_API_KEY is not set! Hugging Face requests will fail when calling /api/ats/analyze. "
                            + "Set the environment variable: export HF_API_KEY=hf_xxxxx");
        }

        return WebClient.builder()
                .baseUrl("https://router.huggingface.co")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + hfApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
                .build();
    }
}