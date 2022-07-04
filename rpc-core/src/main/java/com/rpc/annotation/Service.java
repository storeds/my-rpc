package com.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 21:58
 * @description: 表示一个服务提供类，用于远程接口的实现类
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    public String name() default "";
}
