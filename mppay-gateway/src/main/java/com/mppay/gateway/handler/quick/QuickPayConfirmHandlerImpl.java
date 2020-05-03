package com.mppay.gateway.handler.quick;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.service.IMercOrderService;
import com.mppay.service.service.ISmsOrderService;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;

@Service("quickPayConfirmHandler")
@Slf4j
public class QuickPayConfirmHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private ISmsOrderService smsOrderService;

	@Autowired
	private IMercOrderService mercOrderService;

	@Autowired
	private ICipherService cipherServiceImpl;

	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        
        String agrNoCipher = (String) requestMsg.get("agrNo");
        String agrNo = cipherServiceImpl.decryptAES(agrNoCipher);
		SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>()
				.eq("agr_no", agrNo)
				.eq("sms_order_no", requestMsg.get("smsOrderNo"))
				);

		if (null == smsOrder) {
			log.error("短信订单不存在！请求参数={}", requestMsg);
			throw new BusiException(31021);
		}

		//拼出service name
		String serviceName = smsOrder.getRouteCode().toLowerCase() + ConstEC.QUICKBUSIHANDLER;

		//通过spring ApplicationContext获取service对象
		QuickBusiHandler quickBusiHandler = (QuickBusiHandler) SpringContextHolder.getBean(serviceName);

		if (null == quickBusiHandler) {
			log.error("serviceName[{}]业务处理服务不存在!", serviceName);
			throw new BusiException(11114);
		}
        requestMsg.put("userOperNo", smsOrder.getUserOperNo());
		requestMsg.put("outTradeNo", smsOrder.getOutTradeNo());

		quickBusiHandler.confirmOrder(requestMsg, responseMsg);

		String returnCode = (String) responseMsg.get(ConstEC.RETURNCODE);
		String returnMsg = (String) responseMsg.get(ConstEC.RETURNMSG);

		log.info("请求结果responseMsg[{}]", responseMsg);
		if (StringUtils.isBlank(returnCode)) {
			log.error("请求结果responseMsg[{}]", responseMsg);
			throw new BusiException(11001);
		}

		if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
			log.error("请求结果responseMsg[{}]", responseMsg);
			throw new BusiException(returnCode, returnMsg);
		}

		MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>()
				.eq("merc_id", smsOrder.getMercId())
				.eq("out_trade_no", smsOrder.getOutTradeNo())
				);

		if (!mercOrder.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
			throw new BusiException(returnCode, returnMsg);
		}

		Map<String, Object> data = new HashMap<String, Object>();

		data.put("mercId", mercOrder.getMercId());
		data.put("payNo", mercOrder.getOutTradeNo());
		data.put("orderNo", mercOrder.getOrderNo());
		data.put("orderStatus", mercOrder.getOrderStatus());
		data.put("userId", mercOrder.getUserId());
		data.put("orderDate", mercOrder.getOrderDate());
		data.put("orderTime", mercOrder.getOrderTime());
		data.put("payDate", mercOrder.getPayDate());
		data.put("payTime", mercOrder.getPayTime());
		data.put("price", mercOrder.getPrice());
		data.put("bankCode", mercOrder.getBankCode());

		responseMsg.put(ConstEC.DATA, data);
	}

}
