package com.mppay.gateway.handler.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mppay.core.constant.TradeCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.BankCode;
import com.mppay.core.constant.BusiType;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.TradeOrderMppay;
import com.mppay.service.service.ITradeOrderMppayService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户充值
 * 
 */
@Service("depositRechargeHandler")
@Slf4j
public class DepositRechargeHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private ITradeOrderMppayService tradeOrderMppayService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

		String busiType = (String) requestMsg.get("busiType");
		String bankCode = (String) requestMsg.get("bankCode");
		String tradeCode = (String) requestMsg.get("tradeCode");
		
		if (!(BusiType.CONSUME.getId().equals(busiType) || BusiType.INCOME.getId().equals(busiType) || BusiType.CASH.getId().equals(busiType) || BusiType.SUBTRACT.getId().equals(busiType) || BusiType.SUBTRACTSCT.getId().equals(busiType))) {
			log.info("busiType类型不正确" + requestMsg.toString());
			return;
		}

		if (BusiType.CASH.getId().equals(busiType) && !TradeCode.ADJUSTMENT.getId().equals(tradeCode)) {
			// 调账校验
			log.info("busiType类型不正确" + requestMsg.toString());
			return;
		}
		
		if (!BankCode.MPPAY.getId().equals(bankCode)) {
			log.info("bankCode类型不正确" + requestMsg.toString());
			return;
		}
		
		TradeOrderMppay tradeOrderMppay = tradeOrderMppayService.getOne(new QueryWrapper<TradeOrderMppay>().eq("out_trade_no", requestMsg.get("outTradeNo")));
		
		if (null == tradeOrderMppay) {
			log.error("交易流水不存在" + requestMsg.toString());
			throw new BusiException("11115", ApplicationYmlUtil.get("11115"));
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("bankCode", BankCode.MPPAY.getId());
		data.put("routeCode", RouteCode.MPPAY.getId());
		data.put("openId", tradeOrderMppay.getOpenId());
		data.put("appId", tradeOrderMppay.getAppId());
		data.put("tradeType", tradeOrderMppay.getTradeType());
		data.put("fundBank", "MPPAYACCOUNT");
		data.put("payDate", DateTimeUtil.formatTimestamp2String(new Date(), "yyyy-MM-dd"));
		data.put("payTime", DateTimeUtil.formatTimestamp2String(new Date(), "HH:mm:ss"));
		data.put("outTradeNo", tradeOrderMppay.getOutTradeNo());
		data.put("bankTradeNo", tradeOrderMppay.getBankTradeNo());
		data.put("price", tradeOrderMppay.getPrice());
		data.put("returnCode", "10000");
		data.put("returnMsg", "交易成功");
		
		DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.MPPAY.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
		handler.proc(data);
		
		
		tradeOrderMppay = tradeOrderMppayService.getOne(new QueryWrapper<TradeOrderMppay>().eq("out_trade_no", requestMsg.get("outTradeNo")));
		if (!tradeOrderMppay.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
			throw new BusiException("11002", ApplicationYmlUtil.get("11002"));
		}
		data = new HashMap<String, Object>();
		data.put("mercId", requestMsg.get("mercId"));
		data.put("userId", requestMsg.get("userId"));
		data.put("orderNo", requestMsg.get("orderNo"));
		data.put("bankCode", BankCode.MPPAY.getId());
		data.put("openId", tradeOrderMppay.getOpenId());
		data.put("payDate", tradeOrderMppay.getPayDate());
		data.put("payTime", tradeOrderMppay.getPayTime());
		data.put("payNo", tradeOrderMppay.getOutTradeNo());
		data.put("price", tradeOrderMppay.getPrice());
		data.put("orderStatus", tradeOrderMppay.getOrderStatus());
		data.put("tradeType", tradeOrderMppay.getTradeType());
		
		responseMsg.put(ConstEC.DATA, data);
	}

}
