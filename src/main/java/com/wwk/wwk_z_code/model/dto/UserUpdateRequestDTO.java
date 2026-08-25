package com.wwk.wwk_z_code.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserUpdateRequestDTO implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @Length(min = 6, max = 32, message = "用户账号长度不合规")
    private String userAccount;

    /**
     * 密码
     */
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
    @Length(max = 1024, message = "用户头像过长")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Length(max = 512, message = "用户简介过长")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Length(max = 256, message = "用户角色过长")
    private String userRole;
}
