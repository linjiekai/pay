package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.TradeOrderMppay;
import com.mppay.service.service.ITradeOrderMppayService;

import lombok.extern.slf4j.Slf4j;

@Service("mppayDepositProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class MppayDepositProcHandlerImpl extends BaseDepositProcHandlerImpl implements DepositProcHandler {

	@Autowired
	private ITradeOrderMppayService tradeOrderMppayService;
	
	@Override
	public void proc(Map<String, Object> data) throws Exception {
		TradeOrderMppay tradeOrderMppay = tradeOrderMppayService.getOne(new QueryWrapper<TradeOrderMppay>().eq("out_trade_no", data.get("outTradeNo")));
		
		if (null == tradeOrderMppay) {
			log.error("支付订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
			throw new BusiException(11115);
		}
		
		if (OrderStatus.SUCCESS.getId().equals(tradeOrderMppay.getOrderStatus())
				|| OrderStatus.REFUND_FULL.getId().equals(tradeOrderMppay.getOrderStatus())
				|| OrderStatus.REFUND_PART.getId().equals(tradeOrderMppay.getOrderStatus())) {
			log.error("支付订单号OutTradeNo[{}]状态[{}],直接返回", tradeOrderMppay.getOutTradeNo(), tradeOrderMppay.getOrderStatus());
			return;
		}
		
		if (!OrderStatus.WAIT_PAY.getId().equals(tradeOrderMppay.getOrderStatus())){
			log.error("支付订单号OutTradeNo[{}]状态[{}],异常返回",tradeOrderMppay.getOutTradeNo(), tradeOrderMppay.getOrderStatus());
			throw new BusiException(11002);
		}
		
		BigDecimal price = new BigDecimal(data.get("price").toString());
		
		if (price.compareTo(tradeOrderMppay.getPrice()) != 0){
			log.error("支付订单号OutTradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", tradeOrderMppay.getOutTradeNo(), price, tradeOrderMppay.getPrice());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}
		
		BeanUtils.populate(tradeOrderMppay, data);
		
		tradeOrderMppay.setOrderStatus(OrderStatus.SUCCESS.getId());
		
		
		boolean flag = tradeOrderMppayService.update(tradeOrderMppay, new UpdateWrapper<TradeOrderMppay>()
				.eq("out_trade_no", data.get("outTradeNo"))
				.eq("order_status", OrderStatus.WAIT_PAY.getId())
				);
		
		if (!flag) {
			log.error("支付订单号OutTradeNo[{}], 订单状态不等于[{}]", tradeOrderMppay.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
			throw new BusiException("11002", ApplicationYmlUtil.get("11002"));
		}
		
		data.put("tradeNo", tradeOrderMppay.getTradeNo());
		pre(data);
	}

	@Override
	public void procWithdr(Map<String, Object> data) throws Exception {
		
	}

}
