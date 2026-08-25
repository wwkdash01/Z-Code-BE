package com.wwk.wwk_z_code.service;

import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.UserRoleEnum;
import com.wwk.wwk_z_code.model.vo.UserVO;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    // TODO 二期作完整RBAC
    /**
     * 校验参数）
     * @param userVO 用户信息
     * @param roleRequirement 方法权限要求
     * @return 校验结果（越权false，反之true）
     */
    public Boolean authCheck(UserVO userVO, UserRoleEnum roleRequirement) {
        // 1-参数校验
        if (userVO == null || roleRequirement == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "权限信息缺失");
        }
        
        // 2-权限校验
        UserRoleEnum userRole = UserRoleEnum.getEnumByRole(userVO.getUserRole());
        if (userRole == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无效的用户角色");
        }
        return roleRequirement.getLevel() <= userRole.getLevel();
    }
}
