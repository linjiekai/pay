package com.mppay.gateway.handler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户变更
 *
 */
@Service("userChangeHandler")
@Slf4j
public class UserChangeHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private UnifiedHandler addUserHandler;
	
	@Autowired
	private UnifiedHandler updateUserHandler;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		String type = (String) requestMsg.get("type");
		
		if ("ADD".equals(type)) {
			responseMsg = addUserHandler.execute(requestMsg);
		} else if ("UPDATE".equals(type)) {
			responseMsg = updateUserHandler.execute(requestMsg);
		}
		
	}

}
