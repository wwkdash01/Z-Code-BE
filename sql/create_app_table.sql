create database if not exists wwk_z_code;

use wwk_z_code;

create table if not exists app
(
    id              bigint                                 not null comment 'id' primary key,

    appName         varchar(256)                           not null comment '应用名称',
    cover           varchar(512)                           not null comment '应用封面url',
    initPrompt      text                                   null     comment '应用初始化提示词',
    codeGenType     varchar(64)                            null     comment '应用生成类型（枚举：单文件/多文件）',

    priority        int          default 0                 not null comment '应用展示优先级',

    deployKey       varchar(64)                            null     comment '应用部署密钥',
    deployDir       varchar(256)                           null     comment '应用部署目录',
    deployTime      datetime                               null     comment '应用部署时间',

    createUserId    bigint                                 not null comment '创建用户id',

    editTime        datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null comment '更新时间' on update CURRENT_TIMESTAMP ,
    isDelete        tinyint      default 0                 not null comment '是否删除',

    UNIQUE KEY uk_deployKey (deployKey),
    INDEX idx_appName (appName),
    INDEX idx_createUserId (createUserId)
    ) comment '应用' collate = utf8mb4_unicode_ci;