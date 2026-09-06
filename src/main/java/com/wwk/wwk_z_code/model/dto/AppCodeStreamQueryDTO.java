package com.wwk.wwk_z_code.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppCodeStreamQueryDTO implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用id
     */
    @NotNull
    @Min(value = 1L, message = "应用id不能小于1")
    @Schema(type = "string", description = "应用主键ID")
    private Long appId;

    /**
     * 用户提示词
     */
    @NotNull
    @NotBlank
    private String userPrompt;
}
