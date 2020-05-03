package com.mppay.gateway.handler.impl;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mppay.core.constant.Align;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.service.entity.TradeOrderMppay;
import com.mppay.service.entity.TradeRefundMppay;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderMppayService;
import com.mppay.service.service.ITradeRefundMppayService;

import lombok.extern.slf4j.Slf4j;

@Service("mppayBankBusiHandler")
@Slf4j
public class MppayBankBusiHandlerImpl implements BankBusiHandler{

	@Autowired
	private ITradeOrderMppayService tradeOrderMppayService;
	
	@Autowired
	private ITradeRefundMppayService tradeRefundMppayService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	@Override
	public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		TradeOrderMppay tradeOrderMppay = new TradeOrderMppay();
		String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_MPPAY.getId(), 8, Align.LEFT);
		String outTradeNo = DateTimeUtil.date8() + seq;
		
		requestMsg.put("outTradeNo", outTradeNo);
		
		BeanUtils.populate(tradeOrderMppay, requestMsg.getMap());
		
		tradeOrderMppay.setTradeDate(DateTimeUtil.date10());
		tradeOrderMppay.setTradeTime(DateTimeUtil.time8());
		tradeOrderMppay.setBankMercId(requestMsg.get("mercId").toString());
		tradeOrderMppay.setOrderStatus(OrderStatus.WAIT_PAY.getId());
		//创建订单流水
		tradeOrderMppayService.save(tradeOrderMppay);
		
		responseMsg.put("appId", "");
		responseMsg.put("outTradeNo", outTradeNo);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, "成功");
	}

	@Override
	public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
			
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		return responseMsg;
	}

	@Override
	public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
		
		TradeRefundMppay tradeRefundMppay = new TradeRefundMppay();
		BeanUtils.populate(tradeRefundMppay, requestMsg.getMap());
		
		String seq = seqIncrService.nextVal(SeqIncrType.OUT_REFUND_NO_MPPAY.getId(), 8, Align.LEFT);
		String outRefundNo = DateTimeUtil.date8() + seq;
		
		tradeRefundMppay.setOutRefundNo(outRefundNo);
		tradeRefundMppay.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
		tradeRefundMppay.setRefundDate(DateTimeUtil.date10());
		tradeRefundMppay.setRefundTime(DateTimeUtil.time8());
		
		tradeRefundMppayService.save(tradeRefundMppay);
		
		responseMsg.put("refundNo", requestMsg.get("refundNo"));
		responseMsg.put("outRefundNo", outRefundNo);
		responseMsg.put("bankRefundNo", tradeRefundMppay.getBankRefundNo());
		
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		return responseMsg;
	}
	
	@Override
	public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		return responseMsg;
	}
	
}
