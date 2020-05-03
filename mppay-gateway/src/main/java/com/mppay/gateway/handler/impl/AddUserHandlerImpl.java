package com.mppay.gateway.handler.impl;

import java.util.UUID;

import cn.hutool.json.JSONUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.MasterAccountStatus;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.constant.UserOperStatus;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.service.entity.MasterAccount;
import com.mppay.service.entity.MasterAccountBal;
import com.mppay.service.entity.UserOper;
import com.mppay.service.service.IMasterAccountBalService;
import com.mppay.service.service.IMasterAccountService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.IUserOperService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户变更
 *
 */
@Service("addUserHandler")
@Slf4j
public class AddUserHandlerImpl implements UnifiedHandler{
	
	@Autowired
	private IUserOperService userOperService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	@Autowired
	private IMasterAccountService masterAccountService;
	
	@Autowired
	private IMasterAccountBalService masterAccountBalService;

	@Override
	public ResponseMsg execute(RequestMsg requestMsg) throws Exception {
		log.info("|新增用户|开始|requestMsg：{}", JSONUtil.toJsonStr(requestMsg));
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		
		
		if (null == requestMsg.get("userId") || StringUtils.isEmpty(requestMsg.get("userId").toString())) {
			return responseMsg;
		}
		
		UserOper userOper = userOperService.getOne(new QueryWrapper<UserOper>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("user_id", requestMsg.get("userId"))
				);
		
		if (null == requestMsg.get("userId") || StringUtils.isEmpty(requestMsg.get("userId").toString())) {
			return responseMsg;
		}
		
		String seq = null;
		String userOperNo = null;
		String userNo = null;
		String acNo = null;
		if (null != userOper) {
			BeanUtils.populate(userOper, requestMsg.getMap());
			if (userOper.getStatus() != UserOperStatus.NORMAL.getId()) {
				
				if (userOper.getStatus() == UserOperStatus.FREEZE.getId()) {
					log.error("用户状态不正确[" + userOper.getStatus() +"]" + requestMsg.toString());
					return responseMsg;
				}
				userOper.setStatus(UserOperStatus.NORMAL.getId());
			}
			
			userOperService.updateById(userOper);
			
		} else {
			//新增userOper
			seq = seqIncrService.nextVal(SeqIncrType.USER_OPER_NO.getId(), SeqIncrType.USER_OPER_NO.getLength(), Align.LEFT);
			userOperNo = "1" + seq;
			
			seq = seqIncrService.nextVal(SeqIncrType.USER_NO.getId(), SeqIncrType.USER_NO.getLength(), Align.LEFT);
			userNo = "2" + seq;
			
			userOper = new UserOper();
			BeanUtils.populate(userOper, requestMsg.getMap());  
			
			userOper.setUserOperNo(userOperNo);
			userOper.setUserNo(userNo);
			userOper.setSalt(UUID.randomUUID().toString().replace("-", ""));
			userOper.setStatus(UserOperStatus.NORMAL.getId());
			userOper.setRegDate(DateTimeUtil.date10());
			userOper.setRegTime(DateTimeUtil.time8());
			
			userOperService.save(userOper);
		}
		
		MasterAccount masterAccount = masterAccountService.getOne(new QueryWrapper<MasterAccount>().eq("user_no", userOper.getUserNo()));
		
		if (null != masterAccount) {
			if (masterAccount.getStatus() != MasterAccountStatus.NORMAL.getId()) {

				if (masterAccount.getStatus() == MasterAccountStatus.FREEZE.getId()) {
					log.error("用户状态不正确[" + masterAccount.getStatus() +"]" + requestMsg.toString());
					return responseMsg;
				}

				masterAccount.setStatus(MasterAccountStatus.NORMAL.getId());
				masterAccountService.updateById(masterAccount);
			}
		} else {
			//新增masterAccount
			seq = seqIncrService.nextVal(SeqIncrType.AC_NO.getId(), SeqIncrType.AC_NO.getLength(), Align.LEFT);
			acNo = "3" + seq;

			masterAccount = new MasterAccount();

			masterAccount.setAcNo(acNo);
			masterAccount.setUserNo(userOper.getUserNo());
			masterAccount.setAcType("100");
			masterAccount.setSalt(UUID.randomUUID().toString().replace("-", ""));
			masterAccount.setStatus(UserOperStatus.NORMAL.getId());
			masterAccount.setRegDate(DateTimeUtil.date10());
			masterAccount.setRegTime(DateTimeUtil.time8());
			
			masterAccountService.save(masterAccount);
		}
		
		MasterAccountBal masterAccountBal = masterAccountBalService.getOne(new QueryWrapper<MasterAccountBal>().eq("ac_no", masterAccount.getAcNo()));
		
		if (null == masterAccountBal) {
			
			//新增masterAccount
			
			masterAccountBal = new MasterAccountBal();
			
			masterAccountBal.setAcNo(masterAccount.getAcNo());
			masterAccountBal.setUserNo(userOper.getUserNo());
			masterAccountBal.setAcType("100");
			masterAccountBal.setCapType(1);
			masterAccountBal.setRegDate(DateTimeUtil.date10());
			masterAccountBal.setRegTime(DateTimeUtil.time8());
			
			masterAccountBalService.save(masterAccountBal);
		}

		log.info("|新增用户|结束|responseMsg：{}", JSONUtil.toJsonStr(responseMsg));
		return responseMsg;
	}
	

}
