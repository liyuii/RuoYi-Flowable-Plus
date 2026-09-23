package com.ruoyi.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限组
 *
 * @author Lion Li
 * @version 3.5.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 功能级数据范围标识，例如 contract:statistics
     * <p>
     * 为空时使用 sys_role.data_scope 旧逻辑
     */
    String scopeKey() default "";

    DataColumn[] value();

}
