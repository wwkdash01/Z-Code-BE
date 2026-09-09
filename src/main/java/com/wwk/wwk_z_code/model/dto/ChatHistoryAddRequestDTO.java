package com.wwk.wwk_z_code.model.dto;

import com.wwk.wwk_z_code.model.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天记录新增请求DTO
 */
@Data
public class ChatHistoryAddRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    @Schema(type = "string", description = "关联应用ID")
    private Long appId;

    /**
     * 消息内容
     */
    @NotNull(message = "消息内容不能为空")
    @Length(max = 8192, message = "消息内容过长")
    @Schema(type = "string", description = "消息内容")
    private String message;

    /**
     * 消息类型（JSON传入 code 值："user"/"ai"）
     */
    @NotNull(message = "消息类型不能为空")
    @Schema(type = "string", description = "消息类型：user=用户消息，ai=AI回复")
    private MessageType messageType;
}
