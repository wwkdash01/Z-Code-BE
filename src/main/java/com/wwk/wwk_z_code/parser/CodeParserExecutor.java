package com.wwk.wwk_z_code.parser;

import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;

public class CodeParserExecutor {

    private static final SingletonCodeParser singletonCodeParser = new SingletonCodeParser();
    private static final MultifileCodeParser multifileCodeParser = new MultifileCodeParser();

    /**
     * 手动解析LLM流式输出（流式无Json）
     *
     * @param userPrompt 用户提示词
     * @param codeGenEnum 生成模式
     * @return 解析结果PO
     */
    public static Object executeParse(String userPrompt, CodeGenEnum codeGenEnum) {
        // 1-参数校验
        if (userPrompt == null || codeGenEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "空参数");
        }

        // 2-分支执行，兜底异常
        return switch (codeGenEnum) {
            case SINGLETON_HTML -> singletonCodeParser.parseCode(userPrompt);
            case MULTIFILE_HTML -> multifileCodeParser.parseCode(userPrompt);
            default -> {
                String errMsg = "无效的生成模式:" + codeGenEnum.getCodeGenMode();
                throw new BusinessException(ErrorCode.PARAM_ERROR, errMsg);
            }
        };
    }

}
