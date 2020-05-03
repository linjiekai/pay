package com.mppay.gateway.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.TradeOrderGaohuitong;
import com.mppay.service.entity.TradeOrderSheenPay;
import com.mppay.service.entity.WithdrOrderGaohuitong;
import com.mppay.service.service.ITradeOrderGaohuitongService;
import com.mppay.service.service.ITradeOrderSheenPayService;
import com.mppay.service.service.IWithdrOrderGaohuitongService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service("sheenpayhkDepositProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class SheenpayhkDepositProcHandlerImpl extends BaseDepositProcHandlerImpl implements DepositProcHandler {

    @Autowired
    private ITradeOrderSheenPayService iTradeOrderSheenPayService;

    @Override
    public void proc(Map<String, Object> data) throws Exception {
        TradeOrderSheenPay tradeOrderSheenpay = iTradeOrderSheenPayService.getOne(new QueryWrapper<TradeOrderSheenPay>().eq("out_trade_no", data.get("outTradeNo")));

        if (null == tradeOrderSheenpay) {
            log.error("支付订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
            throw new BusiException(11115);
        }

        //快捷支付openId以流水表为准
        data.put("openId", tradeOrderSheenpay.getOpenId());

        if (OrderStatus.SUCCESS.getId().equals(tradeOrderSheenpay.getOrderStatus())
                || OrderStatus.REFUND_FULL.getId().equals(tradeOrderSheenpay.getOrderStatus())
                || OrderStatus.REFUND_PART.getId().equals(tradeOrderSheenpay.getOrderStatus())) {
            log.error("支付订单号OutTradeNo[{}]状态[{}],直接返回", tradeOrderSheenpay.getOutTradeNo(), tradeOrderSheenpay.getOrderStatus());
            return;
        }

        if (!OrderStatus.WAIT_PAY.getId().equals(tradeOrderSheenpay.getOrderStatus())) {
            log.error("支付订单号OutTradeNo[{}]状态[{}],异常返回", tradeOrderSheenpay.getOutTradeNo(), tradeOrderSheenpay.getOrderStatus());
            throw new BusiException(11002);
        }

        BigDecimal price = new BigDecimal(data.get("price").toString());

        if (price.compareTo(tradeOrderSheenpay.getPrice()) != 0) {
            log.error("支付订单号OutTradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", tradeOrderSheenpay.getOutTradeNo(), price, tradeOrderSheenpay.getPrice());
            throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
        }

        BeanUtils.populate(tradeOrderSheenpay, data);

        tradeOrderSheenpay.setOrderStatus(OrderStatus.SUCCESS.getId());

        boolean flag = iTradeOrderSheenPayService.update(tradeOrderSheenpay, new UpdateWrapper<TradeOrderSheenPay>()
                .eq("out_trade_no", data.get("outTradeNo"))
                .eq("order_status", OrderStatus.WAIT_PAY.getId())
        );

        if (!flag) {
            log.error("支付订单号OutTradeNo[{}], 订单状态不等于[{}]", tradeOrderSheenpay.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
            throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
        }

        data.put("tradeNo", tradeOrderSheenpay.getTradeNo());
        pre(data);

    }

    //提现资金业务处理
    @Override
    public void procWithdr(Map<String, Object> data) throws Exception {

    }

}
