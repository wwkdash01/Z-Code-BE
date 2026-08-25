create database if not exists wwk_z_code;

use wwk_z_code;

create table if not exists user
(
    id              bigint                                 not null comment 'id' primary key,

    userAccount     varchar(256)                           not null comment '账号',
    userPassword    varchar(512)                           not null comment '密码',
    userName        varchar(256)                           null     comment '用户昵称',
    userAvatar      varchar(1024)                          null     comment '用户头像',
    userProfile     varchar(512)                           null     comment '用户简介',
    userRole        varchar(256) default 'user'            not null comment '用户角色：user/admin',

    vipExpireTime   datetime                               null     comment '会员过期时间',
    vipCode         varchar(128)                           null     comment '会员兑换码(付费开通为0)',
    vipId           bigint                                 null     comment '会员id',

    shareCode       varchar(20)  default                   null     comment '分享码',
    inviteUser      bigint       default                   null     comment '邀请用户id',

    editTime        datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null comment '更新时间' on update CURRENT_TIMESTAMP ,
    isDelete        tinyint      default 0                 not null comment '是否删除',

    UNIQUE KEY uk_vipId (vipId),
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;