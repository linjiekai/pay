package com.mppay.gateway.handler.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一订单处理
 * @author chenfeihang
 *
 */
@Service("unifiedOrderHandler")
@Slf4j
public class UnifiedOrderHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {
	
	@Autowired
	private ITradeOrderService tradeOrderService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		log.info("|交易订单处理|开始，参数：{}", JSON.toJSONString(requestMsg));
		TradeOrder tradeOrder = new TradeOrder();
		String tradeNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.TRADE_NO.getId(), SeqIncrType.TRADE_NO.getLength(), Align.LEFT);
		requestMsg.put("tradeNo", tradeNo);
		
		BeanUtils.populate(tradeOrder, requestMsg.getMap());  
		tradeOrder.setTradeDate(DateTimeUtil.date10());
		tradeOrder.setTradeTime(DateTimeUtil.time8());
		tradeOrder.setOrderStatus(OrderStatus.ADVANCE.getId());
		tradeOrderService.save(tradeOrder);
		log.info("|交易订单处理|入库 tradeNo：{}",tradeNo);
		//拼出service name
		String serviceName = tradeOrder.getRouteCode().toLowerCase() + ConstEC.BANKBUSIHANDLER;

		log.info("|交易订单处理|外部订单业务开始，routeCode：{}",tradeOrder.getRouteCode());
		BankBusiHandler bankBusiHandler = (BankBusiHandler) SpringContextHolder.getBean(serviceName);
		if (null == bankBusiHandler) {
			log.error("serviceName[{}]业务处理服务不存在!", serviceName);
			throw new BusiException(11114);
		}

		bankBusiHandler.unifiedOrder(requestMsg, responseMsg);
		log.info("|交易订单处理|外部订单业务结束，{}",JSON.toJSONString(responseMsg));

		String returnCode = (String) responseMsg.get(ConstEC.RETURNCODE);
		String returnMsg = (String) responseMsg.get(ConstEC.RETURNMSG);
		if (StringUtils.isBlank(returnCode)) {
			log.info("|交易订单处理|失败，returnCode：{},returnMsg:{}",returnCode,returnMsg);
			throw new BusiException(11001);
		}
		if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
			log.info("|交易订单处理|失败，returnCode：{},returnMsg:{}",returnCode,returnMsg);
			throw new BusiException(returnCode, returnMsg);
		}
		//将交易订单表状态改为W
		tradeOrder.setOrderStatus(OrderStatus.WAIT_PAY.getId());
		tradeOrder.setOutTradeNo((String) responseMsg.get("outTradeNo"));
		tradeOrder.setReturnCode(returnCode);
		tradeOrder.setReturnMsg(returnMsg);
		tradeOrderService.updateById(tradeOrder);
		log.info("|交易订单处理|完成，更新状态为W, tradeNo：{}",tradeNo);
	}
}
