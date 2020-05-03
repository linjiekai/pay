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
import com.mppay.service.entity.MercOrder;
import com.mppay.service.service.IMercOrderService;

import lombok.extern.slf4j.Slf4j;

@Service("queryOrderHandler")
@Slf4j
public class QueryOrderHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private IMercOrderService mercOrderService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>().eq("merc_id", requestMsg.get("mercId")).eq("order_no", requestMsg.get("orderNo")));
		
		if (null == mercOrder) {
			responseMsg.put(ConstEC.RETURNCODE, "11003");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11003"));
			return;
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("mercId", mercOrder.getMercId());
		data.put("orderNo", mercOrder.getOrderNo());
		data.put("orderDate", mercOrder.getOrderDate());
		data.put("orderTime", mercOrder.getOrderTime());
		data.put("payDate", mercOrder.getPayDate());
		data.put("payTime", mercOrder.getPayTime());
		data.put("payNo", mercOrder.getOutTradeNo());
		data.put("price", mercOrder.getPrice());
		data.put("reducePrice", mercOrder.getReducePrice());
		data.put("bankCode", mercOrder.getBankCode());
		data.put("userId", mercOrder.getUserId());
		data.put("orderStatus", mercOrder.getOrderStatus());
		data.put("appId", mercOrder.getAppId());
		data.put("tradeType", mercOrder.getTradeType());
		
		responseMsg.put(ConstEC.DATA, data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
