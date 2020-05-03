package com.mppay.core.sign;

import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AESCoder1 {

	public static final String KEY_ALGORITHM = "AES";
    
	//加密-解密算法 / 工作模式 / 填充方式
	public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";
	
	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	/**
	 * 转换密钥
	 * @param key 二进制密钥
	 * @return 密钥
	 */
	private static Key toKey(byte[] key) throws Exception {
		//实例化密钥材料
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		return secretKey;
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
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		
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
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		
		//初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		
		//执行操作
		return cipher.doFinal(data);
	}
	
	/**
	 * 生成密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] initKey() throws Exception {
		//实例化
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		// AES要求密钥长度为128位，192位或256位
		kg.init(128);
		// 生成秘密密钥
		SecretKey secretKey = kg.generateKey();
		// 获得密钥的二进制编码形式
		return secretKey.getEncoded();
	}
	
	public static void main(String[] args) throws Exception {
		
		//密钥
		String strKey = "IlQew6ZlFUgH0Q6+UpVKxg==";
		String inputStr = "AES";
		System.out.println("原文：\t" + inputStr);
		System.out.println("密钥：\t" + strKey);
		
		//明文转换成二进制编码
		byte[] inputData = inputStr.getBytes();

//		byte[] key = AESCoder.initKey();
//		System.out.println(Base64.encodeBase64String(key));
		//密钥base64解码，返回二进制编码
		byte[] key = Base64.decodeBase64(strKey);
		
		inputData = AESCoder1.encrypt(inputData, key);
	
		System.out.println("加密后：\t" + Base64.encodeBase64String(inputData));
		
		byte[] outputData = AESCoder1.decrypt(inputData, key);
	
		String outputStr = new String(outputData);
	
		System.out.println("解密后：\t" + outputStr);
	
	}
}