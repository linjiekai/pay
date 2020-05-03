package com.mppay.service.service.common.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CipherServiceImpl implements ICipherService {

	@Autowired
    private IDictionaryService dictionaryService;
	
	@Override
	public String decryptAES(String content) {
		String aesKey = dictionaryService.findForString("SecretKey", "AES","0");
		String aesIv = dictionaryService.findForString("SecretKey", "IV","0");

		try {
			content = AESCoder.decrypt(content, aesKey, aesIv);
		} catch (Exception e) {
			log.error("解密失败， content={}", content);
			throw new BusiException(31102);
		}
		
		return content;
	}

	@Override
	public String encryptAES(String content) {
		String aesKey = dictionaryService.findForString("SecretKey", "AES","0");
		String aesIv = dictionaryService.findForString("SecretKey", "IV","0");

		try {
			content = AESCoder.encrypt(content, aesKey, aesIv);
		} catch (Exception e) {
			log.error("加密失败， content={}", content);
			throw new BusiException(31102);
		}
		
		return content;
	}

}
