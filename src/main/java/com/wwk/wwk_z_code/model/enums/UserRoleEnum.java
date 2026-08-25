package com.wwk.wwk_z_code.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum UserRoleEnum {
    GUEST("guest", "游客", 0),
    USER("user", "用户", 1),
    ADMIN("admin", "管理员", 2),;

    private final String role;
    private final String description;
    private final Integer level;

    UserRoleEnum(String role, String description, Integer level) {
        this.role = role;
        this.description = description;
        this.level = level;
    }

    /**
     * 根据角色名获取对应枚举类单例
     * @param role 角色名
     * @return 角色枚举类单例
     */
    public static UserRoleEnum getEnumByRole(String role) {
        if (ObjectUtil.isEmpty(role)) {
            return null;
        }

        for (UserRoleEnum e : UserRoleEnum.values()) {
            if (e.role.equals(role)) {
                return e;
            }
        }

        return null;
    }
}
