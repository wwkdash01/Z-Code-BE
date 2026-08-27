package com.wwk.wwk_z_code.parser;

import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * SingletonCodeParser 单元测试：验证从 LLM 输出的 Markdown 围栏代码块中提取 HTML 代码段；无 HTML 代码段抛业务异常。
 */
class SingletonCodeParserTest {

    private final SingletonCodeParser singletonCodeParser = new SingletonCodeParser();

    @Test
    void parseCode_withHtmlBlock_returnsExtractedHtml() {
        String output = """
                这是生成的单文件网页。

                ```html
                <!DOCTYPE html>
                <html lang="zh">
                <head><title>个人博客</title></head>

                <body><h1>欢迎光临</h1></body>
                </html>
                ```""";

        SingletonHtmlCodeResult result = singletonCodeParser.parseCode(output);

        assertEquals("""
                <!DOCTYPE html>
                <html lang="zh">
                <head><title>个人博客</title></head>

                <body><h1>欢迎光临</h1></body>
                </html>""", result.getHtmlCode());
    }

    @Test
    void parseCode_withoutHtmlBlock_throws() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> singletonCodeParser.parseCode("生成失败，直接返回原文"));

        assertEquals(ErrorCode.CODE_GENERATE_ERROR.getCode(), e.getCode());
    }
}
