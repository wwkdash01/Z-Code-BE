package com.wwk.wwk_z_code.aspect;

import com.wwk.wwk_z_code.annotation.AuthCheck;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.model.enums.UserRoleEnum;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;

@Component
@Aspect
@RequiredArgsConstructor
public class AuthInterceptor {
    private final AuthService authService;
    /**
     * 环绕拦截
     * @param joinPoint 切入点（AuthCheck注解方法）
     * @param authCheck 方法所需角色
     * @return 方法返回值
     * @throws Throwable 方法异常
     */
    @Around("@annotation(authCheck)")
    public Object around(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1-获取session 判断是否登录 放行游客
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        UserRoleEnum roleRequirement = authCheck.roleRequirement();

        if (roleRequirement == UserRoleEnum.GUEST) return joinPoint.proceed();

        // 2-获取用户角色和要求角色
        HttpSession session = httpServletRequest.getSession(false);
        UserVO currentUserVO = (session == null) ? null : (UserVO)session.getAttribute(USER_LOGIN_STATUS);
        if (currentUserVO == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 3-越权抛异常
        if (!authService.authCheck(currentUserVO, roleRequirement)) throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "越权接口访问");

        // 4-正常返回
        return joinPoint.proceed();
    }

}
