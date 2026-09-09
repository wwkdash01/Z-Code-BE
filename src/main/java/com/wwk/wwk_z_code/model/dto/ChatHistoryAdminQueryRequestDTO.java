package com.wwk.wwk_z_code.model.dto;

import cn.hutool.core.util.StrUtil;
import com.wwk.wwk_z_code.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 聊天记录管理分页查询DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryAdminQueryRequestDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @Schema(type = "string", description = "记录主键ID")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(type = "string", description = "关联应用ID")
    private Long appId;

    /**
     * 用户ID
     */
    @Schema(type = "string", description = "关联用户ID")
    private Long userId;

    /**
     * 消息类型查询（按 code 值："user"/"ai"）
     */
    @Length(max = 64, message = "消息类型不合法")
    @Schema(type = "string", description = "消息类型过滤：user/ai")
    private String messageType;

    /**
     * 排序字段白名单（仅支持 id / messageType / createTime）
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "messageType", "createTime");

    /**
     * 校验排序字段：仅允许为空或在管理员可排序白名单内
     * @return 排序字段是否合法
     */
    @AssertTrue(message = "排序字段不合法")
    public boolean isSortFieldValid() {
        return StrUtil.isBlank(getSortField()) || SORTABLE_FIELDS.contains(getSortField());
    }
}
