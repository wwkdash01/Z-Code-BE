package com.wwk.wwk_z_code.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wwk.wwk_z_code.annotation.AuthCheck;
import com.wwk.wwk_z_code.annotation.Sortable;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.dto.*;
import com.wwk.wwk_z_code.model.entity.User;
import com.wwk.wwk_z_code.mapper.UserMapper;
import com.wwk.wwk_z_code.model.enums.UserRoleEnum;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;

/**
 *  服务层实现。
 *
 * @author wwk
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    /**
     * 注册用户
     * @param userRegisterRequestDTO 用户注册DTO
     * @return 用户id
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.GUEST)
    public Long userRegister(UserRegisterRequestDTO userRegisterRequestDTO) {
        // 0-获取DTO字段
        String userAccount = userRegisterRequestDTO.getUserAccount();
        String password = userRegisterRequestDTO.getPassword();
        String confirmPassword = userRegisterRequestDTO.getConfirmPassword();

        // 1-校验参数是否符合业务逻辑
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "确认密码与原密码不一致");
        }

        // 2-校验用户是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号已存在");
        }

        // 3-加密密码
        String encryptedPassword = encryptPassword(password);

        // 4-落库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName("默认用户名"+ UUID.randomUUID());
        user.setUserRole(UserRoleEnum.USER.getRole());

        boolean dbResult = this.save(user);
        if (dbResult) {
            return user.getId();
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，DB异常");
        }
    }

    /**
     * 用户登录
     * @param userLoginRequestDTO 用户登录DTO
     * @param request HTTP请求
     * @return 用户视图类
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.GUEST)
    public UserVO login(UserLoginRequestDTO userLoginRequestDTO, HttpServletRequest request) {
        // 1-获取数据库中实体，不存在抛异常
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userLoginRequestDTO.getUserAccount());
        User user = this.mapper.selectOneByQuery(queryWrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }

        // 2-加密传入密码
        String encryptedPassword = encryptPassword(userLoginRequestDTO.getPassword());

        // 3-比较
        if (encryptedPassword.equals(user.getUserPassword())) {
            // 3.1-一致返回VO登录成功
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            request.getSession().setAttribute(USER_LOGIN_STATUS, userVO);
            return userVO;
        } else {
            // 3.2-不一致抛异常
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码错误");
        }
    }

    /**
     * 获取当前请求中登录的用户
     * @param request HTTP请求
     * @return 用户视图类
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public UserVO getCurrentUser(HttpServletRequest request) {
        // 1-从session获取当前用户登录状态并返回
        return getUserVOFromSession(request);
    }

    /**
     * 注销请求中登录的用户
     * @param request HTTP请求
     * @return {@code true} 注销成功，{@code false} 注销失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public Boolean userLogout(HttpServletRequest request) {
        // 1-已登录移除session中登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATUS);
        return true;
    }

    /**
     * 分页查询用户信息
     * @param userQueryRequestDTO 用户分页查询DTO
     * @return 用户分页对象
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Page<User> getUserByPage(UserQueryRequestDTO userQueryRequestDTO) {
        // 1-校验参数
        validateQueryParam(userQueryRequestDTO);

        // 2-封装QueryWrapper（检索字段为空时不拼进条件）
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq("id", userQueryRequestDTO.getId(), userQueryRequestDTO.getId() != null)
                .eq("vipId", userQueryRequestDTO.getVipId(), userQueryRequestDTO.getVipId() != null)
                .like("userName", userQueryRequestDTO.getUserName(), StrUtil.isNotBlank(userQueryRequestDTO.getUserName()))
                .like("userAccount", userQueryRequestDTO.getUserAccount(), StrUtil.isNotBlank(userQueryRequestDTO.getUserAccount()))
                .like("userRole", userQueryRequestDTO.getUserRole(), StrUtil.isNotBlank(userQueryRequestDTO.getUserRole()));

        // 3-封装排序
        if (StrUtil.isNotBlank(userQueryRequestDTO.getSortField())) {
            queryWrapper.orderBy(userQueryRequestDTO.getSortField(), "ascend".equals(userQueryRequestDTO.getSortOrder()));
        }

        // 4-查询
        Page<User> result = new Page<>(userQueryRequestDTO.getPageNum(), userQueryRequestDTO.getPageSize());
        return this.page(result, queryWrapper);
    }

    /**
     * 新增用户
     * @param userAddRequestDTO 用户新增DTO
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Boolean saveUser(UserAddRequestDTO userAddRequestDTO) {
        // 1-校验账号是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAddRequestDTO.getUserAccount());
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号已存在");
        }

        // 2-复制DTO属性到PO
        User dbUser = new User();
        BeanUtils.copyProperties(userAddRequestDTO, dbUser);

        // 3-加密密码
        dbUser.setUserPassword(encryptPassword(userAddRequestDTO.getUserPassword()));

        // 4-落库
        boolean dbResult = this.save(dbUser);
        if (dbResult) {
            return true;
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增用户失败，DB异常");
        }
    }

    /**
     * 根据主键删除用户
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Boolean removeUserById(Long id) {
        // 1-校验id是否存在
        checkUserExists(this.getById(id));

        // 2-删除
        this.removeById(id);
        return true;
    }

    /**
     * 根据主键更新用户信息（未传入的字段不更新）
     * @param userUpdateRequestDTO 用户更新DTO
     * @param id 主键
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Boolean updateUserById(UserUpdateRequestDTO userUpdateRequestDTO, Long id) {
        // 1-校验id是否存在
        User dbUser = this.getById(id);
        checkUserExists(dbUser);

        // 2-复制DTO属性到PO（updateById 默认忽略 null 字段，未传字段不更新）
        BeanUtils.copyProperties(userUpdateRequestDTO, dbUser);

        // 3-密码非空则加密覆盖
        if (StrUtil.isNotBlank(userUpdateRequestDTO.getUserPassword())) {
            dbUser.setUserPassword(encryptPassword(userUpdateRequestDTO.getUserPassword()));
        }

        // 4-落库
        this.updateById(dbUser);
        return true;
    }

    /**
     * 根据主键获取用户信息
     * @param id 主键
     * @return 用户实体
     */
    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public User getUserById(Long id) {
        // 1-校验id是否存在
        User dbUser = this.getById(id);
        checkUserExists(dbUser);

        return dbUser;
    }

    // region 私有工具方法
    /**
     * 可排序字段白名单：反射 User 实体中标了 @Sortable 的属性名（本项目列名=属性名）
     */
    private static final Set<String> SORTABLE_FIELDS = Arrays.stream(User.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .filter(field -> field.isAnnotationPresent(Sortable.class))
            .map(Field::getName)
            .collect(Collectors.toSet());

    /**
     * 校验分页查询参数合法性：sortField 白名单 / sortOrder 取值 / userRole 转枚举
     */
    private void validateQueryParam(UserQueryRequestDTO dto) {
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
        // userRole：转枚举，转换失败（null）即非法
        if (StrUtil.isNotBlank(dto.getUserRole())
                && UserRoleEnum.getEnumByRole(dto.getUserRole()) == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户角色不合法");
        }
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
     * 校验用户是否存在，不存在抛异常
     * @param dbUser 用户实体
     */
    private static void checkUserExists(User dbUser) {
        if (dbUser == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户不存在或已删除");
        }
    }

    public static String encryptPassword(String password) {
        final String SALT = "wwk_z_code";
        return DigestUtils.md5DigestAsHex((password + SALT).getBytes());
    }

    // endregion
}
