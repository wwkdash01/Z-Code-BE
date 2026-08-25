package com.wwk.wwk_z_code.constant;

import com.wwk.wwk_z_code.model.enums.UserRoleEnum;

public interface UserConstant {
    /**
     * 用户登录状态键
     */
    String USER_LOGIN_STATUS = "user_login";

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = UserRoleEnum.USER.getRole();

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = UserRoleEnum.ADMIN.getRole();
}
