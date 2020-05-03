package com.mppay.gateway.handler;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;

/**
 * 快捷业务处理
 * @author Administrator
 *
 */
public interface QuickBusiHandler {
	
	/**
	 * 快捷支付下单
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 快捷支付确认
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void confirmOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 支付短信 【高汇通：支付短验发送】
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void smsOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 订单查询
	 * @param requestMsg
	 * @return
	 * @throws Exception
	 */
	public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception;
	
	/**
	 * 退款订单
	 * @param requestMsg
	 * @return
	 * @throws Exception
	 */
	public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception;
	
	/**
	 * 退款订单查询
	 * @param requestMsg
	 * @return
	 * @throws Exception
	 */
	public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception;
	
	/**
	 * 签约短信 【高汇通：绑卡短信请求接口】
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void smsSign(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 快捷签约 鉴权
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void quickSign(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 快捷签约确认
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void confirmSign(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 解约短信
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void smsCancel(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	
	/**
	 * 快捷解约
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void quickCancel(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 查询签约信息
	 * @param requestMsg
	 * @throws Exception
	 */
	public ResponseMsg querySign(RequestMsg requestMsg) throws Exception;

	/**
	 * 查询银行卡信息 【高汇通：银行卡信息查询】
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void queryCardBind(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
}
