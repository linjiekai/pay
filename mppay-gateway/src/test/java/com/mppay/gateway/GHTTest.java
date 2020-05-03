package com.mppay.gateway;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.mppay.core.constant.Align;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.utils.DateUtil;
import com.mppay.core.utils.RegularUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.PlatformBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IAliResourcesService;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.IWithdrOrderService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GHTTest {

    @Autowired
    PlatformBusiHandler gaohuitongPlatformBusiHandler;
    @Autowired
    QuickBusiHandler gaohuitongQuickBusiHandler;
    @Autowired
    WithdrOrderBusiHandler gaohuitongWithdrOrderBusiHandler;
    @Autowired
    private UnifiedHandler chainHandler;
    @Autowired
    private UnifiedHandler withdrOrderHandler;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private IWithdrOrderService iWithdrOrderService;
    @Autowired
    private IAliResourcesService aliResourcesService;
    @Autowired
    protected ApplicationContext ctx;

    RequestMsg msg = new RequestMsg();

    @Before
    public void setup() throws Exception {
        String aesKey = dictionaryService.findForString("SecretKey", "AES");
        String aesIv = dictionaryService.findForString("SecretKey", "IV");

        String agrNo = "201909170000000155";
        String encrypt = AESCoder.encrypt(agrNo, aesKey, aesIv);
        String cardNo = "445201199207050019";
        String mobile = "13509030019";
        String bankCardNo = "6222620710031141995";
        String  cardNoEn = AESCoder.encrypt(cardNo, aesKey, aesIv);
        mobile = AESCoder.encrypt(mobile, aesKey, aesIv);
        bankCardNo = AESCoder.encrypt(bankCardNo, aesKey, aesIv);

        //支付请求
        msg.setAttr("methodType", "QuickPayReq");
        msg.setAttr("requestId", System.currentTimeMillis() + "");
        msg.setAttr("prePayNo", "156872304859700007541");
        msg.setAttr("payType", "3");
        msg.setAttr("agrNo", encrypt);
        msg.setAttr("sysCnl", "IOS");
        msg.setAttr("clientIp", "127.0.0.1");
        msg.setAttr("mercId", "888000000000001");
        msg.setAttr("platform", "MPMALL");
        msg.setAttr("cardNo", cardNoEn);
        msg.setAttr("mobile", mobile);
        msg.setAttr("bankCardNo", bankCardNo);
        msg.setAttr("certNo", cardNo);


        //快捷签约
        /*msg.setAttr("methodType", "QuickSign");
        msg.setAttr("requestId", System.currentTimeMillis() +"");
        msg.setAttr("userId", "403");
        msg.setAttr("cardNo", cardNo);
        msg.setAttr("cardType", "1");
        msg.setAttr("mobile", mobile);
        msg.setAttr("bankCode", "CCB");
        msg.setAttr("bankCardName", "林大爷");
        msg.setAttr("bankCardNo", bankCardNo);
        msg.setAttr("bankCardType", "01");
        msg.setAttr("sysCnl", "IOS");
        msg.setAttr("clientIp", "127.0.0.1");
       ;*/
    }

    //商户注册
    @Test
    public void baseInfoRegister() throws Exception {
        msg.setAttr("bankCardName", "林大爷");
        msg.setAttr("bankCode", "COMM");
        msg.setAttr("bankCardType", "01");
        msg.setAttr("mercId", "888000000000001");
        msg.setAttr("platform", "MPMALL");
        msg.setAttr("userId", "539");
        msg.setAttr("picType", "02");
        msg.setAttr("bankCardImgFront", "https://static-mpmall.mingpinmao.cn/bankCard/jx59rqrmwwtfbaptbvh9.png");
        msg.setAttr("imgFront", "https://static-mpmall.mingpinmao.cn/idCard/wzx70x4d7drpxnle9g6a.png");
        msg.setAttr("imgBack", "https://static-mpmall.mingpinmao.cn/idCard/i6d6o3nr2gpts3rcdivd.png");
        //注册
        ResponseMsg responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.baseInfoRegister(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
        //上传图片
        responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.addImageInfo(msg,responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
        //开通业务
        responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.initiateBusi(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));

        gaohuitongPlatformBusiHandler.queryBalanceInfo(msg,responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));

       /* gaohuitongPlatformBusiHandler.queryCardBind(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));*/
    }

    //鉴权绑卡
    @Test
    public void quickSign() throws Exception {
        ResponseMsg execute = chainHandler.execute(msg);
        System.out.println(JSON.toJSONString(execute));
    }

    //绑卡短信请求
    @Test
    public void smsSign() throws Exception {
        ResponseMsg execute = chainHandler.execute(msg);
        System.out.println(JSON.toJSONString(execute));
    }

    //绑卡信息确认
    @Test
    public void confirmSign() throws Exception {
        ResponseMsg execute = chainHandler.execute(msg);
        System.out.println(JSON.toJSONString(execute));
    }

    //快捷支付下单
    @Test
    public void quickUnifiedOrder() throws Exception {
        String aesKey = dictionaryService.findForString("SecretKey", "AES");
        String aesIv = dictionaryService.findForString("SecretKey", "IV");
        String mobile = (String) msg.get("mobile");
        String bankCardNo = (String) msg.get("bankCardNo");
        String cardNo = (String) msg.get("cardNo");
        //匹配正则
        if (StringUtils.isNotEmpty(mobile)) {
            String decrypt = AESCoder.decrypt(mobile, aesKey, aesIv);
            if (!RegularUtil.matches(decrypt, RegularUtil.REGULAR_MOBILE)) {
                throw new BusiException(31105);
            }
        }
        if (StringUtils.isNotEmpty(bankCardNo)) {
            String decrypt = AESCoder.decrypt(bankCardNo, aesKey, aesIv);
            if (!RegularUtil.matches(decrypt, RegularUtil.REGULAR_BANKCARDNO)) {
                throw new BusiException(31105);
            }
        }
        if (StringUtils.isNotEmpty(cardNo)) {
            String decrypt = AESCoder.decrypt(cardNo, aesKey, aesIv);
            if (!RegularUtil.matches(decrypt, RegularUtil.REGULAR_ID_CARD)) {
                throw new BusiException(31105);
            }
        }

       /* ResponseMsg execute = chainHandler.execute(msg);
        System.out.println(JSON.toJSONString(execute));*/
    }

    //转账
    @Test
    public void withdrOrder() throws Exception {
        String withdrOrderNo = "2019091900000171";
        WithdrOrder withdrOrder = iWithdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", withdrOrderNo));
        msg.putAll(BeanUtils.beanToMap(withdrOrder));

        ResponseMsg execute = withdrOrderHandler.execute(msg);
        System.out.println(JSON.toJSONString(execute));
    }


    @Test
    public void createWithdrOrder() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String orderNo = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + iSeqIncrService.nextVal("withdr_order_no", 8, Align.LEFT);

        WithdrOrder withdrOrder = new WithdrOrder();
        withdrOrder.setUserId(520l);
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
        System.out.println(JSON.toJSONString(withdrOrder));
    }



}
