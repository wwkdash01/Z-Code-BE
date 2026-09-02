package com.wwk.wwk_z_code.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wwk.wwk_z_code.annotation.AuthCheck;
import com.wwk.wwk_z_code.annotation.Sortable;
import com.wwk.wwk_z_code.common.PageRequest;
import com.wwk.wwk_z_code.common.ThrowUtils;
import com.wwk.wwk_z_code.constant.AppConstant;
import com.wwk.wwk_z_code.core.AiCodeGeneratorFacade;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.mapper.AppMapper;
import com.wwk.wwk_z_code.mapper.UserMapper;
import com.wwk.wwk_z_code.model.dto.*;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.entity.User;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import com.wwk.wwk_z_code.model.enums.UserRoleEnum;
import com.wwk.wwk_z_code.model.vo.AppVO;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.AppService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;

/**
 *  服务层实现。
 *
 * @author wwk
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final AiCodeGeneratorFacade aiCodeGeneratorFacade;
    private final UserMapper userMapper;

    // region 用户接口

    /**
     * 新增应用（只建实体，不调AI生成）
     * @param appAddRequestDTO 应用新增DTO
     * @param request HTTP请求
     * @return 新创建应用的主键 id
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public Long saveApp(AppAddRequestDTO appAddRequestDTO, HttpServletRequest request) {
        // 1-获取当前登录用户
        UserVO currentUser = getUserVOFromSession(request);

        // 2-复制DTO属性到PO，填充默认值（cover 为空置 ""，deployKey/deployTime 暂不设）
        App app = new App();
        BeanUtils.copyProperties(appAddRequestDTO, app);
        app.setCover(StrUtil.isBlank(app.getCover()) ? "" : app.getCover());
        app.setPriority(0);
        app.setDeployKey(null);
        app.setDeployTime(null);
        app.setCreateUserId(currentUser.getId());

        // 3-落库
        boolean dbResult = this.save(app);
        if (dbResult) {
            return app.getId();
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增应用失败，DB异常");
        }
    }

    /**
     * 根据主键获取应用详情（脱敏VO）
     * @param id 主键
     * @param request HTTP请求
     * @return 应用视图对象
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public AppVO getAppById(Long id, HttpServletRequest request) {
        // 1-校验归属
        App dbApp = checkAppOwnership(id, request);

        // 2-复制PO属性到VO（脱敏，剔除 priority/deployKey/审计字段）
        return toAppVO(dbApp);
    }

    /**
     * 根据主键更新应用名称（仅允许改 appName）
     * @param appUpdateRequestDTO 应用更新DTO
     * @param id 主键
     * @param request HTTP请求
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public Boolean updateAppById(AppUpdateRequestDTO appUpdateRequestDTO, Long id, HttpServletRequest request) {
        // 1-校验归属
        App dbApp = checkAppOwnership(id, request);

        // 2-仅更新应用名称
        dbApp.setAppName(appUpdateRequestDTO.getAppName());

        // 3-落库
        this.updateById(dbApp);
        return true;
    }

    /**
     * 根据主键删除应用
     * @param id 主键
     * @param request HTTP请求
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public Boolean removeAppById(Long id, HttpServletRequest request) {
        // 1-校验归属
        checkAppOwnership(id, request);

        // 2-删除
        this.removeById(id);
        return true;
    }

    /**
     * 分页查询我的应用（脱敏VO）
     * @param appQueryRequestDTO 应用分页查询DTO
     * @param request HTTP请求
     * @return 应用视图分页对象
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public Page<AppVO> getMyAppByPage(AppQueryRequestDTO appQueryRequestDTO, HttpServletRequest request) {
        // 1-校验分页参数
        validateQueryParam(appQueryRequestDTO);

        // 2-获取当前登录用户
        UserVO currentUser = getUserVOFromSession(request);

        // 3-封装QueryWrapper（检索字段为空时不拼进条件）
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq("createUserId", currentUser.getId())
                .like("appName", appQueryRequestDTO.getAppName(), StrUtil.isNotBlank(appQueryRequestDTO.getAppName()))
                .eq("appTag", appQueryRequestDTO.getAppTag(), StrUtil.isNotBlank(appQueryRequestDTO.getAppTag()));

        // 4-封装排序（用户分页仅支持 appName，未传不排序）
        if (StrUtil.isNotBlank(appQueryRequestDTO.getSortField())) {
            queryWrapper.orderBy(appQueryRequestDTO.getSortField(), "ascend".equals(appQueryRequestDTO.getSortOrder()));
        }

        // 5-查询并转脱敏VO
        Page<App> result = new Page<>(appQueryRequestDTO.getPageNum(), appQueryRequestDTO.getPageSize());
        return toAppVOPage(this.page(result, queryWrapper));
    }

    /**
     * 分页查询精选应用（游客公开，priority=99，脱敏VO）
     * @param appQueryRequestDTO 应用分页查询DTO
     * @return 应用视图分页对象
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.GUEST)
    public Page<AppVO> getFeaturedAppByPage(AppQueryRequestDTO appQueryRequestDTO) {
        // 1-校验分页参数
        validateQueryParam(appQueryRequestDTO);

        // 2-封装QueryWrapper（精选 priority=99）
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq("priority", 99)
                .like("appName", appQueryRequestDTO.getAppName(), StrUtil.isNotBlank(appQueryRequestDTO.getAppName()))
                .eq("appTag", appQueryRequestDTO.getAppTag(), StrUtil.isNotBlank(appQueryRequestDTO.getAppTag()));

        // 3-封装排序（精选分页仅支持 appName，未传不排序）
        if (StrUtil.isNotBlank(appQueryRequestDTO.getSortField())) {
            queryWrapper.orderBy(appQueryRequestDTO.getSortField(), "ascend".equals(appQueryRequestDTO.getSortOrder()));
        }

        // 4-查询并转脱敏VO
        Page<App> result = new Page<>(appQueryRequestDTO.getPageNum(), appQueryRequestDTO.getPageSize());
        return toAppVOPage(this.page(result, queryWrapper));
    }

    // endregion

    // region 管理员接口

    /**
     * 根据主键获取应用信息（不脱敏返回PO）
     * @param id 主键
     * @return 应用实体
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public App getAppByAdmin(Long id) {
        // 1-校验id是否存在
        App dbApp = this.getById(id);
        checkAppExists(dbApp);

        return dbApp;
    }

    /**
     * 根据主键更新应用信息（未传入字段不更新）
     * @param appAdminUpdateRequestDTO 应用管理更新DTO
     * @param id 主键
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Boolean updateAppByAdmin(AppAdminUpdateRequestDTO appAdminUpdateRequestDTO, Long id) {
        // 1-校验id是否存在
        App dbApp = this.getById(id);
        checkAppExists(dbApp);

        // 2-复制DTO属性到PO（updateById 默认忽略 null 字段，未传字段不更新）
        BeanUtils.copyProperties(appAdminUpdateRequestDTO, dbApp);

        // 3-落库
        this.updateById(dbApp);
        return true;
    }

    /**
     * 根据主键删除应用
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Boolean removeAppByAdmin(Long id) {
        // 1-校验id是否存在
        checkAppExists(this.getById(id));

        // 2-删除
        this.removeById(id);
        return true;
    }

    /**
     * 分页查询应用信息（全条件，除审计字段，不脱敏）
     * @param appAdminQueryRequestDTO 应用管理分页查询DTO
     * @return 应用分页对象
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Page<App> getAppByAdminPage(AppAdminQueryRequestDTO appAdminQueryRequestDTO) {
        // 1-校验分页参数
        validateQueryParam(appAdminQueryRequestDTO);

        // 2-封装QueryWrapper（检索字段为空时不拼进条件）
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq("id", appAdminQueryRequestDTO.getId(), appAdminQueryRequestDTO.getId() != null)
                .eq("appName", appAdminQueryRequestDTO.getAppName(), StrUtil.isNotBlank(appAdminQueryRequestDTO.getAppName()))
                .eq("cover", appAdminQueryRequestDTO.getCover(), StrUtil.isNotBlank(appAdminQueryRequestDTO.getCover()))
                .like("initPrompt", appAdminQueryRequestDTO.getInitPrompt(), StrUtil.isNotBlank(appAdminQueryRequestDTO.getInitPrompt()))
                .eq("priority", appAdminQueryRequestDTO.getPriority(), appAdminQueryRequestDTO.getPriority() != null)
                .eq("deployKey", appAdminQueryRequestDTO.getDeployKey(), StrUtil.isNotBlank(appAdminQueryRequestDTO.getDeployKey()))
                .eq("deployTime", appAdminQueryRequestDTO.getDeployTime(), appAdminQueryRequestDTO.getDeployTime() != null);

        // codeGenType 查询条件用 codeGenMode 字符串（枚举校验后取存储值）
        String codeGenType = appAdminQueryRequestDTO.getCodeGenType();
        if (StrUtil.isNotBlank(codeGenType)) {
            CodeGenEnum codeGenEnum = CodeGenEnum.getCodeGenEnum(codeGenType);
            if (codeGenEnum == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "应用生成类型不合法");
            }
            queryWrapper.eq("codeGenType", codeGenEnum.getCodeGenMode());
        }

        // appTag 查询条件：直接按 tag 字符串匹配
        queryWrapper.eq("appTag", appAdminQueryRequestDTO.getAppTag(), StrUtil.isNotBlank(appAdminQueryRequestDTO.getAppTag()));

        // 3-封装排序
        if (StrUtil.isNotBlank(appAdminQueryRequestDTO.getSortField())) {
            queryWrapper.orderBy(appAdminQueryRequestDTO.getSortField(), "ascend".equals(appAdminQueryRequestDTO.getSortOrder()));
        }

        // 4-查询
        Page<App> result = new Page<>(appAdminQueryRequestDTO.getPageNum(), appAdminQueryRequestDTO.getPageSize());
        return this.page(result, queryWrapper);
    }

    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public Flux<String> getCodeGenStream(AppCodeStreamQueryDTO appCodeStreamQueryDTO, HttpServletRequest request) {
        // 1-校验应用存在 + 归属当前用户（内部含 getById + checkAppExists）
        App dbApp = checkAppOwnership(appCodeStreamQueryDTO.getAppId(), request);

        // 2-获取生成类型
        CodeGenEnum codeGenEnum = dbApp.getCodeGenType();

        // 3-调用门面生成返回并回调更新代码生成路径
        Consumer<String> callback = (saveDir) -> {
            dbApp.setCodeGenDir(saveDir);
            this.updateById(dbApp);
        };

        return aiCodeGeneratorFacade.generateAndSaveCodeByStream(
                appCodeStreamQueryDTO.getUserPrompt(),
                codeGenEnum,
                dbApp.getId(),
                callback
        );
    }

    @Override
    public String deployApp(AppDeployRequestDTO appDeployRequestDTO, HttpServletRequest request) {
        // 1-查询App并校验App存在 && 校验App属于用户，否则抛异常
        App dbApp = checkAppOwnership(appDeployRequestDTO.getAppId(), request);

        // 2-检查DeployKey，不存在则生成（6位唯一随机串）
        if (StrUtil.isBlank(dbApp.getDeployKey())) {
            String deployKey = RandomUtil.randomString(6);
            while (existsByDeployKey(deployKey)) {
                deployKey = RandomUtil.randomString(6);
            }
            dbApp.setDeployKey(deployKey);
        }

        // 3-获取代码生成目录
        String codeGenDir = dbApp.getCodeGenDir();
        if (StrUtil.isBlank(codeGenDir)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "应用未配置代码生成目录");
        }

        // 4-校验生成目录是否存在，不存在抛异常
        File sourceDir = new File(AppConstant.FILE_SAVE_ROOT_DIR + File.separator + codeGenDir);
        if (!sourceDir.exists()) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_NOT_FOUND, "代码尚未生成，无法部署");
        }

        // 5-复制生成目录内容到部署目录
        String targetPath = AppConstant.CODE_DEPLOY_DIR + File.separator + dbApp.getDeployKey();
        File targetDir = new File(targetPath);
        if (!targetDir.exists()) {
            FileUtil.mkdir(targetDir);
        }

        try {
            FileUtil.copyContent(sourceDir, targetDir, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署文件复制失败");
        }

        // 6-将DeployKey和codeGenDir落库
        ThrowUtils.throwIf(!this.updateById(dbApp), ErrorCode.SYSTEM_ERROR, "更新应用信息失败");

        // 7-返回URL
        return String.format("%s/%s", AppConstant.LOCAL_DEPLOY_BASE_URL, dbApp.getDeployKey());
    }

    @Override
    public String previewApp(Long appId, HttpServletRequest request) {
        // 1-查询App校验是否存在 & 是否属于该用户
        App dbApp = checkAppOwnership(appId, request);

        // 2-获取App代码生成目录
        String codeGenDir = dbApp.getCodeGenDir();
        if (StrUtil.isBlank(codeGenDir)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "应用未配置代码生成目录");
        }

        // 3-校验目录内容，不存在抛异常
        File previewDir = new File(AppConstant.FILE_SAVE_ROOT_DIR + File.separator + codeGenDir);
        if (!previewDir.exists()) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_NOT_FOUND, "代码尚未生成，无法预览");
        }

        // 3-返回访问URL
        return String.format("%s/%s", AppConstant.LOCAL_RREVIEW_BASE_URL, codeGenDir);
    }

    /**
     * 检查deployKey是否已存在（用于保证唯一性）
     */
    private boolean existsByDeployKey(String deployKey) {
        App existing = getOne(new QueryWrapper().eq("deployKey", deployKey));
        return existing != null;
    }


    // endregion

    // region 私有工具方法
    /**
     * 可排序字段白名单：反射 App 实体中标了 @Sortable 的属性名（本项目列名=属性名）
     */
    private static final Set<String> SORTABLE_FIELDS = Arrays.stream(App.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .filter(field -> field.isAnnotationPresent(Sortable.class))
            .map(Field::getName)
            .collect(Collectors.toSet());

    /**
     * 校验分页查询参数合法性：sortField 白名单 / sortOrder 取值（controller 层 DTO 绑定已按角色限制，此处兜底防 SQL 注入）
     */
    private void validateQueryParam(PageRequest dto) {
        // sortField：必须为可排序白名单字段（防 orderBy 拼接非法列/SQL 注入）
        if (StrUtil.isNotBlank(dto.getSortField()) && !SORTABLE_FIELDS.contains(dto.getSortField())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "排序字段不合法");
        }
        // sortOrder：仅允许 ascend / descend
        if (StrUtil.isNotBlank(dto.getSortOrder())
                && !"ascend".equals(dto.getSortOrder())
                && !"descend".equals(dto.getSortOrder())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "排序方式不合法");
        }
    }

    /**
     * 校验应用归属（createUserId == 当前登录用户），并返回应用实体
     * @param id 应用主键
     * @param request HTTP请求
     * @return 应用实体
     */
    private App checkAppOwnership(Long id, HttpServletRequest request) {
        // 1-获取当前登录用户
        UserVO currentUser = getUserVOFromSession(request);

        // 2-校验应用存在
        App dbApp = this.getById(id);
        checkAppExists(dbApp);

        // 3-校验归属
        if (!currentUser.getId().equals(dbApp.getCreateUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权访问该应用");
        }

        return dbApp;
    }

    /**
     * 从Session中获取请求用户信息（未登录抛异常）
     * @param request http请求
     * @return 用户视图类
     */
    private UserVO getUserVOFromSession(HttpServletRequest request) {
        // 1-从session获取登录状态（未登录时getSession(false)返回null，需判空）
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        UserVO currentUserVO = (UserVO) session.getAttribute(USER_LOGIN_STATUS);

        // 2-检查登录状态
        if (currentUserVO == null || currentUserVO.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        return currentUserVO;
    }

    /**
     * 校验应用是否存在，不存在抛异常
     * @param dbApp 应用实体
     */
    private static void checkAppExists(App dbApp) {
        if (dbApp == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "应用不存在或已删除");
        }
    }

    /**
     * PO转脱敏VO（剔除 priority/deployKey/审计字段，补充创建人信息）
     * @param app 应用实体
     * @return 应用视图对象
     */
    private AppVO toAppVO(App app) {
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);

        // 补充创建人用户名和头像
        if (app.getCreateUserId() != null) {
            User creator = userMapper.selectOneById(app.getCreateUserId());
            if (creator != null) {
                appVO.setUserName(creator.getUserName());
                appVO.setUserAvatar(creator.getUserAvatar());
            }
        }

        return appVO;
    }

    /**
     * 应用分页对象转脱敏VO分页对象
     * @param appPage 应用分页对象
     * @return 应用视图分页对象
     */
    private Page<AppVO> toAppVOPage(Page<App> appPage) {
        List<AppVO> voRecords = appPage.getRecords().stream()
                .map(this::toAppVO)
                .collect(Collectors.toList());
        return new Page<>(voRecords, appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
    }

    // endregion
}
