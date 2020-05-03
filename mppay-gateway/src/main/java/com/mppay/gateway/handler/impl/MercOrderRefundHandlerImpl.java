package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mppay.core.utils.DateUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.constant.TradeCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.MercOrderRefund;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.entity.TradeRefund;
import com.mppay.service.service.IMercOrderRefundService;
import com.mppay.service.service.IMercOrderService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderService;
import com.mppay.service.service.ITradeRefundService;

import lombok.extern.slf4j.Slf4j;

@Service("mercOrderRefundHandler")
@Slf4j
public class MercOrderRefundHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private IMercOrderService mercOrderService;
	
	@Autowired
	private IMercOrderRefundService mercOrderRefundService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	@Autowired
    protected ITradeRefundService tradeRefundService;
	
	@Autowired
    protected ITradeOrderService tradeOrderService;
	
	@Transactional(rollbackFor=Exception.class)
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>().eq("order_no", requestMsg.get("orderNo")).eq("merc_id", requestMsg.get("mercId")));
		
		if (null == mercOrder) {
			log.error("订单不存在,退款失败,{}", requestMsg);
			throw new BusiException(11003);
		}
		
		MercOrderRefund mercOrderRefund = mercOrderRefundService.getOne(new QueryWrapper<MercOrderRefund>()
				.select("refund_order_no", "order_no", "merc_id", "price", "order_status")
				.eq("refund_order_no", requestMsg.get("refundOrderNo"))
				.eq("merc_id", requestMsg.get("mercId"))
				);

		if (null != mercOrderRefund) {
			responseMsg.put("data", mercOrderRefund);
	        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
	        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	        return;
		}

		BigDecimal price = new BigDecimal(requestMsg.get("price").toString());

		if (mercOrder.getPrice().compareTo(price.add(mercOrder.getRefundPrice())) < 0) {
			log.error("退款失败,退款订单金额不能大于订单金额{}", requestMsg);
			throw new BusiException(11100);
		}
		
		if (!(mercOrder.getOrderStatus().equals(OrderStatus.SUCCESS.getId()) || mercOrder.getOrderStatus().equals(OrderStatus.REFUND_PART.getId()))) {
			log.error("订单状态不正确:{}", requestMsg);
			throw new BusiException(11002);
		}

		String orderStatus = OrderStatus.REFUND_FULL.getId();
		//部分退款
		if (mercOrder.getPrice().compareTo(price.add(mercOrder.getRefundPrice())) > 0) {
			orderStatus = OrderStatus.REFUND_PART.getId();
		}
		//交易订单改状态
		boolean flag = mercOrderService.update(new MercOrder(), new UpdateWrapper<MercOrder>()
				.setSql("order_status= '" + orderStatus + "', refund_price=refund_price + " + price)
				.eq("merc_order_no", mercOrder.getMercOrderNo())
				.last(" and price >= refund_price + " + price)
				);
		
		if (!flag) {
			log.error("退款失败,退款订单金额不能大于订单金额{}", requestMsg);
			throw new BusiException(11100);
		}
		
		String mercRefundNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.MERC_REFUND_NO.getId(), SeqIncrType.MERC_REFUND_NO.getLength(), Align.LEFT);
		
		mercOrderRefund = new MercOrderRefund();
		
		BeanUtils.populate(mercOrderRefund, requestMsg.getMap());
		
		mercOrderRefund.setMercRefundNo(mercRefundNo);
		mercOrderRefund.setMercOrderNo(mercOrder.getMercOrderNo());
		mercOrderRefund.setOrderNo(mercOrder.getOrderNo());
		mercOrderRefund.setOrderStatus(OrderStatus.REFUND.getId());
		mercOrderRefund.setOutTradeNo(mercOrder.getOutTradeNo());
		mercOrderRefund.setUserId(mercOrder.getUserId());
		mercOrderRefund.setUserOperNo(mercOrder.getUserOperNo());
		mercOrderRefund.setRefundDate(DateUtil.dateFormat(new Date(),DateUtil.DATE_PATTERN));
		mercOrderRefund.setRefundTime(DateUtil.dateFormat(new Date(),DateUtil.TIME_PATTERN));
		mercOrderRefund.setBankCode(mercOrder.getBankCode());
		mercOrderRefundService.save(mercOrderRefund);
		
        TradeOrder tradeOrder = tradeOrderService.getOne(new QueryWrapper<TradeOrder>()
        		.eq("out_trade_no", mercOrder.getOutTradeNo())
        		.eq("merc_order_no", mercOrder.getMercOrderNo())
        		);

        if (null == tradeOrder) {
        	log.error("订单不存在,退款失败,{}", requestMsg);
			throw new BusiException(11003);
        }
        
        if (!(tradeOrder.getOrderStatus().equals(OrderStatus.SUCCESS.getId()) || tradeOrder.getOrderStatus().equals(OrderStatus.REFUND_PART.getId()))) {
			log.error("充值订单状态不正确,退款失败 tradeNo:{},请求参数requestMsg:{}", tradeOrder.getTradeNo(), requestMsg);
			throw new BusiException(11002);
		}

		//退款交易单
		TradeRefund tradeRefund = new TradeRefund();
		tradeRefund.setMercRefundNo(mercRefundNo);
		tradeRefund.setMercOrderNo(tradeOrder.getMercOrderNo());
        tradeRefund.setMercId(tradeOrder.getMercId());
        tradeRefund.setOutTradeNo(tradeOrder.getOutTradeNo());
        tradeRefund.setTradeNo(tradeOrder.getTradeNo());
        tradeRefund.setBankTradeNo(tradeOrder.getBankTradeNo());
        tradeRefund.setBankCode(tradeOrder.getBankCode());
        tradeRefund.setRouteCode(tradeOrder.getRouteCode());
        tradeRefund.setFundBank(tradeOrder.getFundBank());
        tradeRefund.setTradeType(tradeOrder.getTradeType());
        tradeRefund.setRefundDate(DateTimeUtil.date10());
        tradeRefund.setRefundTime(DateTimeUtil.time8());
        tradeRefund.setTradeCode(TradeCode.TRADEREFUND.getId());
        // 退款登记
        tradeRefund.setOrderStatus(OrderStatus.REFUND.getId());
        tradeRefund.setApplyPrice(tradeOrder.getPrice());
        tradeRefund.setPrice(tradeOrder.getPrice());

        String refundNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REFUND_NO.getId(), SeqIncrType.REFUND_NO.getLength(), Align.LEFT);

        log.info("交易订单号outTradeNo[{}]发起退款,退款流水号refundNo[{}]", tradeOrder.getOutTradeNo(), refundNo);

        tradeRefund.setRefundNo(refundNo);
        tradeRefundService.save(tradeRefund);
		
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("refundOrderNo", mercOrderRefund.getRefundOrderNo());
        data.put("orderNo", mercOrderRefund.getOrderNo());
        data.put("mercId", mercOrderRefund.getMercId());
        data.put("price", mercOrderRefund.getPrice());
        data.put("orderStatus", mercOrderRefund.getOrderStatus());

        responseMsg.put("data", data);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
