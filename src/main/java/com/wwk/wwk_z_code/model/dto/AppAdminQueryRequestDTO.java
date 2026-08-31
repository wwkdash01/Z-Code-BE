package com.wwk.wwk_z_code.model.dto;

import cn.hutool.core.util.StrUtil;
import com.wwk.wwk_z_code.common.PageRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppAdminQueryRequestDTO extends PageRequest implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Min(value = 1L, message = "应用id不合法")
    private Long id;

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
     * 应用初始化提示词
     */
    private String initPrompt;

    /**
     * 应用生成类型（单文件/多文件，查询用 codeGenMode 字符串）
     */
    @Length(max = 64, message = "应用生成类型不合法")
    private String codeGenType;

    /**
     * 应用展示优先级
     */
    @Min(value = 0, message = "优先级不能小于0")
    private Integer priority;

    /**
     * 应用标签（用于按标签过滤，空值全量返回）
     */
    private String appTag;

    /**
     * 应用部署密钥
     */
    @Length(max = 64, message = "应用部署密钥过长")
    private String deployKey;

    /**
     * 应用部署时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deployTime;

    /**
     * 管理员可排序字段白名单（App 实体非审计字段）
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "appName", "cover", "initPrompt", "codeGenType", "appTag", "priority", "deployKey", "deployTime");

    /**
     * 校验排序字段：仅允许为空或在管理员可排序白名单内
     * @return 排序字段是否合法
     */
    @AssertTrue(message = "排序字段不合法")
    public boolean isSortFieldValid() {
        return StrUtil.isBlank(getSortField()) || SORTABLE_FIELDS.contains(getSortField());
    }
}
