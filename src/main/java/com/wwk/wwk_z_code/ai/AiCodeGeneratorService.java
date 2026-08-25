package com.wwk.wwk_z_code.ai;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {
    /**
     * 单文件代码生成接口
     *
     * @param userPrompt 用户提示词
     * @return LLM输出
     */
    @SystemMessage(fromResource = "prompt/singleton-code-generate-sys-prompt.txt")
    SingletonHtmlCodeResult generateSingletonFileCode(String userPrompt);

    /**
     * 多文件代码生成接口
     *
     * @param userPrompt 用户提示词
     * @return LLM输出
     */
    @SystemMessage(fromResource = "prompt/muti-file-code-generate-sys-prompt.txt")
    MultiFileHtmlCodeResult generateMultiFileCode(String userPrompt);
}
