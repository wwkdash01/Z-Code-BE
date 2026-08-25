package com.wwk.wwk_z_code.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图对象，返回给前端的用户信息，不包含敏感字段（密码、会员兑换码等）。
 *
 * @author wwk
 */
@Data
public class UserVO implements Serializable {

    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 会员过期时间
     */
    private LocalDateTime vipExpireTime;

    /**
     * 会员id
     */
    private Long vipId;

    /**
     * 分享码
     */
    private String shareCode;

    /**
     * 邀请用户id
     */
    private Long inviteUser;

    /**
     * 编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
