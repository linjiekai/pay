package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.CardBindStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.CardBind;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;

/**
 * 提现解绑
 */
@Service("unCardBindHandler")
@Slf4j
public class UnCardBindHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private ICardBindService cardBindService;

    @Autowired
	private ICipherService cipherServiceImpl;
    
    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        responseMsg.put("data", data);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);

        QueryWrapper<CardBind> cardBindQueryWrapper = new QueryWrapper<>();
        
        String agrNo = (String) requestMsg.get("agrNo");
        cardBindQueryWrapper.eq("user_oper_no", requestMsg.get("userOperNo"))
                .eq("status", CardBindStatus.BINDING.getId())
                .eq("merc_id", requestMsg.get("mercId"));
        if (StringUtils.isNotBlank(agrNo)) {
        	String agrNoCipher = (String) requestMsg.get("agrNo");
            agrNo = cipherServiceImpl.decryptAES(agrNoCipher);
            cardBindQueryWrapper.eq("agr_no", agrNo);
        } else {
            cardBindQueryWrapper.eq("bank_card_no", requestMsg.get("bankCardNo"))
                    .eq("bank_code", requestMsg.get("bankCode"));
        }
        CardBind cardBind = cardBindService.getOne(cardBindQueryWrapper);

        if (null == cardBind) {
            log.error("银行卡号未绑定或状态不正确：{}", JSON.toJSONString(requestMsg));
            throw new BusiException(15002);
        }

        cardBind.setStatus(CardBindStatus.UNBINDING.getId());
        cardBindService.updateById(cardBind);

        return;
    }

}
