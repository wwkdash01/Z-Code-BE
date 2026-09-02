package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wwk.wwk_z_code.core.AiCodeGeneratorFacade;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.mapper.UserMapper;
import com.wwk.wwk_z_code.model.dto.AppAddRequestDTO;
import com.wwk.wwk_z_code.model.dto.AppAdminQueryRequestDTO;
import com.wwk.wwk_z_code.model.dto.AppAdminUpdateRequestDTO;
import com.wwk.wwk_z_code.model.dto.AppCodeStreamQueryDTO;
import com.wwk.wwk_z_code.model.dto.AppQueryRequestDTO;
import com.wwk.wwk_z_code.model.dto.AppUpdateRequestDTO;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.entity.User;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import com.wwk.wwk_z_code.model.enums.TagEnum;
import com.wwk.wwk_z_code.model.vo.AppVO;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.impl.AppServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

/**
 * AppServiceImpl 单元测试：覆盖用户接口、管理员接口以及 appTag / createTime / 创建人信息补充等逻辑。
 */
@ExtendWith(MockitoExtension.class)
class AppServiceImplTest {

    @Mock
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private AppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = Mockito.spy(new AppServiceImpl(aiCodeGeneratorFacade, userMapper));
    }

    // region 私有工具方法测试

    @Test
    void getCodeGenStream_ownerSuccess_returnsFacadeStream() {
        loginAs(1L);
        doReturn(App.builder().id(100L).createUserId(1L).codeGenType(CodeGenEnum.SINGLETON_HTML).build())
                .when(service).getById(100L);
        when(aiCodeGeneratorFacade.generateAndSaveCodeByStream(eq("做个博客"), eq(CodeGenEnum.SINGLETON_HTML), eq(100L), any(Consumer.class)))
                .thenReturn(Flux.just("a", "b"));

        List<String> emitted = service.getCodeGenStream(streamDTO(100L, "做个博客"), request)
                .collectList().block();

        assertEquals(List.of("a", "b"), emitted);
        verify(aiCodeGeneratorFacade).generateAndSaveCodeByStream(eq("做个博客"), eq(CodeGenEnum.SINGLETON_HTML), eq(100L), any(Consumer.class));
    }

    @Test
    void getCodeGenStream_notLoggedIn_throwsNotLoginError() {
        when(request.getSession(false)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getCodeGenStream(streamDTO(100L, "做个博客"), request));

        assertEquals(ErrorCode.NOT_LOGIN_ERROR.getCode(), e.getCode());
    }

    @Test
    void getCodeGenStream_appNotExists_throwsForbiddenError() {
        loginAs(1L);
        doReturn(null).when(service).getById(100L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getCodeGenStream(streamDTO(100L, "做个博客"), request));

        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
        assertEquals("应用不存在或已删除", e.getMessage());
    }

    @Test
    void getCodeGenStream_notOwner_throwsForbiddenError() {
        loginAs(1L);
        doReturn(App.builder().id(100L).createUserId(2L).codeGenType(CodeGenEnum.MULTIFILE_HTML).build())
                .when(service).getById(100L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getCodeGenStream(streamDTO(100L, "做个博客"), request));

        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
        assertEquals("无权访问该应用", e.getMessage());
    }

    // endregion

    // region 用户业务方法

    @Test
    void saveApp_success_setsDefaults() {
        loginAs(1L);
        doReturn(true).when(service).save(any(App.class));

        AppAddRequestDTO dto = new AppAddRequestDTO();
        dto.setAppName("我的博客");
        dto.setInitPrompt("做个博客");
        dto.setCodeGenType(CodeGenEnum.SINGLETON_HTML);
        dto.setAppTag(TagEnum.TOOL);

        service.saveApp(dto, request);

        ArgumentCaptor<App> captor = ArgumentCaptor.forClass(App.class);
        verify(service).save(captor.capture());
        App saved = captor.getValue();
        assertEquals("我的博客", saved.getAppName());
        assertEquals("", saved.getCover());
        assertEquals(0, saved.getPriority());
        assertEquals(1L, saved.getCreateUserId());
        assertNull(saved.getDeployKey());
        assertEquals(TagEnum.TOOL, saved.getAppTag());
    }

    @Test
    void saveApp_dbFail_throwsSystemError() {
        loginAs(1L);
        doReturn(false).when(service).save(any(App.class));

        AppAddRequestDTO dto = new AppAddRequestDTO();
        dto.setAppName("我的博客");
        dto.setInitPrompt("做个博客");
        dto.setCodeGenType(CodeGenEnum.SINGLETON_HTML);

        BusinessException e = assertThrows(BusinessException.class, () -> service.saveApp(dto, request));
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), e.getCode());
        assertEquals("新增应用失败，DB异常", e.getMessage());
    }

    @Test
    void getAppById_success_returnsMaskedVO_withCreatorInfo() {
        loginAs(1L);
        App app = App.builder().id(100L).appName("我的博客").cover("http://c")
                .initPrompt("p").codeGenType(CodeGenEnum.MULTIFILE_HTML)
                .appTag(TagEnum.WEB_PAGE).priority(99).deployKey("k")
                .createUserId(1L).createTime(LocalDateTime.now()).build();
        doReturn(app).when(service).getById(100L);
        User creator = User.builder().id(1L).userName("tester").userAvatar("http://avatar").build();
        when(userMapper.selectOneById(1L)).thenReturn(creator);

        AppVO vo = service.getAppById(100L, request);

        assertEquals(100L, vo.getId());
        assertEquals("我的博客", vo.getAppName());
        assertEquals(CodeGenEnum.MULTIFILE_HTML, vo.getCodeGenType());
        assertEquals(TagEnum.WEB_PAGE, vo.getAppTag());
        assertNotNull(vo.getCreateTime());
        assertEquals("tester", vo.getUserName());
        assertEquals("http://avatar", vo.getUserAvatar());
    }

    @Test
    void getAppById_creatorDeleted_returnsVO_withoutUserInfo() {
        loginAs(1L);
        App app = App.builder().id(100L).appName("我的博客").createUserId(1L).build();
        doReturn(app).when(service).getById(100L);
        when(userMapper.selectOneById(1L)).thenReturn(null);

        AppVO vo = service.getAppById(100L, request);

        assertEquals(100L, vo.getId());
        assertNull(vo.getUserName());
        assertNull(vo.getUserAvatar());
    }

    @Test
    void updateAppById_success_onlyChangesAppName() {
        loginAs(1L);
        doReturn(App.builder().id(100L).appName("旧名").codeGenType(CodeGenEnum.SINGLETON_HTML).createUserId(1L).build())
                .when(service).getById(100L);
        doReturn(true).when(service).updateById(any(App.class));

        AppUpdateRequestDTO dto = new AppUpdateRequestDTO();
        dto.setAppName("新名字");
        assertTrue(service.updateAppById(dto, 100L, request));

        ArgumentCaptor<App> captor = ArgumentCaptor.forClass(App.class);
        verify(service).updateById(captor.capture());
        App updated = captor.getValue();
        assertEquals("新名字", updated.getAppName());
        assertEquals(CodeGenEnum.SINGLETON_HTML, updated.getCodeGenType());
    }

    @Test
    void removeAppById_success_returnsTrue() {
        loginAs(1L);
        doReturn(App.builder().id(100L).createUserId(1L).build()).when(service).getById(100L);
        doReturn(true).when(service).removeById(100L);

        assertTrue(service.removeAppById(100L, request));
        verify(service).removeById(100L);
    }

    @Test
    void getMyAppByPage_success_returnsPagedVO_withCreatorInfo() {
        loginAs(1L);
        LocalDateTime now = LocalDateTime.now();
        App app = App.builder().id(100L).appName("我的应用").codeGenType(CodeGenEnum.SINGLETON_HTML)
                .createUserId(1L).createTime(now).build();
        Page<App> dbPage = new Page<>(List.of(app), 1, 10, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));
        User creator = User.builder().id(1L).userName("tester").userAvatar("http://avatar").build();
        when(userMapper.selectOneById(1L)).thenReturn(creator);

        AppQueryRequestDTO dto = new AppQueryRequestDTO();
        dto.setSortField("appName");
        dto.setSortOrder("ascend");

        Page<AppVO> result = service.getMyAppByPage(dto, request);

        assertEquals(1, result.getRecords().size());
        assertEquals("我的应用", result.getRecords().get(0).getAppName());
        assertEquals(CodeGenEnum.SINGLETON_HTML, result.getRecords().get(0).getCodeGenType());
        assertEquals(now, result.getRecords().get(0).getCreateTime());
        assertEquals("tester", result.getRecords().get(0).getUserName());
        assertEquals("http://avatar", result.getRecords().get(0).getUserAvatar());
    }

    @Test
    void getMyAppByPage_invalidSortField_throwsParamError() {
        // validateQueryParam 在取登录用户前抛错，无需登录态
        AppQueryRequestDTO dto = new AppQueryRequestDTO();
        dto.setSortField("xxx");

        BusinessException e = assertThrows(BusinessException.class, () -> service.getMyAppByPage(dto, request));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("排序字段不合法", e.getMessage());
    }

    @Test
    void getFeaturedAppByPage_success_returnsPagedVO() {
        LocalDateTime now = LocalDateTime.now();
        App app = App.builder().id(100L).appName("精选应用").codeGenType(CodeGenEnum.MULTIFILE_HTML)
                .createUserId(1L).createTime(now).build();
        Page<App> dbPage = new Page<>(List.of(app), 1, 10, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));
        User creator = User.builder().id(1L).userName("admin").userAvatar("http://av").build();
        when(userMapper.selectOneById(1L)).thenReturn(creator);

        AppQueryRequestDTO dto = new AppQueryRequestDTO();
        dto.setSortField("appName");

        Page<AppVO> result = service.getFeaturedAppByPage(dto);

        assertEquals(1, result.getRecords().size());
        assertEquals("精选应用", result.getRecords().get(0).getAppName());
        assertEquals(now, result.getRecords().get(0).getCreateTime());
        assertEquals("admin", result.getRecords().get(0).getUserName());
    }

    // endregion

    // region 管理员业务方法

    @Test
    void getAppByAdmin_success_returnsPO() {
        doReturn(App.builder().id(100L).appName("管理视角").priority(99).deployKey("k").appTag(TagEnum.PROFILE).build())
                .when(service).getById(100L);

        App po = service.getAppByAdmin(100L);

        assertEquals(100L, po.getId());
        assertEquals(99, po.getPriority());
        assertEquals("k", po.getDeployKey());
        assertEquals(TagEnum.PROFILE, po.getAppTag());
    }

    @Test
    void getAppByAdmin_notFound_throwsForbidden() {
        doReturn(null).when(service).getById(100L);

        BusinessException e = assertThrows(BusinessException.class, () -> service.getAppByAdmin(100L));
        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
        assertEquals("应用不存在或已删除", e.getMessage());
    }

    @Test
    void updateAppByAdmin_success_updatesOnlyProvidedFields_includingAppTag() {
        doReturn(App.builder().id(100L).appName("旧名").cover("http://old").priority(0).build())
                .when(service).getById(100L);
        doReturn(true).when(service).updateById(any(App.class));

        AppAdminUpdateRequestDTO dto = new AppAdminUpdateRequestDTO();
        dto.setAppName("新名");
        dto.setPriority(99);
        dto.setAppTag(TagEnum.TOOL);

        assertTrue(service.updateAppByAdmin(dto, 100L));

        ArgumentCaptor<App> captor = ArgumentCaptor.forClass(App.class);
        verify(service).updateById(captor.capture());
        App updated = captor.getValue();
        assertEquals("新名", updated.getAppName());
        assertEquals(99, updated.getPriority());
        assertEquals(TagEnum.TOOL, updated.getAppTag());
    }

    @Test
    void removeAppByAdmin_success_returnsTrue() {
        doReturn(App.builder().id(100L).build()).when(service).getById(100L);
        doReturn(true).when(service).removeById(100L);

        assertTrue(service.removeAppByAdmin(100L));
        verify(service).removeById(100L);
    }

    @Test
    void getAppByAdminPage_success_returnsPagedApp() {
        App app = App.builder().id(100L).appName("管理视角").priority(99).build();
        Page<App> dbPage = new Page<>(List.of(app), 1, 10, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        AppAdminQueryRequestDTO dto = new AppAdminQueryRequestDTO();
        dto.setAppName("管理");
        dto.setSortField("appName");

        Page<App> result = service.getAppByAdminPage(dto);

        assertEquals(1, result.getRecords().size());
        assertEquals("管理视角", result.getRecords().get(0).getAppName());
    }

    @Test
    void getAppByAdminPage_success_filterByAppTag() {
        App app = App.builder().id(100L).appName("工具应用").appTag(TagEnum.TOOL).build();
        Page<App> dbPage = new Page<>(List.of(app), 1, 10, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        AppAdminQueryRequestDTO dto = new AppAdminQueryRequestDTO();
        dto.setAppTag("tool");

        Page<App> result = service.getAppByAdminPage(dto);

        assertEquals(1, result.getRecords().size());
        assertEquals("工具应用", result.getRecords().get(0).getAppName());
    }

    @Test
    void getAppByAdminPage_success_sortByAppTag() {
        App app = App.builder().id(100L).appName("排序测试").build();
        Page<App> dbPage = new Page<>(List.of(app), 1, 10, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        AppAdminQueryRequestDTO dto = new AppAdminQueryRequestDTO();
        dto.setSortField("appTag");

        Page<App> result = service.getAppByAdminPage(dto);

        assertEquals(1, result.getRecords().size());
    }

    @Test
    void getAppByAdminPage_invalidCodeGenType_throwsParamError() {
        AppAdminQueryRequestDTO dto = new AppAdminQueryRequestDTO();
        dto.setCodeGenType("xxx");

        BusinessException e = assertThrows(BusinessException.class, () -> service.getAppByAdminPage(dto));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("应用生成类型不合法", e.getMessage());
    }

    @Test
    void getAppByAdminPage_invalidSortField_throwsParamError() {
        AppAdminQueryRequestDTO dto = new AppAdminQueryRequestDTO();
        dto.setSortField("xxx");

        BusinessException e = assertThrows(BusinessException.class, () -> service.getAppByAdminPage(dto));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("排序字段不合法", e.getMessage());
    }

    // endregion

    // region 辅助方法

    private void loginAs(Long userId) {
        when(request.getSession(false)).thenReturn(session);
        UserVO vo = new UserVO();
        vo.setId(userId);
        when(session.getAttribute(USER_LOGIN_STATUS)).thenReturn(vo);
    }

    private AppCodeStreamQueryDTO streamDTO(Long appId, String prompt) {
        AppCodeStreamQueryDTO dto = new AppCodeStreamQueryDTO();
        dto.setAppId(appId);
        dto.setUserPrompt(prompt);
        return dto;
    }

    // endregion
}
