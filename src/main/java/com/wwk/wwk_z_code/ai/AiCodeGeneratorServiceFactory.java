package com.wwk.wwk_z_code.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @ AI服务创建工厂
 */
@Configuration
@RequiredArgsConstructor
public class AiCodeGeneratorServiceFactory {
    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    /**
     * 创建原始AI代码生成器（LangChain4j 代理实现）
     *
     * @return 原始AI代码生成器
     */
    @Bean("rawAiCodeGeneratorService")
    public AiCodeGeneratorService rawAiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }

    /**
     * 创建重试装饰器：非流式强制 JSON 输出并带纠正重试兜底，作为默认注入的 AI 代码生成器
     *
     * @param rawService 原始AI代码生成器
     * @return 重试装饰器
     */
    @Bean
    @Primary
    public AiCodeGeneratorService aiCodeGeneratorService(
            @Qualifier("rawAiCodeGeneratorService") AiCodeGeneratorService rawService) {
        return new AiCodeGeneratorServiceRetryDecorator(rawService);
    }
}
