package com.mppay.gateway.annotation.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.MercStatus;
import com.mppay.core.constant.UserOperStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.service.entity.Merc;
import com.mppay.service.entity.UserOper;
import com.mppay.service.service.IMercService;
import com.mppay.service.service.IUserOperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ParamCheckInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private IMercService mercService;

    @Autowired
    private IUserOperService userOperService;

    private static final String OPTIONAL = "^\\[\\w+(-\\w+)*\\]$";
    private static final String MIDBRACKETS = "\\[|\\]";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        //日志唯一id
        MDC.put("LOGGER_ID", String.valueOf(UUID.randomUUID()));

        checkParam(request);

        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }

    private void checkParam(HttpServletRequest request) throws Exception {
        Map<String, Object> requestMap = JSONObject.parseObject(request.getInputStream(), Map.class);
        String objTojson = JSON.toJSONString(requestMap);
        String methodType = (String) requestMap.get(ConstEC.METHODTYPE);
        String itfParam = null;

        log.info("请求methodType: {}", methodType);
        log.info("请求requestMap: {}", objTojson);

        if (StringUtils.isEmpty(methodType)) {
            log.error("接口请求参数【{}】", objTojson);
            throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", "methodType"));
        }

        String mercId = (String) requestMap.get("mercId");
        if (StringUtils.isEmpty(mercId)) {
            log.error("接口请求参数【{}】", objTojson);
            throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", "mercId"));
        }

        Merc merc = mercService.getOne(new QueryWrapper<Merc>().eq("merc_id", mercId));

        if (null == merc) {
            log.error("接口请求参数【{}】", objTojson);
            throw new BusiException("11901");
        }

        if (merc.getStatus() != MercStatus.NORMAL.getId()) {
            log.error("接口请求参数【{}】", objTojson);
            throw new BusiException("11902");
        }

        // 从配置文件中读取请求接口的相应参数
        itfParam = ApplicationYmlUtil.get(ConstEC.PRE_REQ + methodType);
        if ("".equals(itfParam.trim()) || itfParam.isEmpty() || itfParam.length() == 0) {
            log.error("接口请求参数【{}】", objTojson);
            throw new BusiException("11015", ApplicationYmlUtil.get("11015"));
        }

        // 是否属于非必传参数,配置文件中使用[]来区分非必传参数
        boolean isOption = false;

        // 分割请求参数
        String[] keys = itfParam.split(",");
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        int index = 0;
        Object value = null;
        for (String key : keys) {
            value = requestMap.get(key);
            isOption = key.matches(OPTIONAL);
            // 判断是否必传参数
            if (isOption) {
                // 切出[]括号中的参数名
                key = key.replaceAll(MIDBRACKETS, "");
                value = requestMap.get(key);
                String property = StrUtil.isNotBlank(ApplicationYmlUtil.get(ConstEC.PRE_REGS + key)) ? ApplicationYmlUtil.get(ConstEC.PRE_REGS + key) : "";
                // 判断非必传参数值是否为空，再用正则匹配
                if (null != value && !StringUtils.isEmpty(value.toString())) {
                    if (!value.toString().trim().matches(property)) {
                        log.error(ApplicationYmlUtil.get("11004").replace("$", key));
                        throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", key) + ", 接口请求参数：" + objTojson);
                    }
                    parameterMap.put(key, value.toString().trim());
                }
                keys[index] = key;

                // 判断必传参数值是否为空，再用正则匹配
            } else if (null != value && !StringUtils.isEmpty(value.toString())) {
                String property = StrUtil.isNotBlank(ApplicationYmlUtil.get(ConstEC.PRE_REGS + key)) ? ApplicationYmlUtil.get(ConstEC.PRE_REGS + key) : "";
                if (value.toString().trim().matches(property)) {
                    parameterMap.put(key, value.toString().trim());
                }
            } else {
                log.error(ApplicationYmlUtil.get("11004").replace("$", key) + ", 接口请求参数：" + objTojson);
                throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", key));
            }
            index++;
        }

        // 签名字符串
        String sign = request.getHeader(ConstEC.X_MPMALL_SIGN);
        if (StringUtils.isBlank(sign)) {
            sign = request.getHeader("X-MP-Sign");
        }

        // 待签名串
        String plain = Sign.getPlain(parameterMap, keys);
        plain += "&key=" + merc.getPrivateKey();
        log.info("plain：{}", plain);
        if (!Sign.verify(plain, sign)) {
            String signVer = request.getHeader(ConstEC.X_MPMALL_SIGNVER);

            if (!StringUtils.isBlank(signVer)) {
                parameterMap.put(ConstEC.X_MPMALL_SIGNVER, signVer);
            } else {
                signVer = request.getHeader("X-MP-SignVer");
                parameterMap.put("X-MP-SignVer", signVer);
            }

            plain = Sign.getPlain(parameterMap, keys);
            plain += "&key=" + merc.getPrivateKey();
            log.info("plain：{}", plain);

            if (!Sign.verify(plain, sign)) {
                log.error("签名失败, 后台待签名串plain：{},前端签名串X-MP-Sign：{}", plain, sign);
                throw new BusiException("11012");
            }

        }

        if (null != requestMap.get("userId")) {
            UserOper userOper = userOperService.getOne(new QueryWrapper<UserOper>()
                    .eq("merc_id", requestMap.get("mercId")).eq("user_id", requestMap.get("userId")));
            // 用户操作信息不存在
            if (null == userOper) {
                log.error("{}::{}", ApplicationYmlUtil.get(11301), JSONUtil.toJsonStr(requestMap));
                throw new BusiException(11301);
            }
            // 用户操作状态不正确
            if (userOper.getStatus().intValue() != UserOperStatus.NORMAL.getId()) {
                log.error(ApplicationYmlUtil.get(11303) + requestMap.toString());
                throw new BusiException(11303);
            }
            parameterMap.put("nickname", userOper.getNickname());
            parameterMap.put("userOperNo", userOper.getUserOperNo());
            parameterMap.put("userNo", userOper.getUserNo());
            parameterMap.put("userOper_name", userOper.getName());
        }

        parameterMap.put("mercName", merc.getMercAbbr());
//		String clientIp = this.getIpAddr(request);
//		request.setAttribute("clientIp", clientIp);
        request.setAttribute("paramMap", parameterMap);
    }

    /**
     * 获取请求的IP地址
     *
     * @param request
     * @return
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        } else if (null != request.getHeader("Proxy-Client-IP") && request.getHeader("Proxy-Client-IP").length() > 0
                && !"unknown".equalsIgnoreCase(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        } else if (null != request.getHeader("WL-Proxy-Client-IP")
                && request.getHeader("WL-Proxy-Client-IP").length() > 0
                && !"unknown".equalsIgnoreCase(request.getHeader("WL-Proxy-Client-IP"))) {
            return request.getHeader("WL-Proxy-Client-IP");
        } else {
            return request.getRemoteHost();
        }
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        try {
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            return outSteam.toByteArray();
        } finally {

            if (null != outSteam) {
                outSteam.close();
            }
            if (null != inStream) {
                inStream.close();
            }

        }
    }

}
