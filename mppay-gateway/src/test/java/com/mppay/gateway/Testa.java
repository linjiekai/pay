package com.mppay.gateway;

import java.math.BigDecimal;
import java.sql.Date;

import com.mppay.core.utils.DateTimeUtil;

public class Testa {

	public static void main(String[] args) {

		Long s  = System.currentTimeMillis();
		s = s / 1000;
		System.out.println(s);
		s = s * 1000;
		System.out.println(s);
		System.out.println(DateTimeUtil.date14(new Date(s)));
		System.out.println(DateTimeUtil.date14(new Date(s / 1000 * 1000)));
		System.out.println(DateTimeUtil.date14(new Date(1568260622000L)));
	}

}
