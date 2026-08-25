package com.wwk.wwk_z_code.common;

import com.wwk.wwk_z_code.exception.ErrorCode;

public class ResponseUtils {
    /**
     * 返回成功响应类
     * @param data 响应数据
     * @return 响应类
     * @param <T> 响应数据类型
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 返回标准异常响应类
     * @param errorCode 标准状态码
     * @return 异常响应类
     */
    public static BaseResponse<?> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), null);

    }

    /**
     * 返回自定义异常信息响应类
     * @param errorCode 标准异常码
     * @param message 自定义异常信息
     * @return 异常响应类
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), message, null);
    }

    /**
     * 返回自定义异常响应类
     * @param code 自定义异常码
     * @param message 自定义异常信息
     * @return 异常响应类
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }
}
