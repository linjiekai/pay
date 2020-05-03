package com.mppay.gateway.handler.quick;

import java.util.HashMap;
import java.util.Map;

import com.mppay.core.sign.AESCoder;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.common.ICipherService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;
import com.mppay.service.entity.QuickAgr;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.service.IQuickAgrService;
import com.mppay.service.service.ISmsOrderService;

import lombok.extern.slf4j.Slf4j;

@Service("quickSignConfirmHandler")
@Slf4j
public class QuickSignConfirmHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private ISmsOrderService smsOrderService;
	@Autowired
	private IQuickAgrService quickAgrService;
	@Autowired
	private ICipherService cipherServiceImpl;

	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

		String agrNo = null;
		String agrNoCipher = null;
		try {
			agrNoCipher = (String) requestMsg.get("agrNo");
			agrNo = cipherServiceImpl.decryptAES(agrNoCipher);
			requestMsg.put("agrNo", agrNo);
		} catch (Exception e) {
			log.error("解密失败， requestMsg={}", requestMsg);
			throw new BusiException(31102);
		}

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

		requestMsg.put("smsRequestId",smsOrder.getSmsRequestId());
		requestMsg.put("bindOrderNo",smsOrder.getBindOrderNo());
		requestMsg.put("userOperNo",smsOrder.getUserOperNo());
		quickBusiHandler.confirmSign(requestMsg, responseMsg);

		String returnCode = (String) responseMsg.get(ConstEC.RETURNCODE);
		String returnMsg = (String) responseMsg.get(ConstEC.RETURNMSG);

		log.info("请求结果responseMsg[{}]", responseMsg);
		if (StringUtils.isBlank(returnCode)) {
			throw new BusiException(11001);
		}

		if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
			throw new BusiException(returnCode, returnMsg);
		}

		smsOrder.setBindAgrNo((String) responseMsg.get("bindAgrNo"));
		smsOrderService.updateById(smsOrder);

		QuickAgr quickAgr = new QuickAgr();

		//签约确认成功后拷贝数据到快捷签约信息
		BeanUtils.copyProperties(smsOrder, quickAgr);
		quickAgr.setCreateTime(null);
		quickAgr.setUpdateTime(null);
		quickAgr.setId(null);

		String mobileAbbr = null;
		String cardNoAbbr = null;
		String bankCardNoAbbr = null;
		try {
			mobileAbbr = cipherServiceImpl.decryptAES(quickAgr.getMobile());
			cardNoAbbr =cipherServiceImpl.decryptAES(quickAgr.getCardNo());
			bankCardNoAbbr = cipherServiceImpl.decryptAES(quickAgr.getBankCardNo());
			mobileAbbr = mobileAbbr.substring(0, 3) + "****" + mobileAbbr.substring(mobileAbbr.length() - 4, mobileAbbr.length());
			cardNoAbbr = cardNoAbbr.substring(0, 4) + "****" + cardNoAbbr.substring(cardNoAbbr.length() - 4, cardNoAbbr.length());
			bankCardNoAbbr = bankCardNoAbbr.substring(0, 4) + "****" + bankCardNoAbbr.substring(bankCardNoAbbr.length() - 4, bankCardNoAbbr.length());

			quickAgr.setMobileAbbr(mobileAbbr);
			quickAgr.setCardNoAbbr(cardNoAbbr);
			quickAgr.setBankCardNoAbbr(bankCardNoAbbr);
		} catch (Exception e) {
			log.error("解密失败， mobileAbbr={}, cardNoAbbr={}, bankCardNoAbbr={},requestMsg={}", mobileAbbr, cardNoAbbr, bankCardNoAbbr, requestMsg);
			throw new BusiException(31102);
		}

		//保存快捷签约信息
		quickAgrService.save(quickAgr);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("agrNo", agrNoCipher);
		data.put("smsOrderNo", smsOrder.getSmsOrderNo());

		responseMsg.put(ConstEC.DATA, data);
	}

}
