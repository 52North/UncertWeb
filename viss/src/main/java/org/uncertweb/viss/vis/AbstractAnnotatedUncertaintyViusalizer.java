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
package org.uncertweb.viss.vis;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;

public abstract class AbstractAnnotatedUncertaintyViusalizer extends
    AbstractMultiResourceTypeVisualizer {

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Type {
		UncertaintyType[] value();
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Description {
		String value();
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Id {
		String value();
	}

	@Override
	public Set<UncertaintyType> getCompatibleUncertaintyTypes() {
		Type t = findAnnotation(Type.class, getClass());
		if (t == null)
			return UwCollectionUtils.set();
		else
			return UwCollectionUtils.set(t.value());
	}

	@Override
	public String getDescription() {
		Description t = findAnnotation(Description.class, getClass());
		if (t == null)
			return "";
		else
			return t.value();

	}

	private static <T extends Annotation> T findAnnotation(Class<T> annotation,
	    Class<?> c) {
		T t = null;
		while ((t = c.getAnnotation(annotation)) == null && c != null) {
			if (t == null)
				c = c.getSuperclass();
		}
		return t;
	}

	@Override
	public String getShortName() {
		Id id = findAnnotation(Id.class, getClass());
		if (id != null)
			return id.value();
		return getClass().getSimpleName();
	}
}
