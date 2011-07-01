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
package org.uncertweb.sta.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.n52.wps.io.IOHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class Utils extends org.uncertweb.intamap.utils.Utils {

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * The common {@link NumberFormat} this service uses.
	 */
	public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(
			"0.########", new DecimalFormatSymbols(Locale.US));

	/* @formatter off */
	private static PeriodFormatter PERIOD_FORMAT = new PeriodFormatterBuilder()
		.appendYears().appendSuffix(" y", " y").appendSeparator(", ", " and ")
		.appendMonths().appendSuffix(" m", " m").appendSeparator(", ", " and ")
		.appendWeeks().appendSuffix(" w", " w").appendSeparator(", ", " and ")
		.appendDays().appendSuffix(" d", " d").appendSeparator(", ", " and ")
		.appendHours().appendSuffix(" h", " h").appendSeparator(", ", " and ")
		.appendMinutes().appendSuffix(" m", " m").appendSeparator(", ", " and ")
		.appendSeconds().appendSuffix(" s", " s").appendSeparator(", ", " and ")
		.appendMillis().appendSuffix(" ms", " ms").toFormatter();
	/* @formatter on */

	/**
	 * RNG.
	 */
	private static final Random random = new Random();

	/**
	 * Formats the time elapsed since {@code start}.
	 * 
	 * @param start the start point
	 * @return a {@link String} describing the elapsed time
	 */
	public static String timeElapsed(long start) {
		return PERIOD_FORMAT.print(new Period(System.currentTimeMillis() - start));
	}
	
	/**
	 * Sends an HTTP POST request to the specified URL.
	 * 
	 * @param url the URL the POST request will be send to
	 * @param request the request which will be send
	 * 
	 * @return the resulting {@code InputStream}
	 * @throws IOException if an IO error occurs
	 * @see Constants.Http.Timeout#CONNECTION
	 * @see Constants.Http.Timeout#READ
	 */
	public static InputStream sendPostRequest(String url, String request)
			throws IOException {
		OutputStreamWriter wr = null;
		Properties systemProperties = System.getProperties();
		systemProperties
				.setProperty(Constants.Http.Timeout.CONNECTION_PROPERTY, String
						.valueOf(Constants.Http.Timeout.CONNECTION));
		systemProperties
				.setProperty(Constants.Http.Timeout.READ_PROPERTY, String
						.valueOf(Constants.Http.Timeout.READ));
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url)
					.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(Constants.Http.Method.POST.name());
			conn.setRequestProperty(Constants.Http.Header.CONTENT_TYPE, IOHandler.DEFAULT_MIMETYPE);
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(request);
			wr.flush();
			return conn.getInputStream();
		} finally {
			if (wr != null)
				wr.close();
			System.getProperties()
					.remove(Constants.Http.Timeout.CONNECTION_PROPERTY);
			System.getProperties().remove(Constants.Http.Timeout.READ_PROPERTY);
		}
	}

	/**
	 * Builds a URL from a given URL and a set of parameters. {@code url} can or
	 * can not end with an '?'.
	 * 
	 * @param url the base URL
	 * @param props a key-value parameter set
	 * @return the complete URL
	 */
	public static String buildGetRequest(String url, Map<?, ?> props) {
		if (url == null || props == null) {
			throw new NullPointerException();
		}
		StringBuilder sb = new StringBuilder(url);
		if (!url.endsWith("?") && !props.isEmpty()) {
			sb.append("?");
		}
		boolean first = true;
		for (Map.Entry<?, ?> e : props.entrySet()) {
			if (!first) {
				sb.append("&");
			} else {
				first = false;
			}
			sb.append(e.getKey().toString()).append("=")
					.append(e.getValue().toString());
		}
		return sb.toString();
	}

	/**
	 * Shortcut for
	 * 
	 * <pre>
	 * sendGetRequest(buildGetRequest(url, props));
	 * </pre>
	 * 
	 * @param url the URL
	 * @param props the key-value-paramters
	 * @return the resulting {@link InputStream}
	 * @throws IOException if an IO error occurs
	 * @see #sendGetRequest(String)
	 * @see #buildGetRequest(String, Map)
	 */
	public static InputStream sendGetRequest(String url, Map<?, ?> props)
			throws IOException {
		return sendGetRequest(buildGetRequest(url, props));
	}

	/**
	 * Sends an HTTP GET request to the specified URL.
	 * 
	 * @param url the URL the GET request will be send to
	 * @return the resulting {@code InputStream}
	 * @throws IOException if an IO error occurs
	 * @see #buildGetRequest(String, Map)
	 * @see Constants.Http.Timeout#CONNECTION
	 * @see Constants.Http.Timeout#READ
	 */
	public static InputStream sendGetRequest(String url) throws IOException {
		Properties systemProperties = System.getProperties();
		systemProperties
				.setProperty(Constants.Http.Timeout.CONNECTION_PROPERTY, String
						.valueOf(Constants.Http.Timeout.CONNECTION));
		systemProperties
				.setProperty(Constants.Http.Timeout.READ_PROPERTY, String
						.valueOf(Constants.Http.Timeout.READ));
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url)
					.openConnection();
			conn.setRequestMethod(Constants.Http.Method.GET.name());
			log.debug("Sending Request...");
			return conn.getInputStream();
		} finally {
			System.getProperties()
					.remove(Constants.Http.Timeout.CONNECTION_PROPERTY);
			System.getProperties().remove(Constants.Http.Timeout.READ_PROPERTY);
		}
	}

	/**
	 * Helper method for creating a {@link Collection}.
	 * 
	 * @param <T> type of elements in collection
	 * @param ts elements of collection
	 * @return collection
	 */
	public static <T> Collection<T> collection(T... ts) {
		return list(ts);
	}

	/**
	 * Helper method for creating a {@link Set}.
	 * 
	 * @param <T> type of elements in set
	 * @param ts elements of set
	 * @return set containing the passed elements
	 */
	public static <T> Set<T> set(T... ts) {
		Set<T> set = new HashSet<T>();
		if (ts != null) {
			for (int i = 0; i < ts.length; i++) {
				set.add(ts[i]);
			}
		}
		return set;
	}

	/**
	 * Returns an mutable {@link List} containing only the specified object.
	 * 
	 * @param <T> type of t
	 * @param t the sole object to be stored in the returned set.
	 * @return an mutable {@code List} containing only the specified object.
	 * @see Collections#singletonList(Object)
	 */
	public static <T> List<T> list(T t) {
		LinkedList<T> l = new LinkedList<T>();
		l.add(t);
		return l;
	}
	
	/**
	 * Helper method for creating a {@link List}.
	 * 
	 * @param <T> type of elements in list
	 * @param ts elements of list
	 * @return list containing the passed elements
	 */
	public static <T> List<T> list(T... ts) {
		LinkedList<T> list = new LinkedList<T>();
		Collections.addAll(list, ts);
		return list;
	}

	/**
	 * Helper method to merge multiple {@link Set}s.
	 * 
	 * @param <T> type of elements
	 * @param ts {@code Set}s of elements of the {@code Set} returned
	 * @return the {@code Set} containing all elements
	 */
	public static <T> Set<T> multiSet(Set<T>... ts) {
		HashSet<T> set = new HashSet<T>();
		if (ts != null) {
			for (Set<T> s : ts) {
				set.addAll(s);
			}
		}
		return set;
	}

	/**
	 * Generates a random {@code double} between {@code min} and {@code max}.
	 * 
	 * @param min the minimum {@code double} (inclusive)
	 * @param max the maximum {@code double} (inclusive)
	 * @return a random {@code double} between {@code min} and {@code max}.
	 */
	public static double randomBetween(double min, double max) {
		return Math.min(min, max) + random.nextDouble() * Math.abs(max - min);
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
	 * @param str the {@code String}
	 * @param lowFirstLetter if the first letter should be lower case
	 * 
	 * @return the camelized {@code String}
	 */
	public static String camelize(String str, boolean lowFirstLetter) {
		String[] split = str.toLowerCase().split("_");
		for (int i = (lowFirstLetter) ? 1 : 0; i < split.length; i++) {
			split[i] = Character.toUpperCase(split[i].charAt(0))
					+ split[i].substring(1);
		}
		StringBuilder buf = new StringBuilder(str.length());
		for (String s : split)
			buf.append(s);
		return buf.toString();
	}
	
}
