package com.wwk.wwk_z_code.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.wwk.wwk_z_code.annotation.Sortable;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  实体类。
 *
 * @author wwk
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app")
public class App implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Sortable
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 应用名称
     */
    @Sortable
    @Column("appName")
    private String appName;

    /**
     * 应用封面url
     */
    @Sortable
    @Column("cover")
    private String cover;

    /**
     * 应用初始化提示词
     */
    @Sortable
    @Column("initPrompt")
    private String initPrompt;

    /**
     * 应用生成类型（枚举：单文件/多文件，数据库保存 codeGenMode）
     */
    @Sortable
    @Column("codeGenType")
    private CodeGenEnum codeGenType;

    /**
     * 应用代码生成位置
     */
    @Column("codeGenDir")
    private String codeGenDir;

    /**
     * 应用展示优先级
     */
    @Sortable
    @Column("priority")
    private Integer priority;

    /**
     * 应用部署密钥
     */
    @Sortable
    @Column("deployKey")
    private String deployKey = null;

    /**
     * 应用部署目录
     */
    @Sortable
    @Column("deployDir")
    private String deployDir;

    /**
     * 应用部署时间
     */
    @Sortable
    @Column("deployTime")
    private LocalDateTime deployTime = null;

    /**
     * 创建用户id
     */
    @Column("createUserId")
    private Long createUserId;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
