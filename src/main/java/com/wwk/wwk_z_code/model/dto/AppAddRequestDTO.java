package com.wwk.wwk_z_code.model.dto;

import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import com.wwk.wwk_z_code.model.enums.TagEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppAddRequestDTO implements Serializable {
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

    /**
     * 应用封面url（非必填，为空时服务端置 "" 落库）
     */
    @Length(max = 512, message = "应用封面url过长")
    private String cover;

    /**
     * 应用初始化提示词
     */
    @NotNull(message = "应用初始化提示词为空")
    private String initPrompt;

    /**
     * 应用生成类型（单文件/多文件，JSON 传 codeGenMode 值）
     */
    @NotNull(message = "应用生成类型为空")
    private CodeGenEnum codeGenType;

    /**
     * 应用标签
     */
    private TagEnum appTag;
}
