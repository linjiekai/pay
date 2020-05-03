package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;

import lombok.extern.slf4j.Slf4j;

@Service("parameterCheckHandler")
@Slf4j
public class ParameterCheckHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

		if (null != requestMsg.get("price")) {
			BigDecimal price = new BigDecimal(requestMsg.get("price").toString());
			if (price.compareTo(new BigDecimal(0)) <= 0) {
				throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", "price"));
			}
		}
		
		if (null != requestMsg.get("period")) {
			String period = (String) requestMsg.get("period");
			if (Integer.parseInt(period) <= 0) {
				throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", "period"));
			}
		}
		
//		String methodType = (String) requestMsg.get("methodType");
//		if (methodType.equals("DirectPay")) {
//			String tradeType = (String) requestMsg.get("tradeType");
//			if (tradeType.equals(TradeType.JSAPI.getId())) {
//				String openId = (String) requestMsg.get("openId");
//				if (StringUtils.isBlank(openId)) {
//					throw new BusiException("11004", RequestMsgUtil.get("11004").replace("$", "openId"));
//				}
//			}
//		}
	}

}
