package com.mppay.gateway.handler;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;

/**
 * 统一业务处理
 *
 */
public interface BaseBusiHandler {

	public void handle(RequestMsg requestMsg, ResponseMsg responseMsg, String[] beans, int index) throws Exception;

	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
}
