package com.mppay.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.service.entity.CardBind;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.vo.CardBindLastBindVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 提现绑卡
 */
@Slf4j
@RestController
@RequestMapping("/admin/card/bind")
public class AdminCardBindController {

    @Autowired
    private ICardBindService cardBindService;

    /**
     * 提现银行卡列表
     */
    @PostMapping("/withdr/bank/list")
    public Object withdrBankList(@RequestBody String reqMsg) throws Exception {
        log.info("|提现银行卡列表|接收到请求参数:{}", reqMsg);
        Map<String, Object> reqMap = JSONObject.parseObject(reqMsg, Map.class);
        return cardBindService.withdrCardBindList(reqMap);
    }

    /**
     * 根据用户id批量查询绑卡信息
     */
    @PostMapping("/lastbind/byuserids")
    public Object lastBindByUserIds(@RequestBody String reqMsg) {
        log.info("|提现银行卡-根据userIds查询|接收到请求参数:{}", reqMsg);
        Map<String, Object> reqMap = JSONObject.parseObject(reqMsg, Map.class);
        List<CardBindLastBindVO> lastBindVOS = cardBindService.lastBindByUserIds(reqMap);
        return ResponseUtil.ok(lastBindVOS);
    }

    /**
     * 根据协议号查询绑定信息
     *
     * @param cardBind
     * @return
     */
    @PostMapping("/get/cardbind")
    public Object getCardBindByAgrNo(@RequestBody CardBind cardBind) {
        log.info("|根据协议号查询绑定信息|接收到请求报文:{}", cardBind);
        cardBind = cardBindService.getCardBindByAgrNo(cardBind);
        return ResponseUtil.ok(cardBind);
    }

}
