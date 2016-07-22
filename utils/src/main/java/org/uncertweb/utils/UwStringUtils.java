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

import java.util.Iterator;

public class UwStringUtils extends UwUtils {

	public static String join(Object... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(col[0].toString());
		for (int i = 1; i < col.length; ++i)
			sb.append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, Object... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(col[0].toString());
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}


	public static String join(String sep, int... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(String.valueOf(col[0]));
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, double... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(String.valueOf(col[0]));
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, float... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(String.valueOf(col[0]));
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, char... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(String.valueOf(col[0]));
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, byte... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(String.valueOf(col[0]));
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, long... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(String.valueOf(col[0]));
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, short... col) {
		if (col == null || col.length == 0)
			return "";
		if (col.length == 1)
			return String.valueOf(col[0]);
		StringBuilder sb = new StringBuilder(String.valueOf(col[0]));
		for (int i = 1; i < col.length; ++i)
			sb.append(sep).append(String.valueOf(col[i]));
		return sb.toString();
	}

	public static String join(String sep, Iterable<? extends Object> col) {
		Iterator<? extends Object> i;
		if (col == null || (!(i = col.iterator()).hasNext()))
			return "";
		StringBuilder sb = new StringBuilder(String.valueOf(i.next()));
		while (i.hasNext())
			sb.append(sep).append(i.next());
		return sb.toString();
	}

	/**
	 * Creates a camel case {@code String} from an upper case, underscore
	 * separated {@code String}
	 *
	 * <pre>
	 * camelize(&quot;THIS_IS_UPPER_CASE&quot;, false);
	 * </pre>
	 *
	 * results in <code>ThisIsUpperCase</code> and
	 *
	 * <pre>
	 * camelize(&quot;THIS_IS_UPPER_CASE&quot;, true);
	 * </pre>
	 *
	 * results in <code>thisIsUpperCase</code>.
	 *
	 * @param str
	 *            the {@code String}
	 * @param lowFirstLetter
	 *            if the first letter should be lower case
	 *
	 * @return the camelized {@code String}
	 */
	public static String camelize(String str, boolean lowFirstLetter) {
		String[] split = str.toLowerCase().split("_");
		for (int i = (lowFirstLetter) ? 1 : 0; i < split.length; i++) {
			split[i] = Character.toUpperCase(split[i].charAt(0))
					+ split[i].substring(1);
		}
		return join((Object[]) split);
	}
}
