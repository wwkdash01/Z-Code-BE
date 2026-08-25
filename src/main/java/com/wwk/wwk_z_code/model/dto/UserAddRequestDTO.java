package com.wwk.wwk_z_code.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserAddRequestDTO implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @NotNull(message = "用户账号为空")
    @Length(min = 6, max = 32, message = "用户账号长度不合规")
    private String userAccount;

    /**
     * 密码
     */
    @NotNull(message = "用户密码为空")
    @Length(min = 6, max = 24, message = "用户密码长度不合规")
    private String userPassword;

    /**
     * 用户昵称
     */
    @Length(max = 256, message = "用户昵称过长")
    private String userName;

    /**
     * 用户头像
     */
    @Length(max = 1024, message = "用户头像格式不合规")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Length(max = 512, message = "用户简介过长")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Length(max = 256, message = "无效的用户角色")
    private String userRole;

    /**
     * 会员过期时间
     */
    private LocalDateTime vipExpireTime;

    /**
     * 会员兑换码(付费开通为0)
     */
    @Length(max = 128, message = "会员兑换码过长")
    private String vipCode;

    /**
     * 会员id
     */
    @Min(value = 1L, message = "会员id不合法")
    private Long vipId;

    /**
     * 分享码
     */
    @Length(max = 20, message = "分享码过长")
    private String shareCode;

    /**
     * 邀请用户id
     */
    @Min(value = 1L, message = "邀请用户id不合法")
    private Long inviteUser;
}
