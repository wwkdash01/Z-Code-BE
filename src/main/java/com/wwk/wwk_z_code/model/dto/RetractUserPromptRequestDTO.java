package com.wwk.wwk_z_code.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 撤销用户提示词请求DTO
 */
@Data
public class RetractUserPromptRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    @Min(value = 1L, message = "应用ID不能小于1")
    @Schema(type = "string", description = "关联应用ID")
    private Long appId;

    /**
     * 目标消息ID（不传则按最新一轮失败对话定位待撤销的用户提示词）
     */
    @Min(value = 1L, message = "消息id不能小于1")
    @Schema(type = "string", description = "目标用户提示词的消息ID，不传则撤销最新一轮失败对话")
    private Long chatHistoryId;
}
