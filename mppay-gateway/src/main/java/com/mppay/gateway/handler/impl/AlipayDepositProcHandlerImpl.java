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
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.TradeOrderAlipay;
import com.mppay.service.entity.WithdrOrderAlipay;
import com.mppay.service.service.ITradeOrderAlipayService;
import com.mppay.service.service.IWithdrOrderAlipayService;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;

@Service("alipayDepositProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AlipayDepositProcHandlerImpl extends BaseDepositProcHandlerImpl implements DepositProcHandler {

	@Autowired
	private ITradeOrderAlipayService tradeOrderAlipayService;
	
	@Autowired
	private IWithdrOrderAlipayService withdrOrderAlipayService;
	
	@Autowired
	private ICipherService cipherServiceImpl;
	
	@Override
	public void proc(Map<String, Object> data) throws Exception {
		TradeOrderAlipay tradeOrderAlipay = tradeOrderAlipayService.getOne(new QueryWrapper<TradeOrderAlipay>().eq("out_trade_no", data.get("outTradeNo")));
		
		if (null == tradeOrderAlipay) {
			log.error("支付订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
			throw new BusiException(11115);
		}
		
		if (OrderStatus.SUCCESS.getId().equals(tradeOrderAlipay.getOrderStatus())
				|| OrderStatus.REFUND_FULL.getId().equals(tradeOrderAlipay.getOrderStatus())
				|| OrderStatus.REFUND_PART.getId().equals(tradeOrderAlipay.getOrderStatus())) {
			log.error("支付订单号OutTradeNo[{}]状态[{}],直接返回", tradeOrderAlipay.getOutTradeNo(), tradeOrderAlipay.getOrderStatus());
			return;
		}
		
		if (!OrderStatus.WAIT_PAY.getId().equals(tradeOrderAlipay.getOrderStatus())){
			log.error("支付订单号OutTradeNo[{}]状态[{}],异常返回",tradeOrderAlipay.getOutTradeNo(), tradeOrderAlipay.getOrderStatus());
			throw new BusiException(11002);
		}
		
		BigDecimal price = new BigDecimal(data.get("price").toString());
		
		if (price.compareTo(tradeOrderAlipay.getPrice()) != 0){
			log.error("支付订单号OutTradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", tradeOrderAlipay.getOutTradeNo(), price, tradeOrderAlipay.getPrice());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}
		
		String openId = (String) data.get("openId");
		//AES加密
		openId = cipherServiceImpl.encryptAES(openId);
		data.put("openId", openId);
		
		BeanUtils.populate(tradeOrderAlipay, data);
		
		tradeOrderAlipay.setOrderStatus(OrderStatus.SUCCESS.getId());
		
		boolean flag = tradeOrderAlipayService.update(tradeOrderAlipay, new UpdateWrapper<TradeOrderAlipay>()
				.eq("out_trade_no", data.get("outTradeNo"))
				.eq("order_status", OrderStatus.WAIT_PAY.getId())
				);
		
		if (!flag) {
			log.error("支付订单号OutTradeNo[{}], 订单状态不等于[{}]", tradeOrderAlipay.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}
		
		data.put("tradeNo", tradeOrderAlipay.getTradeNo());
		pre(data);
		
	}

	@Override
	public void procWithdr(Map<String, Object> data) throws Exception {
		WithdrOrderAlipay withdrOrderAlipay = withdrOrderAlipayService.getOne(new QueryWrapper<WithdrOrderAlipay>().eq("out_trade_no", data.get("outTradeNo")));
		
		if (null == withdrOrderAlipay) {
			log.error("提现订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
			throw new BusiException(11115);
		}
		
		if (!WithdrOrderStatus.BANK_WAIT.getId().equals(withdrOrderAlipay.getOrderStatus())){
			log.error("提现订单号OutTradeNo[{}]状态[{}],异常返回",withdrOrderAlipay.getOutTradeNo(), withdrOrderAlipay.getOrderStatus());
			throw new BusiException(11002);
		}
		
		BeanUtils.populate(withdrOrderAlipay, data);
		
		boolean flag = withdrOrderAlipayService.update(withdrOrderAlipay, new UpdateWrapper<WithdrOrderAlipay>()
				.eq("out_trade_no", data.get("outTradeNo"))
				.eq("order_status", WithdrOrderStatus.BANK_WAIT.getId())
				);
		
		if (!flag) {
			log.error("支付订单号OutTradeNo[{}], 订单状态不等于[{}]", withdrOrderAlipay.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}
		
		data.put("withdrOrderNo", withdrOrderAlipay.getWithdrOrderNo());
		preWithdr(data);
	}

}
