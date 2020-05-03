package com.mppay.gateway.handler.quick;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.exception.BusiException;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;
import com.mppay.service.entity.Bank;
import com.mppay.service.service.IBankService;

import lombok.extern.slf4j.Slf4j;

@Service("quickBankQueryHandler")
@Slf4j
public class QuickBankQueryHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

	@Autowired
	private IBankService bankService;

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
    	requestMsg.setAttr("mercId", PlatformType.MPMALL.getId());
        requestMsg.setAttr("platform", PlatformType.MPMALL.getCode());
        requestMsg.put("routeCode", RouteCode.GAOHUITONG.getId());
        //拼出service name
        String serviceName = RouteCode.GAOHUITONG.getId().toLowerCase() + ConstEC.QUICKBUSIHANDLER;
        QuickBusiHandler handler = SpringContextHolder.getBean(serviceName);
        if (null == handler) {
            log.error("serviceName[{}]业务处理服务不存在!", serviceName);
            throw new BusiException(11114);
        }

        handler.queryCardBind(requestMsg, responseMsg);

        String returnCode = (String) responseMsg.get(ConstEC.RETURNCODE);
        String returnMsg = (String) responseMsg.get(ConstEC.RETURNMSG);

        log.info("请求结果responseMsg[{}]", responseMsg);
        if (StringUtils.isBlank(returnCode)) {
            throw new BusiException(11001);
        }

        if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
            throw new BusiException(returnCode, returnMsg);
        }
        
        Bank bank = bankService.getOne(new QueryWrapper<Bank>().eq("bank_code", responseMsg.get("bankCode")).eq("status", 1));
        if (bank == null) {
        	throw new BusiException(15000);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bankCode", responseMsg.get("bankCode"));
        data.put("bankName", responseMsg.get("bankName"));
        data.put("bankAbbr", responseMsg.get("bankAbbr"));
        data.put("bankCardType", responseMsg.get("bankCardType"));

        responseMsg.put(ConstEC.DATA, data);
    }

}
