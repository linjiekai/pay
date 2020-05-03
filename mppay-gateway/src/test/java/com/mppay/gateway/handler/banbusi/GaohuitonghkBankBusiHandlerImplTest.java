package com.mppay.gateway.handler.banbusi;

import java.util.Map;

import com.mppay.core.constant.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.BaseTest;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.entity.TradeRefund;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderService;
import com.mppay.service.service.ITradeRefundService;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GaohuitonghkBankBusiHandlerImplTest extends BaseTest {
    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    protected ITradeOrderService tradeOrderService;
    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private UnifiedHandler tradeRefundHandler;
    @Autowired
    protected ITradeRefundService tradeRefundService;

    RequestMsg msg = new RequestMsg();
    ResponseMsg responseMsg = new ResponseMsg();
    private String prePayNo ;
    @Before
    public void setUp() throws Exception {
        super.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        // 请求公共参数
        msg.put("userId",942l); //用户id
        msg.put("sysCnl", "IOS");
        msg.put("mercId", PlatformType.XFYLMALL.getId());
        msg.put("clientIp", "127.0.0.1");
        msg.put("timestamp", (int) (System.currentTimeMillis() / 1000));
        msg.put("requestId", System.currentTimeMillis());
        msg.put("platform", PlatformType.XFYLMALL.getCode());

    }

    //预支付订单
    @Test
    public void DirectPrePay() throws  Exception {
        msg.put("methodType", "DirectPrePay");
        msg.put("orderNo", DateTimeUtil.getTime() + "");
        msg.put("orderDate", DateTimeUtil.date10());
        msg.put("orderTime", DateTimeUtil.time8());
        msg.put("price", 1);
        msg.put("periodUnit", "00");
        msg.put("period", "30");
        msg.put("mobile", "13825051122");
        msg.put("busiType", "05");
        msg.put("tradeCode", "02");
        msg.put("clientIp", "192.168.0.1");
        msg.put("goodsId", 111);
        msg.put("goodsName", "商品名称");
        msg.put("notifyUrl", "http://127.0.0.1:15111/shop/mobile/pay/notify");
        JSONObject response = response(msg);
        System.out.println(response.toString());
        prePayNo=(String) response.get("prePayNo");
    }

    //订单支付
    @Test
    public void unifiedOrder() throws  Exception {
        DirectPrePay();
        msg.put("prePayNo",prePayNo);
        msg.put("tradeType", TradeType.MWEB.getId());
        msg.put("openId", "oCDJG53LTJWZBjmMl4rV4NT0dSVE");
        msg.put("bankCode", BankCode.ALIPAY.getId());
        msg.put("bankCardType", "08");
        msg.put("methodType", "DirectPay");
        msg.put("callbackUrl", "https://test-mppay.mingpinmao.cn/notify/gaohuitong/callback");
        JSONObject response = response(msg);
        System.out.println(response.toString());
    }



    public JSONObject response(RequestMsg msg) throws Exception {
        Map<String, Object> map = msg.getMap();
        String sign = sign(map);
        System.out.println(sign);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        System.out.println(reqJson);
        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));

        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        String content = mrs.getResponse().getContentAsString();
        JSONObject jsonObject = JSONObject.parseObject(content);
        String code = (String) jsonObject.get("code");
        if (!ConstEC.SUCCESS_10000.equalsIgnoreCase(code)) {
            throw new RuntimeException();
        }
        Object data = jsonObject.get("data");
        String s = JSON.toJSONString(data);
        JSONObject dataObj = JSONObject.parseObject(s);
        return dataObj;
    }


    //退款申请
    @Test
    public void refundOrder() throws  Exception {
        TradeOrder tradeOrder = tradeOrderService.getById(258878);
        TradeRefund tradeRefund = new TradeRefund();
        tradeRefund.setMercId(tradeOrder.getMercId());
        tradeRefund.setOutTradeNo(tradeOrder.getOutTradeNo());
        tradeRefund.setTradeNo(tradeOrder.getTradeNo());
        tradeRefund.setBankTradeNo(tradeOrder.getBankTradeNo());
        tradeRefund.setBankCode(tradeOrder.getBankCode());
        tradeRefund.setRouteCode(tradeOrder.getRouteCode());
        tradeRefund.setFundBank(tradeOrder.getFundBank());
        tradeRefund.setTradeType(tradeOrder.getTradeType());
        tradeRefund.setRefundDate(DateTimeUtil.date10());
        tradeRefund.setRefundTime(DateTimeUtil.time8());
        tradeRefund.setTradeCode(TradeCode.TRADEREFUND.getId());
        tradeRefund.setOrderStatus(OrderStatus.REFUND.getId());
        tradeRefund.setApplyPrice(tradeOrder.getPrice());
        tradeRefund.setPrice(tradeOrder.getPrice());
        String refundNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REFUND_NO.getId(), 8, Align.LEFT);
        tradeRefund.setRefundNo(refundNo);
        tradeRefundService.save(tradeRefund);
        msg.put("refundNo",refundNo);
        ResponseMsg responseMsg = tradeRefundHandler.execute(msg);
        System.out.println();
    }
}