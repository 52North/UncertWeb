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
import java.util.Comparator;
import java.util.EnumMap;
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

	public static <T extends Number> double[] toDoubleArray(T[] l) {
		if (l == null) {
			return null;
		}
		double[] a = new double[l.length];
		for (int i = 0; i < l.length; i++) {
			a[i] = l[i].doubleValue();
		}
		return a;
	}

	public static double[] toDoubleArray(Collection<? extends Number> l) {
		if (l == null) {
			return null;
		}
		double[] a = new double[l.size()];
		int i = 0;
		for (Number n : l) {
			a[i++] = n.doubleValue();
		}
		return a;
	}

	public static <T extends Number> int[] toIntArray(T[] l) {
		if (l == null) {
			return null;
		}
		int[] a = new int[l.length];
		for (int i = 0; i < l.length; i++) {
			a[i] = l[i].intValue();
		}
		return a;
	}

	public static int[] toIntArray(Collection<? extends Number> l) {
		if (l == null) {
			return null;
		}
		int[] a = new int[l.size()];
		int i = 0;
		for (Number n : l) {
			a[i++] = n.intValue();
		}
		return a;
	}

	public static <T extends Number> long[] toLongArray(T[] l) {
		if (l == null) {
			return null;
		}
		long[] a = new long[l.length];
		for (int i = 0; i < l.length; i++) {
			a[i] = l[i].longValue();
		}
		return a;
	}

	public static long[] toLongArray(Collection<? extends Number> l) {
		if (l == null) {
			return null;
		}
		long[] a = new long[l.size()];
		int i = 0;
		for (Number n : l) {
			a[i++] = n.longValue();
		}
		return a;
	}

	public static <T extends Number> short[] toShortArray(T[] l) {
		if (l == null) {
			return null;
		}
		short[] a = new short[l.length];
		for (int i = 0; i < l.length; i++) {
			a[i] = l[i].shortValue();
		}
		return a;
	}

	public static short[] toShortArray(Collection<? extends Number> l) {
		if (l == null) {
			return null;
		}
		short[] a = new short[l.size()];
		int i = 0;
		for (Number n : l) {
			a[i++] = n.shortValue();
		}
		return a;
	}

	public static <T extends Number> byte[] toByteArray(T[] l) {
		if (l == null) {
			return null;
		}
		byte[] a = new byte[l.length];
		for (int i = 0; i < l.length; i++) {
			a[i] = l[i].byteValue();
		}
		return a;
	}

	public static byte[] toByteArray(Collection<? extends Number> l) {
		if (l == null) {
			return null;
		}
		byte[] a = new byte[l.size()];
		int i = 0;
		for (Number n : l) {
			a[i++] = n.byteValue();
		}
		return a;
	}

	public static <T extends Number> float[] toFloatArray(T[] l) {
		if (l == null) {
			return null;
		}
		float[] a = new float[l.length];
		for (int i = 0; i < l.length; i++) {
			a[i] = l[i].floatValue();
		}
		return a;
	}

	public static float[] toFloatArray(Collection<? extends Number> l) {
		if (l == null) {
			return null;
		}
		float[] a = new float[l.size()];
		int i = 0;
		for (Number n : l) {
			a[i++] = n.byteValue();
		}
		return a;
	}

	@SuppressWarnings("serial")
	public static <T> Set<T> set(final T t) {
		return new HashSet<T>(1) {
			{
				add(t);
			}
		};
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
	public static <T> Set<T> set(final Iterable<? extends T> elements) {
		return new HashSet<T>() {
			{
				for (T t : elements)
					add(t);
			}
		};
	}

	public static <T> void addAll(Collection<T> col, T[] elem) {
		for (T t : elem) {
			col.add(t);
		}
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

	public static <T> Set<T> asSet(T[] col) {
		if (col == null || col.length == 0)
			return Collections.emptySet();
		Set<T> set = set();
		for (T t : col)
			set.add(t);
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

	public static <T> List<T> asList(T[] col) {
		if (col == null || col.length == 0)
			return Collections.emptyList();
		List<T> list = list();
		for (T t : col)
			list.add(t);
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
		return new ArrayList<T>(1) {
			{
				add(t);
			}
		};
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
		return Collections
				.newSetFromMap(UwCollectionUtils.<T, Boolean> tsMap());
	}

	public static <T, U> Map<T, U> map() {
		return new HashMap<T, U>();
	}

	public static <T, U> Map<T, U> tsMap() {
		return new ConcurrentHashMap<T, U>();
	}
	
	public static <T extends Enum<T>, V> Map<T, V> enumMap(Class<T> c) {
		return new EnumMap<T, V>(c);
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
	
	
	
	public static <T extends Comparable<? super T>> int binarySearch(List<T> a, T key) {
		int l = 0;
		int h = a.size() - 1;

		while (l <= h) {
			int m = (l + h) >>> 1;
			T midVal = a.get(m);
			int cmp = midVal.compareTo(key);
			if (cmp < 0) {
				l = m + 1;
			} else if (cmp > 0) {
				h = m - 1;
			} else {
				return m;
			}
		}
		return -1;
	}
	
	public static <T> int binarySearch(List<T> a, T key, Comparator<? super T> c) {
		int l = 0;
		int h = a.size() - 1;

		while (l <= h) {
			int m = (l + h) >>> 1;
			T t = a.get(m);
			int cmp = c.compare(t, key);
			if (cmp < 0) {
				l = m + 1;
			} else if (cmp > 0) {
				h = m - 1;
			} else {
				return m;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] append(T[] a, T t) {
		T[] n = (T[]) Arrays.copyOf(a, a.length + 1, a.getClass());
		n[a.length + 1] = t;
		return n;
	}
	
	public static int[] append(int[] a, int t) {
		int[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;
	}
	
	public static long[] append(long[] a, long t) {
		long[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;	
	}

	public static short[] append(short[] a, short t) {
		short[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;	
	}

	public static byte[] append(byte[] a, byte t) {
		byte[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;	
	}

	public static boolean[] append(boolean[] a, boolean t) {
		boolean[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;	
	}

	public static double[] append(double[] a, double t) {
		double[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;	
	}

	public static float[] append(float[] a, float t) {
		float[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;	
	}

	public static char[] append(char[] a, char t) {
		char[] n = Arrays.copyOf(a, a.length + 1);
		n[a.length] = t;
		return n;	
	}
	
	public static <T> Set<T> filter(Set<? extends T> s, Filter<? super T>... filter) {
		return filter(s, UwCollectionUtils.<T> set(), filter);
	}

	public static <T> List<T> filter(List<? extends T> s,
			Filter<? super T>... filter) {
		return filter(s, UwCollectionUtils.<T> list(), filter);
	}

	public static <T> Collection<T> filter(Collection<? extends T> s,
			Filter<? super T>... filter) {
		return filter(s, UwCollectionUtils.<T> list(), filter);
	}

	public static <T, V extends Collection<T>> V filter(
			Iterable<? extends T> source, V target, Filter<? super T>... filter) {
		for (T t : source) {
			boolean failed = false;
			for (Filter<? super T> f : filter) {
				if (!f.test(t)) {
					failed = true;
					break;
				}
			}
			if (!failed) {
				target.add(t);
			}
		}
		return target;
	}
	
	
	public static interface Filter<T> {
		public boolean test(T t);
	}
	
}
