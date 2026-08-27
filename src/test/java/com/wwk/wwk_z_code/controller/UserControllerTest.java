package com.wwk.wwk_z_code.controller;

import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.entity.User;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController 切片测试：只加载 Web 层，UserService 用 mock，覆盖接口契约。
 * 注意：MockMvc 不应用 server.servlet.context-path，请求路径不带 /api。
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    // region register

    @Test
    void register_success_returnsUserId() throws Exception {
        when(userService.userRegister(any())).thenReturn(10086L);

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"zhangsan\",\"password\":\"123456\",\"confirmPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                // JsonConfig 把 Long 序列化为字符串，10086L → "10086"
                .andExpect(jsonPath("$.data").value("10086"));

        verify(userService).userRegister(any());
    }

    @Test
    void register_invalidParams_returnsParamError() throws Exception {
        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"a\",\"password\":\"1\",\"confirmPassword\":\"1\"}"))
                .andExpect(status().isOk())
                // @Validated 校验失败 → MethodArgumentNotValidException → GlobalExceptionHandler 返回 PARAM_ERROR
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void register_blankParams_returnsParamError() throws Exception {
        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                // @NotNull 校验失败 → MethodArgumentNotValidException → GlobalExceptionHandler 返回 PARAM_ERROR
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region login

    @Test
    void login_success_returnsUserVO() throws Exception {
        UserVO vo = new UserVO();
        vo.setId(1L);
        vo.setUserAccount("zhangsan");
        when(userService.login(any(), any())).thenReturn(vo);

        mvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"zhangsan\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userAccount").value("zhangsan"));
    }

    @Test
    void login_wrongPassword_returnsParamError() throws Exception {
        when(userService.login(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.PARAM_ERROR, "密码错误"));

        mvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"zhangsan\",\"password\":\"123456\"}"))
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    // endregion

    // region login-status / logout

    @Test
    void loginStatus_success_returnsUserVO() throws Exception {
        UserVO vo = new UserVO();
        vo.setId(1L);
        vo.setUserAccount("zhangsan");
        when(userService.getCurrentUser(any())).thenReturn(vo);

        mvc.perform(get("/users/login-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userAccount").value("zhangsan"));
    }

    @Test
    void logout_success_returnsTrue() throws Exception {
        when(userService.userLogout(any())).thenReturn(true);

        mvc.perform(post("/users/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    // endregion

    // region saveUser (POST /users)

    @Test
    void saveUser_success_returnsTrue() throws Exception {
        when(userService.saveUser(any())).thenReturn(true);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"zhangsan\",\"userPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).saveUser(any());
    }

    @Test
    void saveUser_invalidParams_returnsParamError() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"zhangsan\",\"userPassword\":\"1\"}"))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region delete (DELETE /users/{id})

    @Test
    void removeUserById_success_returnsTrue() throws Exception {
        when(userService.removeUserById(1L)).thenReturn(true);

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).removeUserById(1L);
    }

    @Test
    void removeUserById_invalidId_returnsSystemError() throws Exception {
        mvc.perform(delete("/users/0"))
                .andExpect(jsonPath("$.code").value(50000));
    }

    // endregion

    // region update (PUT /users/{id})

    @Test
    void update_success_returnsTrue() throws Exception {
        when(userService.updateUserById(any(), eq(1L))).thenReturn(true);

        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"newname\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).updateUserById(any(), eq(1L));
    }

    @Test
    void update_invalidParams_returnsParamError() throws Exception {
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userPassword\":\"1\"}"))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // endregion

    // region getInfo (GET /users/Info/{id})

    @Test
    void getInfo_success_returnsUser() throws Exception {
        User user = User.builder().id(1L).userAccount("zhangsan").build();
        when(userService.getUserById(1L)).thenReturn(user);

        mvc.perform(get("/users/Info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userAccount").value("zhangsan"));
    }

    @Test
    void getInfo_invalidId_returnsSystemError() throws Exception {
        mvc.perform(get("/users/Info/0"))
                .andExpect(jsonPath("$.code").value(50000));
    }

    // endregion

    // region page (GET /users/page)

    @Test
    void page_success_returnsPagedData() throws Exception {
        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(User.builder().id(1L).userAccount("zhangsan").build()));
        when(userService.getUserByPage(any())).thenReturn(page);

        mvc.perform(get("/users/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].userAccount").value("zhangsan"));
    }

    // endregion
}
