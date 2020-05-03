package com.mppay.gateway.handler.quick;

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
import com.mppay.gateway.handler.impl.BaseDepositProcHandlerImpl;
import com.mppay.service.entity.TradeOrderGaohuitong;
import com.mppay.service.entity.WithdrOrderGaohuitong;
import com.mppay.service.service.ITradeOrderGaohuitongService;
import com.mppay.service.service.IWithdrOrderGaohuitongService;

import lombok.extern.slf4j.Slf4j;

@Service("gaohuitonghkDepositProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class GaohuitonghkDepositProcHandlerImpl extends BaseDepositProcHandlerImpl implements DepositProcHandler {

	@Autowired
	private ITradeOrderGaohuitongService tradeOrderGaohuitongService;
	
	@Autowired
	private IWithdrOrderGaohuitongService withdrOrderGaohuitongService;
	
	//提现资金业务处理
	@Override
	public void proc(Map<String, Object> data) throws Exception {
		TradeOrderGaohuitong tradeOrderGaohuitong = tradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>().eq("out_trade_no", data.get("outTradeNo")));

		if (null == tradeOrderGaohuitong) {
			log.error("支付订单号OutTradeNo[{}]不存在，异常返回", data.get("outTradeNo"));
			throw new BusiException(11115);
		}

		if (OrderStatus.SUCCESS.getId().equals(tradeOrderGaohuitong.getOrderStatus())
				|| OrderStatus.REFUND_FULL.getId().equals(tradeOrderGaohuitong.getOrderStatus())
				|| OrderStatus.REFUND_PART.getId().equals(tradeOrderGaohuitong.getOrderStatus())) {
			log.error("支付订单号OutTradeNo[{}]状态[{}],直接返回", tradeOrderGaohuitong.getOutTradeNo(), tradeOrderGaohuitong.getOrderStatus());
			return;
		}

		if (!OrderStatus.WAIT_PAY.getId().equals(tradeOrderGaohuitong.getOrderStatus())){
			log.error("支付订单号OutTradeNo[{}]状态[{}],异常返回",tradeOrderGaohuitong.getOutTradeNo(), tradeOrderGaohuitong.getOrderStatus());
			throw new BusiException(11002);
		}

		BigDecimal price = new BigDecimal(data.get("price").toString());

		if (price.compareTo(tradeOrderGaohuitong.getPrice()) != 0){
			log.error("支付订单号OutTradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", tradeOrderGaohuitong.getOutTradeNo(), price, tradeOrderGaohuitong.getPrice());
			throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
		}

		BeanUtils.populate(tradeOrderGaohuitong, data);

		tradeOrderGaohuitong.setOrderStatus(OrderStatus.SUCCESS.getId());

		boolean flag = tradeOrderGaohuitongService.update(tradeOrderGaohuitong, new UpdateWrapper<TradeOrderGaohuitong>()
				.eq("out_trade_no", data.get("outTradeNo"))
				.eq("order_status", OrderStatus.WAIT_PAY.getId())
		);

		if (!flag) {
			log.error("支付订单号OutTradeNo[{}], 订单状态不等于[{}]", tradeOrderGaohuitong.getOutTradeNo(), OrderStatus.WAIT_PAY.getId());
			throw new BusiException(11002);
		}

		data.put("tradeNo", tradeOrderGaohuitong.getTradeNo());

		pre(data);

	}
	@Override
	public void procWithdr(Map<String, Object> data) throws Exception {
		WithdrOrderGaohuitong withdrOrderGaohuitong = withdrOrderGaohuitongService.getOne(new QueryWrapper<WithdrOrderGaohuitong>().eq("out_trade_no", data.get("outTradeNo")));
		
		if (null == withdrOrderGaohuitong) {
			log.info("提现订单号不存在，OutTradeNo：{}，", data.get("outTradeNo"));
			return;
		}
		if (!WithdrOrderStatus.BANK_WAIT.getId().equalsIgnoreCase(withdrOrderGaohuitong.getOrderStatus())) {
			log.info("提现订单号状态异常，OutTradeNo：{}，orderStatus：{}", data.get("outTradeNo"),withdrOrderGaohuitong.getOrderStatus());
			return;
		}

		BeanUtils.populate(withdrOrderGaohuitong, data);
		data.put("withdrOrderNo", withdrOrderGaohuitong.getWithdrOrderNo());
		//资金处理前的处理
		preWithdr(data);
	}

}
