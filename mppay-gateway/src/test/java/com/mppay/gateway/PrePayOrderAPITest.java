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
public class PrePayOrderAPITest extends BaseTest{
	
	@Test
	public void testPrePayOrderAPI() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("methodType", "PayCashier");
		map.put("mercId", "888000000000002");
		map.put("prePayNo", "156826828801800007266");
		map.put("platform", "MPMALL");
		String sign = sign(map);

		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/mobile/unified").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).header("X-MPMALL-SignVer", "v1").header("X-MPMALL-Sign", sign).content(itemJSONObj.toJSONString()));
		
		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		//
		Assert.assertEquals(200, resultStr);
	}
	
	
	@Test
	public void notifyGHTHK() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("signData", "J7BR3pkg5/B4eyWvR19pEclMholK+h6ejGzQewQTUYDCjzhuC4zvzZx4RijXCJ1FQIhxHKn8La2uT6o04M2iDf4VInrAuVyWUZAI68hr0/ANrC/T1+QoWJrADydtsEnCY53SR8eOof7xw6qOpzGoIGKT9+w8F+KlYgc89VlU5RYZo8vg341Unik+EDdL6sIAtXxH8RRoee07l34h3kfhYAwTedFnuwfW7IWvONU7YdW649RguwojNQ7Ps4uo8BiQht7OqoVE83sl6pcXALG0ZAO8u2SNq7WdUWkJn+RU2QJ0x3EVt9A4JVKLwzAJfu6bVhrUWT5mrVyeo0nxiB/i7w==");
		map.put("agencyId", "549034453990596");
		map.put("encryptKey", "iGaEbHV/10tvwODYoiPYv2+ctgd42eczUTYI2v2hE5gJXenyJ++6WBzHncEz/QNrzjLCoZN/NqCqnP9MDBFDx7cL0MPcTcBfC9M/om02rCWeyP3C/ccV6BDpY2uzWw7vZragXpsTzktHhBhv80O+NcVSomXY3ySA1MwRYr71PlFKneukG88QW0HRey/9ltFyh1Pr09hUC5FQjzI6d9t3mPfj+hb31dFw6XMJ1AejV7rd9Ulu0a962osItfX7RUj3cuDOy+mkEozAOxBWyqdod1uze07jdh2mD8p2UxSA4vNd5cgk7PALwuMMp/kVArxcpJhN5ZdHEGkycd3fd3Axhg==");
		map.put("encryptData", "qHR/NM/plcQAbr88/YAL/BgzIBNwB1WxCha3wRaFgL9+IUSX6uiG5WNX7kYdpbjs8EWOdloKOUD9Fra1SJRF0hUZ9qd5mBWh5S+QNFWODqiNJpjzBPTvHGLj6aHkmXm13ApjHCJTawBoIDC9Hfa3FKXwGqqb+GsOPUfd1BuyAnfauU1WpD4LL0OklUGNRgRZncZbSPqwD5XPZ+wfDR8JDYqZ9Ys72zUEdAz8yPcAfR7PgX0/sdATYqgQU5jilQe61D8jADTTDxIfYTRYy3gtB3XmWUiVZ93+Xc+PFlVQ/9YBpLnSsiuujDotL4fto31jBEF/kxkBLuEECzWKpSCc/fOyDun/pgcYhLHYVixLImcpfd7H05ZEA1QTNYXAUBZJVArzF/t2y/KwQAauHGYi2Hhb1vUoNOMA238xd78t42EfBhvJ/LvMJEZwMWupqBg8O7PH8WvfSwwlLJxwEqOMSP5tvAJwaSxv+Dyc6pjymcYwZFHtbXorhc0gNOgBPOtBhSTdFaUVjDIABktt0fwLWUaXIKIIJBpPsQdq9K1qRgEKE6ly+YiDbEE+CNQzs4bT5loCk8VMtoT1KjHmsq0s0NbRnQWDn9fhThQjEVaSCH9ZAvG+KRyT26sc0HgRejGK");
		
		JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/notify/gaohuitonghk/offline").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(itemJSONObj.toJSONString()));
		
		MvcResult mrs = result.andDo(MockMvcResultHandlers.print()).andReturn();
		
		int resultStr = mrs.getResponse().getStatus();
		String content = mrs.getResponse().getContentAsString();
		System.out.println("content[" + content + "]");
		//
		Assert.assertEquals(200, resultStr);
	}

}
