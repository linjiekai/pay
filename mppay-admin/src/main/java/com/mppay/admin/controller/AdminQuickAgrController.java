package com.mppay.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.mppay.service.service.IQuickAgrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 快捷绑卡
 */
@Slf4j
@RestController
@RequestMapping("/admin/quick/agr")
public class AdminQuickAgrController {

    @Autowired
    private IQuickAgrService quickAgrService;

    /**
     * 查询快捷银行卡信息
     */
    @PostMapping("/quick/bank/list")
    public Object quickBankList(@RequestBody String reqMsg) throws Exception {
        log.info("|查询快捷银行卡信息|接收到请求参数:{}", reqMsg);
        Map<String, Object> reqMap = JSONObject.parseObject(reqMsg, Map.class);
        return quickAgrService.quickCardBindList(reqMap);
    }

}
