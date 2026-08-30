package com.wwk.wwk_z_code.model.enums;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 应用标签枚举类
 */
@Getter
public enum TagEnum {
    TOOL("tool", "工具类网页"),
    WEB_PAGE("webPage", "普通网页"),
    PROFILE("profile", "个人主页");

    /**
     * 应用标签
     */
    @EnumValue
    @JsonValue
    private final String tag;

    /**
     * 标签描述
     */
    private final String description;

    TagEnum(String tag, String description) {
        this.tag = tag;
        this.description = description;
    }

    /**
     * 外部接口 根据tag返回枚举类
     *
     * @param tag 应用标签
     * @return 枚举类
     */
    @JsonCreator
    public static TagEnum getTagEnumByTag(String tag) {
        if (ObjectUtil.isEmpty(tag)) return null;

        for (TagEnum tagEnum : TagEnum.values()) {
            if (tagEnum.getTag().equals(tag)) {
                return tagEnum;
            }
        }

        return null;
    }
}
