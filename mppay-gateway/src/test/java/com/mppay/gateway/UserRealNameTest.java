package com.mppay.gateway;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mppay.core.sign.AESCoder1;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRealNameTest extends BaseTest{

	@Test
	public void testUserRealNameAPI() throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		String key = "Sj0dWrO4DOFKkYtXHBAGK+wbVht3ZSYOf/Md4dNMQKM=";
		String cardNo = "440825198609133452";
		cardNo = Base64.encodeBase64String(AESCoder1.encrypt(cardNo.getBytes(), Base64.decodeBase64(key)));
		
		map.put("methodType", "UserRealName");
		map.put("mercId", "888000000000001");
		map.put("platform", "MPMALL");
		map.put("cardType", 0);
		map.put("userId", 479);
		map.put("name", "陈妃杭");
		map.put("cardNo", cardNo);
		map.put("imgFront", "https://static-mpmall.mingpinmao.cn/logo/wechat.png");
		map.put("imgBack", "https://static-mpmall.mingpinmao.cn/logo/wechat.png");
		map.put("realSource", 0);
		map.put("sysCnl", "IOS");
		
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
	}
	
	@Test
	public void testUserRealNameDetailsListAPI() throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("methodType", "UserRealNameDetailsList");
		map.put("mercId", "888000000000001");
		map.put("platform", "MPMALL");
//		
//		map.put("userId", 479);
//		map.put("cardNo", "440825198609133452");
		map.put("page", 1);
		map.put("limit", 2);
		
		
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
	}
	
}
