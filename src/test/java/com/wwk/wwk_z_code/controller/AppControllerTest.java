package com.wwk.wwk_z_code.controller;

import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;
import com.wwk.wwk_z_code.model.vo.AppVO;
import com.wwk.wwk_z_code.service.AppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AppController 切片测试：只加载 Web 层，AppService 用 mock，覆盖接口契约。
 * 注意：MockMvc 不应用 server.servlet.context-path，请求路径不带 /api。
 */
@WebMvcTest(AppController.class)
class AppControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AppService appService;

    // region featured (GET /apps/guest/page/featured)

    @Test
    void featured_success_returnsPagedAppVO() throws Exception {
        AppVO vo = new AppVO();
        vo.setId(1L);
        vo.setAppName("精选博客");
        vo.setCodeGenType(CodeGenEnum.SINGLETON_HTML);
        Page<AppVO> page = new Page<>(List.of(vo), 1, 10, 1);
        when(appService.getFeaturedAppByPage(any())).thenReturn(page);

        mvc.perform(get("/apps/guest/page/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].appName").value("精选博客"))
                // CodeGenEnum 经 @JsonValue 序列化为 codeGenMode 值
                .andExpect(jsonPath("$.data.records[0].codeGenType").value("singleton"));

        verify(appService).getFeaturedAppByPage(any());
    }

    @Test
    void featured_invalidSortField_returnsParamError() throws Exception {
        // 用户分页仅支持 appName 排序，sortField=priority 触发 DTO @AssertTrue → BindException → PARAM_ERROR
        mvc.perform(get("/apps/guest/page/featured").param("sortField", "priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region saveApp (POST /apps/user)

    @Test
    void saveApp_success_returnsId() throws Exception {
        when(appService.saveApp(any(), any())).thenReturn(1000L);

        mvc.perform(post("/apps/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"我的博客\",\"initPrompt\":\"做一个博客\",\"codeGenType\":\"singleton\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1000));

        verify(appService).saveApp(any(), any());
    }

    @Test
    void saveApp_invalidParams_returnsParamError() throws Exception {
        // 缺少 appName/initPrompt/codeGenType 任一必填字段 → MethodArgumentNotValidException → PARAM_ERROR
        mvc.perform(post("/apps/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"我的博客\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void saveApp_invalidCodeGenType_returnsParamError() throws Exception {
        // codeGenType 非法枚举值 → @JsonCreator 反序列化为 null → @NotNull 校验失败 → PARAM_ERROR
        mvc.perform(post("/apps/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"我的博客\",\"initPrompt\":\"做一个博客\",\"codeGenType\":\"xxx\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region getAppVOById (GET /apps/user/{id})

    @Test
    void getAppVOById_success_returnsAppVO() throws Exception {
        AppVO vo = new AppVO();
        vo.setId(1L);
        vo.setAppName("我的博客");
        vo.setCodeGenType(CodeGenEnum.MULTIFILE_HTML);
        when(appService.getAppById(eq(1L), any())).thenReturn(vo);

        mvc.perform(get("/apps/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.appName").value("我的博客"))
                .andExpect(jsonPath("$.data.codeGenType").value("multifile"));
    }

    @Test
    void getAppVOById_invalidId_returnsSystemError() throws Exception {
        // @PathVariable @Min 校验失败 → ConstraintViolationException → 兜底 SYSTEM_ERROR
        mvc.perform(get("/apps/user/0"))
                .andExpect(jsonPath("$.code").value(50000));
    }

    // endregion

    // region updateAppById (PUT /apps/user/{id})

    @Test
    void updateAppById_success_returnsTrue() throws Exception {
        when(appService.updateAppById(any(), eq(1L), any())).thenReturn(true);

        mvc.perform(put("/apps/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"新名字\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(appService).updateAppById(any(), eq(1L), any());
    }

    @Test
    void updateAppById_invalidParams_returnsParamError() throws Exception {
        // 用户更新仅允许 appName，缺 appName → @NotNull 校验失败 → PARAM_ERROR
        mvc.perform(put("/apps/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region removeAppById (DELETE /apps/user/{id})

    @Test
    void removeAppById_success_returnsTrue() throws Exception {
        when(appService.removeAppById(eq(1L), any())).thenReturn(true);

        mvc.perform(delete("/apps/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(appService).removeAppById(eq(1L), any());
    }

    @Test
    void removeAppById_invalidId_returnsSystemError() throws Exception {
        mvc.perform(delete("/apps/user/0"))
                .andExpect(jsonPath("$.code").value(50000));
    }

    // endregion

    // region my-apps (GET /apps/user/page/my-apps)

    @Test
    void myApps_success_returnsPagedAppVO() throws Exception {
        AppVO vo = new AppVO();
        vo.setId(1L);
        vo.setAppName("我的博客");
        Page<AppVO> page = new Page<>(List.of(vo), 1, 10, 1);
        when(appService.getMyAppByPage(any(), any())).thenReturn(page);

        mvc.perform(get("/apps/user/page/my-apps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].appName").value("我的博客"));

        verify(appService).getMyAppByPage(any(), any());
    }

    @Test
    void myApps_invalidSortField_returnsParamError() throws Exception {
        // 用户分页仅支持 appName 排序，sortField=priority 触发 DTO @AssertTrue → PARAM_ERROR
        mvc.perform(get("/apps/user/page/my-apps").param("sortField", "priority"))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region getAppByAdmin (GET /apps/admin/{id})

    @Test
    void getAppByAdmin_success_returnsApp() throws Exception {
        App app = App.builder().id(1L).appName("管理视角").codeGenType(CodeGenEnum.SINGLETON_HTML).build();
        when(appService.getAppByAdmin(1L)).thenReturn(app);

        mvc.perform(get("/apps/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.appName").value("管理视角"))
                .andExpect(jsonPath("$.data.codeGenType").value("singleton"));
    }

    @Test
    void getAppByAdmin_invalidId_returnsSystemError() throws Exception {
        mvc.perform(get("/apps/admin/0"))
                .andExpect(jsonPath("$.code").value(50000));
    }

    // endregion

    // region updateAppByAdmin (PUT /apps/admin/{id})

    @Test
    void updateAppByAdmin_success_returnsTrue() throws Exception {
        when(appService.updateAppByAdmin(any(), eq(1L))).thenReturn(true);

        mvc.perform(put("/apps/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appName\":\"管理改\",\"cover\":\"http://x\",\"priority\":99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(appService).updateAppByAdmin(any(), eq(1L));
    }

    @Test
    void updateAppByAdmin_invalidPriority_returnsParamError() throws Exception {
        // priority 不允许为负 → @Min 校验失败 → PARAM_ERROR
        mvc.perform(put("/apps/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":-1}"))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region removeAppByAdmin (DELETE /apps/admin/{id})

    @Test
    void removeAppByAdmin_success_returnsTrue() throws Exception {
        when(appService.removeAppByAdmin(1L)).thenReturn(true);

        mvc.perform(delete("/apps/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(appService).removeAppByAdmin(1L);
    }

    @Test
    void removeAppByAdmin_invalidId_returnsSystemError() throws Exception {
        mvc.perform(delete("/apps/admin/0"))
                .andExpect(jsonPath("$.code").value(50000));
    }

    // endregion

    // region admin page (GET /apps/admin/page)

    @Test
    void adminPage_success_returnsPagedData() throws Exception {
        Page<App> page = new Page<>(List.of(App.builder().id(1L).appName("管理视角").build()), 1, 10, 1);
        when(appService.getAppByAdminPage(any())).thenReturn(page);

        mvc.perform(get("/apps/admin/page")
                        .param("appName", "管理")
                        .param("sortField", "appName")
                        .param("sortOrder", "ascend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].appName").value("管理视角"));

        verify(appService).getAppByAdminPage(any());
    }

    @Test
    void adminPage_invalidSortField_returnsParamError() throws Exception {
        // sortField 不在管理员可排序白名单内 → DTO @AssertTrue → PARAM_ERROR
        mvc.perform(get("/apps/admin/page").param("sortField", "createUserId"))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region code-stream (GET /apps/user/code-stream)

    @Test
    void codeStream_success_returnsSSE() throws Exception {
        when(appService.getCodeGenStream(any(), any())).thenReturn(Flux.just("a", "b"));

        MvcResult mvcResult = mvc.perform(get("/apps/user/code-stream")
                        .param("appId", "1")
                        .param("userPrompt", "做个博客"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/event-stream"))
                .andExpect(content().string(containsString("data:{\"d\":\"a\"}")))
                .andExpect(content().string(containsString("event:done")));

        verify(appService).getCodeGenStream(any(), any());
    }

    @Test
    void codeStream_missingAppId_returnsParamError() throws Exception {
        // 缺 appId → AppCodeStreamQueryDTO @NotNull 校验失败 → PARAM_ERROR
        mvc.perform(get("/apps/user/code-stream").param("userPrompt", "做个博客"))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion
}
