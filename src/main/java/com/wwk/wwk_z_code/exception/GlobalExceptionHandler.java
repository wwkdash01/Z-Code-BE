package com.wwk.wwk_z_code.exception;

import com.wwk.wwk_z_code.common.BaseResponse;
import com.wwk.wwk_z_code.common.ResponseUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.error("业务异常", e);
        return ResponseUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数不合规", e);
        return ResponseUtils.error(ErrorCode.PARAM_ERROR, "参数不合规");
    }

    @ExceptionHandler(BindException.class)
    public BaseResponse<?> handleBindException(BindException e) {
        log.error("参数绑定失败", e);
        return ResponseUtils.error(ErrorCode.PARAM_ERROR, "参数绑定失败");
    }

    @ExceptionHandler(value = Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        log.error("未知异常", e);
        return ResponseUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), "未知异常");
    }


}
