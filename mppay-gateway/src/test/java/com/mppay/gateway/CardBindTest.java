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
import com.mppay.core.sign.AESCoder;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CardBindTest extends BaseTest {
    // 密钥
    private static final String CRYPT_KEY = "y2W8CL6BkRrFlJPN";

    // 密钥偏移量IV
    private static final String IV_STRING = "dMbtHORyqseYwE0o";
	@Test
	public void testCardBing() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		
		
		map.put("requestId", System.currentTimeMillis() + "");
		map.put("methodType", "CardBind");
		map.put("mercId", "888000000000002");
		map.put("platform", "MWJMALL");
		map.put("bankCardName", "aaaa");
		map.put("bankCardNo", AESCoder.encrypt("1245698797", CRYPT_KEY, IV_STRING));
		map.put("bankCardType", 8);
		map.put("bankNo", "0001");
		map.put("userId", 62);
		map.put("bankCode", BankCode.WEIXIN.getId());
		map.put("clientIp", "192.168.0.1");
		map.put("X-MP-SignVer", "v1");

		String sign = sign(map);

		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).header("X-MP-SignVer", "v1")
				.header("X-MP-Sign", sign).content(itemJSONObj.toJSONString()).characterEncoding("utf-8"));

		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();

		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");

		Assert.assertEquals(200, resultStr);
	}

	@Test
	public void testUnCardBing() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("requestId", System.currentTimeMillis() + "");
		map.put("methodType", "UnCardBind");
		map.put("mercId", "888000000000001");
		map.put("platform", "MPMALL");
		map.put("bankCardNo", "1245698797");
		map.put("userId", 1);
		map.put("bankCode", BankCode.WEIXIN.getId());
		map.put("clientIp", "192.168.0.1");
		map.put("X-MP-SignVer", "v1");

		String sign = sign(map);

		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).header("X-MP-SignVer", "v1")
				.header("X-MP-Sign", sign).content(itemJSONObj.toJSONString()).characterEncoding("utf-8"));

		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();

		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");

		Assert.assertEquals(200, resultStr);
	}
//	{"mercId":"888000000000004","methodType":"QueryCardBind","X-MPMALL-SignVer":"v1","userId":11181,"platform":"ZBMALL"}
	@Test
	public void testQueryAPI() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("methodType", "QueryCardBind");
		map.put("mercId", "888000000000004");
		map.put("userId", 11181);
//		map.put("requestId", System.currentTimeMillis());
		map.put("platform", "ZBMALL");
		
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
		map.put("userId", 59);
//		map.put("requestId", System.currentTimeMillis());
		map.put("platform", "MPWJMALL");
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
}
