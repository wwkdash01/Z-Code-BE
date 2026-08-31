package com.wwk.wwk_z_code.controller;

import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.model.dto.*;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.vo.AppVO;
import com.wwk.wwk_z_code.service.AppService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 *  控制层。
 *
 * @author wwk
 */
@RestController
@RequestMapping("/apps")
@RequiredArgsConstructor
@Validated
public class AppController {

    private final AppService appService;

    /**
     * 分页查询精选应用(GUEST，游客公开)
     *
     * @param appQueryRequestDTO 应用分页查询DTO（query 参数自动绑定）
     * @return 应用视图分页对象
     */
    @GetMapping("/guest/page/featured")
    public Page<AppVO> getFeaturedAppByPage(
            @ModelAttribute
            @Valid
            @ParameterObject
            AppQueryRequestDTO appQueryRequestDTO) {
        return appService.getFeaturedAppByPage(appQueryRequestDTO);
    }

    /**
     * 新增应用(USER，只建实体不调AI)
     *
     * @param appAddRequestDTO 应用新增DTO
     * @param request Http请求
     * @return 新创建应用的主键 id
     */
    @PostMapping("/user")
    public Long saveApp(
            @RequestBody
            @Valid
            AppAddRequestDTO appAddRequestDTO,
            HttpServletRequest request) {
        return appService.saveApp(appAddRequestDTO, request);
    }

    /**
     * 根据主键获取应用详情(USER，脱敏VO)
     *
     * @param id 主键
     * @param request Http请求
     * @return 应用视图对象
     */
    @GetMapping("/user/{id}")
    public AppVO getAppById(
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id,
            HttpServletRequest request) {
        return appService.getAppById(id, request);
    }

    /**
     * 根据主键更新应用名称(USER，仅允许改 appName)
     *
     * @param appUpdateRequestDTO 应用更新DTO
     * @param id 主键
     * @param request Http请求
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/user/{id}")
    public Boolean updateAppById(
            @RequestBody
            @Valid
            AppUpdateRequestDTO appUpdateRequestDTO,

            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id,
            HttpServletRequest request) {
        return appService.updateAppById(appUpdateRequestDTO, id, request);
    }

    /**
     * 根据主键删除应用(USER)
     *
     * @param id 主键
     * @param request Http请求
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/user/{id}")
    public Boolean removeAppById(
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id,
            HttpServletRequest request) {
        return appService.removeAppById(id, request);
    }

    /**
     * 分页查询我的应用(USER)
     *
     * @param appQueryRequestDTO 应用分页查询DTO（query 参数自动绑定）
     * @param request Http请求
     * @return 应用视图分页对象
     */
    @GetMapping("/user/page/my-apps")
    public Page<AppVO> getMyAppByPage(
            @ModelAttribute
            @Valid
            @ParameterObject
            AppQueryRequestDTO appQueryRequestDTO,
            HttpServletRequest request) {
        return appService.getMyAppByPage(appQueryRequestDTO, request);
    }

    /**
     * 获取代码生成输出流(USER)
     *
     * @param appCodeStreamQueryDTO 代码输出流请求DTO
     * @param request Http请求
     * @return Flux输出流
     */
    @GetMapping(value = "/user/code-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getCodeGenStream(
            @Valid
            AppCodeStreamQueryDTO appCodeStreamQueryDTO,
            HttpServletRequest request) {

        Flux<String> codeStream = appService.getCodeGenStream(appCodeStreamQueryDTO, request);
        return codeStream
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonWrapper = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonWrapper)
                            .build();
                })
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                            .event("done")
                            .data("")
                            .build()
                ));
    }

    /**
     * 部署应用(USER)
     *
     * @param appDeployRequestDTO 部署应用请求DTO
     * @return 网页访问站点URL
     */
    @PostMapping("/user/deployment")
    public String deployApp(
            @RequestBody
            @Valid
            AppDeployRequestDTO appDeployRequestDTO,
            HttpServletRequest request) {
        return appService.deployApp(appDeployRequestDTO, request);
    }

    @GetMapping("/user/preview/{appId}")
    public String previewApp(
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long appId,
            HttpServletRequest request) {
        return appService.previewApp(appId, request);
    }

    /**
     * 根据主键获取应用信息(ADMIN，不脱敏返回PO)
     *
     * @param id 主键
     * @return 应用实体
     */
    @GetMapping("/admin/{id}")
    public App getAppByAdmin(
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id) {
        return appService.getAppByAdmin(id);
    }

    /**
     * 根据主键更新应用信息(ADMIN，可改 appName/cover/priority)
     *
     * @param appAdminUpdateRequestDTO 应用管理更新DTO
     * @param id 主键
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/admin/{id}")
    public Boolean updateAppByAdmin(
            @RequestBody
            @Valid
            AppAdminUpdateRequestDTO appAdminUpdateRequestDTO,

            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id) {
        return appService.updateAppByAdmin(appAdminUpdateRequestDTO, id);
    }

    /**
     * 根据主键删除应用(ADMIN)
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/admin/{id}")
    public Boolean removeAppByAdmin(
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id) {
        return appService.removeAppByAdmin(id);
    }

    /**
     * 分页查询应用信息(ADMIN，全条件，除审计字段)
     *
     * @param appAdminQueryRequestDTO 应用管理分页查询DTO（query 参数自动绑定）
     * @return 应用分页对象
     */
    @GetMapping("/admin/page")
    public Page<App> getAppByAdminPage(
            @ModelAttribute
            @Valid
            @ParameterObject
            AppAdminQueryRequestDTO appAdminQueryRequestDTO) {
        return appService.getAppByAdminPage(appAdminQueryRequestDTO);
    }
}
