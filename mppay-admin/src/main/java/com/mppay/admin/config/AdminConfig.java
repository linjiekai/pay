package com.mppay.admin.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mppay.admin.annotation.support.LoginAdminHandlerMethodArgumentResolver;
import com.mppay.admin.handler.auth.AuthHandlerInterceptor;

@Configuration
public class AdminConfig implements WebMvcConfigurer {

    @Autowired
    private LoginAdminHandlerMethodArgumentResolver loginAdminHandlerMethodArgumentResolver;
    @Autowired
    private AuthHandlerInterceptor authHandlerInterceptor;

    /*@Bean
    public LoginUserHandlerMethodArgumentResolver loginUserHandlerMethodArgumentResolver(){
        return new LoginUserHandlerMethodArgumentResolver();
    }

    @Bean
    public LoginAdminHandlerMethodArgumentResolver loginAdminHandlerMethodArgumentResolver(){
        return new LoginAdminHandlerMethodArgumentResolver();
    }

    @Bean
    public AuthHandlerInterceptor authHandlerInterceptor(){
        return new AuthHandlerInterceptor();
    }*/


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginAdminHandlerMethodArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authHandlerInterceptor).addPathPatterns("/admin/**");
    }
}
