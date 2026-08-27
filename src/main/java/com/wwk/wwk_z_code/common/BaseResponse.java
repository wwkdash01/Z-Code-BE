package com.wwk.wwk_z_code.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    /**
     * 构造自定义响应结果
     * @param code 标准状态码
     * @param message 自定义状态信息
     * @param data 响应数据
     */
    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造无信息响应结果
     * @param code 标准状态码
     * @param data 响应数据
     */
    public BaseResponse(int code, T data) {
        this.code = code;
        this.message = "";
        this.data = data;
    }

}
