package com.mppay.admin.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.service.entity.Bank;
import com.mppay.service.service.IBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 银行信息
 */
@Slf4j
@RestController
@RequestMapping("/admin/bank")
public class AdminBankController {

    @Autowired
    private IBankService bankService;

    /**
     * 根据银行code查询银行信息
     *
     * @param reqMsg bankCodes 银行编码列表
     * @return
     */
    @PostMapping("/list/by/bankcode")
    public Object listByBankCode(@RequestBody JSONObject reqMsg) {
        log.info("|银行信息|根据bankCodes查询|接收到请求报文:{}|", reqMsg);
        JSONArray bankCodeArr = reqMsg.getJSONArray("bankCodes");
        List<String> bankCodes = bankCodeArr.toJavaList(String.class);
        List<Bank> banks = new ArrayList<>();
        if (bankCodes != null && bankCodes.size() > 0) {
            banks = bankService.list(new QueryWrapper<Bank>()
                    .eq("status", 1)
                    .in("bank_code", bankCodes));
        }

        return ResponseUtil.ok(banks);
    }

}
