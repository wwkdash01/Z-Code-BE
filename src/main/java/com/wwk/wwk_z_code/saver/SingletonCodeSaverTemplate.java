package com.wwk.wwk_z_code.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;

import java.io.File;

/**
 * 单文件代码保存模板：将单文件 HTML 代码结果保存为 index.html
 */
public class SingletonCodeSaverTemplate extends CodeFileSaverTemplate<SingletonHtmlCodeResult> {

    /**
     * 内部方法 校验单文件代码结果，HTML 代码为空抛业务异常
     *
     * @param result 单文件代码结果
     */
    @Override
    protected void validateResult(SingletonHtmlCodeResult result) {
        super.validateResult(result);
        if (result.getHtmlCode() == null || result.getHtmlCode().isBlank()) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "代码生成异常");
        }
    }

    /**
     * 内部方法 将 HTML 代码写入指定目录的 index.html
     *
     * @param result  单文件代码结果
     * @param saveDir 代码保存目录
     */
    @Override
    protected void saveCodeResultToFile(SingletonHtmlCodeResult result, String saveDir) {
        String htmlFileName = saveDir + File.separator + "index.html";
        FileUtil.writeString(result.getHtmlCode(), htmlFileName, CharsetUtil.CHARSET_UTF_8);
    }
}
