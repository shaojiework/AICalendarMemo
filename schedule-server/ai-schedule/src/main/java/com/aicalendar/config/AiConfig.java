package com.aicalendar.config;

import com.aicalendar.service.ai.AiToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import lombok.extern.slf4j.Slf4j;

/**
 * AI配置类 - Spring AI 1.0.0 正式版
 * 配置 OpenAI API 和 ChatClient
 */
@Slf4j
@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    /**
     * 配置 OpenAiApi - 使用 Builder 模式
     */
    @Bean
    public OpenAiApi openAiApi(RestClient.Builder restClientBuilder) {
        log.info("[AI配置] 创建 OpenAiApi，baseUrl: {}", baseUrl);
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restClientBuilder)
                .build();
    }

    /**
     * 配置 ChatClient，绑定系统prompt和工具
     * Spring AI 1.0.0 会自动扫描 @Tool 注解，但需要通过 defaultTools 明确绑定
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, AiToolService aiToolService) {
        log.info("[AI配置] 创建 ChatClient，绑定工具服务");
        return chatClientBuilder
                .defaultSystem(AiPromptConfig.SYSTEM_PROMPT)
                .defaultTools(aiToolService)
                .build();
    }
}