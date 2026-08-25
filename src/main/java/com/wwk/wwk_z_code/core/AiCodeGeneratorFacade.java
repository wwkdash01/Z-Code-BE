package com.wwk.wwk_z_code.core;

import com.wwk.wwk_z_code.ai.AiCodeGeneratorService;
import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 代码生成门面类
 */
@RequiredArgsConstructor
@Service
public class AiCodeGeneratorFacade {
    private final AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 外部接口：代码生成统一入口，根据生成模式生成代码并保存到本地
     *
     * @param userPrompt 用户提示词
     * @param codeGenEnum 代码生成模式枚举
     * @return 代码保存目录
     */
    public File generateAndSaveCode(String userPrompt, CodeGenEnum codeGenEnum) {
        if (userPrompt == null || codeGenEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "空参数");
        }

        return switch (codeGenEnum) {
            case SINGLETON_HTML -> generateAndSaveSingletonCode(userPrompt);
            case MULTIFILE_HTML -> generateAndSaveMultiFileCode(userPrompt);
            default -> {
                String errMsg = "无效的生成模式:" + codeGenEnum.getCodeGenMode();
                throw new BusinessException(ErrorCode.PARAM_ERROR, errMsg);
            }
        };
    }

    /**
     * 内部方法：生成单文件 HTML 代码并保存
     *
     * @param userPrompt 用户提示词
     * @return 代码保存目录
     */
    private File generateAndSaveSingletonCode(String userPrompt) {
        SingletonHtmlCodeResult result = aiCodeGeneratorService.generateSingletonFileCode(userPrompt);
        return CodeResultSaver.saveSingletonCodeResult(result);
    }

    /**
     * 内部方法：生成多文件 HTML 代码并保存
     *
     * @param userPrompt 用户提示词
     * @return 代码保存目录
     */
    private File generateAndSaveMultiFileCode(String userPrompt) {
        MultiFileHtmlCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userPrompt);
        return CodeResultSaver.saveMultiFileCodeResult(result);
    }
}
