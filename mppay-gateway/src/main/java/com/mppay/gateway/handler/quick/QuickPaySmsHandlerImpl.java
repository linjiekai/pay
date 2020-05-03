package com.mppay.gateway.handler.quick;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.utils.RedisUtil;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.SmsOrderType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;

import lombok.extern.slf4j.Slf4j;

@Service("quickPaySmsHandler")
@Slf4j
public class QuickPaySmsHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private ISmsOrderService smsOrderService;

	@Autowired
	private ICipherService cipherServiceImpl;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		log.info("|快捷支付|支付短信|开始 参数：{}", JSON.toJSONString(requestMsg));
		// 根据 smsOrderNo 判断是否可重发验证码,60s内不可重发
		String smsOrderNo = (String) requestMsg.get("smsOrderNo");
		String smsRetryKey = GaohuitongConstants.REDIS_KEY_PREFIX_QUICK_PAY_SMS_RETRY + smsOrderNo;
		boolean smsRetryFlag = RedisUtil.hasKey(smsRetryKey);
		if(smsRetryFlag){
			log.error("快捷支付短信60秒内不可重发");
			long expireTime = RedisUtil.getExpire(smsRetryKey);
			responseMsg.put(ConstEC.RETURNCODE, "31022");
			responseMsg.put(ConstEC.RETURNMSG, expireTime + "秒后请再获取短信验证码");
			return;
		}

		String agrNoCipher = (String) requestMsg.get("agrNo");
		String agrNo = cipherServiceImpl.decryptAES(agrNoCipher);
		SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>()
				.eq("agr_no", agrNo)
				.eq("sms_order_no", smsOrderNo)
		);

        if (null == smsOrder) {
			log.error("支付订单不存在！请求参数={}", requestMsg);
			throw new BusiException(31021);
		}
        Integer smsOrderType = Integer.parseInt(requestMsg.get("smsOrderType").toString());
        
        if (smsOrderType != SmsOrderType.PAY.getId()) {
        	log.error("短信订单类型不正确！请求参数={}", requestMsg);
            throw new BusiException(31026);
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
		quickBusiHandler.smsOrder(requestMsg, responseMsg);

		String returnCode = (String) responseMsg.get(ConstEC.RETURNCODE);
		String returnMsg = (String) responseMsg.get(ConstEC.RETURNMSG);
		
		log.info("请求结果responseMsg[{}]", responseMsg);
		if (StringUtils.isBlank(returnCode)) {
			throw new BusiException(11001);
		}
		if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
			throw new BusiException(returnCode, returnMsg);
		}

		smsOrder.setSmsOrderType(smsOrderType);
		// 短信订单流水号
		smsOrder.setSmsRequestId((String) responseMsg.get("smsRequestId"));
		smsOrderService.updateById(smsOrder);

		RedisUtil.set(smsRetryKey, smsOrderNo, 60);
		Map<String, Object> data = new HashMap<>();
		responseMsg.put(ConstEC.DATA, data);
		log.info("|快捷支付|支付短信|结束 响应：{}", JSON.toJSONString(responseMsg));
	}

}
