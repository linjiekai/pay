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
import com.mppay.service.entity.TradeOrderWeixin;
import com.mppay.service.entity.WithdrOrderWeixin;
import com.mppay.service.service.ITradeOrderWeixinService;
import com.mppay.service.service.IWithdrOrderWeixinService;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信交易资金处理 
 *
 */
@Service("weixinDepositProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class WeixinDepositProcHandlerImpl extends BaseDepositProcHandlerImpl implements DepositProcHandler {

	@Autowired
	private ITradeOrderWeixinService tradeOrderWeixinService;
	
	@Autowired
	private IWithdrOrderWeixinService withdrOrderWeixinService;
	
	@Autowired
	private ICipherService cipherServiceImpl;
	
	@Override
	public void proc(Map<String, Object> data) throws Exception {
		TradeOrderWeixin tradeOrderWeixin = tradeOrderWeixinService.getOne(new QueryWrapper<TradeOrderWeixin>().eq("out_trade_no", data.get("outTradeNo")));
		
		if (null == tradeOrderWeixin) {
			log.error("支付订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
			throw new BusiException(11115);
		}
		
		if (OrderStatus.SUCCESS.getId().equals(tradeOrderWeixin.getOrderStatus())
				|| OrderStatus.REFUND_FULL.getId().equals(tradeOrderWeixin.getOrderStatus())
				|| OrderStatus.REFUND_PART.getId().equals(tradeOrderWeixin.getOrderStatus())) {
			log.error("支付订单号OutTradeNo[{}]订单状态[{}],直接返回", tradeOrderWeixin.getOutTradeNo(), tradeOrderWeixin.getOrderStatus());
			return;
		}
		
		if (!OrderStatus.WAIT_PAY.getId().equals(tradeOrderWeixin.getOrderStatus())){
			log.error("支付订单号OutTradeNo[{}]订单状态不正确[{}],异常返回",tradeOrderWeixin.getOutTradeNo(), tradeOrderWeixin.getOrderStatus());
			throw new BusiException(11002);
		}
		
		BigDecimal price = new BigDecimal(data.get("price").toString());
		
		if (price.compareTo(tradeOrderWeixin.getPrice()) != 0){
			log.error("支付订单号OutTradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", tradeOrderWeixin.getOutTradeNo(), price, tradeOrderWeixin.getPrice());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}
		
		String openId = (String) data.get("openId");
		//AES加密
		openId = cipherServiceImpl.encryptAES(openId);
		data.put("openId", openId);
		
//		tradeOrderWeixin = new TradeOrderWeixin();
		BeanUtils.populate(tradeOrderWeixin, data);
		
		tradeOrderWeixin.setOrderStatus(OrderStatus.SUCCESS.getId());
		
//		tradeOrderWeixinService.updateById(tradeOrderWeixin);
		
		tradeOrderWeixin.setId(null);
		tradeOrderWeixin.setCreateTime(null);
		tradeOrderWeixin.setUpdateTime(null);
		boolean flag = tradeOrderWeixinService.update(tradeOrderWeixin, new UpdateWrapper<TradeOrderWeixin>()
				.eq("out_trade_no", data.get("outTradeNo"))
				.eq("order_status", OrderStatus.WAIT_PAY.getId())
				);
		
		
		if (!flag) {
			log.error("支付订单号OutTradeNo[{}], 订单状态不等于[{}]", tradeOrderWeixin.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
			throw new BusiException("11002", ApplicationYmlUtil.get("11002"));
		}
		data.put("tradeNo", tradeOrderWeixin.getTradeNo());
		pre(data);
	}

	@Override
	public void procWithdr(Map<String, Object> data) throws Exception {
		WithdrOrderWeixin withdrOrderWeixin = withdrOrderWeixinService.getOne(new QueryWrapper<WithdrOrderWeixin>().eq("out_trade_no", data.get("outTradeNo")));
		
		if (null == withdrOrderWeixin) {
			log.error("提现订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
			throw new BusiException(11115);
		}
		
		if (!WithdrOrderStatus.BANK_WAIT.getId().equals(withdrOrderWeixin.getOrderStatus())){
			log.error("提现订单号OutTradeNo[{}]状态[{}],异常返回",withdrOrderWeixin.getOutTradeNo(), withdrOrderWeixin.getOrderStatus());
			throw new BusiException(11002);
		}
		
		BigDecimal price = new BigDecimal(data.get("price").toString());
		
		if (price.compareTo(withdrOrderWeixin.getPrice()) != 0){
			log.error("支付订单号OutTradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", withdrOrderWeixin.getOutTradeNo(), price, withdrOrderWeixin.getPrice());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}
		
		BeanUtils.populate(withdrOrderWeixin, data);
		
		boolean flag = withdrOrderWeixinService.update(withdrOrderWeixin, new UpdateWrapper<WithdrOrderWeixin>()
				.eq("out_trade_no", data.get("outTradeNo"))
				.eq("order_status", WithdrOrderStatus.BANK_WAIT.getId())
				);
		
		if (!flag) {
			log.error("提现订单号OutTradeNo[{}], 订单状态不等于[{}]", withdrOrderWeixin.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}
		
		data.put("withdrOrderNo", withdrOrderWeixin.getWithdrOrderNo());
		preWithdr(data);
	}

}
