package com.mppay.gateway.handler;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;


/**
 * 银行业务处理
 * @author Administrator
 *
 */
public interface BankBusiHandler {

	/**
	 * 统一下单
	 * @param requestMsg
	 * @param responseMsg
	 * @throws Exception
	 */
	public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception;
	
	/**
	 * 订单查询，查询待支付订单在第三方平台是否成功
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

}
