package com.wwk.wwk_z_code.model.vo;

import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用视图对象，返回给用户的脱敏应用信息，不包含优先级、部署密钥与审计字段。
 *
 * @author wwk
 */
@Data
public class AppVO implements Serializable {

    /**
     * 序列化id
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面url
     */
    private String cover;

    /**
     * 应用初始化提示词
     */
    private String initPrompt;

    /**
     * 应用生成类型（单文件/多文件）
     */
    private CodeGenEnum codeGenType;
}
