package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.wwk.wwk_z_code.model.dto.*;
import com.wwk.wwk_z_code.model.entity.User;
import com.wwk.wwk_z_code.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 *  service
 *
 * @author wwk
 */
public interface UserService extends IService<User> {
    /**
     * 注册用户
     * @param userRegisterRequestDTO 用户注册DTO
     * @return 用户id
     */
    Long userRegister(UserRegisterRequestDTO userRegisterRequestDTO);

    UserVO login(UserLoginRequestDTO userLoginRequestDTO, HttpServletRequest request);

    UserVO getCurrentUser(HttpServletRequest request);

    Boolean userLogout(HttpServletRequest request);

    Page<User> getUserByPage(UserQueryRequestDTO userQueryRequestDTO);

    Boolean saveUser(UserAddRequestDTO userAddRequestDTO);

    Boolean removeUserById(Long id);

    Boolean updateUserById(UserUpdateRequestDTO userUpdateRequestDTO, Long id);

    User getUserById(Long id);
}
