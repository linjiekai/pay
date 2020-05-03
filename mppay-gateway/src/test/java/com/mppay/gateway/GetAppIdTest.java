package com.mppay.gateway;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mppay.core.constant.BankCode;
import com.mppay.core.constant.TradeType;
import com.mppay.core.utils.DateTimeUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GetAppIdTest extends BaseTest{
	private static final String OPTIONAL = "^\\[\\w+(-\\w+)*\\]$";
	
	@Test
	public void testDirect() throws Exception {
		System.out.println("[Go]".matches(OPTIONAL));
	}
	
	@Test
	public void testDirectPrePayAPI() throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
//		bankCode=WEIXIN&mercId=888000000000003&methodType=GetAppId&operType=1&platform=XFYLMALL&requestId=1575967792282&sysCnl=IOS&tradeType=APP&key=1234567890123456
				
		map.put("bankCode", "WEIXIN");
		map.put("requestId", DateTimeUtil.getTime() + "");
		map.put("methodType", "GetAppId");
		map.put("platform", "XFYLMALL");
		map.put("sysCnl", "ANDROID");
		map.put("mercId", "888000000000003");
		map.put("tradeType", "APP");
		map.put("operType", 1);
		String sign = sign(map);
		
		
//		parameterMap.put("sign", sign);
		
//		request.getHeader("X-MP-Sign");
//		for (Map.Entry<String, Object> entry : parameterMap.entrySet()) { 
//		  
//		  if (!StringUtils.isBlank(entry.getKey()) && null != entry.getValue()) {
//			  requestBuilder.param(entry.getKey(), entry.getValue().toString());
//		  }
//		  
//		}
//		String jsonStr = (String) JSONObject.toJSON(parameterMap);
		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.header("X-MP-SignVer", "v1")
				.header("X-MP-Sign", sign)
				.content(itemJSONObj.toJSONString())
				.characterEncoding("utf-8")
				);
		

		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		/*
		 * http://localhost:8080/pay/pay/unifiedportal.do?requestId=1222222&orderId=201600000&orderDate=20160317&amount=
		 * 0.01&
		 * methodType=DirectPay&mobile=&version=1.0&sign=Zjk3ZTcwYzExZjEzZjhmYjMwZGViMDIzNDU4YWY3YWU=&callbackUrl=qq&
		 * notifyUrl=rr&
		 * periodUnit=00&period=30&signType=md5&sysCnl=WWW&bankCode=weixin&currency=00&orderTime=20160415&mercOrdNo=
		 * 20160415000& productId=00&productName=00&userId=00&bankUserId=00
		 */
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		
		Assert.assertEquals(200, resultStr);
	}
	
	@Test
	public void testPayAPI() throws Exception {
		// o1gEVs8EouIDNZqwflQy-jDFDUu4
		// http://localhost:8080/pay/pay/unifiedportal.do?userId=00&periodUnit=00&period=30&requestId=1222222&signType=md5&sysCnl=WAP&orderId=201600000&tradeType=APP&bankCode=WEIXIN&orderTime=150203&orderDate=20160317&amount=0.01&mercOrdNo=20160519000&methodType=DirectPay&mobile=&version=1.0&sign=1111&notifyUrl=00
		//methodType,(callbackUrl),notifyUrl,requestId,orderNo,orderDate,orderTime,price,version,tradeType,(sysCnl),bankCode,appId,userId,period,periodUnit,clientIp,(goodsId),(goodsName)
		
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("mercId", "888000000000003");
		map.put("platform", "XFYLMALL");
		map.put("prePayNo", "157373740318200011849");
		map.put("methodType", "DirectPay");
		
		map.put("tradeType", TradeType.APP.getId());
		map.put("bankCode", BankCode.WEIXIN.getId());
//		map.put("openId", "oCDJG5wv0pS1ezcsW05gJF1_AXeI");
		map.put("sysCnl", "IOS");
		map.put("clientIp", "27.47.152.46");
		
		String sign = sign(map);
//		parameterMap.put("sign", sign);
		
//		request.getHeader("X-MP-Sign");
//		for (Map.Entry<String, Object> entry : parameterMap.entrySet()) { 
//		  
//		  if (!StringUtils.isBlank(entry.getKey()) && null != entry.getValue()) {
//			  requestBuilder.param(entry.getKey(), entry.getValue().toString());
//		  }
//		  
//		}
//		String jsonStr = (String) JSONObject.toJSON(parameterMap);
		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).header("X-MP-SignVer", "v1").header("X-MP-Sign", sign).content(itemJSONObj.toJSONString()));
		
		
		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		/*
		 * http://localhost:8080/pay/pay/unifiedportal.do?requestId=1222222&orderId=201600000&orderDate=20160317&amount=
		 * 0.01&
		 * methodType=DirectPay&mobile=&version=1.0&sign=Zjk3ZTcwYzExZjEzZjhmYjMwZGViMDIzNDU4YWY3YWU=&callbackUrl=qq&
		 * notifyUrl=rr&
		 * periodUnit=00&period=30&signType=md5&sysCnl=WWW&bankCode=weixin&currency=00&orderTime=20160415&mercOrdNo=
		 * 20160415000& productId=00&productName=00&userId=00&bankUserId=00
		 */
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		
		Assert.assertEquals(200, resultStr);
	}
	
	@Test
	public void testOrderQuery() throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("requestId", DateTimeUtil.getTime());
		map.put("mercId", "888000000000001");
		map.put("methodType", "OrderQuery");
		map.put("orderNo", "2019042400000266");
		map.put("X-MP-SignVer", "v1");
		
		String sign = sign(map);
		
		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).header("X-MP-SignVer", "v1").header("X-MP-Sign", sign).content(itemJSONObj.toJSONString()));
		
		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		
		Assert.assertEquals(200, resultStr);
	}
	
}
