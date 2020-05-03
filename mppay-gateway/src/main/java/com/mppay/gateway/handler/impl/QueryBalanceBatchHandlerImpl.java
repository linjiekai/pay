package com.mppay.gateway.handler.impl;

import com.alibaba.fastjson.JSONArray;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.CardType;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.PlatformBusiHandler;
import com.mppay.service.service.IMasterAccountBalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询用户余额
 *
 */
@Service("queryBalanceBatchHandler")
@Slf4j
public class QueryBalanceBatchHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private IMasterAccountBalService masterAccountBalService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		//查第三方支付平台余额
		if(requestMsg.get("routecode")!=null){
			String routeCode = (String)requestMsg.get("routecode");
			//拼出service name
			String serviceName =routeCode.toLowerCase() + ConstEC.PLATFORMBUSIHANDLER;
			PlatformBusiHandler ght = (PlatformBusiHandler)SpringContextHolder.getBean(serviceName);
			ght.queryBalanceInfo(requestMsg,responseMsg);
			return;
		}

		//查可提现余额
		List<Long> userIds = JSONArray.parseArray((String) requestMsg.get("userIds"), Long.class);
		String mercId = (String) requestMsg.get("mercId");
		List<Map<String,Object>> masterAccountBalList = masterAccountBalService.listByUserIdAndMercId(userIds, mercId);
		
		if (null == masterAccountBalList || masterAccountBalList.size() == 0) {
			log.error(ApplicationYmlUtil.get("11302") + requestMsg.toString());
			throw new BusiException(11302);
		}

		List<Map<String, Object>> dataList = new ArrayList<>();
		Map<String, Object> data;
		for (Map<String,Object> masterAccountBal : masterAccountBalList) {
			data = new HashMap<>();
			data.put("mercId", masterAccountBal.get("merc_id"));
			data.put("userId", masterAccountBal.get("user_id"));
			data.put("acBal", masterAccountBal.get("ac_bal"));
			data.put("uavaBal", masterAccountBal.get("uava_bal"));
			data.put("notTxAvaBal", masterAccountBal.get("not_tx_ava_bal"));
			data.put("sctBal", masterAccountBal.get("sct_bal"));
			data.put("withdrBal", masterAccountBal.get("withdr_bal"));
			data.put("cardNo", masterAccountBal.get("card_no"));
			data.put("cardType", masterAccountBal.get("card_type"));
			data.put("cardTypeName", CardType.getNameById((Integer) masterAccountBal.get("card_type")));
			dataList.add(data);
		}

		responseMsg.put("data", dataList);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
