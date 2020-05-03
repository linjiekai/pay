package com.mppay.service.service.common;

public interface ICipherService {

	/**
	 * @Description(描述): 解密
	 * @auther: Jack Lin
	 * @param :[content]
	 * @return :java.lang.String
	 * @date: 2020/4/11 16:21
	 */
	public String decryptAES(String content);

	/**
	 * @Description(描述): 加密
	 * @auther: Jack Lin
	 * @param :[content]
	 * @return :java.lang.String
	 * @date: 2020/4/11 16:21
	 */
	public String encryptAES(String content);
}
