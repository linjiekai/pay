package com.mppay.gateway.handler.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mppay.core.constant.BankStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.TradeCode;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.service.common.ICipherService;
import com.mppay.service.vo.CardBindVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 提现银行卡号查询
 *
 */
@Service("queryCardBindHandler")
@Slf4j
public class QueryCardBindHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private ICardBindService cardBindService;
	
	@Autowired
	private ICipherService iCipherService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		requestMsg.put("tradeCode", TradeCode.WITHDRAW.getId());
		requestMsg.put("status", BankStatus.NORMAL.getId());
		
		String version = (String) requestMsg.get("versionHeader");
		
		if (!StringUtils.isBlank(version)) {
			if (version.contains("(")) {// IOS旧版问题
				version = version.substring(0, version.indexOf("("));
			}
			if (version.length() > 3) {
				version = version.substring(0, 3);
			}

			if ("1.4".compareTo(version) > 0) {
				requestMsg.put("bankType", 0);
			}
		}
		
		List<CardBindVO> cardBindList = cardBindService.cardBindList(requestMsg.getMap());
		
		if (null == cardBindList) {
			cardBindList = new ArrayList<CardBindVO>();
		} else {
			for (CardBindVO vo : cardBindList) {
				if (!StringUtils.isBlank(vo.getAgrNo())) {
					vo.setAgrNo(iCipherService.encryptAES(vo.getAgrNo()));
				}
			}
		}
		
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("cardBindList", cardBindList);
		
		responseMsg.put("data", data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
