package com.mppay.core.sign;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

public class DES3Coder {

	public static final String KEY_ALGORITHM = "DESede";
    
	//加密-解密算法 / 工作模式 / 填充方式
	public static final String CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding";
	
	/**
	 * 转换密钥
	 * @param key 二进制密钥
	 * @return 密钥
	 */
	private static Key toKey(byte[] key) throws Exception {
		//实例化DES密钥材料
		DESedeKeySpec keySpec = new DESedeKeySpec(key);
		//实例化秘密密钥工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		//生成秘密密钥
		return keyFactory.generateSecret(keySpec);
	}
	
	/**
	 * 解密
	 * @param data 待解密数据
	 * @param key 密钥
	 * @return 解密数据
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		Key k = toKey(key);
		
		/**
		 * 实例化
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		
		//初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		
		//执行操作
		return cipher.doFinal(data);
	}
	
	/**
	 * 加密
	 * @param data 待加密数据
	 * @param key 密钥
	 * @return 加密数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		Key k = toKey(key);
		
		/**
		 * 实例化
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		
		//初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		
		//执行操作
		return cipher.doFinal(data);
	}
	
	public static byte[] initKey() throws Exception {
		//实例化
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		// 要求密钥长度为112位，168位
		kg.init(168);
		// 生成秘密密钥
		SecretKey secretKey = kg.generateKey();
		// 获得密钥的二进制编码形式
		return secretKey.getEncoded();
	}
	
	public static void main(String[] args) throws Exception {
		String inputStr = "DESede";
		String keyStr = "iUSl6c3jrhPmryCUXOISpvEb5bFvJaNLkhRJM//Z6tU=";
		
		System.out.println("原文：\t" + inputStr);
		System.out.println("密钥：\t" + keyStr);
		
//		byte[] key = AESCoder.initKey();
		
		//密钥base64解码，返回二进制编码
		byte[] key = Base64.decodeBase64(keyStr);
	
		byte[] inputData = inputStr.getBytes();
		//数据加密
		inputData = DES3Coder.encrypt(inputData, key);
		System.out.println("加密后：\t" + Base64.encodeBase64String(inputData));
		
		//数据解密，返回二进制编码数据
		byte[] outputData = DES3Coder.decrypt(inputData, key);
		//二进制编码数据转换成字符串
		String outputStr = new String(outputData);
		System.out.println("解密后：\t" + outputStr);
	}
}