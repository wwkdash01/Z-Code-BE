package com.wwk.wwk_z_code.model.dto;

import com.wwk.wwk_z_code.model.enums.TagEnum;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppAdminUpdateRequestDTO implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用名称
     */
    @Length(max = 256, message = "应用名称过长")
    private String appName;

    /**
     * 应用封面url
     */
    @Length(max = 512, message = "应用封面url过长")
    private String cover;

    /**
     * 应用展示优先级
     */
    @Min(value = 0, message = "优先级不能小于0")
    private Integer priority;

    /**
     * 应用标签
     */
    private TagEnum appTag;
}
