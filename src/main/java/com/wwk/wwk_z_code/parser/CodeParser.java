package com.wwk.wwk_z_code.parser;

import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface CodeParser<T> {
    /**
     * 解析llm输出并封装结果类
     *
     * @param llmResult LLM输出
     * @return 解析结果（MultiFileCodeResult/SingletonCodeResult）
     */
    T parseCode(String llmResult);

    /**
     * 校验代码段是否匹配失败，失败抛业务异常
     *
     * @param code     提取的代码段
     * @param codeType 代码段类型（HTML / CSS / JavaScript）
     */
    default void throwIfMatchFailed(String code, String codeType) {
        if (code == null || code.trim().isBlank()) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, codeType + "代码段匹配失败");
        }
    }

    /**
     * 根据正则表达式抽取代码
     * @param code LLM输出
     * @param pattern 正则表达式
     * @return 代码
     */
    default String extractCodeByPattern(String code, Pattern pattern) {
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
