package com.mppay.gateway;

public class Test {

	public static void main(String[] args) {
//		int i = 10;
//		
//		String strBinaryNumber = Integer.toBinaryString(i);
//		
//		System.out.println(i + "的二进制是" + strBinaryNumber);
//		
//		System.out.println(i >> 1 );
//		
//		System.out.println(i + "的二进制是" + strBinaryNumber);
		
		// 399
		int c = 1;// ...0001=2^0
		// 600
		int r = 2;// ...0010=2^1
		// 9980
		int u = 4;// ...0100=2^3
		// 8888
		int d = 8;// ...1000=2^4
		
		// 用户A有添加和修改权限
		int usera = c | r | u;
 
		// 用户B有添加和删除权限
		int userb = c | d;
		
//		System.out.println(usera);
//		System.out.println(Integer.toBinaryString(usera));
//		System.out.println(Integer.toBinaryString(userb));
//		System.out.println(usera & r);
//		
//		
		System.out.println(1<<0);
		System.out.println(1<<1);
		System.out.println(1<<2);
		System.out.println(1<<3);
		System.out.println(1<<4);
		System.out.println(1<<5);
		
		
		System.out.println("1.4".compareTo("1.5"));
	}
	
}
