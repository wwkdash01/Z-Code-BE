package com.wwk.wwk_z_code.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {
    /**
     * 删除实体id
     */
    private Long id;

    /**
     * 序列化版本控制
     * 检查类型是否改变，变了UID不一样报错
     */
    @Serial
    private static final long serialVersionUID = 1L;
}
