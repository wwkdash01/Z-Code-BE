package com.wwk.wwk_z_code.saver;

import cn.hutool.core.io.FileUtil;
import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
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
 * CodeFileSaverExecutor 单元测试：按生成模式分发到对应模板并透传 appId；文件落盘与代码段校验
 */
class CodeFileSaverExecutorTest {

    private final List<File> createdDirs = new ArrayList<>();

    @AfterEach
    void cleanup() {
        for (File dir : createdDirs) {
            FileUtil.del(dir);
        }
    }

    @Test
    void saveCode_singletonMode_savesIndexHtmlWithAppIdSuffix() {
        SingletonHtmlCodeResult result = new SingletonHtmlCodeResult();
        result.setHtmlCode("<h1>hello</h1>");

        File savedDir = CodeFileSaverExecutor.saveCode(result, CodeGenEnum.SINGLETON_HTML, 100L);
        createdDirs.add(savedDir);

        assertNotNull(savedDir);
        assertTrue(savedDir.isDirectory());
        assertTrue(savedDir.getName().endsWith("_100"));
        assertEquals("<h1>hello</h1>", FileUtil.readUtf8String(new File(savedDir, "index.html")));
    }

    @Test
    void saveCode_multifileMode_savesThreeFiles() {
        MultiFileHtmlCodeResult result = new MultiFileHtmlCodeResult();
        result.setHtmlCode("<h1>博客</h1>");
        result.setCssCode("h1 { color: red; }");
        result.setJsCode("console.log('ok');");

        File savedDir = CodeFileSaverExecutor.saveCode(result, CodeGenEnum.MULTIFILE_HTML, 200L);
        createdDirs.add(savedDir);

        assertNotNull(savedDir);
        assertEquals("<h1>博客</h1>", FileUtil.readUtf8String(new File(savedDir, "index.html")));
        assertEquals("h1 { color: red; }", FileUtil.readUtf8String(new File(savedDir, "style.css")));
        assertEquals("console.log('ok');", FileUtil.readUtf8String(new File(savedDir, "script.js")));
    }

    @Test
    void saveCode_nullAppId_throwsParamError() {
        SingletonHtmlCodeResult result = new SingletonHtmlCodeResult();
        result.setHtmlCode("<h1>hello</h1>");

        BusinessException e = assertThrows(BusinessException.class,
                () -> CodeFileSaverExecutor.saveCode(result, CodeGenEnum.SINGLETON_HTML, null));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
    }

    @Test
    void saveCode_singletonBlankHtml_throwsCodeGenerateError() {
        SingletonHtmlCodeResult result = new SingletonHtmlCodeResult();
        result.setHtmlCode("   ");

        BusinessException e = assertThrows(BusinessException.class,
                () -> CodeFileSaverExecutor.saveCode(result, CodeGenEnum.SINGLETON_HTML, 1L));

        assertEquals(ErrorCode.CODE_GENERATE_ERROR.getCode(), e.getCode());
    }

    @Test
    void saveCode_multifileMissingCss_throwsCodeGenerateError() {
        MultiFileHtmlCodeResult result = new MultiFileHtmlCodeResult();
        result.setHtmlCode("<h1>博客</h1>");
        result.setJsCode("console.log('ok');");
        // cssCode 缺省

        BusinessException e = assertThrows(BusinessException.class,
                () -> CodeFileSaverExecutor.saveCode(result, CodeGenEnum.MULTIFILE_HTML, 1L));

        assertEquals(ErrorCode.CODE_GENERATE_ERROR.getCode(), e.getCode());
    }
}
