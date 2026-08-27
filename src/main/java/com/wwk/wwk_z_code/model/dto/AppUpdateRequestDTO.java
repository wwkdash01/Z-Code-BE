package com.wwk.wwk_z_code.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppUpdateRequestDTO implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用名称
     */
    @NotNull(message = "应用名称为空")
    @Length(max = 256, message = "应用名称过长")
    private String appName;
}
