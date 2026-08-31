package com.wwk.wwk_z_code.model.dto;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wwk.wwk_z_code.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

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
     * 应用标签（用于按标签过滤，空值/空串全量返回，非法串返回空集）
     */
    @Schema(description = "应用标签：tool/webPage/profile")
    private String appTag;

    /**
     * 用户/游客侧可排序字段白名单（App 实体字段）
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "appName", "createTime");

    /**
     * 排序字段（用户分页仅支持 id/appName/createTime，未传不排序）
     */
    private String sortField = "";

    /**
     * 校验排序字段：仅允许为空或在管理员可排序白名单内
     * @return 排序字段是否合法
     */
    @JsonIgnore
    @AssertTrue(message = "排序字段不合法")
    public boolean isSortFieldValid() {
        return StrUtil.isBlank(getSortField()) || SORTABLE_FIELDS.contains(getSortField());
    }
}
