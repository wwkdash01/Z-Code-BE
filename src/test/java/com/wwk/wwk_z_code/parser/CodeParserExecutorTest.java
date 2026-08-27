package com.wwk.wwk_z_code.parser;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CodeParserExecutor 单元测试：验证按生成模式分发到对应解析器；参数为空抛业务异常。
 */
class CodeParserExecutorTest {

    @Test
    void executeParse_singletonMode_returnsSingletonResult() {
        String output = """
                ```html
                <h1>单文件</h1>
                ```
                """;

        Object result = CodeParserExecutor.executeParse(output, CodeGenEnum.SINGLETON_HTML);

        assertTrue(result instanceof SingletonHtmlCodeResult);
        assertEquals("<h1>单文件</h1>", ((SingletonHtmlCodeResult) result).getHtmlCode());
    }

    @Test
    void executeParse_multifileMode_returnsMultiFileResult() {
        String output = """
                ```html
                <h1>多文件</h1>
                ```

                ```css
                h1 { color: red; }
                ```

                ```javascript
                console.log('ok');
                ```
                """;

        Object result = CodeParserExecutor.executeParse(output, CodeGenEnum.MULTIFILE_HTML);

        assertTrue(result instanceof MultiFileHtmlCodeResult);
        MultiFileHtmlCodeResult multifileResult = (MultiFileHtmlCodeResult) result;
        assertEquals("<h1>多文件</h1>", multifileResult.getHtmlCode());
        assertEquals("h1 { color: red; }", multifileResult.getCssCode());
        assertEquals("console.log('ok');", multifileResult.getJsCode());
    }

    @Test
    void executeParse_nullParam_throws() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> CodeParserExecutor.executeParse(null, CodeGenEnum.SINGLETON_HTML));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
    }
}
