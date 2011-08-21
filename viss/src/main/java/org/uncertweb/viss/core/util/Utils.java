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
package org.uncertweb.viss.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.coverage.grid.GridCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.UncertML;
import org.uncertweb.viss.core.VissError;

public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

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

	public static String join(String sep, Iterable<? extends Object> col) {
		Iterator<? extends Object> i;
		if (col == null || (!(i = col.iterator()).hasNext()))
			return "";
		StringBuilder sb = new StringBuilder(String.valueOf(i.next()));
		while (i.hasNext())
			sb.append(sep).append(i.next());
		return sb.toString();
	}

	public static File to(File dir, String sub) {
		if (dir == null || sub == null || !dir.isDirectory())
			return null;
		return new File(dir.getAbsolutePath() + File.separator + sub);
	}

	public static boolean deleteRecursively(File path) {
		if (path != null && path.exists()) {
			log.debug("Deleting {}", path.getAbsolutePath());
			if (path.isDirectory()) {
				for (File f : path.listFiles()) {
					if (f.isDirectory())
						Utils.deleteRecursively(f);
					else
						f.delete();
				}
			}
			return path.delete();
		} else
			return false;
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
		return new HashSet<T>() {{
			for (Set<T> s : values)
				addAll(s);
		}};
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

	@SuppressWarnings("serial")
	public static <T> List<T> list(final T... elements) {
		if (elements == null || elements.length == 0)
			return Collections.emptyList();
		return new LinkedList<T>() {
			{
				for (T t : elements)
					add(t);
			}
		};
	}

	@SuppressWarnings("serial")
	public static <T, U> Map<T, U> map(final T t, final U u) {
		return new HashMap<T, U>() {
			{
				put(t, u);
			}
		};
	}

	public static void saveToFile(File f, String s) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(f);
			IOUtils.write(s, os);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	public static long saveToFileWithChecksum(File f, InputStream is) throws IOException {
		CheckedOutputStream cos = null;
		try {
			cos = new CheckedOutputStream(new FileOutputStream(f), new CRC32());
			IOUtils.copy(is, cos);
			return cos.getChecksum().getValue();
		} finally {
			IOUtils.closeQuietly(cos);
		}
	}

	public static void saveToFile(File f, InputStream is) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(f);
			IOUtils.copy(is, os);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	public static void saveToFile(File f, URL url) throws IOException {
		final URLConnection con = url.openConnection();
		con.setDoOutput(false);
		con.setDoInput(true);
		saveToFile(f, con.getInputStream());
	}

	public static void writeAsGeoTIFF(GridCoverage coverage, String file) {
		writeAsGeoTIFF(coverage, new File(file));
	}

	public static void writeAsGeoTIFF(GridCoverage coverage, File file) {
		try {
			writeAsGeoTIFF(coverage, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw VissError.internal(e);
		}
	}

	public static void writeAsGeoTIFF(GridCoverage coverage, OutputStream out) {
		try {
			new GeoTiffWriter(out).write(coverage, null);
		} catch (IOException e) {
			throw VissError.internal(e);
		} finally {
		}
	}

	public static <T> Set<T> set() {
		return new HashSet<T>();
	}

	public static <T> Set<T> setMT() {
		return Collections.newSetFromMap(Utils.<T, Boolean> mapMT());
	}

	public static <T, U> Map<T, U> map() {
		return new HashMap<T, U>();
	}

	public static <T, U> Map<T, U> mapMT() {
		return new ConcurrentHashMap<T, U>();
	}

	public static <T> boolean in(T t, T[] ts) {
		for (T i : ts)
			if (i.equals(t))
				return true;
		return false;
	}

	public static URI getUncertURI(Class<?> c) {
		try {
			return new URI(UncertML.getURI(c));
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public static String stringifyJson(JSONObject json) throws JSONException {
		if (Constants.PRETTY_PRINT_IO)
			return json.toString(4);
		else
			return json.toString();
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
	/**
	 * 
	 * <pre>flatJSON("one","two","three","four")</pre>
	 * creates 
	 * <pre>    
	 * { "one": { "two": { "three": "four" } } }
	 * </pre>
	 * 
	 * 
	 * @param s
	 * @return
	 * @throws JSONException
	 */
	public static final JSONObject flatJSON(String... s) throws JSONException{
		int l = s.length;
		if (s.length < 2)
			throw VissError.internal("Invalid parameter number");
		JSONObject j = new JSONObject().put(s[l-2], s[l-1]);
		for (int i = l-3; i >= 0; --i) {
			j = new JSONObject().put(s[i], j);
		}
		return j;
	}
}
