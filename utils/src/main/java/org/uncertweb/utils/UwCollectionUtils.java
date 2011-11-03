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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UwCollectionUtils extends UwUtils {
	
	@SuppressWarnings("serial")
	public static <T> Set<T> set(final T t) {
		return new HashSet<T>(1) {{ add(t); }};
	}

	
	@SuppressWarnings("serial")
	public static <T> Set<T> set(final T... elements) {
		if (elements == null || elements.length == 0)
			return Collections.emptySet();
		return new HashSet<T>(elements.length) {
			{
				for (T t : elements)
					add(t);
			}
		};
	}

	@SuppressWarnings("serial")
	public static <T> Set<T> combineSets(final Set<T>... values) {
		if (values == null || values.length == 0) {
			return Collections.emptySet();
		}
		return new HashSet<T>() {
			{
				for (Set<T> s : values)
					addAll(s);
			}
		};
	}

	public static <T> Set<T> asSet(Iterable<? extends T> col) {
		Iterator<? extends T> i;
		if (col == null || (!(i = col.iterator()).hasNext()))
			return Collections.emptySet();
		Set<T> set = set();
		while (i.hasNext()) {
			set.add(i.next());
		}
		return set;
	}

	public static <T> List<T> asList(Iterable<? extends T> col) {
		Iterator<? extends T> i;
		if (col == null || (!(i = col.iterator()).hasNext()))
			return Collections.emptyList();
		List<T> list = list();
		while (i.hasNext()) {
			list.add(i.next());
		}
		return list;
	}

	public static <T> List<T> list() {
		return new LinkedList<T>();
	}
	
	public static <T> List<T> tsList() {
		return new CopyOnWriteArrayList<T>();
	}

	@SuppressWarnings("serial")
	public static <T> List<T> list(final T t) {
		return new ArrayList<T>(1) {{add(t);}};
	}
	
	@SuppressWarnings("serial")
	public static <T> List<T> list(final T... elements) {
		if (elements == null || elements.length == 0)
			return new LinkedList<T>();
		return new ArrayList<T>(elements.length) {
			{
				for (T t : elements)
					add(t);
			}
		};
	}
	
	public static <T> T[] sort(T[] t) {
		Arrays.sort(t);
		return t;
	}

	@SuppressWarnings("serial")
	public static <T, U> Map<T, U> map(final T t, final U u) {
		return new HashMap<T, U>() {
			{
				put(t, u);
			}
		};
	}

	public static <T> Set<T> set() {
		return new HashSet<T>();
	}

	public static <T> Set<T> tsSet() {
		return Collections.newSetFromMap(UwCollectionUtils.<T, Boolean> tsMap());
	}

	public static <T, U> Map<T, U> map() {
		return new HashMap<T, U>();
	}
	
	public static <T, U> Map<T, U> tsMap() {
		return new ConcurrentHashMap<T, U>();
	}

	public static <T> boolean in(T t, T[] ts) {
		if (t == null) {
			for (T i : ts)
				if (i == null)
					return true;
			return false;
		}
		for (T i : ts)
			if (i.equals(t))
				return true;
		return false;
	}

	public static <T> Collection<T> collection(T... ts) {
		return list(ts);
	}
}
