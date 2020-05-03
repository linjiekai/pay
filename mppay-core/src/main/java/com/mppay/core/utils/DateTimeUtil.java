package com.mppay.core.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateTimeUtil {

	public static String date6() {
		return date6(new Timestamp(System.currentTimeMillis()));
	}

	public static String date6(Timestamp t) {
		return formatTimestamp2String(t, "yyyyMM");
	}
	
	public static String date8() {
		return date8(new Timestamp(System.currentTimeMillis()));
	}

	public static String date8(Object t) {
		return formatTimestamp2String(t, "yyyyMMdd");
	}
	
	public static String date10() {
		return date10(new Timestamp(System.currentTimeMillis()));
	}

	public static String date10(Object t) {
		return formatTimestamp2String(t, "yyyy-MM-dd");
	}
	
	public static String date14() {
		return date14(new Timestamp(System.currentTimeMillis()));
	}

	public static String date14(Object t) {
		return formatTimestamp2String(t, "yyyyMMddHHmmss");
	}
	
	public static String date19() {
		return date19(new Timestamp(System.currentTimeMillis()));
	}

	public static String date19(Object t) {
		return formatTimestamp2String(t, "yyyy-MM-dd HH:mm:ss");
	}
	
	public static String time6() {
		return time6(new Timestamp(System.currentTimeMillis()));
	}

	public static String time6(Object t) {
		return formatTimestamp2String(t, "HHmmss");
	}
	
	public static String time8() {
		return time8(new Timestamp(System.currentTimeMillis()));
	}

	public static String time8(Object t) {
		return formatTimestamp2String(t, "HH:mm:ss");
	}


	public static long getTime() {
		return new Date().getTime();
	}

	/**
	 * 格式化时间戳为字符串
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatTimestamp2String(Object date, String format) {
		if (null == date) {
			return null;
		}
		if (null == format || ("").equals(format)) {
			return null;
		}
		if ((date instanceof Date) || (date instanceof Timestamp)) {
			return new SimpleDateFormat(format).format(date);
		}
		return null;
	}

	/**
	 * 格式化字符串为时间戳
	 * @param date
	 * @param format
	 * @return
	 */
	public static Timestamp formatString2Timestamp(String date, String format) {
		if (null == date || ("").equals(date)) {
			return null;
		}
		if (null == format || ("").equals(format)) {
			return null;
		}
		return new Timestamp(formatStringToDate(date, format).getTime());
	}

	/**
	 * 将日期格式的字符串转换为日期
	 * @param date 源日期字符串
	 * @param format 源日期字符串格式
	 */
	public static Date formatStringToDate(String date, String format) {
		if (null == date || ("").equals(date)) {
			return null;
		}
		if (null == format || ("").equals(format)) {
			return null;
		}
		SimpleDateFormat format2 = new SimpleDateFormat(format);
		try {
			Date newDate = format2.parse(date);
			return newDate;
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}

	}

	public static Timestamp addDay(Timestamp date, int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, day);
		return new Timestamp(cal.getTimeInMillis());
	}

	/**
	 * 将日期格式的字符串格式化成指定的日期格式
	 * @param dateStr 源日期字符串
	 * @param formatStr1 源日期字符串日期格式
	 * @param formatStr2 新日期字符串日期格式
	 */
	public static String formatDateStringToString(String dateStr, String formatStr1, String formatStr2) {
		SimpleDateFormat format = new SimpleDateFormat(formatStr1);
		try {
			if (null == dateStr || ("").equals(dateStr)) {
				return null;
			}
			Date date = format.parse(dateStr);
			return new SimpleDateFormat(formatStr2).format(date);
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	/**
	 * 获得当前时间所在月的第一天和最后一天
	 * @param dateTime yyyy-MM-dd
	 * @return
	 */
	public static Map<String, String> getTime(String dateTime) {
		Map<String, String> timeMap = new HashMap<String, String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int year = 0;
		int month = 0;
		int day = 0;
		if (dateTime.length() == 8) {
			StringBuilder sb = new StringBuilder(dateTime);
			year = Integer.parseInt(sb.substring(0, 4));
			month = Integer.parseInt(sb.substring(4, 6));
			day = Integer.parseInt(sb.substring(6, 8));
		} else if (dateTime.length() == 10) {
			String[] dateArray = dateTime.split("-");
			year = Integer.parseInt(dateArray[0]);
			month = Integer.parseInt(dateArray[1]);
			day = Integer.parseInt(dateArray[2]);
		}
		Calendar ca = Calendar.getInstance();
		String fromTime = "";
		String toTime = "";
		String FromTimePre = "";
		String ToTimePre = "";
		// 本月
		ca.set(year, month - 1, day);
		ca.set(Calendar.DAY_OF_MONTH, 1);
		fromTime = sdf.format(ca.getTime());
		ca.add(Calendar.MONTH, 1);
		ca.add(Calendar.DAY_OF_MONTH, -1);
		toTime = sdf.format(ca.getTime());
		// 上个月
		ca.set(year, month - 2, day);
		ca.set(Calendar.DAY_OF_MONTH, 1);
		FromTimePre = sdf.format(ca.getTime());
		ca.add(Calendar.MONTH, 1);
		ca.add(Calendar.DAY_OF_MONTH, -1);
		ToTimePre = sdf.format(ca.getTime());
		timeMap.put("fromTime", fromTime);
		timeMap.put("toTime", toTime);
		timeMap.put("fromTimePre", FromTimePre);
		timeMap.put("toTimePre", ToTimePre);
		return timeMap;
	}

	public static Date parseDate8(String dateStr) throws ParseException {
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			throw new ParseException("时间格式转换异常", 0);
		}
		return date;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: getMoreMonth|描述: 获取多个月份
	 * </p>
	 * @param date 需要处理的日期 例:2010-09-01
	 * @param monthNumber 需要几个月份 例: monthNumber = 3 --- > 201007, 201008, 201009
	 * @return
	 */
	public static Map<String, String> getMoreMonth(String dateTime, int monthNumber) {
		Map<String, String> map = new HashMap<String, String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if (dateTime != null && !"".equals(dateTime)) {
			dateTime = dateTime.replaceAll("-", "");
		} else {
			dateTime = sdf.format(new Date());
		}

		if (dateTime.length() != 8) {
			dateTime = sdf.format(new Date());
		}
		int year = 0;
		int month = 0;
		int day = 0;

		Calendar calendar = Calendar.getInstance();
		StringBuilder sb = new StringBuilder(dateTime);
		year = Integer.parseInt(sb.substring(0, 4));
		month = Integer.parseInt(sb.substring(4, 6));
		day = Integer.parseInt(sb.substring(6, 8));

		String yearMonth = "";
		for (int i = monthNumber; i > 0; i--) {
			calendar.set(year, month - i, day);
			yearMonth = sdf.format(calendar.getTime());
			map.put("statmonth" + i, yearMonth.substring(0, 6));
		}
		return map;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: getSpecifyDate|描述: 根据指定的日期dateTime:20101127, 获取多少天的日期dayNumber:3, 返回结果为20101130.
	 * </p>
	 * @param dateTime
	 * @param dayNumber
	 * @return
	 */
	public static String getSpecifyDate(String dateTime, int dayNumber) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if (dateTime != null && !"".equals(dateTime)) {
			dateTime = dateTime.replaceAll("-", "");
		} else {
			dateTime = sdf.format(new Date());
		}

		if (dateTime.length() != 8) {
			dateTime = sdf.format(new Date());
		}

		int year = 0;
		int month = 0;
		int day = 0;

		Calendar calendar = Calendar.getInstance();
		StringBuilder sb = new StringBuilder(dateTime);
		year = Integer.parseInt(sb.substring(0, 4));
		month = Integer.parseInt(sb.substring(4, 6));
		day = Integer.parseInt(sb.substring(6, 8));

		calendar.set(year, month - 1, day + dayNumber);

		return sdf.format(calendar.getTime());

	}

	public static String expTime(long time) {
		Date date = new Date();
		time += date.getTime();

		String expTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(time));

		return expTime;
	}

	public static String expTime(Date date, long time) {
		time += date.getTime();
		String expTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(time));
		return expTime;
	}

	/**
	 * 获取当前时间前几分钟的时间，负数为后几天
	 * @param minute
	 * @return
	 */
	public static Date beforeMinute(int minute) {
		Calendar calendar = Calendar.getInstance();
		int i = calendar.get(Calendar.MINUTE);
		calendar.set(Calendar.MINUTE, i - minute);
		return new Date(calendar.getTimeInMillis());
	}

	/**
	 * 获取当前时间前几天的时间,负数为后几天
	 * @param minute
	 * @return
	 */
	public static Date beforeDay(int day) {
		Calendar calendar = Calendar.getInstance();
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i - day);
		return new Date(calendar.getTimeInMillis());
	}

	/**
	 * 获取当前时间前几天的时间,负数为后几天
	 * @param minute
	 * @return
	 */
	public static Date beforeDay(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i - day);
		date = calendar.getTime();
		return date;
	}
	
	
	/**
	 * 获取当前时间后几天的时间,负数为前几天
	 * @param minute
	 * @return
	 */
	public static Date afterDay(int day) {
		Calendar calendar = Calendar.getInstance();
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i + day);
		return new Date(calendar.getTimeInMillis());
	}

	/**
	 * 获取当前时间后几天的时间,负数为前几天
	 * @param minute
	 * @return
	 */
	public static Date afterDay(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i + day);
		date = calendar.getTime();
		return date;
	}
	
	
	/**
	 * 获得当前时间的秒数
	 * @return
	 */
	public static long getTimeSecond() {
		return (long) System.currentTimeMillis() / 1000;
	}

	/**
	 * 当前时间转换为字符串
	 * @return
	 */
	public static String getTimeSecondStr() {
		long time = getTimeSecond();
		return String.valueOf(time);
	}
	
	public static Date getPeriodTime(int period, String periodUnit) {
		Calendar cal = Calendar.getInstance();
		switch (periodUnit) {
		case "00":
			cal.add(Calendar.MINUTE, period);
			break;
		case "01":
			cal.add(Calendar.HOUR_OF_DAY, period);
			break;
		case "02":
			cal.add(Calendar.DAY_OF_MONTH, period);
			break;
		case "03":
			cal.add(Calendar.MONTH, period);
			break;
		default:
			cal.add(Calendar.MINUTE, period);
		}
		return new Date(cal.getTimeInMillis());
	}

	public static String periodTimeToSec(int period, String periodUnit) {
	Integer sec = 0;
		switch (periodUnit) {
		case "00": //分钟
			sec=period*60;
			break;
		case "01": // 小时
			sec=period*60*60;
			break;
		case "02": //天
			sec=period*60*60*24;
			break;
		case "03": //月，默认30天
			sec=period*60*60*24*30;
			break;
		default:
			break;
		}
		return sec+"";
	}

	public static void main(String[] args) throws Exception{

		String s ="2020-04-23+22%3A42%3A50";
		String decode = URLDecoder.decode(s, "utf-8");
		System.out.println(decode);
	}

}
