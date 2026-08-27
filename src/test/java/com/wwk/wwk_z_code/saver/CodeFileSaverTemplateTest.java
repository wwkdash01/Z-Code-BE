package com.wwk.wwk_z_code.saver;

import cn.hutool.core.io.FileUtil;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CodeFileSaverTemplate 抽象模板契约测试：校验结果 → 构建唯一目录（appId 必填、目录名含 appId）→ 写入钩子
 */
class CodeFileSaverTemplateTest {

    private final TestTemplate template = new TestTemplate();

    @AfterEach
    void cleanup() {
        for (String saveDir : template.writtenDirs) {
            FileUtil.del(saveDir);
        }
    }

    @Test
    void saveCodeResult_nullResult_throwsCodeGenerateError() {
        BusinessException e = assertThrows(BusinessException.class, () -> template.saveCodeResult(null, 1L));

        assertEquals(ErrorCode.CODE_GENERATE_ERROR.getCode(), e.getCode());
    }

    @Test
    void saveCodeResult_nullAppId_throwsParamError() {
        BusinessException e = assertThrows(BusinessException.class, () -> template.saveCodeResult("<h1>ok</h1>", null));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertTrue(e.getMessage().contains("应用id不能为空"));
    }

    @Test
    void saveCodeResult_validResult_createsDirWithAppIdSuffixAndInvokesHook() {
        File savedDir = template.saveCodeResult("<h1>ok</h1>", 123L);

        assertNotNull(savedDir);
        assertTrue(savedDir.isDirectory());
        assertTrue(savedDir.getAbsolutePath().contains(File.separator + "tmp" + File.separator + "code_outputs"));
        assertTrue(savedDir.getName().endsWith("_123"));

        assertEquals(1, template.writtenDirs.size());
        assertEquals(savedDir.getAbsolutePath(), template.writtenDirs.get(0));
    }

    /**
     * 测试子类：仅记录写入钩子收到的 saveDir，便于断言模板主流程
     */
    private static class TestTemplate extends CodeFileSaverTemplate<String> {
        private final List<String> writtenDirs = new ArrayList<>();

        @Override
        protected void saveCodeResultToFile(String result, String saveDir) {
            writtenDirs.add(saveDir);
        }
    }
}
