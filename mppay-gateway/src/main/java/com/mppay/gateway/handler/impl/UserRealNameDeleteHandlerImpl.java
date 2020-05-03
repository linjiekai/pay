package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.UserRealName;
import com.mppay.service.entity.UserRealNameDetails;
import com.mppay.service.service.IUserRealNameService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户实名认证信息删除
 *
 */
@Service("UserRealNameDeleteHandler")
@Slf4j
public class UserRealNameDeleteHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private IUserRealNameService userRealNameService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		UserRealNameDetails details = new UserRealNameDetails();
		BeanUtils.populate(details, requestMsg.getMap());
		
		UserRealName userRealName = userRealNameService.getOne(new QueryWrapper<UserRealName>().eq("card_no", requestMsg.get("cardNo")).eq("name", requestMsg.get("name")).eq("status", 1));
		
		if (null != userRealName) {
			log.error("实名信息不存在,请求参数" + requestMsg.toString());
			throw new BusiException(11307);
		}
		
		userRealName.setStatus(2);
		
		userRealNameService.updateById(userRealName);
		
        Map<String, Object> data = new HashMap<String, Object>();
        
        responseMsg.put(ConstEC.DATA, data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
