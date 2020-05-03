package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;

import com.mppay.core.config.SpringContextHolder;
import com.mppay.gateway.handler.PlatformBusiHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.MasterAccountBal;
import com.mppay.service.service.IMasterAccountBalService;

import lombok.extern.slf4j.Slf4j;

/**
 * 查询用户余额
 *
 */
@Service("queryBalanceHandler")
@Slf4j
public class QueryBalanceHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
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
		MasterAccountBal masterAccountBal = masterAccountBalService.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", requestMsg.get("userNo")));
		
		if (null == masterAccountBal) {
			log.error(ApplicationYmlUtil.get("11302") + requestMsg.toString());
			throw new BusiException(11302);
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("mercId", requestMsg.get("mercId"));
		data.put("userId", requestMsg.get("userId"));
		data.put("acBal", masterAccountBal.getAcBal());
		data.put("uavaBal", masterAccountBal.getUavaBal());
		data.put("notTxAvaBal", masterAccountBal.getNotTxAvaBal());
		data.put("sctBal", masterAccountBal.getSctBal());
		data.put("withdrBal", masterAccountBal.getWithdrBal());
		
		responseMsg.put("data", data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
