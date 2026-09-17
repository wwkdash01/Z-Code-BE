package com.wwk.wwk_z_code.model.dto;

import com.wwk.wwk_z_code.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天记录游标查询请求DTO（USER级，IM无限屏场景）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryUserCursorQueryRequestDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @Schema(type = "string", description = "关联应用ID")
    private Long appId;

    /**
     * 消息类型过滤（按 code 值："user"/"ai"/"error"/"retraction"）
     */
    @Length(max = 64, message = "消息类型不合法")
    @Schema(description = "消息类型过滤：user=用户消息，ai=AI回复，error=错误消息，retraction=撤销的消息")
    private String messageType;

    /**
     * 翻页游标（Base64 JSON {"createTime":"...","id":123}），首次传 null/空串不加载更早数据
     */
    @Schema(description = "翻页游标，首次加载不传")
    private String cursor;
}
