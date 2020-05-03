package com.mppay.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.mppay.service.service.IUserRealNameDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @title: AdminUserRealNameDetailsController
 * @projectName xfhl-mppay-api
 * @description: 用户实名详情
 * @date 2019/10/16 16:23
 */
@Slf4j
@RestController
@RequestMapping("/admin/user/real/name/details")
public class AdminUserRealNameDetailsController {

    @Autowired
    private IUserRealNameDetailsService userRealNameDetailsService;

    /**
     * 实名认证详情列表
     *
     * @param reqMsg mercId     商户id
     *               userId     用户id
     *               cardNo     身份证号码
     *               realSource 来源 0：我的、1：提交订单 2：快捷支付
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody String reqMsg) {
        log.info("|用户实名信息|实名认证详情列表|接收到请求报文:{}", reqMsg);
        Map<String, Object> reqMap = JSONObject.parseObject(reqMsg, Map.class);
        return userRealNameDetailsService.pageByCondition(reqMap);
    }
}
