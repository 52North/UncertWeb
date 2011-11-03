package org.uncertweb.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class UwReflectionUtils extends UwUtils {
	public static boolean isParameterizedWith(Type t, Class<?> collClass,
			Class<?> itemClass) {
		if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) t;
			if (collClass.isAssignableFrom((Class<?>) pt.getRawType())) {
				Type argT = pt.getActualTypeArguments()[0];
				Class<?> tV = null;
				if (argT instanceof ParameterizedType) {
					tV = (Class<?>) ((ParameterizedType) argT).getRawType();
				} else if (argT instanceof Class) {
					tV = (Class<?>) argT;
				} else {
					return false;
				}
				return itemClass.isAssignableFrom(tV);
			}
		}
		return false;
	}
}
