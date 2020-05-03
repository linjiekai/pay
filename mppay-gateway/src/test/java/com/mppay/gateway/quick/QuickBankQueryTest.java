package com.mppay.gateway.quick;

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
import com.mppay.core.constant.BusiType;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.BaseTest;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QuickBankQueryTest extends BaseTest{

	@Test
	public void testQuickBankQuery()  throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("requestId", DateTimeUtil.getTime() + "");
		map.put("methodType", "QuickBankQuery");
//		map.put("platform", "MPMALL");
		map.put("platform", "MPWJMALL");
//		map.put("bankCardNo", "6212263602097118699");
//		map.put("bankCardNo", "6222600260001072444");
		map.put("bankCardNo", "6228480086861717477");
		map.put("sysCnl", "IOS");
		map.put("userId", 60);
		map.put("mercId", "888000000000001");
		map.put("clientIp", "192.168.0.1");
		map.put("timestamp", (int)(System.currentTimeMillis() / 1000));
		
		String sign = sign(map);
		System.out.println("==============================="+sign);
		System.out.println("==============================="+JSON.toJSONString(map));

		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.header("X-MPMALL-Sign", sign)
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
