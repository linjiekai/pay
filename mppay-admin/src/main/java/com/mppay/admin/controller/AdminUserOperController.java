package com.mppay.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.service.service.IUserOperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户操作基础信息
 */
@Slf4j
@RestController
@RequestMapping("/admin/user/oper")
public class AdminUserOperController {

    @Autowired
    private IUserOperService userOperService;


    /**
     * 取消实名
     *
     * @return
     */
    @PostMapping("/cancel/realname")
    public Object cancelRealName(@RequestBody String reqMsg) {
        log.info("|取消实名|接收到请求参数:{}", reqMsg);
        Map<String, Object> reqMap = JSONObject.parseObject(reqMsg, Map.class);
        userOperService.cancelRealname(reqMap);
        return ResponseUtil.ok();
    }

}
