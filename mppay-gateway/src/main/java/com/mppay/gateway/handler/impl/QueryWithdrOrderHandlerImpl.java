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
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IWithdrOrderService;

import lombok.extern.slf4j.Slf4j;

@Service("queryWithdrOrderHandler")
@Slf4j
public class QueryWithdrOrderHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private IWithdrOrderService withdrOrderService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("merc_id", requestMsg.get("mercId")).eq("order_no", requestMsg.get("orderNo")));
		
		if (null == withdrOrder) {
			responseMsg.put(ConstEC.RETURNCODE, "11003");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11003"));
			return;
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("mercId", withdrOrder.getMercId());
		data.put("orderNo", withdrOrder.getOrderNo());
		data.put("orderDate", withdrOrder.getOrderDate());
		data.put("orderTime", withdrOrder.getOrderTime());
		data.put("bankWithdrDate", withdrOrder.getBankWithdrDate());
		data.put("bankWithdrTime", withdrOrder.getBankWithdrTime());
		data.put("outTradeNo", withdrOrder.getOutTradeNo());
		data.put("price", withdrOrder.getPrice());
		data.put("bankCode", withdrOrder.getBankCode());
		data.put("userId", withdrOrder.getUserId());
		data.put("orderStatus", withdrOrder.getOrderStatus());
		
		responseMsg.put(ConstEC.DATA, data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
