package com.mppay.gateway.handler.impl;

import com.mppay.core.constant.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IWithdrOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * 提现订单查询业务处理类
 *
 */
@Service("withdrOrderQueryHandler")
@Slf4j
public class WithdrOrderQueryHandlerImpl implements UnifiedHandler {
	
	@Autowired
	private IWithdrOrderService withdrOrderService;
	
	@Override
	public ResponseMsg execute(RequestMsg requestMsg) throws Exception {
		WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", requestMsg.get("withdrOrderNo")));
		
		if (null == withdrOrder) {
			return null;
		}
		
		//拼出service name
		String serviceName = withdrOrder.getRouteCode().toLowerCase() + ConstEC.WITHDRORDERBUSIHANDLER;
		
		//通过spring ApplicationContext获取service对象
		WithdrOrderBusiHandler withdrOrderBusiHandler = (WithdrOrderBusiHandler) SpringContextHolder.getBean(serviceName);
		
		if (null == withdrOrderBusiHandler) {
			log.error("serviceName[{}]业务处理服务不存在!", serviceName);
			return null;
		}
		
		requestMsg.put("platform", withdrOrder.getPlatform());
		requestMsg.put("bankMercId", withdrOrder.getBankMercId());
		ResponseMsg responseMsg = withdrOrderBusiHandler.queryWithdrOrder(requestMsg);

		return responseMsg;
	}

}
