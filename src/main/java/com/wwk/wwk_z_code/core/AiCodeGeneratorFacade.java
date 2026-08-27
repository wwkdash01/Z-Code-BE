package com.wwk.wwk_z_code.core;

import com.wwk.wwk_z_code.ai.AiCodeGeneratorService;
import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;

import com.wwk.wwk_z_code.parser.CodeParserExecutor;
import com.wwk.wwk_z_code.saver.CodeFileSaverExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成门面类
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    private final AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 外部接口：代码生成统一入口，根据生成模式生成代码并保存到本地
     *
     * @param userPrompt 用户提示词
     * @param codeGenEnum 代码生成模式枚举
     * @return 代码保存目录
     */
    public File generateAndSaveCode(String userPrompt, CodeGenEnum codeGenEnum, Long appId) throws BusinessException {
        // 1-参数校验
        if (userPrompt == null || codeGenEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未指定生成模式");
        }

        // 2-分支判断执行
        return switch (codeGenEnum) {
            case SINGLETON_HTML -> generateAndSaveSingletonCode(userPrompt,  appId);
            case MULTIFILE_HTML -> generateAndSaveMultiFileCode(userPrompt, appId);
            default -> {
                String errMsg = "无效的生成模式:" + codeGenEnum.getCodeGenMode();
                throw new BusinessException(ErrorCode.PARAM_ERROR, errMsg);
            }
        };
    }

    /**
     * 外部接口：代码生成(流式)统一入口，根据生成模式生成代码并保存到本地
     *
     * @param userPrompt  用户提示词
     * @param codeGenEnum 代码生成模式枚举
     * @return LLM 流式输出
     */
    public Flux<String> generateAndSaveCodeByStream(String userPrompt, CodeGenEnum codeGenEnum, Long appId) throws BusinessException {
        // 1-参数校验
        if (userPrompt == null || codeGenEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未指定生成模式");
        }

        // 2-分支执行，获取对应模式的流式输出
        Flux<String> llmStream = switch (codeGenEnum) {
            case SINGLETON_HTML -> aiCodeGeneratorService.generateSingletonFileCodeByStream(userPrompt);
            case MULTIFILE_HTML -> aiCodeGeneratorService.generateMultiFileCodeByStream(userPrompt);
            default -> {
                String errMsg = "无效的生成模式:" + codeGenEnum.getCodeGenMode();
                throw new BusinessException(ErrorCode.PARAM_ERROR, errMsg);
            }
        };

        // 3-累积流式输出片段
        StringBuilder stringBuilder = new StringBuilder();

        return llmStream
                .doOnNext(stringBuilder::append)
                .doOnComplete(() -> {
                    try {
                        // 4-输出完成后解析并保存
                        String codeResult = stringBuilder.toString();
                        Object parsedResult = CodeParserExecutor.executeParse(codeResult, codeGenEnum);
                        File savedDir = CodeFileSaverExecutor.saveCode(parsedResult, codeGenEnum, appId);

                        log.debug("文件保存成功:{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("文件保存失败:{}", e.getMessage());
                    }
                });
    }

    /**
     * 内部方法：生成单文件 HTML 代码并保存
     *
     * @param userPrompt 用户提示词
     * @return 代码保存目录
     */
    private File generateAndSaveSingletonCode(String userPrompt, Long appId) {
        SingletonHtmlCodeResult result = aiCodeGeneratorService.generateSingletonFileCode(userPrompt);
        return CodeFileSaverExecutor.saveCode(result, CodeGenEnum.SINGLETON_HTML, appId);
    }

    /**
     * 内部方法：生成多文件 HTML 代码并保存
     *
     * @param userPrompt 用户提示词
     * @return 代码保存目录
     */
    private File generateAndSaveMultiFileCode(String userPrompt, Long appId) {
        MultiFileHtmlCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userPrompt);
        return CodeFileSaverExecutor.saveCode(result, CodeGenEnum.MULTIFILE_HTML, appId);
    }

}
