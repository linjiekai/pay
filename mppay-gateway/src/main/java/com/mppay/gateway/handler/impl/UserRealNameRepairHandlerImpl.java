package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.UserRealName;
import com.mppay.service.entity.UserRealNameDetails;
import com.mppay.service.service.IUserRealNameDetailsService;
import com.mppay.service.service.IUserRealNameService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户实名认证补录
 *
 */
@Service("UserRealNameRepairHandler")
@Slf4j
public class UserRealNameRepairHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private IUserRealNameDetailsService userRealNameDetailsService;
	
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
		
		//实名上传图片，实名标识realed改成强实名
		if (!StringUtils.isBlank(details.getImgFront()) && !StringUtils.isBlank(details.getImgBack())) {
        	userRealName.setRealed(2);
        	userRealName.setImgBack(details.getImgBack());
        	userRealName.setImgFront(details.getImgFront());
        	userRealNameService.updateById(userRealName);
        }
		
		userRealName.setId(null);
		userRealName.setCreateTime(null);
		userRealName.setUpdateTime(null);
		
		details = new UserRealNameDetails();
		BeanUtils.populate(details, requestMsg.getMap());
		
		org.springframework.beans.BeanUtils.copyProperties(userRealName, details);
		
		details.setRealDate(DateTimeUtil.date10());
		details.setRealTime(DateTimeUtil.time8());
		details.setReturnCode(ConstEC.SUCCESS_10000);
		details.setReturnMsg(ConstEC.SUCCESS_MSG);
		userRealNameDetailsService.save(details);
		
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("mercId", details.getMercId());
        data.put("platform", details.getPlatform());
        data.put("name", details.getName());
        data.put("gender", details.getGender());
        data.put("cardType", details.getCardType());
        data.put("cardNo", details.getCardNo());
        data.put("cardNoAbbr", details.getCardNoAbbr());
        data.put("status", details.getStatus());
        data.put("addressCode", details.getAddressCode());
        data.put("lastCode", details.getLastCode());
        data.put("imgFront", details.getImgFront());
        data.put("imgBack", details.getImgBack());
        data.put("realDate", details.getRealDate());
        data.put("realTime", details.getRealTime());
        
        responseMsg.put(ConstEC.DATA, data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}

}
