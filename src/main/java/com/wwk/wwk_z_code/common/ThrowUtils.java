package com.wwk.wwk_z_code.common;

import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;

public class ThrowUtils {
    public static void throwIf(boolean condition, ErrorCode errorCode, String message){
        if(condition){
            throw new BusinessException(errorCode, message);
        }
    }
}
