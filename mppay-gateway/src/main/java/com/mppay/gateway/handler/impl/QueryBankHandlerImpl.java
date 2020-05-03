package com.mppay.gateway.handler.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mppay.core.constant.BankStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.TradeCode;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.vo.BankRouteVO;

import lombok.extern.slf4j.Slf4j;

@Service("queryBankHandler")
@Slf4j
public class QueryBankHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private IBankRouteService bankRouteService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		String tradeCode = (String) requestMsg.get("tradeCode");
		
		if (tradeCode.equals(TradeCode.CONSUMER.getId()) || tradeCode.equals(TradeCode.TRADE.getId()) ) {
			tradeCode=TradeCode.TRADE.getId();
		}
		
		requestMsg.put("status", BankStatus.NORMAL.getId());
		requestMsg.put("tradeCode", requestMsg.get("tradeCode"));
		
		List<BankRouteVO> bankList = bankRouteService.findBankRoute(requestMsg.getMap());
		
		if (null == bankList) {
			bankList = new ArrayList<BankRouteVO>();
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("bankList", bankList);
		
		responseMsg.put("data", data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}
	
}
