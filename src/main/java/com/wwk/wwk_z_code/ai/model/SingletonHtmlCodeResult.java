package com.wwk.wwk_z_code.ai.model;

import jdk.jfr.Description;
import lombok.Data;

@Description("单文件代码响应结果")
@Data
public class SingletonHtmlCodeResult {

    /**
     * HTML代码
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * 代码描述
     */
    @Description("代码描述")
    private String description;
}
