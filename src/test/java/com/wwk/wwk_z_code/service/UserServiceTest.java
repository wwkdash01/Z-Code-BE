package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.mapper.UserMapper;
import com.wwk.wwk_z_code.model.dto.UserAddRequestDTO;
import com.wwk.wwk_z_code.model.dto.UserLoginRequestDTO;
import com.wwk.wwk_z_code.model.dto.UserQueryRequestDTO;
import com.wwk.wwk_z_code.model.dto.UserRegisterRequestDTO;
import com.wwk.wwk_z_code.model.dto.UserUpdateRequestDTO;
import com.wwk.wwk_z_code.model.entity.User;
import com.wwk.wwk_z_code.model.enums.UserRoleEnum;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserService 单元测试：mock mapper + 继承方法 spy，覆盖业务逻辑分支。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper mapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        UserServiceImpl real = new UserServiceImpl();
        // mapper 是 ServiceImpl 的 protected 字段，反射注入 mock
        ReflectionTestUtils.setField(real, "mapper", mapper);
        // spy：save/getById/updateById/removeById/page 等继承方法可在 spy 上直接 stub
        service = Mockito.spy(real);
    }

    // region userRegister

    @Test
    void userRegister_passwordMismatch_throwsParamError() {
        UserRegisterRequestDTO dto = registerDTO();
        dto.setConfirmPassword("654321");

        BusinessException e = assertThrows(BusinessException.class, () -> service.userRegister(dto));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
    }

    @Test
    void userRegister_duplicateAccount_throwsParamError() {
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);

        BusinessException e = assertThrows(BusinessException.class, () -> service.userRegister(registerDTO()));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("用户账号已存在", e.getMessage());
    }

    @Test
    void userRegister_success_returnsUserIdAndEncryptsPassword() {
        doAnswer(invocation -> {
            invocation.<User>getArgument(0).setId(10086L);
            return true;
        }).when(service).save(any(User.class));

        Long id = service.userRegister(registerDTO());

        assertEquals(10086L, id);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(service).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("zhangsan", saved.getUserAccount());
        assertEquals(UserServiceImpl.encryptPassword("123456"), saved.getUserPassword());
        assertEquals(UserRoleEnum.USER.getRole(), saved.getUserRole());
    }

    // endregion

    // region login

    @Test
    void login_userNotFound_throwsParamError() {
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.login(loginDTO("zhangsan", "123456"), request));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("用户不存在", e.getMessage());
    }

    @Test
    void login_wrongPassword_throwsParamError() {
        User dbUser = User.builder()
                .id(1L)
                .userAccount("zhangsan")
                .userPassword(UserServiceImpl.encryptPassword("654321"))
                .build();
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(dbUser);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.login(loginDTO("zhangsan", "123456"), request));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("密码错误", e.getMessage());
    }

    @Test
    void login_success_writesSessionAndReturnsUserVO() {
        User dbUser = User.builder()
                .id(1L)
                .userAccount("zhangsan")
                .userPassword(UserServiceImpl.encryptPassword("123456"))
                .userRole(UserRoleEnum.USER.getRole())
                .build();
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(dbUser);
        when(request.getSession()).thenReturn(session);

        UserVO vo = service.login(loginDTO("zhangsan", "123456"), request);

        assertEquals("zhangsan", vo.getUserAccount());
        verify(session).setAttribute(USER_LOGIN_STATUS, vo);
    }

    // endregion

    // region getCurrentUser / userLogout

    @Test
    void getCurrentUser_notLoggedIn_throwsNotLoginError() {
        when(request.getSession(false)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class, () -> service.getCurrentUser(request));
        assertEquals(ErrorCode.NOT_LOGIN_ERROR.getCode(), e.getCode());
    }

    @Test
    void getCurrentUser_loggedIn_returnsUserVO() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(USER_LOGIN_STATUS)).thenReturn(userVO(1L, "zhangsan"));

        UserVO result = service.getCurrentUser(request);

        assertEquals("zhangsan", result.getUserAccount());
    }

    @Test
    void userLogout_success_removesSessionAttribute() {
        when(request.getSession()).thenReturn(session);

        Boolean result = service.userLogout(request);

        assertTrue(result);
        verify(session).removeAttribute(USER_LOGIN_STATUS);
    }

    // endregion

    // region saveUser

    @Test
    void saveUser_duplicateAccount_throwsParamError() {
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);

        BusinessException e = assertThrows(BusinessException.class, () -> service.saveUser(addDTO()));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("用户账号已存在", e.getMessage());
    }

    @Test
    void saveUser_success_encryptsPassword() {
        doReturn(true).when(service).save(any(User.class));

        assertTrue(service.saveUser(addDTO()));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(service).save(captor.capture());
        assertEquals(UserServiceImpl.encryptPassword("123456"), captor.getValue().getUserPassword());
    }

    @Test
    void saveUser_dbFailure_throwsSystemError() {
        doReturn(false).when(service).save(any(User.class));

        BusinessException e = assertThrows(BusinessException.class, () -> service.saveUser(addDTO()));
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), e.getCode());
    }

    // endregion

    // region removeUserById / updateUserById / getUserById

    @Test
    void removeUserById_userNotFound_throwsForbiddenError() {
        doReturn(null).when(service).getById(1L);

        BusinessException e = assertThrows(BusinessException.class, () -> service.removeUserById(1L));
        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
    }

    @Test
    void removeUserById_success_deletes() {
        doReturn(User.builder().id(1L).build()).when(service).getById(1L);

        assertTrue(service.removeUserById(1L));
        verify(service).removeById(1L);
    }

    @Test
    void updateUserById_userNotFound_throwsForbiddenError() {
        doReturn(null).when(service).getById(1L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.updateUserById(new UserUpdateRequestDTO(), 1L));
        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
    }

    @Test
    void updateUserById_withPassword_encryptsAndUpdates() {
        doReturn(dbUser()).when(service).getById(1L);
        doReturn(true).when(service).updateById(any(User.class));

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setUserName("新名字");
        dto.setUserPassword("abcdef");

        assertTrue(service.updateUserById(dto, 1L));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(service).updateById(captor.capture());
        assertEquals("新名字", captor.getValue().getUserName());
        assertEquals(UserServiceImpl.encryptPassword("abcdef"), captor.getValue().getUserPassword());
    }

    @Test
    void updateUserById_withoutPassword_leavesPasswordNull() {
        doReturn(dbUser()).when(service).getById(1L);
        doReturn(true).when(service).updateById(any(User.class));

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setUserName("新名字");

        assertTrue(service.updateUserById(dto, 1L));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(service).updateById(captor.capture());
        assertEquals("新名字", captor.getValue().getUserName());
        // Spring BeanUtils.copyProperties 会把 DTO 的 null 密码覆盖到内存对象，
        // 实际靠 MyBatis-Flex updateById 忽略 null 字段保证数据库密码不被更新
        assertNull(captor.getValue().getUserPassword());
    }

    @Test
    void getUserById_success_returnsUser() {
        User dbUser = dbUser();
        doReturn(dbUser).when(service).getById(1L);

        assertSame(dbUser, service.getUserById(1L));
    }

    @Test
    void getUserById_userNotFound_throwsForbiddenError() {
        doReturn(null).when(service).getById(1L);

        BusinessException e = assertThrows(BusinessException.class, () -> service.getUserById(1L));
        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
    }

    // endregion

    // region getUserByPage

    @Test
    void getUserByPage_success_returnsPage() {
        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(User.builder().id(1L).userAccount("zhangsan").build()));
        doReturn(page).when(service).page(any(Page.class), any(QueryWrapper.class));

        Page<User> result = service.getUserByPage(new UserQueryRequestDTO());

        assertSame(page, result);
    }

    @Test
    void getUserByPage_withSortField_returnsPage() {
        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(service).page(any(Page.class), any(QueryWrapper.class));

        UserQueryRequestDTO dto = new UserQueryRequestDTO();
        dto.setSortField("userName");
        dto.setSortOrder("ascend");

        Page<User> result = service.getUserByPage(dto);

        assertSame(page, result);
    }

    @Test
    void getUserByPage_invalidSortField_throws() {
        UserQueryRequestDTO dto = new UserQueryRequestDTO();
        dto.setSortField("userPassword");
        BusinessException e = assertThrows(BusinessException.class, () -> service.getUserByPage(dto));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
    }

    @Test
    void getUserByPage_invalidSortOrder_throws() {
        UserQueryRequestDTO dto = new UserQueryRequestDTO();
        dto.setSortOrder("up");
        BusinessException e = assertThrows(BusinessException.class, () -> service.getUserByPage(dto));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
    }

    @Test
    void getUserByPage_invalidUserRole_throws() {
        UserQueryRequestDTO dto = new UserQueryRequestDTO();
        dto.setUserRole("root");
        BusinessException e = assertThrows(BusinessException.class, () -> service.getUserByPage(dto));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
    }

    // endregion

    // region 私有工具方法

    private UserRegisterRequestDTO registerDTO() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setUserAccount("zhangsan");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");
        return dto;
    }

    private UserLoginRequestDTO loginDTO(String account, String password) {
        UserLoginRequestDTO dto = new UserLoginRequestDTO();
        dto.setUserAccount(account);
        dto.setPassword(password);
        return dto;
    }

    private UserAddRequestDTO addDTO() {
        UserAddRequestDTO dto = new UserAddRequestDTO();
        dto.setUserAccount("zhangsan");
        dto.setUserPassword("123456");
        return dto;
    }

    private UserVO userVO(Long id, String account) {
        UserVO vo = new UserVO();
        vo.setId(id);
        vo.setUserAccount(account);
        return vo;
    }

    private User dbUser() {
        return User.builder()
                .id(1L)
                .userAccount("zhangsan")
                .userPassword(UserServiceImpl.encryptPassword("oldpass"))
                .build();
    }

    // endregion
}
