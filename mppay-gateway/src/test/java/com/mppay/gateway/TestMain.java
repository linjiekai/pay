package com.mppay.gateway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mppay.core.utils.RedisUtil;


public class TestMain{

	
	public static void main(String[] args) {
		String userInfo = "{\"session_key\":\"+U6nsId\\/oXpYUgXM4zfo3A==\",\"openid\":\"oN_R75U1DDnVzUy-IT3JgH0fk2FM\"}";
		JSONObject userJSON = JSON.parseObject(userInfo);
		
		System.out.println(userJSON.get("session_key"));
	}
}
