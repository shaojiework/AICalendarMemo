package com.aicalendar.aop;

import com.aicalendar.annotation.RequireRole;
import com.aicalendar.common.BizException;
import com.aicalendar.common.ResponseCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 角色权限切面：拦截所有带@RequireRole注解的接口，校验当前登录角色
 */
@Aspect
@Component
public class RoleAspect {

    /**
     * 环绕校验：从request域取出JwtInterceptor解析的role，与注解声明的角色比对
     */
    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String role = attributes != null ? (String) attributes.getRequest().getAttribute("role") : null;

        // 角色不在允许列表内，拒绝访问
        if (role == null || !Arrays.asList(requireRole.value()).contains(role)) {
            throw new BizException(ResponseCode.FORBIDDEN);
        }
        return joinPoint.proceed();
    }
}
