package com.wwk.wwk_z_code.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserRegisterRequestDTO implements Serializable {

    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户账号
     */
    @NotNull(message = "用户账号为空")
    @Length(min = 6, max = 32, message = "用户账号长度不合规")
    private String userAccount;

    /**
     * 用户密码
     */
    @NotNull(message = "用户密码为空")
    @Length(min = 6, max = 24, message = "用户密码长度不合规")
    private String password;

    /**
     * 确认密码
     */
    @NotNull(message = "确认密码为空")
    @Length(min = 6, max = 24, message = "确认密码长度不合规")
    private String confirmPassword;
}
