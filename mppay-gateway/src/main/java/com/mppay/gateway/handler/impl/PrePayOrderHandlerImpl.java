package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mppay.core.constant.Align;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.RedisUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.PrePayOrder;
import com.mppay.service.service.IPrePayOrderService;
import com.mppay.service.service.ISeqIncrService;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 生成预支付订单
 *
 */
@Service("prepayOrderHandler")
@Slf4j
public class PrePayOrderHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private IPrePayOrderService prePayOrderService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

		//预支付订单30分钟内失效
		String periodUnit = (String)requestMsg.get("periodUnit");
		String period1 = (String) requestMsg.get("period");
		Integer period = 30;
		if(StringUtils.isBlank(periodUnit)){
			periodUnit = "00";
		}
		if(StringUtils.isNotBlank(periodUnit)){
			period = Integer.valueOf(period1);
		}

		String expTime = DateTimeUtil.formatTimestamp2String(DateTimeUtil.getPeriodTime(period, periodUnit), "yyyyMMddHHmmss");

		PrePayOrder prePayOrder = new PrePayOrder();
		String prePayNo = DateTimeUtil.getTime() + seqIncrService.nextVal(SeqIncrType.PRE_PAY_NO.getId(), 8, Align.LEFT);
		log.info("商城订单号：{} ,生成预支付号prePayNo：{}", requestMsg.get("orderNo"), prePayNo);
		requestMsg.put("prePayNo", prePayNo);
		
		//随机立减金额
		BigDecimal reducePrice = (BigDecimal) RedisUtil.get(ConstEC.USER_REDUCE_PRICE + requestMsg.get("userOperNo"));
		BigDecimal price = new BigDecimal(requestMsg.get("price").toString());
		if (null == reducePrice) {
			String mercId = (String) requestMsg.get("mercId");
			reducePrice = new BigDecimal(0);
			if (mercId.equals(PlatformType.XFYLMALL.getId())
	                || mercId.equals(PlatformType.ZBMALL.getId())) {
	            reducePrice = reducePrice().setScale(2, RoundingMode.HALF_UP);
	            RedisUtil.set(ConstEC.USER_REDUCE_PRICE + requestMsg.get("userOperNo"), reducePrice, 60 * 60 * 24);
	        }
		}
		
		//如果减后金额<0 ,则不进行随机立减
		if ((price).compareTo(reducePrice) <= 0) {
        	reducePrice = new BigDecimal(0);
        } else {
        	price = price.subtract(reducePrice);
        	requestMsg.put("price", price);
        }
		
        requestMsg.put("reducePrice", reducePrice);
		
		BeanUtils.populate(prePayOrder, requestMsg.getMap());
		prePayOrder.setOrderExpTime(expTime);
		prePayOrderService.save(prePayOrder);
		
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("prePayNo", prePayNo);
		responseMap.put("reducePrice", reducePrice);
		responseMap.put("orderNo", prePayOrder.getOrderNo());
		
		responseMsg.put(ConstEC.DATA, responseMap);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		
	}
	
	public BigDecimal reducePrice() {
//        switch (BankCode.parse(bankCode)) {
//            case ALIPAY:
//                return new BigDecimal(RandomUtil.randomDouble(0.01, 0.5));
//            case WEIXIN:
//                return new BigDecimal(RandomUtil.randomDouble(0.01, 0.5));
//            case UPOP:
//                return new BigDecimal(RandomUtil.randomDouble(0.5,1.0));
//            default:
//                return new BigDecimal(0);
//        }
		
		return new BigDecimal(RandomUtil.randomDouble(0.01,1.0));

    }


}
