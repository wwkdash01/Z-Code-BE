package com.wwk.wwk_z_code.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流式代码生成回调结果载体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamCallbackResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 代码保存目录绝对路径
     */
    @Schema(description = "代码保存目录绝对路径")
    private String saveDirPath;

    /**
     * 完整的 AI 回复内容
     */
    @Schema(description = "完整的 AI 回复内容")
    private String aiResponse;
}
