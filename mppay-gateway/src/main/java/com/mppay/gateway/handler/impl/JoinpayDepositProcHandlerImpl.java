package com.mppay.gateway.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.TradeOrderJoinpay;
import com.mppay.service.entity.TradeOrderSheenPay;
import com.mppay.service.service.ITradeOrderJoinpayService;
import com.mppay.service.service.ITradeOrderSheenPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service("joinpayDepositProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class JoinpayDepositProcHandlerImpl extends BaseDepositProcHandlerImpl implements DepositProcHandler {

    @Autowired
    private ITradeOrderJoinpayService iTradeOrderJoinpayService;

    @Override
    public void proc(Map<String, Object> data) throws Exception {
        TradeOrderJoinpay joinpayServiceOne = iTradeOrderJoinpayService.getOne(new QueryWrapper<TradeOrderJoinpay>().eq("out_trade_no", data.get("outTradeNo")));

        if (null == joinpayServiceOne) {
            log.error("支付订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
            throw new BusiException(11115);
        }

        if (OrderStatus.SUCCESS.getId().equals(joinpayServiceOne.getOrderStatus())
                || OrderStatus.REFUND_FULL.getId().equals(joinpayServiceOne.getOrderStatus())
                || OrderStatus.REFUND_PART.getId().equals(joinpayServiceOne.getOrderStatus())) {
            log.error("支付订单号OutTradeNo[{}]状态[{}],直接返回", joinpayServiceOne.getOutTradeNo(), joinpayServiceOne.getOrderStatus());
            return;
        }

        if (!OrderStatus.WAIT_PAY.getId().equals(joinpayServiceOne.getOrderStatus())) {
            log.error("支付订单号OutTradeNo[{}]状态[{}],异常返回", joinpayServiceOne.getOutTradeNo(), joinpayServiceOne.getOrderStatus());
            throw new BusiException(11002);
        }

        BigDecimal price = new BigDecimal(data.get("price").toString());

        if (price.compareTo(joinpayServiceOne.getPrice()) != 0) {
            log.error("支付订单号OutTradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", joinpayServiceOne.getOutTradeNo(), price, joinpayServiceOne.getPrice());
            throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
        }

        BeanUtils.populate(joinpayServiceOne, data);

        joinpayServiceOne.setOrderStatus(OrderStatus.SUCCESS.getId());

        boolean flag = iTradeOrderJoinpayService.update(joinpayServiceOne, new UpdateWrapper<TradeOrderJoinpay>()
                .eq("out_trade_no", data.get("outTradeNo"))
                .eq("order_status", OrderStatus.WAIT_PAY.getId())
        );

        if (!flag) {
            log.error("支付订单号OutTradeNo[{}], 订单状态不等于[{}]", joinpayServiceOne.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
            throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
        }

        data.put("tradeNo", joinpayServiceOne.getTradeNo());
        pre(data);

    }

    //提现资金业务处理
    @Override
    public void procWithdr(Map<String, Object> data) throws Exception {

    }

}
