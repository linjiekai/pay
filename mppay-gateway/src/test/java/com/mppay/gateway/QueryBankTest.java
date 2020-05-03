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

@RunWith(SpringRunner.class)
@SpringBootTest
public class QueryBankTest extends BaseTest{
	
	@Test
	public void testQueryAPI() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("methodType", "QueryBank");
		map.put("mercId", "888000000000001");
		map.put("tradeCode", "01");
		map.put("requestId", System.currentTimeMillis() + "");
		map.put("platform", "MPWJMALL");
		map.put("bankType", "1");
		map.put("bankCardType", "01");
		
		String sign = sign(map);

		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.header("X-MP-SignVer", "v1")
				.header("X-MP-Sign", sign)
				.content(itemJSONObj.toJSONString()));
		
		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		//
		Assert.assertEquals(200, resultStr);
	}
	
	
	@Test
	public void testQueryWithdrBankAPI() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("methodType", "QueryWithdrBank");
		map.put("mercId", "888000000000001");
		map.put("userId", 38);
		map.put("requestId", "1565253154168");
		map.put("platform", "MPMALL");
		map.put("X-MP-SignVer", "v1");
		
		String sign = sign(map);

		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.header("X-MP-SignVer", "v1")
				.header("X-MP-Sign", sign)
				.content(itemJSONObj.toJSONString()));
		
		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		//
		Assert.assertEquals(200, resultStr);
	}
	
	@Test
	public void testQuickBankQueryAPI() throws Exception {
		
		String bankCardNo = "6282680071531706";
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("methodType", "QuickBankQuery");
		map.put("mercId", "888000000000003");
		map.put("userId", 38);
		map.put("requestId", "1565253154168");
		map.put("platform", "MPMALL");
		map.put("X-MP-SignVer", "v1");
		map.put("bankCardNo", bankCardNo);
		map.put("tradeCode", "03");
		map.put("sysCnl", "ANDROID");
		map.put("timestamp", System.currentTimeMillis());
		
		
		String sign = sign(map);

		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.header("X-MP-SignVer", "v1")
				.header("X-MP-Sign", sign)
				.content(itemJSONObj.toJSONString()));
		
		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		//
		Assert.assertEquals(200, resultStr);
	}
	
}
