package com.wwk.wwk_z_code.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {
    /**
     * 当前页号
     */
    @Min(value = 1, message = "分页数不能小于1")
    private Integer pageNum = 1;

    /**
     * 页面大小
     */
    @Min(value = 1, message = "页面大小不能小于1")
    @Max(value = 30, message = "页面大小不能大于30")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField = "id";

    /**
     * 排序顺序(默认降序)
     */
    private String sortOrder = "descend";
}
