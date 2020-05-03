package com.mppay.gateway.aop;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

/**
 * 当请求方法参数中引用LoginAdmin注解，如果LoginAdmin注解对应的参数为空，则要登录
 */
@Aspect
@Component
@Slf4j
public class LoginAspect {
    private static final String LOGGER_ID = "LOGGER_ID";

    @Pointcut("execution(public * com.mppay.gateway.controller.mobile.*.*(..))")
    public void controllerMobile() {
    }

    @Pointcut("execution(public * com.mppay.gateway.controller.notify.*.*(..))")
    public void controllerNotify() {
    }

    @Pointcut("execution(public * com.mppay.core.config.GlobalExceptionHandler.*(..))")
    public void globalException() {
    }

    @Pointcut("execution(public * com.mppay.gateway.mq..*.*(..))")
    public void mq() {
    }

    @Pointcut("execution(public * com.mppay.gateway.scheduled..*.*(..))")
    public void scheduled() {
    }


    @Around("controllerMobile()||controllerNotify()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        /*MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] objects = joinPoint.getArgs();// 参数值

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        Parameter[] parameters = targetMethod.getParameters();
        String s = "";
        try {
            s = objects.length > 0 ? JSON.toJSONString(objects) : null;
        } catch (Exception e) {
            //有异常先不处理
        }

        log.info("请求URI：{}, 请求参数：{}", requestURI, s);*/


        Object result = joinPoint.proceed();


        org.slf4j.MDC.clear();
        return result;
    }


    @Before("mq() || scheduled()||controllerNotify()")
    public void before(JoinPoint joinPoint) {
        // 日志
        if (StrUtil.isEmpty(org.slf4j.MDC.get(LOGGER_ID))) {
            org.slf4j.MDC.put(LOGGER_ID, UUID.randomUUID().toString());
        }
    }

    @After("globalException() || mq() || scheduled()||controllerNotify()")
    public void after(JoinPoint joinPoint) {
        if (StrUtil.isNotEmpty(org.slf4j.MDC.get(LOGGER_ID))) {
            org.slf4j.MDC.remove(LOGGER_ID);
        }
    }
}
