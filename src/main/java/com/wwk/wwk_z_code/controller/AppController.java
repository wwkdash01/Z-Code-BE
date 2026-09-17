package com.wwk.wwk_z_code.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.model.dto.*;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.vo.AppVO;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.service.AppService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 *  控制层。
 *
 * @author wwk
 */
@Slf4j
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
     * 根据主键获取精选应用详情(GUEST，游客公开，脱敏VO)
     *
     * @param id 主键
     * @return 应用视图对象
     */
    @GetMapping("/guest/{id}")
    public AppVO getFeaturedAppById(
            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id) {
        return appService.getFeaturedAppById(id);
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
            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
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

            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id,
            HttpServletRequest request) {
        return appService.updateAppById(appUpdateRequestDTO, id, request);
    }

    /**
     * 根据主键删除应用(USER)
     * 副作用：级联逻辑删除该应用下所有聊天记录
     *
     * @param id 主键
     * @param request Http请求
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/user/{id}")
    public Boolean removeAppById(
            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
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
     * <p>建立流之前的失败（未登录/非本人应用/参数不合规）仍由全局异常处理器返回 JSON，
     * 流建立之后的失败才用 SSE 的 error 事件表达。</p>
     * <p>帧协议：数据帧 {@code event:message}（data 为 {"d":"分片"}）、结束帧 {@code event:done}、
     * 错误帧 {@code event:error}（data 为 {"code":50001,"message":".."}），三帧均显式带事件名，
     * 前端按事件名分流即可（不要再依赖 SSE 默认事件名）。</p>
     * <p>该接口保留在 OpenAPI 文档中（帧协议见下方 @Operation 的 description），但前端不参与
     * openapi2ts 生成：流式响应无法用 schema 表达，由生成前的 afterOpenApiDataInited hook 排除该 path。</p>
     *
     * @param appCodeStreamQueryDTO 代码输出流请求DTO
     * @param request Http请求
     * @return Flux输出流
     */
    @Operation(summary = "获取代码生成输出流", description = """
            SSE 流式输出，帧协议如下（三帧均显式带事件名，前端按事件名分流）：
            - 数据帧：`event:message`，data 为 `{"d":"分片"}`，分片逐块追加
            - 结束帧：`event:done`，data 为空，只有成功才发
            - 错误帧：`event:error`，data 为 `{"code":50001,"message":"..."}`
            流建立之前的失败（未登录/无权限/参数不合规）不进入流，返回 JSON 包装体。
            `retry=true` 时后端不落库本次用户提示词（前端失败重试场景），AI 回复与错误记录仍照常落库。
            前端需手写 SSE 客户端，不要使用 openapi2ts 为该接口生成的调用。""")
    @ApiResponse(responseCode = "200", description = "SSE 事件流",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    schema = @Schema(implementation = String.class)))
    @GetMapping(value = "/user/code-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getCodeGenStream(
            @Valid
            @ParameterObject
            AppCodeStreamQueryDTO appCodeStreamQueryDTO,
            HttpServletRequest request) {
        long t0 = System.currentTimeMillis();
        log.info("[SSE-TIMING] Controller entry: t={}", t0);

        Flux<String> codeStream = appService.getCodeGenStream(appCodeStreamQueryDTO, request);
        return codeStream
                .doOnSubscribe(s -> log.info("[SSE-TIMING] Flux subscribed: t={} (+{}ms)", System.currentTimeMillis(), System.currentTimeMillis() - t0))
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonWrapper = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(jsonWrapper)
                            .build();
                })
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ))
                .onErrorResume(error -> {
                    log.error("代码生成流异常", error);
                    return Mono.just(errorEvent(error));
                });
    }

    /**
     * 构造 SSE 错误事件
     * <p>message 必须兜底：Map.of 拒绝 null 值，error.getMessage() 为 null 时
     * 会在兜底逻辑内抛 NPE，反而覆盖原始错误并导致断流。</p>
     *
     * @param error 流异常
     * @return error 事件，data 为 {@code {"code":50001,"message":".."}}
     */
    private static ServerSentEvent<String> errorEvent(Throwable error) {
        String detail = StrUtil.blankToDefault(error.getMessage(), "");
        String message = StrUtil.maxLength(
                StrUtil.isBlank(detail) ? ErrorCode.CODE_GENERATE_ERROR.getMessage() : "代码生成失败：" + detail,
                500);
        return ServerSentEvent.<String>builder()
                .event("error")
                .data(JSONUtil.toJsonStr(Map.of("code", ErrorCode.CODE_GENERATE_ERROR.getCode(), "message", message)))
                .build();
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
            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
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
            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
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

            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "应用id不能小于1")
            Long id) {
        return appService.updateAppByAdmin(appAdminUpdateRequestDTO, id);
    }

    /**
     * 根据主键删除应用(ADMIN)
     * 副作用：级联逻辑删除该应用下所有聊天记录
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/admin/{id}")
    public Boolean removeAppByAdmin(
            @Parameter(description = "应用ID", schema = @Schema(type = "String"))
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
