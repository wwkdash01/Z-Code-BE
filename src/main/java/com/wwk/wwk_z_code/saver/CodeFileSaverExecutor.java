package com.wwk.wwk_z_code.saver;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;

import java.io.File;

/**
 * 代码保存执行器：按生成模式分发到对应的代码保存模板
 */
public class CodeFileSaverExecutor {
    private static final SingletonCodeSaverTemplate singletonCodeSaverTemplate = new SingletonCodeSaverTemplate();
    private static final MultifileCodeSaverTemplate multifileCodeSaverTemplate = new MultifileCodeSaverTemplate();

    /**
     * 外部接口：按生成模式保存解析结果到本地
     *
     * @param result      解析结果（单文件/多文件代码结果）
     * @param codeGenEnum 代码生成模式枚举
     * @return 代码保存目录
     */
    public static File saveCode(Object result, CodeGenEnum codeGenEnum, Long appId) {
        return switch (codeGenEnum) {
            case SINGLETON_HTML -> singletonCodeSaverTemplate.saveCodeResult((SingletonHtmlCodeResult) result, appId);
            case MULTIFILE_HTML -> multifileCodeSaverTemplate.saveCodeResult((MultiFileHtmlCodeResult) result, appId);
            default -> {
                throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "无效的生成模式");
            }
        };
    }
}
