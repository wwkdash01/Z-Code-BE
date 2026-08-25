package com.wwk.wwk_z_code.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ AI服务创建工厂
 */
@Configuration
@RequiredArgsConstructor
public class AiCodeGeneratorServiceFactory {
    private final ChatModel chatModel;

    /**
     * 创建AI代码生成器
     *
     * @return AI代码生成器
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.create(AiCodeGeneratorService.class, chatModel);
    }
}
