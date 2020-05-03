package com.mppay.gateway.handler.impl;

import org.springframework.stereotype.Service;

import com.mppay.core.config.SpringContextHolder;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;

import lombok.extern.slf4j.Slf4j;

@Service("userAddOrUpdateHandler")
@Slf4j
public class UserAddOrUpdateHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		String type = (String) requestMsg.get("type");
		
		UnifiedHandler unifiedHandler = null;
		if (type.equals("UPDATE")) {
			unifiedHandler = SpringContextHolder.getBean("updateUserHandler");
		} else {
			unifiedHandler = SpringContextHolder.getBean("addUserHandler");
		}
		
		ResponseMsg responseResult = unifiedHandler.execute(requestMsg);
		
		responseMsg.putAll(responseResult.getMap());
	}
	

}
