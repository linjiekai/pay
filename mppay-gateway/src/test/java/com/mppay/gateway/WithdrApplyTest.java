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
import com.mppay.core.utils.DateTimeUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WithdrApplyTest extends BaseTest{
	@Test
	public void testDepositRechargeAPI() throws Exception {
		// o1gEVs8EouIDNZqwflQy-jDFDUu4
		// http://localhost:8080/pay/pay/unifiedportal.do?userId=00&periodUnit=00&period=30&requestId=1222222&signType=md5&sysCnl=WAP&orderId=201600000&tradeType=APP&bankCode=WEIXIN&orderTime=150203&orderDate=20160317&amount=0.01&mercOrdNo=20160519000&methodType=DirectPay&mobile=&version=1.0&sign=1111&notifyUrl=00
		//methodType,(callbackUrl),notifyUrl,requestId,orderNo,orderDate,orderTime,price,version,tradeType,(sysCnl),bankCode,appId,userId,period,periodUnit,clientIp,(goodsId),(goodsName)
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("methodType", "WithdrApply");
		map.put("requestId", DateTimeUtil.getTime() + "");
		map.put("mercId", "888000000000001");
		map.put("platform", "MPMALL");
		map.put("orderNo", DateTimeUtil.getTime() + "");
		map.put("orderDate", DateTimeUtil.date10());
		map.put("orderTime", DateTimeUtil.time8());
		map.put("price", 1);
		map.put("sysCnl", "ANDROID");
		map.put("userId", 13);
		map.put("bankCode", BankCode.WEIXIN.getId());
		map.put("clientIp", "192.168.0.1");
		map.put("checkName", "NO_CHECK");
		map.put("bankCardName", "dsfdsfsdf");
		map.put("bankCardNo", "oCDJG52Lme5r4NXPJ9KjjSGf80PA");
		map.put("X-MP-SignVer", "v1");
		
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
	public void testQueryWithdrOrder() throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("requestId", DateTimeUtil.getTime());
		map.put("mercId", "888000000000001");
		map.put("methodType", "QueryWithdrOrder");
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
