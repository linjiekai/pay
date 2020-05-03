package com.mppay.core.sign;

import java.security.MessageDigest;

/**
 * MD5签名
 *
 */
public class SHACoder {

	public static byte[] encode256(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(data);
	}
	
}
