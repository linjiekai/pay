package com.mppay.gateway.handler.quick;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mppay.core.constant.*;
import com.mppay.service.service.common.ICipherService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;
import com.mppay.service.entity.BankRoute;
import com.mppay.service.entity.QuickAgr;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IQuickAgrService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ISmsOrderService;

import lombok.extern.slf4j.Slf4j;

@Service("quickSignHandler")
@Slf4j
public class QuickSignHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private ISmsOrderService smsOrderService;
	@Autowired
	private IQuickAgrService quickAgrService;
	@Autowired
	private ISeqIncrService seqIncrService;
	@Autowired
	private IBankRouteService bankRouteService;
	@Autowired
	private ICipherService cipherServiceImpl;

	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

		String bankCardType = (String) requestMsg.get("bankCardType");

		//贷记卡校验cvn2和有效期
		if (BankCardType.CREDIT.getId().equals(bankCardType)) {
			if (null == requestMsg.get("cvn2")) {
				log.error("无效的cvn2, requestMsg={}", requestMsg);
				throw new BusiException(15007);
			}
			if (null == requestMsg.get("validDate")) {
				log.error("无效的有期效, requestMsg={}", requestMsg);
				throw new BusiException(15008);
			}
		}

		//查下有没有重复绑卡
		QuickAgr quickAgr = quickAgrService.getOne(new QueryWrapper<QuickAgr>()
				.eq("user_oper_no", requestMsg.get("userOperNo"))
				.eq("card_no", requestMsg.get("cardNo"))
				.eq("bank_card_no", requestMsg.get("bankCardNo"))
				.eq("status", QuickAgrStatus.NORMAL.getId())
				);
		if (null != quickAgr) {
			log.error("银行卡已被绑定，requestMsg={}", requestMsg);
			throw new BusiException(15001);
		}

		SmsOrder smsOrder = new SmsOrder();
		String agrNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.AGR_NO.getId(), SeqIncrType.AGR_NO.getLength(), Align.LEFT);
		smsOrder.setAgrNo(agrNo);
		String smsOrderNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.SMS_ORDER_NO.getId(), SeqIncrType.SMS_ORDER_NO.getLength(), Align.LEFT);
		BeanUtils.populate(smsOrder, requestMsg.getMap());
		smsOrder.setSmsOrderNo(smsOrderNo);
		smsOrder.setAgrType("01");
		smsOrder.setBindDate(DateTimeUtil.date10());
		smsOrder.setBindTime(DateTimeUtil.time8());

		//这里可能会有多个，取其中一个就行
		BankRoute bankRoute = bankRouteService.getOne(new QueryWrapper<BankRoute>()
				.eq("trade_code", TradeCode.CONSUMER.getId())
				.eq("bank_card_type", requestMsg.get("bankCardType"))
				.eq("bank_code", requestMsg.get("bankCode"))
				.eq("merc_id", requestMsg.get("mercId"))
				.last(" limit 1")
				);
		Optional.ofNullable(bankRoute).orElseThrow(() -> new BusiException(31116));

		requestMsg.put("agrNo", agrNo);
		requestMsg.put("routeCode", bankRoute.getRouteCode());
		requestMsg.put("smsOrderNo", smsOrderNo);

		//短信流水存库
		smsOrder.setRouteCode(bankRoute.getRouteCode());
		smsOrderService.save(smsOrder);

		//根据路由编码找到对应 支付平台实现
		String serviceName = bankRoute.getRouteCode().toLowerCase() + ConstEC.QUICKBUSIHANDLER;
		QuickBusiHandler quickBusiHandler = (QuickBusiHandler) SpringContextHolder.getBean(serviceName);
		if (null == quickBusiHandler) {
			log.error("serviceName[{}]业务处理服务不存在!", serviceName);
			throw new BusiException(11114);
		}

		//调各个支付平台实现
		quickBusiHandler.quickSign(requestMsg, responseMsg);

		String returnCode = (String) responseMsg.get(ConstEC.RETURNCODE);
		String returnMsg = (String) responseMsg.get(ConstEC.RETURNMSG);

		log.info("请求结果responseMsg[{}]", responseMsg);
		if (StringUtils.isBlank(returnCode)) {
			throw new BusiException(11001);
		}
		if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
			throw new BusiException(returnCode, returnMsg);
		}
		//在更新下短信订单
		smsOrder.setBindAgrNo((String) responseMsg.get("bindAgrNo"));
		smsOrder.setNeedSms((String) responseMsg.get("needSms"));
		smsOrderService.updateById(smsOrder);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("agrNo", cipherServiceImpl.encryptAES(agrNo));
		data.put("smsOrderNo", smsOrderNo);
		data.put("needSms", responseMsg.get("needSms"));

		responseMsg.put(ConstEC.DATA, data);
	}

}
