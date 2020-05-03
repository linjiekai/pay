package com.mppay.gateway.handler.banbusi;

import com.mppay.core.constant.*;
import com.mppay.gateway.BaseTest;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.math.RoundingMode;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class JoinpayBankBusiHandlerImplTest extends BaseTest {


    RequestMsg msg = new RequestMsg();
    ResponseMsg responseMsg = new ResponseMsg();

    @Autowired()
    @Qualifier("joinpayBankBusiHandler")
    private BankBusiHandler joinpayBankBusiHandler;

    @Before
    public void setUp() throws Exception {
        super.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        msg.put("userId",942l); //用户id
        msg.put("sysCnl", SysCnlType.WX_PUBLIC.getId());
        msg.put("mercId", PlatformType.ZBMALL.getId());
        msg.put("clientIp", "127.0.0.1");
        msg.put("timestamp", (int) (System.currentTimeMillis() / 1000));
        msg.put("requestId", System.currentTimeMillis());
        msg.put("platform", PlatformType.ZBMALL.getCode());

    }

    @Test
    public void unifiedOrder() throws Exception {
        msg.put("routeCode", RouteCode.JOINPAY.getId());
        msg.put("price", new BigDecimal(0.01).setScale(2, RoundingMode.HALF_UP));
        msg.put("tradeType", TradeType.JSAPI.getId());
        msg.put("bankCode", BankCode.WEIXIN.getId());
        msg.put("openId", "oCDJG53LTJWZBjmMl4rV4NT0dSVE");


        joinpayBankBusiHandler.unifiedOrder(msg,responseMsg);

    }

    @Test
    public void queryOrder()  throws Exception {

        msg.put("routeCode", RouteCode.JOINPAY.getId());
        msg.put("price", new BigDecimal(0.01).setScale(2, RoundingMode.HALF_UP));
        msg.put("tradeType", TradeType.JSAPI.getId());
        msg.put("bankCode", BankCode.WEIXIN.getId());
        msg.put("openId", "oCDJG53LTJWZBjmMl4rV4NT0dSVE");
        msg.put("outTradeNo", "202004240000003230");
        ResponseMsg responseMsg = joinpayBankBusiHandler.queryOrder(msg);
    }

    @Test
    public void refundOrder()throws Exception {

        msg.put("routeCode", RouteCode.JOINPAY.getId());
        msg.put("price", new BigDecimal(0.01).setScale(2, RoundingMode.HALF_UP));
        msg.put("tradeType", TradeType.JSAPI.getId());
        msg.put("bankCode", BankCode.WEIXIN.getId());
        msg.put("openId", "oCDJG53LTJWZBjmMl4rV4NT0dSVE");
        msg.put("outTradeNo", "202004230000003182");
        ResponseMsg responseMsg = joinpayBankBusiHandler.refundOrder(msg);

    }

    @Test
    public void queryRefundOrder()throws Exception {
        msg.put("routeCode", RouteCode.JOINPAY.getId());
        msg.put("price", new BigDecimal(0.01).setScale(2, RoundingMode.HALF_UP));
        msg.put("tradeType", TradeType.JSAPI.getId());
        msg.put("bankCode", BankCode.WEIXIN.getId());
        msg.put("openId", "oCDJG53LTJWZBjmMl4rV4NT0dSVE");
        msg.put("outTradeNo", "202004230000003221");
        ResponseMsg responseMsg = joinpayBankBusiHandler.queryRefundOrder(msg);

    }
}