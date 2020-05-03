package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.MercOrderRefund;
import com.mppay.service.service.IMercOrderRefundService;

import lombok.extern.slf4j.Slf4j;

@Service("queryOrderRefundHandler")
@Slf4j
public class QueryOrderRefundHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private IMercOrderRefundService mercOrderRefundService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		MercOrderRefund mercOrderRefund = mercOrderRefundService.getOne(new QueryWrapper<MercOrderRefund>().eq("merc_id", requestMsg.get("mercId")).eq("refund_order_no", requestMsg.get("refundOrderNo")));
		
		if (null == mercOrderRefund) {
			responseMsg.put(ConstEC.RETURNCODE, "11003");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11003"));
			return;
		}
		//这里不再去查第三方的退款状态，直接返回数据库的结果

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("mercId", mercOrderRefund.getMercId());
		data.put("orderNo", mercOrderRefund.getOrderNo());
		data.put("refundDate", mercOrderRefund.getRefundDate());
		data.put("refundTime", mercOrderRefund.getRefundTime());
		data.put("price", mercOrderRefund.getPrice());
		data.put("bankCode", mercOrderRefund.getBankCode());
		data.put("userId", mercOrderRefund.getUserId());
		data.put("orderStatus", mercOrderRefund.getOrderStatus());
		data.put("appId", mercOrderRefund.getAppId());
		
		responseMsg.put(ConstEC.DATA, data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
