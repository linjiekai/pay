package com.mppay.gateway.handler;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;


/**
 * 银行提现业务处理
 *
 * @author Administrator
 */
public interface WithdrOrderBusiHandler {

    /**
     * 获取外部银行商户信息
     *
     * @param requestMsg
     * @return
     * @throws Exception
     */
    ResponseMsg getOutMercInfo(RequestMsg requestMsg) throws Exception;

    /**
     * 提现绑卡
     *
     * @param requestMsg
     * @return
     * @throws Exception
     */
    ResponseMsg withdrCardBind(RequestMsg requestMsg) throws Exception;

    /**
     * 提现解绑
     *
     * @param requestMsg
     * @return
     * @throws Exception
     */
    ResponseMsg withdrUnCardBind(RequestMsg requestMsg) throws Exception;

    /**
     * 提现订单
     *
     * @param requestMsg
     * @return
     */
    ResponseMsg withdrOrder(RequestMsg requestMsg) throws Exception;

    /**
     * 提现订单查询
     *
     * @param requestMsg
     * @return
     */
    ResponseMsg queryWithdrOrder(RequestMsg requestMsg) throws Exception;

    /**
     * 绑卡前的操作
     *
     * @param requestMsg
     * @return
     */
    void beforeWithdrCardBind(RequestMsg requestMsg,ResponseMsg responseMsg) throws Exception;

}
