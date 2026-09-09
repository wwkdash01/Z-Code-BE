package com.wwk.wwk_z_code.model.enums;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 消息类型枚举
 * <p>持久化与 JSON 序列化均使用 {@link #getCode()} 值（"user"/"ai"）。</p>
 */
@Getter
public enum MessageType {

    USER("user", "用户消息"),
    AI("ai", "AI回复");

    /**
     * 消息类型代码（数据库存储值 / JSON 序列化值）
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 描述
     */
    private final String description;

    MessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据消息类型代码获取枚举
     *
     * @param code 消息类型代码
     * @return 匹配的枚举，未匹配返回 null
     */
    @JsonCreator
    public static MessageType getMessageType(String code) {
        if (ObjectUtil.isEmpty(code)) return null;

        for (MessageType messageType : MessageType.values()) {
            if (code.equals(messageType.getCode())) {
                return messageType;
            }
        }

        return null;
    }
}
