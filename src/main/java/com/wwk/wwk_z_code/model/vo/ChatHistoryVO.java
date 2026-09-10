package com.wwk.wwk_z_code.model.vo;

import com.wwk.wwk_z_code.model.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天记录视图对象，仅暴露用户接口所需字段。
 *
 * @author wangwenkai
 * @since 2026-09-10
 */
@Data
public class ChatHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联应用ID
     */
    @Schema(type = "string", description = "关联应用ID")
    private Long appId;

    /**
     * 关联用户ID
     */
    @Schema(type = "string", description = "关联用户ID")
    private Long userId;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String message;

    /**
     * 消息类型（user/ai）
     */
    @Schema(type = "string", description = "消息类型：user=用户消息，ai=AI回复")
    private MessageType messageType;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
