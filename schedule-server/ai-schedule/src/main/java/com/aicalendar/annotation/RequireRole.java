package com.aicalendar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解：标记Controller方法允许访问的角色
 * 由RoleAspect切面统一校验，不满足则返回403
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许访问的角色列表（如{"ADMIN"}） */
    String[] value();
}
