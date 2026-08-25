package com.wwk.wwk_z_code.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 代码生成类型枚举
 */
@Getter
public enum CodeGenEnum {

    SINGLETON_HTML("singleton", "单文件"),
    MULTIFILE_HTML("multifile", "多文件");

    /**
     * 代码生成模式
     */
    private final String codeGenMode;

    /**
     * 描述
     */
    private final String description;

    CodeGenEnum(String codeGenMode, String description) {
        this.codeGenMode = codeGenMode;
        this.description = description;
    }

    public static CodeGenEnum getCodeGenEnum(String codeGenMode) {
        if (ObjectUtil.isEmpty(codeGenMode)) return null;

        for (CodeGenEnum codeGenEnum : CodeGenEnum.values()) {
            if (codeGenMode.equals(codeGenEnum.getCodeGenMode())) {
                return codeGenEnum;
            }
        }

        return null;
    }
}
