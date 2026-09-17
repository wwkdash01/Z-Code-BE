create database if not exists wwk_z_code;

use wwk_z_code;

create table if not exists chat_history
(
    id              bigint                                 not null comment 'id' primary key,

    appId           bigint                                 not null comment '应用id',
    userId          bigint                                 not null comment '用户id',
    message         text                                   not null comment '消息',
    messageType     varchar(16)                            not null comment '消息类型(user/ai/error/retraction)',

    editTime        datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null comment '更新时间' on update CURRENT_TIMESTAMP ,
    isDelete        tinyint      default 0                 not null comment '是否删除',

    INDEX idx_appId (appId),                                                
    INDEX idx_creteTime (createTime),
    INDEX idx_appId_createTime (appId, createTime)
    ) comment '对话历史' collate = utf8mb4_unicode_ci;