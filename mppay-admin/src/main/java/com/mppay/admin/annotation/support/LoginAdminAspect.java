package com.mppay.admin.annotation.support;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.mppay.core.annotation.LoginAdmin;
import com.mppay.core.annotation.LoginUser;
import com.mppay.core.utils.ResponseUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 当请求方法参数中引用LoginAdmin注解，如果LoginAdmin注解对应的参数为空，则要登录
 */
@Aspect
@Component
@Slf4j
public class LoginAdminAspect {

    private ThreadLocal<Long> threadLocal = new ThreadLocal<>();


    @Around("execution(public * com.mppay.admin.controller..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        threadLocal.set(System.currentTimeMillis());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] objects = joinPoint.getArgs();// 参数值

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        Parameter[] parameters = targetMethod.getParameters();

        //admin日志
        String s = "";
        try {
            s = objects.length > 0 ? JSON.toJSONString(objects) : null;
        } catch (Exception e) {
            //有异常先不处理
        }
        log.info("请求URI：{}, 请求参数：{}", requestURI, s);

        if (parameters != null) {
            int i = 0;
            for (Parameter p : parameters) {
                Annotation[] annotations = p.getAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        // 有LoginAdmin注解的参数如果没值，要登录
                        if (annotation.annotationType() == LoginUser.class || annotation.annotationType() == LoginAdmin.class) {
                            if (objects[i] == null) {
                                return ResponseUtil.unlogin();
                            }
                        }
                    }
                }
                i++;
            }
        }
        Object result = joinPoint.proceed();
        log.info("请求处理耗时：{} 毫秒, 返回数据：{}", (System.currentTimeMillis() - threadLocal.get()), JSON.toJSONString(result));
        threadLocal.remove();
        MDC.clear();
        return result;
    }
}
