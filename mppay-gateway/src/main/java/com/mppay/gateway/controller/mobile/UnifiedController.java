package com.mppay.gateway.controller.mobile;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.UnifiedHandler;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/mobile")
@Slf4j
public class UnifiedController {

    @Autowired
    private UnifiedHandler chainHandler;

    @PostMapping("/unified")
    public Object unified(HttpServletRequest request) {
        Map<String, Object> paramMap = (Map<String, Object>) request.getAttribute("paramMap");


        String version = request.getHeader("X-MPMALL-APPVer");
        if (StringUtils.isBlank(version)) {
            version = request.getHeader("X-MP-APPVer");
        }
        paramMap.put("versionHeader", version);
        RequestMsg requestMsg = new RequestMsg(paramMap);
        ResponseMsg responseMsg = new ResponseMsg();
        try {
            //业务链处理
            responseMsg = chainHandler.execute(requestMsg);

            String code = (String) responseMsg.get(ConstEC.RETURNCODE);
            String msg = (String) responseMsg.get(ConstEC.RETURNMSG);
            //如果没有返回码就指定一个错误的返回码
            if (StringUtils.isBlank(code)) {
                log.error("[{}]业务处理失败, 异常信息为{}", JSON.toJSONString(paramMap), responseMsg);
                return ResponseUtil.result(11001);
            }

            //如果交易不成功就直接返回
            if (!ConstEC.SUCCESS_10000.equals(code)) {
                return ResponseUtil.fail(code, msg);
            }
            Object o = responseMsg.get(ConstEC.DATA);

            return ResponseUtil.ok(ConstEC.SUCCESS_MSG, o==null? new HashMap<>():o);
        } catch (BusiException e) {
            log.error("[" +JSON.toJSONString(paramMap) + "]业务处理失败, 异常信息为{}", e);
            throw new BusiException(e.getCode(), e.getMsg(), e);
        } catch (Exception e) {
            log.error("[" +JSON.toJSONString(paramMap) + "]业务处理失败, 异常信息为{}", e);
            throw new BusiException("11001", ApplicationYmlUtil.get("11001"), e);
        }
    }

}
