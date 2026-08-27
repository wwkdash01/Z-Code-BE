package com.wwk.wwk_z_code.controller;

import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.model.dto.*;
import com.wwk.wwk_z_code.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.wwk.wwk_z_code.model.entity.User;
import com.wwk.wwk_z_code.service.UserService;

/**
 *  控制层。
 *
 * @author wwk
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 新增用户(ADMIN)
     *
     * @param userAddRequestDTO 数据库表实体PO
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("/admin")
    public Boolean saveUser(
            @RequestBody
            @Valid
            UserAddRequestDTO userAddRequestDTO) {
        return userService.saveUser(userAddRequestDTO);
    }

    /**
     * 根据主键删除用户(ADMIN)
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/admin/{id}")
    public Boolean removeUserById(
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "用户id不能为负")
            Long id) {
        return userService.removeUserById(id);
    }

    /**
     * 根据主键更新用户信息(ADMIN)
     *
     * @param userUpdateRequestDTO 数据库表实体PO
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/admin/{id}")
    public Boolean update(
            @RequestBody
            @Valid
            UserUpdateRequestDTO userUpdateRequestDTO,

            @PathVariable
            @NotNull
            @Min(value = 1L, message = "用户id不能为负")
            Long id) {
        return userService.updateUserById(userUpdateRequestDTO, id);
    }

    /**
     * 根据主键获取用户信息(ADMIN)
     *
     * @param id 主键
     * @return 数据库表实体PO
     */
    @GetMapping("/admin/{id}")
    public User getInfo(
            @PathVariable
            @Min(value = 1L, message = "用户id不能为负")
            Long id) {
        return userService.getUserById(id);
    }

    /**
     * 分页查询用户信息(ADMIN)
     *
     * @param userQueryRequestDTO 用户分页查询DTO（query 参数自动绑定）
     * @return 分页对象
     */
    @GetMapping("/admin/page")
    public Page<User> getUserByPage(
            @ModelAttribute
            @Valid
            UserQueryRequestDTO userQueryRequestDTO) {
        return userService.getUserByPage(userQueryRequestDTO);
    }

    /**
     * 用户注册(GUEST)
     * @param userRegisterRequestDTO 用户注册DTO
     * @return 用户id
     */
    @PostMapping("/guest/register")
    public Long userRegister(
            @RequestBody
            @Valid
            UserRegisterRequestDTO userRegisterRequestDTO) {
        return userService.userRegister(userRegisterRequestDTO);
    }

    /**
     * 用户登录(GUEST)
     * @param userLoginRequestDTO 用户登录DTO
     * @param request Http请求
     * @return 用户信息
     */
    @PostMapping("/guest/login")
    public UserVO userLogin(
            @RequestBody
            @Valid
            UserLoginRequestDTO userLoginRequestDTO,
            HttpServletRequest request) {
        return userService.login(userLoginRequestDTO, request);
    }

    /**
     * 获取登录状态(USER)
     * @param request Http请求
     * @return 用户信息
     */
    @GetMapping("/user/login-status")
    public UserVO getCurrentUser(HttpServletRequest request) {
        return userService.getCurrentUser(request);
    }

    /**
     * 登出(USER)
     * @param request Http请求
     * @return {@code true} 注销成功，{@code false} 注销失败
     */
    @PostMapping("/user/logout")
    public Boolean userLogout(HttpServletRequest request) {
        return userService.userLogout(request);
    }
}
