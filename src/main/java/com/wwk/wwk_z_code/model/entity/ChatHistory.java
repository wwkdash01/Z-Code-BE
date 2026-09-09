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
     * 消息类型(user/ai)
     */
    @Column("messageType")
    @Schema(type = "string", description = "消息类型：user=用户消息，ai=AI回复")
    private MessageType messageType;

    /**
     * 编辑时间
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
    @Column("isDelete")
    private Integer isDelete;

}
