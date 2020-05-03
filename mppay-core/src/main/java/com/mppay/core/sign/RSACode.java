package com.mppay.core.sign;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.tomcat.util.codec.binary.Base64;

import com.mppay.core.sign.alipay.AlipaySignature;

public class RSACode {

	public static final String KEY_ALGORITHM = "RSA";
	
	/**
	 * 私钥加密
	 * @param data 待加密数据
	 * @param key 私钥
	 * @return 加密数据
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, byte[] key) throws Exception {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        
        return cipher.doFinal(data);
	}
	
	/**
	 * 私钥解密
	 * @param data 待解密数据
	 * @param key 私钥
	 * @return 解密数据
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, byte[] key) throws Exception {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
	}
	
	/**
	 * 公钥加密
	 * @param data 待加密数据
	 * @param key 公钥
	 * @return 加密数据
	 * @throws Exception
	 */
	public static byte[] encryptByPubicKey(byte[] data, byte[] key) throws Exception {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
	}
	
	/**
	 * 公钥解密
	 * @param data 待解密数据
	 * @param key 公钥
	 * @return 解密数据
	 * @throws Exception
	 */
	public static byte[] decryptByPubicKey(byte[] data, byte[] key) throws Exception {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(data);
	}
	
	public static void main(String[] args) throws Exception {
//		String plain = "alipay_sdk=alipay-sdk-java-3.7.4.ALL&app_id=2019032063559949&biz_content={\"body\":\"我是测试数据\",\"out_trade_no\":\"11111111111111111\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"subject\":\"App支付测试Java\",\"timeout_express\":\"30m\",\"total_amount\":\"0.01\"}&charset=UTF-8&format=json&method=alipay.trade.app.pay&notify_url=商户外网可以访问的异步地址&sign_type=RSA2&timestamp=2019-03-27 19:34:46&version=1.0";

		String plain = "alipay_sdk=alipay-sdk-java-3.7.4.ALL";
		
		String privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCs6HfxbZZzqctDsL4+YBdQi70+4mH8wlTePrtvjzgvVe5x3Ssgb+VgCxkd/VTkYSJdUt+AmC6BJ0+LrDuSuBTQpd228UB48ngLRbpZu0209aHEcPis8WnLNetdCmV3VWT8Hlech3toLZuRu4WiDlnFCjqeruLBumWunArBO8Rce5n6L6IiX+D9iqVXuVaYIcC1EEnPbK6ZNHA2nx7RQU17hi1uOfSDE4QDF17R0pZetpcD89A16ShSHDFxVbQwtqq/R3sbuhcNuBh29pTJkIFobouV7hmGZY3irkxr5Z2cn/EgCQxP1WP1cv8cQ3s08QlMxf0ilIaScaI7rNUYcFLLAgMBAAECggEBAKsDhdX4kIy3S/4YKiFhQs38V+GBYG7w+aWSoCn3mhdfQ2XE3OnJIjF66I27yKHU3Hs3Ay2Z1q0Q52D6qTU08glnc8GEHUNCda6YeXZUhcfMBgJXxeyvHPbKzo4IQVDk2etuSzLa4RipuytJMCOXU/xJYK8fw9Dd/cHUPeLerRjV2ctjoa4suhcG8zVeHGt9wuHqg4hsumHVuQvf0oVHwYYaKZy4TRY2i5bo/5lwrGrKlHJEL685YR2mHnjagN6iID9nKNlhMJxK+1eB+ck7CGIeBflaPvVTdkWK7tOL7Tv0BHVDZI7GWR0SrumQOmyILV8wmyTIhObnBUxnitF+n0ECgYEA4WVC6HX1AipBCtZ1nNot7YCbbklg5YviGRmuhYGFxP8frziRW/0Y64ehLBlSPt1unLh265m1oFGvnz0EcCLWF1Z5pUNE/0qF30gOqj/HZmyM7Pm4w/e68u64MKyAC+4w9o/NPnu17lgwIErTF7dUihilQL7ERu+BneyHIDfKimECgYEAxGK+wduIbFTceJ0GJ4HHG5aCFR1odMvGqAHpBrUF8xspdpBcuTcG1fYyXTkaQLf/OkzoPE1lwVP0STDZlBz32LRbCA2JuIwJI3qgrblAOgCbcvynPEI9ILulLLNmNrTAbEJj7mUqynZ7dlWORzep/RZVT03mNXUBcu6NUt0YZKsCgYEAnVjqj4uhIgTLaTxz2K77Nvz8BM7XBQhVK76g9fSIpRCRVuo/l58loW8KldWLc/VzPZcM9cZVY/kCiEC5c2ruWFZ3D2bZqPqmYExWaIWjtx36d4iRcsTjTqpWDSyvKyT6K3YSCxej1yLbXf++Q6UpMEqqOnuyW+7hZpTzdAteqqECgYBRlmB/s0/TCdJqfXngEeeBS58RXJ1X4IzQobKI6c3l+MPaERGklPrPCzBTUHTarFZphyf4XYNHtTRI7/WEtiA1tIAZMV6k//CP/9MfOVY4gqYSOGrV+wLgRXTSxFlTovZKQ662jOshH+Yc/GBWsAZ8MboRb2CTlt+aOwcVhOssbwKBgQC2ScCgn28byvWx+JnunoCZXGUT6hgTbFa2RSyfYxGndXFxqSngao8YCzj0g0+1cNf39EqNJ5pSFWg65fm79CSglq7VP6N3iLNi5meWTVqvNCTp+CSo3bZ+vwY4bQ0Mhr+vvVezbJfk2YCqYurS4WtshClQaCj3F8IK/nj48zWsjg==";
		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArOh38W2Wc6nLQ7C+PmAXUIu9PuJh/MJU3j67b484L1Xucd0rIG/lYAsZHf1U5GEiXVLfgJgugSdPi6w7krgU0KXdtvFAePJ4C0W6WbtNtPWhxHD4rPFpyzXrXQpld1Vk/B5XnId7aC2bkbuFog5ZxQo6nq7iwbplrpwKwTvEXHuZ+i+iIl/g/YqlV7lWmCHAtRBJz2yumTRwNp8e0UFNe4Ytbjn0gxOEAxde0dKWXraXA/PQNekoUhwxcVW0MLaqv0d7G7oXDbgYdvaUyZCBaG6Lle4ZhmWN4q5Ma+WdnJ/xIAkMT9Vj9XL/HEN7NPEJTMX9IpSGknGiO6zVGHBSywIDAQAB";
		
		String sign = AlipaySignature.rsa256Sign(plain, privateKey, "UTF-8");
		System.out.println(AlipaySignature.rsa256Sign(plain, privateKey, "UTF-8"));
		
		System.out.println(AlipaySignature.rsa256CheckContent(plain, sign, publicKey, "UTF-8"));
	}
	
}
