package com.mppay.gateway;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassTest {

	public static void main(String[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		CartParamsDTO2 d = new CartParamsDTO2();
		Class clazz = (Class) d.getClass();
		Annotation[] c = clazz.getAnnotations();
		Field[] fileds = clazz.getDeclaredFields();
		for (Field f : fileds) {
			String firstLetter = f.getName().substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + f.getName().substring(1);

//			Object v = f.get(d);
//			System.out.println(v);
//			System.out.println(f.getName() + " = " + f.getType());
			getFieldValueByName(f.getName(), d);
		}

	}

	private static Object getFieldValueByName(String fieldName, Object o) {
		try {

			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);

			Method method = o.getClass().getDeclaredMethod(getter);
			Object value = method.invoke(o, new Object[] {});
			System.out.println(value);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return o;
	}
	
	
}
