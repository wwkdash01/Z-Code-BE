package com.wwk.wwk_z_code.parser;

import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.constant.AiConstant;

public class SingletonCodeParser implements CodeParser<SingletonHtmlCodeResult> {

    @Override
    public SingletonHtmlCodeResult parseCode(String llmResult) {
        // 1-提取代码
        SingletonHtmlCodeResult result = new SingletonHtmlCodeResult();
        String htmlCode = extractCodeByPattern(llmResult, AiConstant.HTML_CODE_PATTERN);

        // 2-判空抛异常
        throwIfMatchFailed(htmlCode, "HTML");

        // 3-赋值
        result.setHtmlCode(htmlCode);

        // 4-返回
        return result;
    }
}
