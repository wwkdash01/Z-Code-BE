package com.wwk.wwk_z_code.ai;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 单文件代码生成接口
     *
     * @param userPrompt 用户提示词
     * @return LLM输出
     */
    @SystemMessage(fromResource = "prompt/singleton-code-generate-json-sys-prompt.txt")
    SingletonHtmlCodeResult generateSingletonFileCode(String userPrompt);

    /**
     * 多文件代码生成接口
     *
     * @param userPrompt 用户提示词
     * @return LLM输出
     */
    @SystemMessage(fromResource = "prompt/muti-file-code-generate-json-sys-prompt.txt")
    MultiFileHtmlCodeResult generateMultiFileCode(String userPrompt);

    /**
     * 单文件代码生成接口(流式输出)
     *
     * @param userPrompt 用户提示词
     * @return LLM输出
     */
    @SystemMessage(fromResource = "prompt/singleton-code-generate-sys-prompt.txt")
    Flux<String> generateSingletonFileCodeByStream(String userPrompt);

    /**
     * 多文件代码生成接口(流式输出)
     *
     * @param userPrompt 用户提示词
     * @return LLM输出
     */
    @SystemMessage(fromResource = "prompt/muti-file-code-generate-sys-prompt.txt")
    Flux<String> generateMultiFileCodeByStream(String userPrompt);


}
