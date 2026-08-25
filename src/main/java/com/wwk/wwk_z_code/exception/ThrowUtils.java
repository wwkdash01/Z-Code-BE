package com.wwk.wwk_z_code.exception;

public class ThrowUtils {

    /**
     * 条件抛自定义通用异常
     * @param condition 异常条件
     * @param runtimeException 自定义异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件抛标准业务异常
     * @param condition 异常条件
     * @param errorCode 标准业务异常状态码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 条件抛自定义业务异常
     * @param condition 异常条件
     * @param errorCode 标准业务异常状态码
     * @param message 自定义异常信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BusinessException(errorCode, message);
        }
    }
}
