package com.mppay.gateway.handler.quick;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.QuickBusiHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @author j.t
 * @title: GaohuitongQuickBusiHandlerImplTest
 * @projectName xfhl-mppay-api
 * @description: TODO
 * @date 2019/9/11 15:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GaohuitongQuickBusiHandlerImplTest3 {

    private static final String userOperNo = "10000000047";

    @Autowired
    @Qualifier("gaohuitongQuickBusiHandler")
    private QuickBusiHandler gaohuitongQuickBusiHandler;

    /**
     * 绑卡短信请求
     *
     * @throws Exception
     */
    @Test
    public void smsSign() throws Exception {
        RequestMsg requestMsg = new RequestMsg();
        ResponseMsg responseMsg = new ResponseMsg();
        requestMsg.put("userOperNo", "10000000752");
        requestMsg.put("smsOrderNo", "1568630047206");
        requestMsg.put("mercId", "888000000000001");
        requestMsg.put("platform", "MPMALL");
        requestMsg.put("tradeType", "MWEB");
        gaohuitongQuickBusiHandler.smsSign(requestMsg, responseMsg);
    }

    /**
     * 绑卡信息确认
     *
     * @throws Exception
     */
    @Test
    public void confirmSign() throws Exception {
        RequestMsg requestMsg = new RequestMsg();
        ResponseMsg responseMsg = new ResponseMsg();
        requestMsg.put("userOperNo", "10000000752");
        requestMsg.put("smsRequestId", "2019091600002282");
        requestMsg.put("bindOrderNo", "GHTBIND102472");
        requestMsg.put("smsCode", "102472");

        requestMsg.put("mercId", "888000000000001");
        requestMsg.put("platform", "MPMALL");
        requestMsg.put("tradeType", "MWEB");
        gaohuitongQuickBusiHandler.confirmSign(requestMsg, responseMsg);
    }

    /**
     * 支付短信 【高汇通：支付短验发送】
     *
     * @throws Exception
     */
    @Test
    public void smsOrder() throws Exception {
        RequestMsg requestMsg = new RequestMsg();
        ResponseMsg responseMsg = new ResponseMsg();
        requestMsg.put("userOperNo", "10000000047");
        requestMsg.put("smsOrderNo", "10000000568");

        requestMsg.put("mercId", "888000000000001");
        requestMsg.put("platform", "MPMALL");
        requestMsg.put("tradeType", "MWEB");
        gaohuitongQuickBusiHandler.smsOrder(requestMsg, responseMsg);
    }

    /**
     * 快捷支付确认【高汇通：确认支付】
     *
     * @param ----- userOperNo    用户操作号
     *              smsOrderNo    (原订单号)
     *              smsCode   短信验证码
     *              sysCnl    移动终端设备类型
     *              deviceId  移动终端设备的唯一标识
     *              clientIp  客户端ip
     *              childMerchantId   (子商户号，如果为一户一码模式则必填)
     *              mercId    商城商户号
     *              platform  平台编号 名品猫:MPMALL，合伙人：PTMALL ....
     *              tradeType 交易类型 JSAPI：公众号或小程序支付 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付
     * @throws Exception
     */
    @Test
    public void confirmOrder() throws Exception {
        RequestMsg requestMsg = new RequestMsg();
        ResponseMsg responseMsg = new ResponseMsg();
        requestMsg.put("userOperNo", "10000000047");
        requestMsg.put("smsOrderNo", "10000000568");
        requestMsg.put("smsCode", "010203");
        requestMsg.put("sysCnl", "1");
        requestMsg.put("deviceId", "1");
        requestMsg.put("clientIp", "127.0.0.1");

        requestMsg.put("mercId", "888000000000001");
        requestMsg.put("platform", "MPMALL");
        requestMsg.put("tradeType", "MWEB");
        gaohuitongQuickBusiHandler.smsOrder(requestMsg, responseMsg);
    }

}