package com.mppay.gateway.handler.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.constant.TradeType;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.MercOrderRefund;
import com.mppay.service.service.IMercOrderRefundService;
import com.mppay.service.service.IMercOrderService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.entity.TradeRefund;
import com.mppay.service.service.ITradeOrderService;
import com.mppay.service.service.ITradeRefundService;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 交易订单退款查询业务处理类
 */
@Service("tradeRefundQueryHandler")
@Slf4j
public class TradeRefundQueryHandlerImpl implements UnifiedHandler {

    @Autowired
    private ITradeRefundService tradeRefundService;

    @Autowired
    private ITradeOrderService tradeOrderService;


    @Override
    public ResponseMsg execute(RequestMsg requestMsg) throws Exception {
        log.info("|退款订单处理结果查询|开始，参数：{}", JSON.toJSONString(requestMsg));
        TradeRefund tradeRefund = tradeRefundService.getOne(new QueryWrapper<TradeRefund>().eq("refund_no", requestMsg.get("refundNo")));
        if (null == tradeRefund) {
            return null;
        }
        TradeOrder tradeOrder = tradeOrderService.getOne(new QueryWrapper<TradeOrder>().eq("trade_no", tradeRefund.getTradeNo()));
        if (null == tradeOrder) {
            return null;
        }

        requestMsg.put("platform", tradeOrder.getPlatform());
        requestMsg.put("mercId", tradeOrder.getMercId());
        requestMsg.put("outRefundNo", tradeRefund.getOutRefundNo());
        requestMsg.put("outTradeNo", tradeRefund.getOutTradeNo());
        requestMsg.put("tradeType", tradeRefund.getTradeType());
        requestMsg.put("routeCode", tradeRefund.getRouteCode());

        String tradeType = tradeOrder.getTradeType();
        ResponseMsg responseMsg = new ResponseMsg();
        log.info("|退款订单处理结果查询|外部退款订单查询处理|开始，参数：{}", JSON.toJSONString(requestMsg));
        if (TradeType.QUICK.getId().equalsIgnoreCase(tradeType)) {
            String serviceName = tradeRefund.getRouteCode().toLowerCase() + ConstEC.QUICKBUSIHANDLER;
            QuickBusiHandler quickBusiHandler = (QuickBusiHandler) SpringContextHolder.getBean(serviceName);
            if (null == quickBusiHandler) {
                log.error("serviceName[{}]业务处理服务不存在!", serviceName);
                return null;
            }
            responseMsg = quickBusiHandler.queryRefundOrder(requestMsg);
        } else {
            String serviceName = tradeRefund.getRouteCode().toLowerCase() + ConstEC.BANKBUSIHANDLER;
            BankBusiHandler bankBusiHandler = (BankBusiHandler) SpringContextHolder.getBean(serviceName);
            if (null == bankBusiHandler) {
                log.error("serviceName[{}]业务处理服务不存在!", serviceName);
                return null;
            }
            responseMsg = bankBusiHandler.queryRefundOrder(requestMsg);
        }
        log.info("|退款订单处理结果查询|外部退款订单查询处理|结束，响应：{}", JSON.toJSONString(responseMsg));
        //MPPAY 没有这些东西
        if(!RouteCode.MPPAY.getId().equalsIgnoreCase(tradeRefund.getRouteCode())){
            tradeRefund.setBankRefundNo(responseMsg.get("bankRefundNo") + "");
            tradeRefund.setBankReturnDate(responseMsg.get("bankReturnDate") + "");
            tradeRefund.setBankReturnTime(responseMsg.get("bankReturnTime") + "");
            tradeRefund.setActualPrice((BigDecimal)responseMsg.get("actualPrice"));
            tradeRefund.setOrderStatus(responseMsg.get("orderStatus") + "");
            tradeRefund.setOutRefundNo(responseMsg.get("outRefundNo") + "");
        }
        tradeRefund.setReturnCode(responseMsg.get(ConstEC.RETURNCODE) + "");
        tradeRefund.setReturnMsg(responseMsg.get(ConstEC.RETURNMSG) + "");
        tradeRefundService.updateById(tradeRefund);

        log.info("|退款订单处理结果查询|结束，响应：{}", JSON.toJSONString(responseMsg));
        return responseMsg;
    }

}
