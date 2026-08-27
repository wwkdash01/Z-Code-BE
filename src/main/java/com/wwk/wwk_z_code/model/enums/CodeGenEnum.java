package com.wwk.wwk_z_code.model.enums;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 代码生成类型枚举
 * <p>持久化与 JSON 序列化均使用 {@link #getCodeGenMode()} 值（如 "singleton" / "multifile"）。</p>
 */
@Getter
public enum CodeGenEnum {

    SINGLETON_HTML("singleton", "单文件"),
    MULTIFILE_HTML("multifile", "多文件");

    /**
     * 代码生成模式（数据库存储值 / JSON 序列化值）
     */
    @EnumValue
    @JsonValue
    private final String codeGenMode;

    /**
     * 描述
     */
    private final String description;

    CodeGenEnum(String codeGenMode, String description) {
        this.codeGenMode = codeGenMode;
        this.description = description;
    }

    /**
     * 外部接口 根据代码生成模式字符串获取枚举，为空或未匹配返回 null
     *
     * @param codeGenMode 代码生成模式
     * @return 匹配的枚举
     */
    @JsonCreator
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
