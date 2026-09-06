package com.wwk.wwk_z_code.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用部署请求 DTO
 *
 * @author wwk
 */
@Data
public class AppDeployRequestDTO implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @NotNull
    @Min(value = 1L, message = "应用ID不能小于1")
    @Schema(type = "string", description = "应用主键ID")
    private Long appId;
}
