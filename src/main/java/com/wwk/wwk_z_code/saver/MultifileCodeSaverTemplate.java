package com.wwk.wwk_z_code.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;

import java.io.File;

/**
 * 多文件代码保存模板：将多文件代码结果保存为 index.html / style.css / script.js
 */
public class MultifileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileHtmlCodeResult> {

    /**
     * 内部方法 校验多文件代码结果，任一代码段为空抛业务异常
     *
     * @param result 多文件代码结果
     */
    @Override
    protected void validateResult(MultiFileHtmlCodeResult result) {
        super.validateResult(result);

        if (result.getHtmlCode() == null || result.getHtmlCode().isBlank()) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "代码生成异常");
        }

        if (result.getCssCode() == null || result.getCssCode().isBlank()) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "代码生成异常");
        }

        if (result.getJsCode() == null || result.getJsCode().isBlank()) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "代码生成异常");
        }

    }

    /**
     * 内部方法 将 HTML/CSS/JavaScript 代码分别写入指定目录
     *
     * @param result  多文件代码结果
     * @param saveDir 代码保存目录
     */
    @Override
    protected void saveCodeResultToFile(MultiFileHtmlCodeResult result, String saveDir) {
        String htmlFileName = saveDir + File.separator + "index.html";
        String cssFileName = saveDir + File.separator + "style.css";
        String jsFileName = saveDir + File.separator + "script.js";

        FileUtil.writeString(result.getHtmlCode(), htmlFileName, CharsetUtil.CHARSET_UTF_8);
        FileUtil.writeString(result.getCssCode(), cssFileName, CharsetUtil.CHARSET_UTF_8);
        FileUtil.writeString(result.getJsCode(), jsFileName, CharsetUtil.CHARSET_UTF_8);
    }
}
