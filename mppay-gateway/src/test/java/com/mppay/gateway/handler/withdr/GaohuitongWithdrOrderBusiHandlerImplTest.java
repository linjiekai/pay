package com.mppay.gateway.handler.withdr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mppay.core.constant.PlatformType;
import com.mppay.service.entity.*;
import com.mppay.service.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.DateUtil;
import com.mppay.gateway.BaseTest;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.PlatformBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * @author j.t
 * @title: GaohuitongWithdrOrderBusiHandlerImplTest
 * @projectName xfhl-mppay-api
 * @description: 高汇通-商户银行卡信息绑定【提现绑卡】
 * @date 2019/9/6 16:17
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GaohuitongWithdrOrderBusiHandlerImplTest extends BaseTest {

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.queues.withdrcardbind.routing-key}")
    private String bankInfoRegisterRoutingKye;

    private static final String GAOHUITONG_INTERFACEWEB_BANKINFO = "gaohuitong.interfaceWeb.bankInfo";

    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private IWithdrOrderService withdrOrderService;
    @Autowired
    private UnifiedHandler withdrOrderHandler;
    @Autowired
    private UnifiedHandler withdrOrderQueryHandler;
    @Autowired
    private ISmsOrderService smsOrderService;
    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    PlatformBusiHandler gaohuitongPlatformBusiHandler;
    @Autowired
    IUserOperService iUserOperService;
    @Autowired
    WithdrOrderBusiHandler gaohuitongWithdrOrderBusiHandler;
    @Autowired
    ICardBindService iCardBindService;
    @Autowired
    IUserRealNameDetailsService iUserRealNameDetailsService;


    RequestMsg msg = new RequestMsg();
    ResponseMsg responseMsg = new ResponseMsg();
    private String key;
    private String iv;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.key = dictionaryService.findForString("SecretKey", "AES");
        this.iv = dictionaryService.findForString("SecretKey", "IV");
        String bankCardNo = "6212264200013243474";
        String certNo = "445201199207050019";
        String mobile = "13509030019";
        String userToken = "c0e0s8mqcwroqz5k6df030c109561kn7";
        String bankCardName = "林捷凯";
        String bankCardImgFront = "https://static-mpmall.mingpinmao.cn/bankCard/n4ufp71rkpqg05hxj3np.png";
        String imgFront = "https://static-mpmall.mingpinmao.cn/idCard/wzx70x4d7drpxnle9g6a.png";
        String imgBack = "https://static-mpmall.mingpinmao.cn/idCard/i6d6o3nr2gpts3rcdivd.png";
        String bankCode = "CMB";

        // 请求公共参数
        msg.put("userId",942l); //用户id
        msg.put("sysCnl", "IOS");
        msg.put("mercId", PlatformType.XFYLMALL.getId());
        msg.put("platform", PlatformType.XFYLMALL.getCode());
        msg.put("clientIp", "127.0.0.1");
        msg.put("timestamp", (int) (System.currentTimeMillis() / 1000));
        msg.put("requestId", System.currentTimeMillis());

        msg.put("mobile", AESCoder.encrypt(mobile, key, iv));//手机号
        msg.put("certNo", certNo);
        msg.put("cardNo", AESCoder.encrypt(certNo, key, iv)); //证件号
        msg.put("cardType", 1); //身份证类型
        msg.put("bankCode", bankCode);
        msg.put("bankCardNo", AESCoder.encrypt(bankCardNo, key, iv)); //银行卡号
        msg.put("bankCardType", "01");//银行卡类型
        msg.put("bankCardName", bankCardName);
        msg.put("bankCardImgFront", bankCardImgFront); //银行卡正面照
        msg.put("imgFront", imgFront);//身份证正面
        msg.put("imgBack", imgBack);//身份证反面
        msg.put("tradeType", "MWEB");
        msg.put("realSource", 3);
        msg.put("realType", 0);
        msg.put("userToken", userToken);
        msg.put("name", bankCardName);
        msg.put("withdrOrderNo", "2019102300000574");
        msg.put("orderDate", DateUtil.dateFormat(new Date(), DateUtil.DATE_PATTERN));
        msg.put("orderTime", DateUtil.dateFormat(new Date(), DateUtil.TIME_PATTERN));
        msg.put("price", "0.1");
        msg.put("checkName", "NO_CHECK");
        UserOper one = iUserOperService.getOne(new QueryWrapper<UserOper>().eq("merc_id",msg.get("mercId")).eq("user_id", msg.get("userId")));
        msg.put("userNo", one.getUserNo());
        msg.put("userOperNo", one.getUserOperNo());
        msg.put("merchantId", "00851609");
    }

    //流程执行
    @Test
    public void operation() throws Exception {

        queryCardBind();

         //baseInfoRegister();

        //绑卡流程
        /*userRealName();
        withdrCardBind();
        cardBindSms();*/
        //CardBindConfirm();


           queryBalanceInfo();

        //提现流程
        //WithdrApply();
       //withdrOrder();
        //queryWithdrOrder();



    }

    /**
     * 查询余额
     *
     * @throws Exception
     */
    @Test
    public void addImageInfo() throws Exception {
        CardBind cardBind = iCardBindService.getOne(new QueryWrapper<CardBind>().eq("user_oper_no", "10000105239").last("limit 1"));
        UserRealNameDetails userRealNameDetails = iUserRealNameDetailsService.getOne(new QueryWrapper<UserRealNameDetails>().eq("user_oper_no", "10000105239").eq("status", "1").last("limit 1"));

        RequestMsg requestMsg = new RequestMsg();

        requestMsg.put("userOperNo", "10000105239"); //银行卡正面照
        requestMsg.put("bankCardImgFront", cardBind.getBankCardImgFront()); //银行卡正面照
        requestMsg.put("imgFront", userRealNameDetails.getImgFront()); //银行卡正面照
        requestMsg.put("imgBack", userRealNameDetails.getImgBack()); //银行卡正面照
        requestMsg.put("mercId", PlatformType.XFYLMALL.getId());; //银行卡正面照
        requestMsg.put("platform",  PlatformType.XFYLMALL.getCode());; //银行卡正面照

        gaohuitongPlatformBusiHandler.addImageInfo(requestMsg,responseMsg);

    }

    /**
     * 查询余额
     *
     * @throws Exception
     */
    @Test
    public void queryBalanceInfo() throws Exception {
        ResponseMsg responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.queryBalanceInfo(msg, responseMsg);
        System.out.println("用户余额：" + JSON.toJSONString(responseMsg));

        responseMsg = new ResponseMsg();
        msg.remove("merchantId");
        gaohuitongPlatformBusiHandler.queryBalanceInfo(msg, responseMsg);
        System.out.println("公司账户余额：" + JSON.toJSONString(responseMsg));

    }


    /**
     * 绑卡查询
     *
     * @throws Exception
     */
    @Test
    public void queryCardBind() throws Exception {
        ResponseMsg responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.queryCardBind(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
    }

    /**
     * 实名认证
     *
     * @throws Exception
     */
    @Test
    public void userRealName() throws Exception {
        msg.put("methodType", "UserRealName");
        JSONObject dataObj = response(msg);
    }

    /**
     * 提现绑卡
     *
     * @throws Exception
     */
    @Test
    public void withdrCardBind() throws Exception {
        /*msg.put("methodType", "CardBind");
        JSONObject dataObj = response(msg);
        Object agrNo = dataObj.get("agrNo");
        Object smsOrderNo = dataObj.get("smsOrderNo");
        msg.put("agrNo", agrNo);
        msg.put("smsOrderNo", smsOrderNo);*/
        RequestMsg requestMsg = new RequestMsg();
        String s = "{\"agrNo\":\"uBAkHnBxXi+3Rki+Obv6og==\",\"bankCardImgFront\":\"http://static-xfyinli.yinli.gdxfhl.com/bankCard/xfyinli/tysnjg8q12xg8p1jb5aq.jpeg\",\"bankCardName\":\"杨世平\",\"bankCardNo\":\"lrb70tt5iIxsN86khe+LJrjDuyD5RnPK3X13zm5LUIU=\",\"bankCardType\":\"01\",\"bankCity\":\"\",\"bankCode\":\"ICBC\",\"bankNo\":\"\",\"bankProv\":\"\",\"cardBindId\":2052,\"cardNo\":\"347Vcs3/D647+8h1waBhOcA02YMfXCG53boQVd0XtGQ=\",\"cardType\":1,\"certNo\":\"350629196405160014\",\"clientIp\":\"120.38.144.205\",\"imgBack\":\"http://static-xfyinli.yinli.gdxfhl.com/idCard/xfyinli/iznf4eqbgvdm51wwjawo.jpeg\",\"imgFront\":\"http://static-xfyinli.yinli.gdxfhl.com/idCard/xfyinli/b5gsqw39qtohrenvsida.jpeg\",\"mercId\":\"888000000000003\",\"mercName\":\"幸福引力\",\"methodType\":\"CardBindConfirm\",\"mobile\":\"7c06igw+5dhrGPWktQideQ==\",\"nickname\":\"13850593098\",\"platform\":\"XFYLMALL\",\"requestId\":\"1576766714359\",\"routeCode\":\"GAOHUITONG\",\"smsCode\":\"485668\",\"smsOrderNo\":\"201912190001002930\",\"sysCnl\":\"ANDROID\",\"timestamp\":\"1576766713\",\"tradeType\":\"APP\",\"userId\":2005799,\"userNo\":\"20001307379\",\"userOperNo\":\"10001307379\",\"userOper_name\":\"杨世平\"}";
        Map map1 = JSON.parseObject(s, Map.class);
        requestMsg.putAll(map1);
        ResponseMsg responseMsg = gaohuitongWithdrOrderBusiHandler.withdrCardBind(requestMsg);

    }

    //提现绑卡短信
    @Test
    public void cardBindSms() throws Exception {
        msg.put("methodType", "CardBindSms");
        JSONObject dataObj = response(msg);
    }

    //提现绑卡短信确认
    @Test
    public void CardBindConfirm() throws Exception {
        SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>().eq("sms_order_no", msg.get("smsOrderNo")));
        msg.put("smsCode", smsOrder.getSmsCode());
        msg.put("methodType", "CardBindConfirm");
        JSONObject dataObj = response(msg);

    }

    //提现申请
    @Test
    public void WithdrApply() throws Exception {
        msg.put("methodType", "WithdrApply");
        JSONObject dataObj = response(msg);
    }

    //提现订单，转账
    @Test
    public void withdrOrder() throws Exception {
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("order_status", WithdrOrderStatus.WAIT.getId()).eq("order_no", msg.get("orderNo")));
        Map<String, Object> data = new HashMap<String, Object>();
        data.putAll(BeanUtils.beanToMap(withdrOrder));
        RequestMsg requestMsg = new RequestMsg(data);
        withdrOrderHandler.execute(requestMsg);
    }

    //提现订单，代付
    @Test
    public void queryWithdrOrder() throws Exception {
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("order_status", WithdrOrderStatus.BANK_WAIT.getId()).eq("withdr_order_no", msg.get("withdrOrderNo")));
        Map<String, Object> data = new HashMap<String, Object>();
        data.putAll(BeanUtils.beanToMap(withdrOrder));
        RequestMsg requestMsg = new RequestMsg(data);
        withdrOrderQueryHandler.execute(requestMsg);
    }

    public JSONObject response(RequestMsg msg) throws Exception {
        Map<String, Object> map = msg.getMap();
        String sign = sign(map);
        System.out.println(sign);
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(map));
        System.out.println(reqJson);
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
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

    //商户注册
    public void baseInfoRegister() throws Exception {
        ResponseMsg responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.baseInfoRegister(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
        responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.addImageInfo(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
        responseMsg = new ResponseMsg();
        gaohuitongPlatformBusiHandler.initiateBusi(msg, responseMsg);
        System.out.println(JSON.toJSONString(responseMsg));
    }
}