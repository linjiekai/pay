package com.mppay.core.utils;

import java.util.Arrays;

public class HexStr {

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	public static void main(String[] args) {  
	    byte[] bt = new byte[]{10, 2, 12, 14, 1, 0, 0, 1, 0, 31, 45, 1, 8, 0, 1, 0, -96, -45, 10, 3};  
	    System.out.println(bytesToHexString(bt));  
	    System.out.println(Arrays.toString(hexStringToBytes("0A020C0E01000001001F2D0108000100A0D30A03")));  
	}  
}
