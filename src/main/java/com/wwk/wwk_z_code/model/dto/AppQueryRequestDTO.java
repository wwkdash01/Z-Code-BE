package com.wwk.wwk_z_code.model.dto;

import cn.hutool.core.util.StrUtil;
import com.wwk.wwk_z_code.common.PageRequest;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequestDTO extends PageRequest implements Serializable {
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
     * 排序字段（用户分页仅支持 appName 排序，覆盖父类默认 id，未传不排序）
     */
    private String sortField = "";

    /**
     * 校验排序字段：仅允许为空或 appName（controller 层 DTO 绑定限制用户可用排序字段）
     * @return 排序字段是否合法
     */
    @AssertTrue(message = "排序字段不合法")
    public boolean isSortFieldValid() {
        return StrUtil.isBlank(getSortField()) || "appName".equals(getSortField());
    }
}
