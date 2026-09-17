package com.wwk.wwk_z_code.model.entity;

import com.mybatisflex.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import com.wwk.wwk_z_code.model.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  实体类。
 *
 * @author wangwenkai
 * @since 2026-09-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_history")
public class ChatHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(type = "string", description = "会话记录主键ID")
    private Long id;

    /**
     * 应用id
     */
    @Column("appId")
    @Schema(type = "string", description = "关联应用ID")
    private Long appId;

    /**
     * 用户id
     */
    @Column("userId")
    @Schema(type = "string", description = "关联用户ID")
    private Long userId;

    /**
     * 消息
     */
    private String message;

    /**
     * 消息类型(user/ai/error/retraction)
     * <p>retraction 不是新增的行：撤销时把原 user 行的 messageType 就地改写，id 与 message 原文保留，
     * 前端据此定位并丢弃被撤销的那一轮对话。</p>
     */
    @Column("messageType")
    @Schema(type = "string", description = "消息类型：user=用户消息，ai=AI回复，error=错误消息，retraction=撤销的消息")
    private MessageType messageType;

    /**
     * 编辑时间（撤销改写 messageType 时刷新）
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
