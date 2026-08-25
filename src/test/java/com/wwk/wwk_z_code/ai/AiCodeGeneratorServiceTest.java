package com.wwk.wwk_z_code.ai;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {
    @Autowired
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateSingletonFileCode() {
        SingletonHtmlCodeResult re = aiCodeGeneratorService.generateSingletonFileCode("写一个langchain4j后端博客，不超过20行");
        Assertions.assertNotNull(re);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileHtmlCodeResult re = aiCodeGeneratorService.generateMultiFileCode("写一个langchain4j后端博客，不超过60行");
        Assertions.assertNotNull(re);
    }
}