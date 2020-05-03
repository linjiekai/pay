package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mppay.service.service.common.ICipherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.BankCardType;
import com.mppay.core.constant.BankStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.TradeCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.RedisUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.Merc;
import com.mppay.service.entity.PrePayOrder;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IMercService;
import com.mppay.service.service.IPrePayOrderService;
import com.mppay.service.service.IQuickAgrService;
import com.mppay.service.vo.BankRouteVO;
import com.mppay.service.vo.QuickAgrBankVO;

import lombok.extern.slf4j.Slf4j;

@Service("payCashierHandler")
@Slf4j
public class PayCashierHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{

	@Autowired
	private IMercService mercService;
	
	@Autowired
	private IPrePayOrderService prePayOrderService;
	
	@Autowired
	private IBankRouteService bankRouteService;
	
	@Autowired
	private IQuickAgrService quickAgrService;
	
	@Autowired
	private ICipherService cipherServiceImpl;

	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		Merc merc = mercService.getOne(new QueryWrapper<Merc>().select("merc_id", "merc_name", "merc_abbr").eq("merc_id", requestMsg.get("mercId")));
		
		PrePayOrder prePayOrder = prePayOrderService.getOne(
				new QueryWrapper<PrePayOrder>()
				.select("pre_pay_no", "order_no", "price", "order_exp_time", "trade_code", "platform", "merc_id", "user_id", "user_oper_no", "reduce_price")
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("pre_pay_no", requestMsg.get("prePayNo"))
				);
		
		String expTime = DateTimeUtil.date14();
		
		//判断预支付号是否超时
		if (expTime.compareTo(prePayOrder.getOrderExpTime()) >= 0) {
			throw new BusiException(11007);
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("status", BankStatus.NORMAL.getId());
		params.put("tradeCode", TradeCode.TRADE.getId());
		params.put("mercId", prePayOrder.getMercId());
		params.put("bankCardType", BankCardType.THIRD.getId());
		
		List<BankRouteVO> bankList = bankRouteService.findBankRoute(params);
		
		params = new HashMap<String, Object>();
		params.put("status", BankStatus.NORMAL.getId());
		params.put("userOperNo", prePayOrder.getUserOperNo());
		
		String[] bankCardTypes = new String[]{"01", "02"};
		
		if (null != requestMsg.get("bankCardType")) {
			bankCardTypes = new String[]{requestMsg.get("bankCardType").toString()};
		}
		
		params.put("bankCardTypes", bankCardTypes);
		
		List<QuickAgrBankVO> quickAgrBankList = quickAgrService.findQuickAgrBank(params);
		
		if (null == quickAgrBankList) {
			quickAgrBankList = new ArrayList<QuickAgrBankVO>();
		} else {
			for (QuickAgrBankVO vo : quickAgrBankList) {
				vo.setAgrNo(cipherServiceImpl.encryptAES(vo.getAgrNo()));
			}
		}
		
		BigDecimal reducePrice = (BigDecimal) RedisUtil.get(ConstEC.USER_REDUCE_PRICE + requestMsg.get("userOperNo"));
		
		if (null == reducePrice) {
			reducePrice = new BigDecimal(0);
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("merc", merc);
		data.put("prePayOrder", prePayOrder);
		data.put("bankList", bankList);
		data.put("quickAgrBankList", quickAgrBankList);
		data.put("reducePrice", reducePrice);
		
		responseMsg.put("data", data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
