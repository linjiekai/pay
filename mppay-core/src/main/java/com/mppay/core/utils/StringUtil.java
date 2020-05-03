package com.mppay.core.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static String[] bSubstringArray(String s, int length) throws Exception {
		length = length - 1;
		String str = substring(s, length);
		List<String> list = new ArrayList<String>();
		list.add(str);
		while (str.length() < s.length()) {
			int l = str.length();
			s = s.substring(l, s.length());
			str = substring(s, length);
			list.add(str);
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	public static String bSubstring(String s, int length) throws Exception {
		byte[] bytes = s.getBytes("Unicode");
		int n = 0; // 表示当前的字节数
		int i = 2; // 要截取的字节数，从第3个字节开始
		for (; i < bytes.length && n < length; i++) {
			// 奇数位置，如3、5、7等，为UCS2编码中两个字节的第二个字节
			if (i % 2 == 1) {
				n++; // 在UCS2第二个字节时n加1
			} else {
				// 当UCS2编码的第一个字节不等于0时，该UCS2字符为汉字，一个汉字算两个字节
				if (bytes[i] != 0) {
					n++;
				}
			}
		}
		// 如果i为奇数时，处理成偶数
		if (i % 2 == 1) {
			// 该UCS2字符是汉字时，去掉这个截一半的汉字
			if (bytes[i - 1] != 0) {
				i = i - 1;
				// 该UCS2字符是字母或数字，则保留该字符
			} else {
				i = i + 1;
			}
		}

		return new String(bytes, 0, i, "Unicode");
	}

	public static String substring(String str, int toCount) {
		int reInt = 0;
		String reStr = "";
		if (str == null) {
			return "";
		}
		char[] tempChar = str.toCharArray();
		for (int kk = 0; (kk < tempChar.length && toCount > reInt); kk++) {
			String s1 = String.valueOf(tempChar[kk]);
			byte[] b = s1.getBytes();
			reInt += b.length;
			reStr += tempChar[kk];
		}
		return reStr;
	}

	/*
	 * 去掉字符串首尾的 <空格字符>（包括TAB），如果s为null则返回空字符串""。
	 */
	public static String trim(String str) {
		return str == null ? "" : str.trim();
	}

	public static String trim(Object o) {
		if (null != o) {
			return trim(o.toString());
		}
		return "";
	}

	/*
	 * 去掉字符串首部位置中连续出现的chars中的字符， 如chars=" ,;#$";s=",#$, abcd"，则返回abcd。
	 */
	public static String trimLeft(String chars, String s) {

		if (s == null) {
			return "";
		}
		if (s.length() <= 0) {
			return s;
		}
		if (chars == null) {
			return s;
		}
		if (chars.length() <= 0) {
			return s;
		}
		int i = 0;
		for (i = 0; i < s.length(); i++) {
			if (-1 == chars.indexOf(s.charAt(i))) {
				break;
			}
		}
		return s.substring(i);
	}

	/*
	 * 去掉字符串尾部位置中连续出现的chars中的字符， 如chars=" ,;#$";s=",#$, abcd"，则返回abcd。
	 */
	public static String trimRight(String chars, String s) {
		if (s == null) {
			return "";
		}
		if (s.length() <= 0) {
			return s;
		}
		if (chars == null) {
			return s;
		}
		if (chars.length() <= 0) {
			return s;
		}
		int i = s.length();
		for (i = s.length() - 1; i > -1; i--) {
			if (-1 == chars.indexOf(s.charAt(i))) {
				break;
			}
		}
		if (i < 0) {
			return "";
		}
		return s.substring(0, i + 1);
	}

	/*
	 * 去掉字符串中连续出现的chars中的字符， 如chars=" ,;#$";s=",#$, ab,c##d;;;"，则返回abcd。 added by
	 * liuxd 2004-12-02
	 */
	public static String trimAll(String chars, String s) {

		if (s == null) {
			return "";
		}
		if (s.length() <= 0) {
			return s;
		}
		if (chars == null) {
			return s;
		}
		if (chars.length() <= 0) {
			return s;
		}
		int i = 0, j = 0;
		StringBuffer sb = new StringBuffer(s);
		do {
			j = sb.length();
			for (i = 0; i < sb.length(); i++) {
				if (chars.indexOf(sb.charAt(i)) != -1) {
					sb.replace(i, i + 1, "");
				}
			}
		} while (j != sb.length());
		return sb.toString();
	}

	/*
	 * 压缩字符串中连续 <空格字符>，及将多个连续的 <空格字符>变成一个 <空格字符> 如s=",#$, abcd"，则返回,#$, abcd。
	 */
	public static String compressSpace(String s) {
		if (s == null)
			return "";
		String ss = s.trim();
		String tmp = "";
		boolean lastIsSpace = false;
		for (int i = 0; i < ss.length(); i++) {
			if (ss.charAt(i) != ' ' && ss.charAt(i) != '\t') {
				tmp += ss.charAt(i);
				lastIsSpace = false;
			} else if (!lastIsSpace) {
				tmp += ' ';
				lastIsSpace = true;
			} else {
				continue;
			}
		}
		return tmp;
	}

	/*
	 * 判断字符串是否为数字字符串。是则返回true，否则返回false。
	 */
	public static boolean isDigitalString(String s) {
		if (s == null) {
			return false;
		}
		if (s.length() == 0) {
			return false;
		}
		String ds = "0123456789";
		for (int i = 0; i < s.length(); i++) {
			if (ds.indexOf(s.charAt(i)) < 0) {
				return false;
			}
		}
		return true;
	}

	/*
	 * 判断字符串是否为英文字符串。是则返回true，否则返回false。
	 */
	public static boolean isLetterString(String s) {
		if (s == null) {
			return false;
		}
		if (s.length() == 0) {
			return false;
		}
		String ds = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = 0; i < s.length(); i++) {
			if (ds.indexOf(s.charAt(i)) < 0) {
				return false;
			}
		}
		return true;
	}

	/*
	 * ISO8859字符串转换成GB2312字符串
	 */
	public static String ISO8859toGB2312(String s) {
		try {
			return new String(s.getBytes("ISO-8859-1"), "GB2312");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/*
	 * ISO8859字符串转换成GBK字符串（目前该函数已经失效）
	 */
	public static String DBCharset2HostCharset(String s) {
		try {
			return new String(s.getBytes("ISO-8859-1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * 
	 * <p>
	 * 方法名称: fixZeros|描述: 在字符串前补0
	 * </p>
	 * 
	 * @param length 长度
	 * @param num    原字符串或数字
	 * @return
	 */
	public static String fixZeros(int length, Object num) {
		StringBuilder sb = new StringBuilder("");
		int len = null == num ? 0 : num.toString().length();
		if (len > length) {
			return num.toString().substring(len - length, len);
		}
		for (int i = 0; i < length - num.toString().length(); i++) {
			sb.append("0");
		}
		return sb.append(num).toString();
	}

	/**
	 * 
	 * <p>
	 * 方法名称: isMatcher|描述:匹配正则表达式
	 * </p>
	 * 
	 * @param str   匹配的字符串
	 * @param regex 正则表达式
	 * @return
	 */
	public static boolean isMatcher(String str, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	public static int compareInt(Object source, Object target) {
		if (null == source) {
			return -1;
		}

		if (null == target) {
			return -1;
		}
		source = source + "";
		target = target + "";
		return source.toString().compareTo(target.toString());
	}

	public static int compareString(Object source, Object target) {
		if (null == source) {
			return -1;
		}

		if (null == target) {
			return -1;
		}

		return source.toString().compareTo(target.toString());
	}

	/**
	 *  计算时间
	 * @param period     数量
	 * @param periodUnit 有效期单位 00-分 01-小时 02-日 03-月
	 * @return
	 */
	public static String computTime(Integer period, String periodUnit) {
		
		long currentTime = 0;
		switch (periodUnit) {
		case "00":
			currentTime += period * 60 * 1000;
			break;
		case "01":
			currentTime += period * 60 * 60 * 1000;
			break;
		case "02":
			currentTime += period * 24 * 60 * 60 * 1000;
			break;
		case "03":
			currentTime += period * 30 * 24 * 60 * 60 * 1000;
			break;
		default:
			currentTime += period * 2 * 24 * 60 * 60 * 1000;
			break;
		}

		return DateTimeUtil.expTime(currentTime);
	}

	/**
	 * @Description(描述): 随机数
	 * @auther: Jack Lin
	 * @param :[length]
	 * @return :java.lang.String
	 * @date: 2019/9/4 21:40
	 */
	public static String getRandom(int length) {
		Random random = new Random();
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < length; i++) {
			boolean isChar = (random.nextInt(2) % 2 == 0);// 输出字母还是数字
			if (isChar) { // 字符串
				int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写字母还是小写字母
				ret.append((char) (choice + random.nextInt(26)));
			} else { // 数字
				ret.append(Integer.toString(random.nextInt(10)));
			}
		}
		return ret.toString();
	}
}
