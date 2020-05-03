package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mppay.core.constant.ConstEC;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.UserRealNameDetails;
import com.mppay.service.service.IUserRealNameDetailsService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户实名认证
 *
 */
@Service("userRealNameDetailsListHandler")
@Slf4j
public class QueryUserRealNameDetailsListHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private IUserRealNameDetailsService userRealNameDetailsService;
	
	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		
		Integer page = requestMsg.get("page") == null ? 1 : Integer.parseInt(requestMsg.get("page").toString());
		Integer limit = requestMsg.get("limit") == null ? 10 : Integer.parseInt(requestMsg.get("limit").toString());
		Page<UserRealNameDetails> ipage = new Page<>(page, limit);
		
		IPage<UserRealNameDetails> pageList = userRealNameDetailsService.page(ipage, requestMsg.getMap());
        
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("total", pageList.getTotal());
        data.put("items", pageList.getRecords());
        responseMsg.put(ConstEC.DATA, data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
