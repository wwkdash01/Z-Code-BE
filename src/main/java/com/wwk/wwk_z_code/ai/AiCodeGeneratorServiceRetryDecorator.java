package com.wwk.wwk_z_code.ai;

import cn.hutool.core.util.StrUtil;
import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 代码生成重试装饰器：非流式方法强制 JSON 输出，输出为空/非法/字段缺失时带纠正提示词重试；
 * 流式方法原样转发给被装饰的原始服务。
 */
@Slf4j
@RequiredArgsConstructor
public class AiCodeGeneratorServiceRetryDecorator implements AiCodeGeneratorService {

    /**
     * 最大尝试次数
     */
    private static final int MAX_ATTEMPTS = 3;

    /**
     * 重试时追加到用户提示词末尾的纠正提示词
     */
    private static final String CORRECTIVE_PROMPT_SUFFIX = """

            兜底重试提示：你上一次的输出不是合法的 JSON 对象（可能输出了 Markdown 代码块、解释或空内容）。\
            请严格只输出一个符合要求 JSON schema 的 JSON 对象，字段必须完整，禁止 Markdown 代码块、解释或任何多余字符。""";

    private final AiCodeGeneratorService delegate;

    @Override
    public SingletonHtmlCodeResult generateSingletonFileCode(String userPrompt) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                SingletonHtmlCodeResult result = delegate.generateSingletonFileCode(buildPrompt(userPrompt, attempt));
                if (isValid(result)) {
                    return result;
                }
                log.warn("单文件生成第{}次输出不合法，重试", attempt);
            } catch (Exception e) {
                log.warn("单文件生成第{}次异常:{}", attempt, e.getMessage());
            }
        }
        throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "多次调用仍未生成合法的单文件代码");
    }

    @Override
    public MultiFileHtmlCodeResult generateMultiFileCode(String userPrompt) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                MultiFileHtmlCodeResult result = delegate.generateMultiFileCode(buildPrompt(userPrompt, attempt));
                if (isValid(result)) {
                    return result;
                }
                log.warn("多文件生成第{}次输出不合法，重试", attempt);
            } catch (Exception e) {
                log.warn("多文件生成第{}次异常:{}", attempt, e.getMessage());
            }
        }
        throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "多次调用仍未生成合法的多文件代码");
    }

    @Override
    public Flux<String> generateSingletonFileCodeByStream(String userPrompt) {
        return delegate.generateSingletonFileCodeByStream(userPrompt);
    }

    @Override
    public Flux<String> generateMultiFileCodeByStream(String userPrompt) {
        return delegate.generateMultiFileCodeByStream(userPrompt);
    }

    /**
     * 第 1 次用原始提示词，后续尝试追加纠正提示词引导模型输出合法 JSON
     *
     * @param userPrompt 用户提示词
     * @param attempt    当前第几次尝试
     * @return 本次调用的提示词
     */
    private String buildPrompt(String userPrompt, int attempt) {
        if (attempt == 1) {
            return userPrompt;
        }
        return userPrompt + CORRECTIVE_PROMPT_SUFFIX;
    }

    /**
     * 校验单文件生成结果：非空且 HTML 代码非空
     *
     * @param result 单文件生成结果
     * @return {@code true} 合法
     */
    private boolean isValid(SingletonHtmlCodeResult result) {
        return result != null && StrUtil.isNotBlank(result.getHtmlCode());
    }

    /**
     * 校验多文件生成结果：非空且 HTML/CSS/JavaScript 代码均非空
     *
     * @param result 多文件生成结果
     * @return {@code true} 合法
     */
    private boolean isValid(MultiFileHtmlCodeResult result) {
        return result != null
                && StrUtil.isNotBlank(result.getHtmlCode())
                && StrUtil.isNotBlank(result.getCssCode())
                && StrUtil.isNotBlank(result.getJsCode());
    }
}
