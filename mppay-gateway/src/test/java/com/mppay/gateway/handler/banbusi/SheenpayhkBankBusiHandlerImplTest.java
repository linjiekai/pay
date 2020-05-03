package com.mppay.gateway.handler.banbusi;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.constant.SheepayhkApiUrlType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.BaseTest;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.dto.platform.sheen.SheenReq;
import com.mppay.gateway.dto.platform.sheen.SheenResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SheenpayhkBankBusiHandlerImplTest extends BaseTest {
    private static final String OPTIONAL = "^\\[\\w+(-\\w+)*\\]$";
    private static final String MIDBRACKETS = "\\[|\\]";



    RequestMsg msg = new RequestMsg();
    ResponseMsg responseMsg = new ResponseMsg();
    private String prePayNo ;

    @Before
    public void setUp() throws Exception {
        super.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
// 请求公共参数
        msg.put("userId",942l); //用户id
        msg.put("sysCnl", SheepayhkApiUrlType.WXPUBLIC.getId());
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
        msg.put("tradeCode", "01");
        msg.put("clientIp", "192.168.0.1");
        msg.put("goodsId", 111);
        msg.put("goodsName", "商品名称");
        msg.put("notifyUrl", "http://127.0.0.1:15111/shop/mobile/pay/notify");
        JSONObject response = response(msg);
        System.out.println(response.toString());
        prePayNo=(String) response.get("prePayNo");
    }

    @Test
    public void unifiedOrder() throws Exception {
       DirectPrePay();
        msg.put("prePayNo",prePayNo);
        msg.put("tradeType", "MWEB");
        msg.put("openId", "oCDJG53LTJWZBjmMl4rV4NT0dSVE");
        msg.put("bankCode", "WEIXIN");
        msg.put("bankCardType", "08");
        msg.put("methodType", "DirectPay");
        msg.put("callbackUrl", "http://127.0.0.1:15111/shop/mobile/pay/notify");
        JSONObject response = response(msg);
        System.out.println(response.toString());
    }

    @Test
    public void queryOrder() {
    }

    @Test
    public void refundOrder() {
    }

    @Test
    public void queryRefundOrder() {
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


    public String sign (Map<String, Object> map) throws Exception {
        String itfParam = super.wac.getEnvironment().getProperty("req." + map.get("methodType"));
        String[] keys = itfParam.split(",");

        // 是否属于非必传参数,配置文件中使用()来区分非必传参数
        boolean isOption = false;
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        int index = 0;
        Object value = null;
        for (String key : keys) {
            value =  map.get(key);
            isOption = key.matches(OPTIONAL);
            if (isOption) {
                key = key.replaceAll(MIDBRACKETS, "");
                if (null != map.get(key)) {
                    value = map.get(key).toString();
                    parameterMap.put(key, value);
                }
                keys[index] = key;
            } else if (null != value && !StringUtils.isEmpty(value.toString())) {
                parameterMap.put(key, value.toString());
            } else {
                throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", key));
            }
            index++;
        }

        String plain = Sign.getPlain(parameterMap, keys);
        plain += "&key=1234567890123456";
        System.out.println("plain:"+plain);
        String sign = Sign.sign(plain);
        return sign;
    }
}