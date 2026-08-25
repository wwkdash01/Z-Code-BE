package com.wwk.wwk_z_code.annotation;

import com.wwk.wwk_z_code.model.enums.UserRoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    /**
     * 方法调用的权限要求
     */
    UserRoleEnum roleRequirement() default UserRoleEnum.GUEST;
}
