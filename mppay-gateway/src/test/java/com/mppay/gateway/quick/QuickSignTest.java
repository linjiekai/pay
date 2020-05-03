package com.mppay.gateway.quick;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.BaseTest;
import com.mppay.service.service.IDictionaryService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QuickSignTest extends BaseTest{

	@Autowired
	private IDictionaryService dictionaryService;
	
	@Test
	public void testQuickSign()  throws Exception {
		
		String aesKey = dictionaryService.findForString("SecretKey", "AES");
		String aesIv = dictionaryService.findForString("SecretKey", "IV");

		String cardNo = "341126197709218366";
		String bankCardNo = "6216261000000000018";
		String mobile = "13552535506";
		String bankCardName = "全渠道";
		String bankCode = "SPABANK";
		String bankCardType = "01";
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("requestId", DateTimeUtil.getTime() + "");
		map.put("methodType", "QuickSign");
		map.put("platform", "MPMALL");
		map.put("bankCardNo", AESCoder.encrypt(bankCardNo, aesKey, aesIv));
		map.put("cardNo", AESCoder.encrypt(cardNo, aesKey, aesIv));
		map.put("cardType", 1);
		map.put("mobile", AESCoder.encrypt(mobile, aesKey, aesIv));
		map.put("sysCnl", "IOS");
		map.put("bankCardName", bankCardName);
		map.put("bankCardType", bankCardType);
		map.put("bankCode", bankCode);
		map.put("userId", 1392);
		map.put("mercId", "888000000000001");
		map.put("clientIp", "192.168.0.1");
		map.put("timestamp", (int)(System.currentTimeMillis() / 1000));
		
		String sign = sign(map);

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
