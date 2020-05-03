package com.mppay.gateway.handler.quick;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.Align;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.DateUtil;
import com.mppay.gateway.BaseTest;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.PlatformBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ISmsOrderService;
import com.mppay.service.service.IWithdrOrderService;

import lombok.extern.slf4j.Slf4j;

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
public class GaohuitongQuickBusiHandlerImplTest extends BaseTest {

    private String key;
    private String iv;

    // 请求报文封装
    Map<String, Object> reqMap = new HashMap<>();

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.queues.withdrcardbind.routing-key}")
    private String bankInfoRegisterRoutingKye;
    @Autowired
    @Qualifier("gaohuitongQuickBusiHandler")
    private QuickBusiHandler gaohuitongQuickBusiHandler;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ISmsOrderService smsOrderService;
    @Autowired
    PlatformBusiHandler gaohuitongPlatformBusiHandler;
    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private IWithdrOrderService iWithdrOrderService;
    @Autowired
    private UnifiedHandler withdrOrderHandler;

    @Before
    public void setUp() throws Exception {
//        SpringContextUtil.setApplicationContext(ctx);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.key = dictionaryService.findForString("SecretKey", "AES");
        this.iv = dictionaryService.findForString("SecretKey", "IV");
        String bankCardNo = "6217003320038896687";
        String certNo = "445201199207050019";
        String mobile = "13533913510";

        // 请求公共参数
        reqMap.put("userId", "536");
        reqMap.put("sysCnl", "IOS");
        reqMap.put("mercId", "888000000000001");
        reqMap.put("platform", "MPMALL");
        reqMap.put("clientIp", "127.0.0.1");
        reqMap.put("timestamp", (int) (System.currentTimeMillis() / 1000));


        reqMap.put("mobile", AESCoder.encrypt(mobile,key,iv));
        reqMap.put("certNo",  certNo);
        reqMap.put("cardNo",  AESCoder.encrypt(certNo,key,iv));
        reqMap.put("bankCode", "CCB");
        reqMap.put("bankCardNo",   AESCoder.encrypt(bankCardNo,key,iv));
        reqMap.put("bankCardType", "01");
        reqMap.put("bankCardName", "张穗华");
        reqMap.put("bankCardImgFront", "https://static-mpmall.mingpinmao.cn/idCard/9akidtfzhcy2iyxw62mh.jpg");
        reqMap.put("imgFront", "https://static-mpmall.mingpinmao.cn/idCard/9akidtfzhcy2iyxw62mh.jpg");
        reqMap.put("imgBack", "https://static-mpmall.mingpinmao.cn/idCard/9akidtfzhcy2iyxw62mh.jpg");
    }

    /**
     * 快捷签约(绑卡) -> 快捷签约确认 -> 快捷签约确认
     *
     * @throws Exception
     */
    @Test
    public void quickSign() throws Exception {

        //商户入驻
        baseInfoRegister(reqMap);

        // 1. 鉴权绑卡
        JSONObject quickSignResp = quickSign(reqMap);
        System.out.println("鉴权绑卡 === " + JSON.toJSONString(quickSignResp));
        // 2. 绑卡短信请求
        JSONObject smsSignResp = smsSign(quickSignResp, reqMap);
        System.out.println("绑卡短信请求 === " + JSON.toJSONString(smsSignResp));
        // 3. 快捷签约确认
        JSONObject confirmSignResp = confirmSign(smsSignResp, reqMap);
        System.out.println("快捷签约确认 === " + JSON.toJSONString(confirmSignResp));
        // 4. 下单
        JSONObject unifiedOrder = unifiedOrder(reqMap);
        String prePayNo = unifiedOrder.getJSONObject("data").getString("prePayNo");
        // 5. 支付请求
        confirmSignResp.put("prePayNo", prePayNo);
        JSONObject unifiedOrderResp = unifiedOrder(confirmSignResp, reqMap);
        System.out.println("支付请求 === " + JSON.toJSONString(unifiedOrderResp));
        // 6. 支付短信
        JSONObject quickPaySmsResp = quickPaySms(unifiedOrderResp, reqMap);
        System.out.println("支付短信 === " + JSON.toJSONString(quickPaySmsResp));
        // 7. 支付确认
        JSONObject quickPayConfirmResp = quickPayConfirm(unifiedOrderResp, reqMap);
        System.out.println("支付确认 === " + JSON.toJSONString(quickPayConfirmResp));

        // 8.查询余额
        ResponseMsg responseMsg = new ResponseMsg();
        RequestMsg requestMsg = new RequestMsg();
        requestMsg.setAttr("userId",reqMap.get("userId"));
        requestMsg.setAttr("mercId",reqMap.get("mercId"));
        requestMsg.setAttr("platform",reqMap.get("platform"));
        gaohuitongPlatformBusiHandler.queryBalanceInfo(requestMsg,responseMsg);
        System.out.println("查询余额 === " + JSON.toJSONString(responseMsg));


    }

    RequestMsg reqMapToMsg(Map<String, Object> reqMap){
        RequestMsg msg = new RequestMsg();
        reqMap.keySet().stream().forEach(s->{
            msg.setAttr(s,reqMap.get(s));
        });
        return msg;
    }
    //商户注册
    public void baseInfoRegister(Map<String, Object> reqMap) throws Exception {
        RequestMsg msg = reqMapToMsg(reqMap);
        ResponseMsg responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.baseInfoRegister(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
        responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.addImageInfo(msg,responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
        responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.initiateBusi(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
    }

    /**
     * 快捷签约确认
     *
     * @throws Exception
     */
    @Test
    public void quickPay() throws Exception {

        // 3. 快捷签约确认
//        JSONObject smsSignResp = confirmSign(quickSignResp, reqMap);
    }

    /**
     * 签约绑定
     *
     * @throws Exception
     */
    public JSONObject quickSign(Map<String, Object> reqMap) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.putAll(reqMap);
        map.put("methodType", "QuickSign");
        map.put("requestId", System.currentTimeMillis());
        map.put("cardNo", AESCoder.encrypt("410222198706134038", key, iv));
        map.put("cardType", 1);
        map.put("mobile", AESCoder.encrypt("13509030019", key, iv));
        map.put("bankCode", "CCB");
        map.put("bankCardName", "罗松松");
        map.put("bankCardNo", AESCoder.encrypt("6216261000000000101", key, iv));
        map.put("bankCardType", "01");
      //  map.put("cvn2", "321");
        map.put("timestamp", (int) (System.currentTimeMillis() / 1000));
        //map.put("validDate", "2019");

        String sign = sign(map);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("=====================  签名信息 ： {}", sign);
        log.info("=====================  请求报文 ： {}", reqJson);

        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }

    /**
     * 绑卡短信请求
     *
     * @throws Exception
     */
    public JSONObject smsSign(JSONObject argsJson, Map<String, Object> reqMap) throws Exception {
        JSONObject data = argsJson.getJSONObject("data");
        String agrNo = data.getString("agrNo");
//        agrNo = AESCoder.decrypt(agrNo, key, iv);
        String smsOrderNo = data.getString("smsOrderNo");

        Map<String, Object> map = new HashMap<>();
        map.putAll(reqMap);
        map.put("methodType", "QuickSignSms");
        map.put("requestId", System.currentTimeMillis());
//        map.put("agrNo", AESCoder.encrypt(agrNo, key, iv));
        map.put("agrNo", agrNo);
        map.put("smsOrderNo", smsOrderNo);
        map.put("smsCode", "0");
        map.put("cvn2", "321");
        map.put("smsOrderType", 1);
        map.put("validDate", "2019");

        String sign = sign(map);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("=====================  签名信息 ： {}", sign);
        log.info("=====================  请求报文 ： {}", reqJson);

        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        Assert.assertEquals(200, resultStr);
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }

    /**
     * 绑卡信息确认
     * GHTBIND 102454
     *
     * @throws Exception
     */
    public JSONObject confirmSign(JSONObject argsJson, Map<String, Object> reqMap) throws Exception {
        JSONObject data = argsJson.getJSONObject("data");
        String agrNo = data.getString("agrNo");
        agrNo = AESCoder.decrypt(agrNo, key, iv);
        String smsOrderNo = data.getString("smsOrderNo");

        SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>().eq("agr_no", agrNo));
        String bindOrderNo = smsOrder.getBindOrderNo();
        String smsCode = bindOrderNo.substring(bindOrderNo.length() - 6);

        Map<String, Object> map = new HashMap<>();
        map.putAll(reqMap);
        map.put("methodType", "QuickSignConfirm");
        map.put("requestId", System.currentTimeMillis());
        map.put("agrNo", AESCoder.encrypt(agrNo, key, iv));
        map.put("smsOrderNo", smsOrderNo);
        map.put("smsCode", smsCode);

        String sign = sign(map);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("=====================  签名信息 ： {}", sign);
        log.info("=====================  请求报文 ： {}", reqJson);

        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        Assert.assertEquals(200, resultStr);
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }

    /**
     * 支付请求
     * GHTBIND 102454
     *
     * @throws Exception
     */
    public JSONObject unifiedOrder(JSONObject argsJson, Map<String, Object> reqMap) throws Exception {
        JSONObject data = argsJson.getJSONObject("data");
        String agrNo = data.getString("agrNo"); //已经是密文了

        String prePayNo = argsJson.getString("prePayNo");

        Map<String, Object> map = new HashMap<>();
        map.putAll(reqMap);
        map.put("methodType", "QuickPayReq");
        map.put("requestId", System.currentTimeMillis());
        map.put("agrNo", agrNo);
        map.put("payType", "3");
        map.put("prePayNo", prePayNo); //需要时调用UnifiedPortalAPITest.testDirectPrePayAPI() 生成

        String sign = sign(map);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("=====================  签名信息 ： {}", sign);
        log.info("=====================  请求报文 ： {}", reqJson);

        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        Assert.assertEquals(200, resultStr);
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }

    /**
     * 快捷支付下单
     *
     * @throws Exception
     */
    public JSONObject unifiedOrder(Map<String, Object> reqMap) throws Exception {

        Map<String, Object> map = new HashMap<>();
//        map.put("notifyUrl", "https://test-mppay.mingpinmao.cn/notify/gaohuitong/offline");
        map.put("notifyUrl", "http://127.0.0.1:15102/shop/mobile/pay/notify");
//		map.put("callbackUrl", "https://test-mppay.mingpinmao.cn/notify/gaohuitong/callback");
        map.put("requestId", DateTimeUtil.getTime() + "");
        map.put("methodType", "DirectPrePay");
        map.put("orderNo", DateTimeUtil.getTime() + "");
        map.put("orderDate", DateTimeUtil.date10());
        map.put("orderTime", DateTimeUtil.time8());
        map.put("price", 100000);
        map.put("userId", reqMap.get("userId"));
        map.put("periodUnit", "00");
        map.put("period", "30");
        map.put("mobile", "13825051122");
        map.put("busiType", "05");
        map.put("tradeCode", "01");
        map.put("goodsId", 111);
        map.put("goodsName", "商品名称");

        map.put("sysCnl", "ANDROID");
        map.put("mercId", "888000000000001");
        map.put("clientIp", "192.168.0.1");
        map.put("platform", "MPMALL");
        String sign = sign(map);
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MP-SignVer", "v1")
                .header("X-MP-Sign", sign)
                .content(itemJSONObj.toJSONString())
                .characterEncoding("utf-8")
        );
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        Assert.assertEquals(200, resultStr);
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }

    /**
     * 快捷支付
     * GHTBIND 102454
     *
     * @throws Exception
     */
    public JSONObject quickPayReq(JSONObject argsJson, Map<String, Object> reqMap) throws Exception {
        JSONObject data = argsJson.getJSONObject("data");
        String prePayNo = data.getString("prePayNo");
        String agrNo = argsJson.getString("agrNo");

        SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>().eq("agr_no", agrNo));
        String bindOrderNo = smsOrder.getBindOrderNo();
        String smsCode = bindOrderNo.substring(bindOrderNo.length() - 6);

        Map<String, Object> map = new HashMap<>();
        map.putAll(reqMap);
        map.put("methodType", "QuickPayReq");
        map.put("requestId", System.currentTimeMillis());
        map.put("prePayNo", prePayNo);
        map.put("payType", "3");
        map.put("agrNo", agrNo);
        map.put("sysCnl", "WX-APPLET");
        map.put("clientIp", "127.0.0.1");

        String sign = sign(map);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("=====================  签名信息 ： {}", sign);
        log.info("=====================  请求报文 ： {}", reqJson);

        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        Assert.assertEquals(200, resultStr);
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }

    /**
     * 快捷支付短信
     *
     * @throws Exception
     */
    public JSONObject quickPaySms(JSONObject argsJson, Map<String, Object> reqMap) throws Exception {
        JSONObject data = argsJson.getJSONObject("data");
        String smsOrderNo = data.getString("smsOrderNo");
        String agrNo = data.getString("agrNo");

//        SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>().eq("agr_no", agrNo));
//        String bindOrderNo = smsOrder.getBindOrderNo();
//        String smsCode = bindOrderNo.substring(bindOrderNo.length() - 6);

        Map<String, Object> map = new HashMap<>();
        map.put("methodType", "QuickPaySms");
        map.put("requestId", System.currentTimeMillis());
        map.put("agrNo", agrNo);
//        map.put("agrNo", AESCoder.encrypt(agrNo, key, iv));
        map.put("smsOrderType", 2);
        map.put("smsOrderNo", smsOrderNo);

        map.put("sysCnl", "WX-APPLET");
        map.put("clientIp", "127.0.0.1");
        map.put("mercId", "888000000000001");
        map.put("platform", "MPMALL");
        map.put("timestamp", (int) (System.currentTimeMillis() / 1000));


        String sign = sign(map);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("=====================  签名信息 ： {}", sign);
        log.info("=====================  请求报文 ： {}", reqJson);

        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        Assert.assertEquals(200, resultStr);
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }

    /**
     * 快捷支付-确认
     *
     * @throws Exception
     */
    public JSONObject quickPayConfirm(JSONObject argsJson, Map<String, Object> reqMap) throws Exception {
        JSONObject data = argsJson.getJSONObject("data");
        String smsOrderNo = data.getString("smsOrderNo");
        String agrNo = data.getString("agrNo");

//        SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>().eq("agr_no", agrNo));
//        String bindOrderNo = smsOrder.getBindOrderNo();
//        String smsCode = bindOrderNo.substring(bindOrderNo.length() - 6);

        Map<String, Object> map = new HashMap<>();
        map.put("methodType", "QuickPayConfirm");
        map.put("requestId", System.currentTimeMillis());
        map.put("agrNo", agrNo);
//        map.put("agrNo", AESCoder.encrypt(agrNo, key, iv));
        map.put("smsOrderNo", smsOrderNo);
        map.put("smsCode", "000014");

        map.put("sysCnl", "WX-APPLET");
        map.put("clientIp", "127.0.0.1");
        map.put("mercId", "888000000000001");
        map.put("platform", "MPMALL");
        map.put("timestamp", (int) (System.currentTimeMillis() / 1000));

        String sign = sign(map);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("=====================  签名信息 ： {}", sign);
        log.info("=====================  请求报文 ： {}", reqJson);

        ResultActions result = super.mockMvc.perform(MockMvcRequestBuilders
                .post("/mobile/unified")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("X-MPMALL-SignVer", "v1")
                .header("X-MPMALL-Sign", sign)
                .content(reqJson.toJSONString()));
        MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
        int resultStr = mrs.getResponse().getStatus();
        String content = mrs.getResponse().getContentAsString();
        System.out.println("content[" + content + "]");
        Assert.assertEquals(200, resultStr);
        JSONObject contentJson = JSONObject.parseObject(content);
        return contentJson;
    }


    //转账
    public ResponseMsg withdrOrder(RequestMsg requestMsg) throws Exception {
        WithdrOrder withdrOrder1 = createWithdrOrder( requestMsg);
        WithdrOrder withdrOrder = iWithdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", withdrOrder1.getWithdrOrderNo()));
        requestMsg.putAll(com.baomidou.mybatisplus.core.toolkit.BeanUtils.beanToMap(withdrOrder));

        return withdrOrderHandler.execute(requestMsg);
    }


    public WithdrOrder createWithdrOrder(RequestMsg requestMsg) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String orderNo = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + iSeqIncrService.nextVal("withdr_order_no", 8, Align.LEFT);




        WithdrOrder withdrOrder = new WithdrOrder();
        withdrOrder.setUserId(Long.valueOf((String)requestMsg.get("userId")));
        withdrOrder.setMercId("888000000000001");
        withdrOrder.setPlatform("MPWJMALL");
        withdrOrder.setWithdrOrderNo(orderNo);
        withdrOrder.setOrderNo(orderNo);
        withdrOrder.setOrderDate(now.format(DateTimeFormatter.ofPattern(DateUtil.DATE_PATTERN)));
        withdrOrder.setOrderTime(now.format(DateTimeFormatter.ofPattern(DateUtil.TIME_PATTERN)));
        withdrOrder.setOrderStatus("W");
        withdrOrder.setPrice(new BigDecimal("0.01"));
        withdrOrder.setBankCode("CCB");
        withdrOrder.setBankCardNo("r2Au1sEFUsZSZUWd34IzFbDOTd+hZN9MZ8r22RG22FA=");
        withdrOrder.setBankCardType("01");
        withdrOrder.setCheckName("NO_CHECK");
        withdrOrder.setClientIp("127.0.0.1");
        withdrOrder.setCreateTime(now);
        withdrOrder.setUpdateTime(now);
        withdrOrder.setUserOperNo("10000000075");
        withdrOrder.setUserNo("20000000316");
        withdrOrder.setRouteCode(GaohuitongConstants.GHT_ROUTE);
        iWithdrOrderService.save(withdrOrder);
        return  withdrOrder;
    }
}