package com.mppay.gateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mppay.gateway.annotation.support.ParamCheckInterceptor;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    @Autowired
    private ParamCheckInterceptor paramCheckInterceptor;


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对外Api进行接口验签拦截
        registry.addInterceptor(paramCheckInterceptor).addPathPatterns("/mobile/**");
    }
}
