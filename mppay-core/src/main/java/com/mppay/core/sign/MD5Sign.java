package com.mppay.core.sign;

import java.security.MessageDigest;

/**
 * MD5签名
 *
 */
public class MD5Sign {

	private static final String ALGORITHM = "MD5";
	
	public static byte[] encode(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance(MD5Sign.ALGORITHM);
		return md.digest(data);
	}
	
}
