package com.mppay.gateway.handler;

import java.util.Map;

import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.entity.WithdrOrder;

/**
 * 交易资金处理
 *
 */
public interface DepositProcHandler {

	/**
	 * 资金业务处理
	 * @throws Exception
	 */
	public void proc(Map<String, Object> data) throws Exception;
	
	/**
	 * 资金业务处理前
	 * @throws Exception
	 */
	public void pre(Map<String, Object> data) throws Exception;
	
	/**
	 * 资金业务处理后
	 * @throws Exception
	 */
	public MercOrder after(TradeOrder tradeOrder, Map<String, Object> data) throws Exception;
	
	/**
	 *  提现资金业务处理
	 * @param data
	 * @throws Exception
	 */
	public void procWithdr(Map<String, Object> data) throws Exception;
	
	/**
	 * 提现资金业务处理前
	 * @throws Exception
	 */
	public void preWithdr(Map<String, Object> data) throws Exception;
	
	/**
	 * 提现资金业务处理后
	 * @throws Exception
	 */
	public void afterWithdr(WithdrOrder withdrOrder, Map<String, Object> data) throws Exception;
}
