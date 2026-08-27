package com.wwk.wwk_z_code.core;

import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        Long appId = 1L;

        File singletonResult = aiCodeGeneratorFacade.generateAndSaveCode("做一个个人博客网站模版，20行以内", CodeGenEnum.SINGLETON_HTML, appId);
        Assertions.assertNotNull(singletonResult);
        // 目录名以 appId 结尾，验证 appId 端到端透传生效
        Assertions.assertTrue(singletonResult.getAbsolutePath().endsWith("_" + appId));

        File multifileResult = aiCodeGeneratorFacade.generateAndSaveCode("做一个个人博客网站模版，70行以内", CodeGenEnum.MULTIFILE_HTML, appId);
        Assertions.assertNotNull(multifileResult);
        Assertions.assertTrue(multifileResult.getAbsolutePath().endsWith("_" + appId));
    }
}