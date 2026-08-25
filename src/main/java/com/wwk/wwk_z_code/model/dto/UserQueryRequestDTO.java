package com.wwk.wwk_z_code.model.dto;

import com.wwk.wwk_z_code.common.PageRequest;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequestDTO extends PageRequest implements Serializable {
    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Min(value = 1L, message = "用户id不合法")
    private Long id;

    /**
     * 账号
     */
    @Length(max = 256, message = "用户账号过长")
    private String userAccount;

    /**
     * 用户角色：user/admin
     */
    @Length(max = 256, message = "用户角色过长")
    private String userRole;

    /**
     * 用户昵称
     */
    @Length(max = 256, message = "用户昵称过长")
    private String userName;

    /**
     * 会员id
     */
    @Min(value = 1L, message = "会员id不合法")
    private Long vipId;
}
