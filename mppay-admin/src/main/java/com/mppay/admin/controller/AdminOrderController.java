package com.mppay.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.service.IMercOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author: Jiekai Lin
 * @Description(描述):
 * @date: 2019/12/1 11:32
 */
@Slf4j
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {



    @Autowired
    private IMercOrderService mercOrderService;

    /**
     * @Description(描述): 商城订单相关查询
     * @auther: Jack Lin
     * @param :[reqMsg]
     * @return :java.lang.Object
     * @date: 2019/12/1 11:38
     */
    @PostMapping("/list")
    public Object withdrBankList(@RequestBody String reqMsg) throws Exception {
        log.info("|订单查询|接收到请求参数:{}", reqMsg);
        Map<String, Object> reqMap = JSONObject.parseObject(reqMsg, Map.class);
        List<String> orderNos = (List<String>)reqMap.get("orderNos");
        return mercOrderService.list(new QueryWrapper<MercOrder>().select("order_no","order_status","open_id").in("order_no",orderNos));
    }
}
