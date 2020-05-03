package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.AppIdConf;
import com.mppay.service.service.IAppIdConfService;

import lombok.extern.slf4j.Slf4j;

/**
 * 获取appid
 *
 * @author chenfeihang
 */
@Service("getAppIdHandler")
@Slf4j
public class GetAppIdHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

	@Autowired
	private IAppIdConfService appIdConfService; 

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        
    	String tradeType = (String) requestMsg.get("tradeType");
		
		if (null != requestMsg.get("sysCnl") && requestMsg.get("sysCnl").toString().equals("WX-PUBLIC")) {
			tradeType = "PUBLIC";
		}
		
    	AppIdConf appIdConf = appIdConfService.getOne(new QueryWrapper<AppIdConf>().select("app_id", "secrect", "oper_type", "bank_code", "private_key", "public_key", "region")
    			.eq("bank_code", requestMsg.get("bankCode"))
    			.eq("platform", requestMsg.get("platform"))
    			.eq("merc_id", requestMsg.get("mercId"))
    			.eq("trade_type", tradeType)
    			.eq("oper_type", requestMsg.get("operType"))
    			.eq("status", 1)
    			);


        responseMsg.put(ConstEC.DATA, appIdConf);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
    }
}
