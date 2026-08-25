package com.wwk.wwk_z_code.ai.model;

import jdk.jfr.Description;
import lombok.Data;

@Description("多文件代码响应结果")
@Data
public class MultiFileHtmlCodeResult {
    /**
     * HTML代码
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * JavaScript代码
     */
    @Description("JavaScript代码")
    private String jsCode;

    /**
     * CSS样式代码
     */
    @Description("CSS样式代码")
    private String cssCode;

    /**
     * 代码描述
     */
    @Description("代码描述")
    private String description;
}
