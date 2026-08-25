package com.wwk.wwk_z_code.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;

    /**
     * 全参构造
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 半参构造
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 错误码构造
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
