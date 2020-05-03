package com.mppay.gateway;

import org.apache.commons.codec.binary.Base64;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;


public class TestEncype {
	
	public Map parseMap(String response){
		Map<String,String> map=new HashMap<String,String>();
		
		String [] resp=response.split("&");
		for(int i=0; i<resp.length; i++){
			String key=resp[i].split("=")[0];
			String value=resp[i].split("=")[1];
			map.put(key, value);
		}
		return map;
	}
	public String respDecryption(String response,PublicKey yhPubKey,PrivateKey hzfPriKey){
		Map respMap=this.parseMap(response);
		String xmlData = "";
		try {
//			final PrivateKey hzfPriKey = CryptoUtil.getRSAPrivateKeyByFileSuffix("F:/密钥/test/pkcs8_rsa_private_key_2048.pem", "pem", null, "RSA");
//			final PublicKey yhPubKey = CryptoUtil.getRSAPublicKeyByFileSuffix("F:/密钥/生成证书/rsa_public_key_2048.pem", "pem", "RSA");
//			final PublicKey yhPubKey = CryptoUtil.getRSAPublicKeyByFileSuffix("F:/key/test/test_rsa_public_key_2048.pem", "pem", "RSA");
//			final PrivateKey hzfPriKey = CryptoUtil.getRSAPrivateKeyByFileSuffix("F:/key/test/test_pkcs8_rsa_private_key_2048.pem", "pem", null, "RSA");
			byte[] encryptedBytes = Base64.decodeBase64(respMap.get("encryptKey").toString().getBytes("UTF-8"));
//			String encrtptKey = new String(encryptedBytes, "UTF-8");
			byte[] keyBytes = CryptoUtil.RSADecrypt(encryptedBytes, hzfPriKey, 2048, 11, "RSA/ECB/PKCS1Padding");
			byte[] plainBytes = Base64.decodeBase64(respMap.get("encryptData").toString().getBytes("UTF-8"));
			byte[] xmlBytes = CryptoUtil.AESDecrypt(plainBytes, keyBytes, "AES", "AES/ECB/PKCS5Padding", null);
			xmlData = new String(xmlBytes,"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlData;
	}
	
	public static void main(String[] args) throws Exception {
//		String response="KjacPcICCXWf1sXny6nKXXWhG5K2trInA9cbWBRRCqeR/w8a9UktHM00LY8LdZXfi4ChCi+ZMGMF\n/J2agatgYHgMxsYhoCPlHRBKEvAMiuDyp48bQF81Wu0K7KTqe1J6LpATzbCM2vpPC48TrQjEF1qH\nYCG57wR/nV4lZ8BoDwQPdyWgGMnviEgqlyySJGsd4OxlGmt6poFgf3KRPBnLh07qjDorf+/7+hW5\nl9MpTycEyvaDzfpfKYvTqeqRTwTBvn3Dwh2bY6sM67IzzOIBcJ+fwRqUSN+PZ8ozUauJ0XkMNtDH\nJ8U+bBno71Chp/eMW5hhGMsjbgsaSKThXurNOA==";
////		TestEncype tt=new TestEncype();
////		System.out.println(tt.parseMap(response).toString());
////		System.out.println("解密结果: "+tt.respDecryption(response));
//        byte[] encryptedBytes = Base64.decodeBase64(response.getBytes("UTF-8"));
////		PrivateKey hzfPriKey = TestUtil.getPtPrivateKey();
//        byte[] keyBytes = CryptoUtil.RSADecrypt(encryptedBytes, hzfPriKey, 2048, 11, "RSA/ECB/PKCS1Padding");
//        
//        
//        String data = "GzU/TahFvCAZX0b0j/KuIqTWQ0ZQt+84nnD7wQg7ptf5zpkOkHWwMLLJoTaVKvQCrPPD4rLd8wGXjsywdpDYjUzoMoKjT6kHZUa0+8f/UUKsh6sEo3Ve1Ngv3QTMcPgjHZyB2N02Y3yrObr+T3GUpniTafBCE9laSozZuHX9HmTL709LGBlsBT5SYEkn4Roki7v0DgR66NV/ZWgNNA4h47HpQW2vHNvOpvxDJVoW/ZTpnl2OHlTbejGnH2mXUkMp5o/dSJM7d7uBSw0ruT7A5MJzaQbLuv8gtHPNVmyO23ZnUnddwUELnHAjYKicB93yUgaqEgj0HDJbzf7WtLySBTmQDms+09mBitmQmMAk2Me3bhkNbsEUwXm7EOMnB23HwjeKCV7vLaQekFsW3Sp86zoZQ09uoWNAccdQyaLUL+LB5xpKKJ5GwtFkRDVgcdmtcHWgV52jETEGuIvmFMEjp9HyaUFyfXyPkX4YhsGbUeBaxtifoy2NjHMM7Cw6sCF8eR0koTgDKjf0XD9+9bASytt0ZFTW8tEQb4Wh323if/9QnGUZGveRXQaVsPRPiiID0Nhga/P9ALTQeiF4IJu+Bx2EE6bXdfqJanVxAN09IkBOJJM88kReE9ho1WjoRKpQ6I8tZYUzy9RWlU3sw5mGAEqgZVc6h1V6q+3GdUp7hSBOWcgGd0Hmzp7S9e3q3wKN8L1tYzZXQb3T1rzzpJCz3zHhX93kEV1g8V76XqVejiozd2ZSYHpDWTsx2DQ1XrIRe8QruYZHL3jPx/FfADNEYk7JELugqYymRkGlD/9NeSk0OY3xYr/I4Ag3qkcy74dkvFSEzU45mZ0i9jhNncbhPefXl/uAmC8vlGnjGpQIFJFpcv+Gln0smpZYf+gwoRFcvzROlhEhgu+nqxeaTUvi6tC9fe8921SNWrpeY3D+a9lKBkV+sLGItraf6O5A4+UTdp+bmmV3P6gPh1tICUwH522yGOLlARZdTC1i0GPfiOGIxuYnNBZGQmFvyseIAD9hrH6+gcwxkA7KT1xozzsY5NZt3AgrXWjQR2XiZNUwUcn+TtyjQUZ3e7FQMOEdHBRuLel6nt02XVhDzb0qaBwkqOfZGfJjwUfRP72hpl7hJzzSGOK5bAYLMkjfJWgfgfMIDJo7urVM8s2Wtz19/n5JqQMvLQzgNgWW7fHXjpMPsHMCh/hqCDoVOjyveWRPPL1Ygu4tQKr53xQumaUvj1CahJ2td96M9OQnz09pI/47V6A/jaFyetZNh0Ldsaavr/3YgtbIWIXZlTe8HxGhA+qLt+c1fgPRy2alLtuHZSz2at+txu/zo7ruGxYV6NsHY/JRmy0XPnEY4LKHkh7eXE+OsUBX/LNQc9fcu/IRXnPmQv5QjXsx94WkwSPyN8XDZk7NYUgQ9pTNLpszHijdNVzdU0hr3ouHSTJ1bI9GcgHcTDhocPBHVaYUHDNdF9cgbTZl1xpyqRDzpYIx74G4ShuMKXlb0wlmAkRBoUO=";
//        byte[] plainBytes = Base64.decodeBase64(data.getBytes("UTF-8"));
//        byte[] xmlBytes = CryptoUtil.AESDecrypt(plainBytes, "hhhhhhhhhhhhhhhh".getBytes(), "AES", "AES/ECB/PKCS5Padding", null);
//        String xmlData = new String(xmlBytes,"UTF-8");
//        System.out.println(xmlData);
	}
}
