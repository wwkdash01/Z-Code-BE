package com.wwk.wwk_z_code.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 聊天记录游标分页视图对象
 *
 * @author wangwenkai
 * @since 2026-09-10
 */
@Data
public class ChatHistoryUserCursorPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页消息列表
     */
    @Schema(description = "当前页消息列表")
    private List<ChatHistoryVO> records;

    /**
     * 是否有更多历史数据
     */
    @Schema(description = "是否有更多数据")
    private Boolean hasMore;

    /**
     * 下一页使用的游标值
     */
    @Schema(description = "下一页游标")
    private String nextCursor;
}
