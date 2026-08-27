package com.wwk.wwk_z_code.parser;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * MultifileCodeParser 单元测试：验证从 LLM 输出的 Markdown 围栏代码块中提取 HTML / CSS / JavaScript 代码段；缺任一代码段抛业务异常。
 */
class MultifileCodeParserTest {

    private final MultifileCodeParser multifileCodeParser = new MultifileCodeParser();

    @Test
    void parseCode_fullOutput_extractsThreeParts() {
        String output = """
                以下是生成的多文件网页代码。

                index.html:
                ```html
                <!DOCTYPE html>
                <html lang="zh">
                <head><title>个人博客</title></head>
                <body><h1>欢迎光临</h1></body>
                </html>
                ```

                style.css:
                ```css
                body { font-family: sans-serif; }
                h1 { color: #333; }
                ```

                script.js:
                ```javascript
                console.log('hello');
                document.title = '个人博客';
                ```
                """;

        MultiFileHtmlCodeResult result = multifileCodeParser.parseCode(output);

        assertEquals("""
                <!DOCTYPE html>
                <html lang="zh">
                <head><title>个人博客</title></head>
                <body><h1>欢迎光临</h1></body>
                </html>""", result.getHtmlCode());
        assertEquals("""
                body { font-family: sans-serif; }
                h1 { color: #333; }""", result.getCssCode());
        assertEquals("""
                console.log('hello');
                document.title = '个人博客';""", result.getJsCode());
    }

    @Test
    void parseCode_missingCssBlock_throws() {
        String output = """
                ```html
                <h1>标题</h1>
                ```

                ```javascript
                console.log('ok');
                ```
                """;

        BusinessException e = assertThrows(BusinessException.class,
                () -> multifileCodeParser.parseCode(output));

        assertEquals(ErrorCode.CODE_GENERATE_ERROR.getCode(), e.getCode());
    }

    @Test
    void parseCode_jsTagVariant_extractsJs() {
        String output = """
                ```html
                <h1>标题</h1>
                ```

                ```css
                h1 { color: red; }
                ```

                ```js
                var x = 1;
                ```
                """;

        MultiFileHtmlCodeResult result = multifileCodeParser.parseCode(output);

        assertEquals("<h1>标题</h1>", result.getHtmlCode());
        assertEquals("h1 { color: red; }", result.getCssCode());
        assertEquals("var x = 1;", result.getJsCode());
    }

    @Test
    void parseCode_upperCaseLangTags_extracts() {
        String output = """
                ```HTML
                <h1>大写标签</h1>
                ```

                ```CSS
                h1 { color: blue; }
                ```

                ```Javascript
                console.log('大写');
                ```
                """;

        MultiFileHtmlCodeResult result = multifileCodeParser.parseCode(output);

        assertEquals("<h1>大写标签</h1>", result.getHtmlCode());
        assertEquals("h1 { color: blue; }", result.getCssCode());
        assertEquals("console.log('大写');", result.getJsCode());
    }
}
