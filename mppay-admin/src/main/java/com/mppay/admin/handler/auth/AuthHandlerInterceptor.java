package com.mppay.admin.handler.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mppay.core.exception.CheckParamsException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson.JSON;
import com.mppay.core.handler.web.AuthHttpServletRequestWrapper;
import com.mppay.core.utils.IpUtil;
import com.mppay.core.utils.YamlUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthHandlerInterceptor implements HandlerInterceptor {

    /**
     * 非必填参数
     */
    private final String OPTIONAL = "\\[\\w+\\]$";
    /**
     * 手机参数
     */
    private final String MOBILE = "mobile";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        MDC.put("LOGGER_ID", String.valueOf(UUID.randomUUID()));
        MDC.put("CLIENT_IP", IpUtil.getIpAddr(request));
        try {
            return checkAuth(new AuthHttpServletRequestWrapper(request), handler);
        } catch (CheckParamsException e) {
            throw e;
        } catch (Exception e) {
            log.error("参数校验异常：{}", e);
            return false;
        }
    }

    /**
     * 获取请求参数和sign
     *
     * @param requestWrapper
     * @return map: 有序且去掉sign, sign:签名
     */
    private boolean checkAuth(AuthHttpServletRequestWrapper requestWrapper, Object handler) throws Exception {

        Map<String, Object> params;
        if ("GET".equalsIgnoreCase(requestWrapper.getMethod())) {
            params = getParams(requestWrapper);
        } else if ("POST".equalsIgnoreCase(requestWrapper.getMethod())) {

            String requestBody = getRequestBody(requestWrapper);
            if (StringUtils.isBlank(requestBody)) {
                // form表单提交
                params = getParams(requestWrapper);
            } else {
                // body提交
                params = JSON.parseObject(requestBody, HashMap.class);
            }
        } else {
            log.error("不支持的请求类型:{}", requestWrapper.getMethod());
            return false;
        }
        // 校验参数是否合格
        regularExpression(params, handler);
        return true;
    }

    private String getRequestBody(AuthHttpServletRequestWrapper requestWrapper) throws Exception {
        ServletInputStream inputStream = requestWrapper.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        String str = new String(result.toByteArray(), "UTF-8");
        result.close();
        inputStream.close();
        return str;
    }


    /**
     * 获取请求参数key-value
     *
     * @param requestWrapper
     * @return
     */
    private Map<String, Object> getParams(AuthHttpServletRequestWrapper requestWrapper) {

        HashMap<String, Object> params = new HashMap<>();
        Enumeration<String> parameterNames = requestWrapper.getParameterNames();
        String key;
        while (parameterNames.hasMoreElements()) {
            key = parameterNames.nextElement();
            params.put(key, requestWrapper.getParameter(key));
        }
        return params;
    }

    /**
     * 通过配置文件进行参数校验
     * @param requestParams 请求失败
     * @param handler
     */
    private void regularExpression(Map<String, Object> requestParams, Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        String ymlMethodsKey = controllerName + "." + methodName;
        // 配置文件请求方法的参数
        String ymlMethodsParams = YamlUtil.get("application-methods.yml", ymlMethodsKey);

        if (StringUtils.isBlank(ymlMethodsParams)) {
            return;
        }

        String[] ymlMethodsParamsArray = ymlMethodsParams.split(",");
        for (String ymlMethodsParam : ymlMethodsParamsArray) {
            if (ymlMethodsParam.matches(OPTIONAL)) {
                // 非必传
                if (requestParams == null) {
                    continue;
                }
                Object requestParamValue = requestParams.get(ymlMethodsParam);
                // 传非空，判断
                if (requestParamValue != null && StringUtils.isNotBlank(requestParamValue.toString())) {
                    String reg = YamlUtil.get("application-methods-reg.yml", ymlMethodsKey + "." + ymlMethodsParam);
                    if (StringUtils.isBlank(reg)) {
                        continue;
                    }
                    if (!requestParamValue.toString().matches(reg)) {
                        if (MOBILE.equalsIgnoreCase(ymlMethodsParam)) {
                            throw new CheckParamsException("请输入正确的手机号");
                        }
                        throw new CheckParamsException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                    }
                }
            } else {// 必传
                if (requestParams == null || requestParams.size() == 0) {
                    throw new CheckParamsException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                }
                Object requestParamValue = requestParams.get(ymlMethodsParam);
                // 判断不为空后再正则判断
                if (requestParamValue == null || StringUtils.isBlank(requestParamValue.toString())) {
                    if (MOBILE.equalsIgnoreCase(ymlMethodsParam)) {
                        throw new CheckParamsException("请输入正确的手机号");
                    }
                    throw new CheckParamsException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                } else {
                    String reg = YamlUtil.get("application-methods-reg.yml", ymlMethodsKey + "." + ymlMethodsParam);
                    if (StringUtils.isBlank(reg)) {
                        continue;
                    }
                    if (!requestParamValue.toString().matches(reg)) {
                        if (MOBILE.equalsIgnoreCase(ymlMethodsParam)) {
                            throw new CheckParamsException("请输入正确的手机号");
                        }
                        throw new CheckParamsException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                    }
                }
            }
        }
    }

}
