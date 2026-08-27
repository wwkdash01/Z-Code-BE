package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.wwk.wwk_z_code.model.dto.*;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

/**
 *  服务层。
 *
 * @author wwk
 */
public interface AppService extends IService<App> {

    /**
     * 新增应用（USER，只建实体，不调AI）
     * @param appAddRequestDTO 应用新增DTO
     * @param request HTTP请求
     * @return 新创建应用的主键 id
     */
    Long saveApp(AppAddRequestDTO appAddRequestDTO, HttpServletRequest request);

    /**
     * 根据主键获取应用详情（USER，脱敏VO，校验归属）
     * @param id 主键
     * @param request HTTP请求
     * @return 应用视图对象
     */
    AppVO getAppById(Long id, HttpServletRequest request);

    /**
     * 根据主键更新应用名称（USER，校验归属）
     * @param appUpdateRequestDTO 应用更新DTO
     * @param id 主键
     * @param request HTTP请求
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    Boolean updateAppById(AppUpdateRequestDTO appUpdateRequestDTO, Long id, HttpServletRequest request);

    /**
     * 根据主键删除应用（USER，校验归属）
     * @param id 主键
     * @param request HTTP请求
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    Boolean removeAppById(Long id, HttpServletRequest request);

    /**
     * 分页查询我的应用（USER，脱敏VO）
     * @param appQueryRequestDTO 应用分页查询DTO
     * @param request HTTP请求
     * @return 应用视图分页对象
     */
    Page<AppVO> getMyAppByPage(AppQueryRequestDTO appQueryRequestDTO, HttpServletRequest request);

    /**
     * 分页查询精选应用（GUEST，游客公开，脱敏VO）
     * @param appQueryRequestDTO 应用分页查询DTO
     * @return 应用视图分页对象
     */
    Page<AppVO> getFeaturedAppByPage(AppQueryRequestDTO appQueryRequestDTO);

    /**
     * 根据主键获取应用信息（ADMIN，不脱敏返回PO）
     * @param id 主键
     * @return 应用实体
     */
    App getAppByAdmin(Long id);

    /**
     * 根据主键更新应用信息（ADMIN，未传入字段不更新）
     * @param appAdminUpdateRequestDTO 应用管理更新DTO
     * @param id 主键
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    Boolean updateAppByAdmin(AppAdminUpdateRequestDTO appAdminUpdateRequestDTO, Long id);

    /**
     * 根据主键删除应用（ADMIN）
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    Boolean removeAppByAdmin(Long id);

    /**
     * 分页查询应用信息（ADMIN，全条件，不脱敏）
     * @param appAdminQueryRequestDTO 应用管理分页查询DTO
     * @return 应用分页对象
     */
    Page<App> getAppByAdminPage(AppAdminQueryRequestDTO appAdminQueryRequestDTO);


    public Flux<String> getCodeGenStream(AppCodeStreamQueryDTO appCodeStreamQueryDTO, HttpServletRequest request);
}
