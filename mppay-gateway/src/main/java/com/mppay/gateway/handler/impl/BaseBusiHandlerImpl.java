package com.mppay.gateway.handler.impl;

import org.springframework.stereotype.Service;

import com.mppay.core.config.SpringContextHolder;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一流程业务处理抽象类
 *
 */
@Service
@Slf4j
public abstract class BaseBusiHandlerImpl implements BaseBusiHandler{

	public void handle(RequestMsg requestMsg, ResponseMsg responseMsg, String[] beans, int index) throws Exception {
		
		log.info("当前业务处理链为beans[{}], requestId[{}]", beans[index], requestMsg.get("requestId"));
		doBusi(requestMsg, responseMsg);
		BaseBusiHandlerImpl handler = null;
		if (++index < beans.length) {
			handler = (BaseBusiHandlerImpl) SpringContextHolder.getBean(beans[index]);
			handler.handle(requestMsg, responseMsg, beans, index);
		}
	}

	public abstract void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
}
