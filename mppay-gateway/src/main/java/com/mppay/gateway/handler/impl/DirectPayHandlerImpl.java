package com.mppay.gateway.handler.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.BankCode;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.PrePayOrder;
import com.mppay.service.service.IPrePayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 订单支付
 */
@Service("directPayHandler")
@Slf4j
public class DirectPayHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private IPrePayOrderService prePayOrderService;

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        String prePayNo = (String) requestMsg.get("prePayNo");
        String mercId = (String) requestMsg.get("mercId");
        String bankCode = (String) requestMsg.get("bankCode");

        PrePayOrder prePayOrder = prePayOrderService.getOne(new QueryWrapper<PrePayOrder>().eq("pre_pay_no", prePayNo));

        if (null == prePayOrder) {
            throw new BusiException(11117);
        }

        String expTime = DateTimeUtil.date14();
        String orderExpTime = prePayOrder.getOrderExpTime();

        //判断预支付号是否超时
        if (expTime.compareTo(orderExpTime) >= 0) {
            throw new BusiException(11007);
        }

        prePayOrder.setId(null);
        prePayOrder.setCreateTime(null);
        prePayOrder.setUpdateTime(null);
        prePayOrder.setClientIp(null);
        prePayOrder.setSysCnl(null);
        prePayOrder.setTradeType(null);
        if (null != requestMsg.get("platform")) {
            prePayOrder.setPlatform(requestMsg.get("platform").toString());
        }

        String bankCardType = (String) requestMsg.get("bankCardType");
        if (StringUtils.isBlank(bankCardType)) {
            bankCardType = "08";
        }
        requestMsg.put("bankCardType", bankCardType);

        Map<String, Object> params = BeanMap.create(prePayOrder);
        requestMsg.putAll(params);


    }


}
