/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.utils;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class UwReflectionUtils extends UwUtils {
	
	public static boolean isAbstractOrInterface(Class<?> c) {
		 return c != null && (c.isInterface() || Modifier.isAbstract(c.getModifiers()));
	}
	
	
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
