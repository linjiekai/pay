package com.mppay.gateway.handler.impl;

import cn.hutool.json.JSONUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.UserOperStatus;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.service.entity.UserOper;
import com.mppay.service.service.IUserOperService;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户变更
 *
 */
@Service("updateUserHandler")
@Slf4j
public class UpdateUserHandlerImpl implements UnifiedHandler{
	
	@Autowired
	private IUserOperService userOperService;
	
	@Autowired
	private UnifiedHandler addUserHandler;

	@Override
	public ResponseMsg execute(RequestMsg requestMsg) throws Exception {
		log.info("|更新用户|开始|requestMsg：{}", JSONUtil.toJsonStr(requestMsg));
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		
		
		UserOper userOper = userOperService.getOne(new QueryWrapper<UserOper>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("user_id", requestMsg.get("userId"))
				);
		
		if (null == userOper) {
			responseMsg = addUserHandler.execute(requestMsg);
			return responseMsg;
		}
		
		if (userOper.getStatus() == UserOperStatus.FREEZE.getId()) {
			log.error("用户状态不正确[" + userOper.getStatus() +"]" + requestMsg.toString());
			return responseMsg;
		}
		
		BeanUtils.populate(userOper, requestMsg.getMap());
		userOper.setStatus(UserOperStatus.NORMAL.getId());
		userOperService.updateById(userOper);
		log.info("|更新用户|结束|responseMsg：{}", JSONUtil.toJsonStr(responseMsg));
		return responseMsg;
	}
	
	
}
