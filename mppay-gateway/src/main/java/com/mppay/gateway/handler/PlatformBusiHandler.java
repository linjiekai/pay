package com.mppay.gateway.handler;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;

/**
 * @author: Jiekai Lin
 * @Description(描述):    支付平台顶层业务类
 * @date: 2019/9/4 22:25
 */
public interface PlatformBusiHandler {

    /**
     * @Description(描述): 商户基础信息登记（注册）
     * @auther: Jack Lin
     * @param :[requestMsg]
     * @return :com.mppay.gateway.dto.ResponseMsg
     * @date: 2019/9/4 22:25
     */
    void baseInfoRegister(RequestMsg requestMsg,ResponseMsg responseMsg) throws Exception;
	/**
	 * 银行卡信息查询
	 * @param requestMsg
	 * @throws Exception
	 */
	void queryCardBind(RequestMsg requestMsg,ResponseMsg responseMsg) throws Exception;

	/**
	 * @Description(描述):   开通支付平台业务
	 * @auther: Jack Lin
	 * @param :[requestMsg]
	 * @return :com.mppay.gateway.dto.ResponseMsg
	 * @date: 2019/9/4 22:30
	 */
	void initiateBusi(RequestMsg requestMsg,ResponseMsg responseMsg) throws Exception;

	/**
	 * 账户余额查询
	 * @param requestMsg
	 * @throws Exception
	 */
	void queryBalanceInfo(RequestMsg requestMsg,ResponseMsg responseMsg) throws Exception;

	/**
	 * 新增图片信息
	 * @param requestMsg
	 * @throws Exception
	 */
	void addImageInfo(RequestMsg requestMsg,ResponseMsg responseMsg) throws Exception;

}
