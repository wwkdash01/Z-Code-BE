package com.wwk.wwk_z_code.parser;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.constant.AiConstant;

public class MultifileCodeParser implements CodeParser<MultiFileHtmlCodeResult> {
    /**
     * 解析多文件代码
     *
     * @param llmResult LLM输出
     * @return 解析结果PO
     */
    @Override
    public MultiFileHtmlCodeResult parseCode(String llmResult) {
        // 1-提取代码
        MultiFileHtmlCodeResult result = new MultiFileHtmlCodeResult();
        String htmlCode = extractCodeByPattern(llmResult, AiConstant.HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(llmResult, AiConstant.CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(llmResult, AiConstant.JS_CODE_PATTERN);

        // 2-判空抛异常
        throwIfMatchFailed(htmlCode, "HTML");
        throwIfMatchFailed(cssCode, "CSS");
        throwIfMatchFailed(jsCode, "JavaScript");

        // 3-赋值
        result.setHtmlCode(htmlCode);
        result.setCssCode(cssCode);
        result.setJsCode(jsCode);

        // 4-返回
        return result;
    }
}
