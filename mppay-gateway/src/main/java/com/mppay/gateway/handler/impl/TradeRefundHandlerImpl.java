package com.mppay.gateway.handler.impl;

import com.alibaba.fastjson.JSON;
import com.mppay.core.constant.TradeType;
import com.mppay.core.utils.MapUtil;
import com.mppay.gateway.handler.QuickBusiHandler;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
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
import java.util.Map;

/**
 * 交易订单退款业务处理类
 *
 */
@Service("tradeRefundHandler")
@Slf4j
public class TradeRefundHandlerImpl implements UnifiedHandler {

	@Autowired
	private ITradeRefundService tradeRefundService;
	
	@Autowired
	private ITradeOrderService tradeOrderService;
	
	@Override
	public ResponseMsg execute(RequestMsg requestMsg) throws Exception {
		log.info("|退款订单处理|开始，参数：{}", JSON.toJSONString(requestMsg));
		TradeRefund tradeRefund = tradeRefundService.getOne(new QueryWrapper<TradeRefund>().eq("refund_no", requestMsg.get("refundNo")));
		if (null == tradeRefund) {
			return null;
		}
		TradeOrder tradeOrder = tradeOrderService.getOne(new QueryWrapper<TradeOrder>().eq("trade_no", tradeRefund.getTradeNo()));
		if (null == tradeOrder) {
			return null;
		}
		String tradeType = tradeOrder.getTradeType();
		requestMsg.put("platform", tradeOrder.getPlatform());
		requestMsg.put("routeCode",tradeRefund.getRouteCode());
		requestMsg.put("tradeType",tradeRefund.getTradeType());
		requestMsg.put("tradeNo",tradeRefund.getTradeNo());
		requestMsg.put("price",tradeRefund.getPrice());
		requestMsg.put("applyPrice",tradeRefund.getApplyPrice());
		requestMsg.put("outTradeNo",tradeRefund.getOutTradeNo());

		ResponseMsg responseMsg = new ResponseMsg();
		log.info("|退款订单处理|外部退款订单处理|开始，参数：{}", JSON.toJSONString(requestMsg));
		if(TradeType.QUICK.getId().equalsIgnoreCase(tradeType)){
			String serviceName = tradeRefund.getRouteCode().toLowerCase() + ConstEC.QUICKBUSIHANDLER;
			QuickBusiHandler quickBusiHandler = (QuickBusiHandler) SpringContextHolder.getBean(serviceName);
			if (null == quickBusiHandler) {
				log.error("serviceName[{}]业务处理服务不存在!", serviceName);
				return null;
			}
			responseMsg = quickBusiHandler.refundOrder(requestMsg);
		}else{
			String serviceName = tradeRefund.getRouteCode().toLowerCase() + ConstEC.BANKBUSIHANDLER;
			BankBusiHandler bankBusiHandler = (BankBusiHandler) SpringContextHolder.getBean(serviceName);
			if (null == bankBusiHandler) {
				log.error("serviceName[{}]业务处理服务不存在!", serviceName);
				return null;
			}
			responseMsg = bankBusiHandler.refundOrder(requestMsg);
		}
		log.info("|退款订单处理|外部退款订单处理|结束，响应：{}", JSON.toJSONString(responseMsg));
		String code = (String) responseMsg.get(ConstEC.RETURNCODE);
		
		if (StringUtils.isBlank(code)) {
			return null;
		}
		if (!ConstEC.SUCCESS_10000.equals(code)) {
			return null;
		}
		tradeRefund.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
		tradeRefund.setOutRefundNo(responseMsg.get("outRefundNo").toString());
		tradeRefund.setBankRefundNo(responseMsg.get("bankRefundNo").toString());
		tradeRefundService.updateById(tradeRefund);
		log.info("|退款订单处理|结束，响应：{}", JSON.toJSONString(responseMsg));
		return responseMsg;
	}

}
