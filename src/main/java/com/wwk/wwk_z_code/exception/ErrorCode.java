package com.wwk.wwk_z_code.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(40000, "非法请求参数"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统错误"),
    CODE_GENERATE_ERROR(50001, "代码生成失败");


    /**
    * 状态码
    */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
