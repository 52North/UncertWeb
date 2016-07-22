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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.opengis.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertml.sample.AbstractRealisation;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utilities class.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class Utils {

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
			conn.setRequestProperty(Constants.Http.Header.CONTENT_TYPE, "text/xml");

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
	 * Generates GetObservationById GET request for the give observation Id's.
	 *
	 * @param url the base URL
	 * @param observationIds the Id's of the {@link Observation}s
	 * @return the generated request
	 */
	public static String getObservationByIdUrl(String url, Collection<? extends AbstractObservation> observations) {
		HashMap<Constants.Sos.Parameter, Object> props = new HashMap<Constants.Sos.Parameter, Object>();
		props.put(Constants.Sos.Parameter.REQUEST, Constants.Sos.Operation.GET_OBSERVATION_BY_ID);
		props.put(Constants.Sos.Parameter.SERVICE, Constants.Sos.SERVICE_NAME);
		props.put(Constants.Sos.Parameter.VERSION, Constants.Sos.SERVICE_VERSION);
		props.put(Constants.Sos.Parameter.OUTPUT_FORMAT, Constants.Sos.SENSOR_OUTPUT_FORMAT);
		StringBuilder sb = new StringBuilder();
		int size = observations.size(), i = 1;
		for (AbstractObservation id : observations) {
			String s = id.getIdentifier().getIdentifier();
			sb.append(s.trim());
			if (size != i) {
				sb.append(",");
			}
			i++;
		}
		props.put(Constants.Sos.Parameter.OBSERVATION_ID, sb.toString());
		return Utils.buildGetRequest(url, props);
	}

	/**
	 * helper method for generating a spatial sampling feature from a vector feature from Geotools
	 *
	 * @param feature
	 * 			Geotools Feature
	 * @return
	 *
	 * @throws URISyntaxException
	 */
	public static SpatialSamplingFeature createSF4GTFeature(Feature feature, int srid) throws URISyntaxException{
		//TODO might need to be fixed; currently per default the name is taken as id not the feature ID (which in general is a number)
		String id = feature.getIdentifier().toString();
		Geometry geom = (Geometry)feature.getDefaultGeometryProperty().getValue();
		geom.setSRID(srid);
		URI codeSpace = new URI("http://www.uncertweb.org/features");
		Identifier identifier = new Identifier(codeSpace,id);
		SpatialSamplingFeature sfs = new SpatialSamplingFeature(identifier,null,geom);
		return sfs;
	}

	/**
	 * extracts realisations of either ContinuousRealisation or RandomSample with Realisation element for each realisation
	 * and returns a ContinuousRealisation that contains all realisation values in the values array.
	 *
	 * @param uncertResult
	 * 				Realisation or RandomSample that should be converted
	 * @return Returns ContinuousRealisation that contains all realisation values in the values array
	 */
	public static ContinuousRealisation getRealisation4uncertResult(IUncertainty uncertResult){
		if (uncertResult instanceof ContinuousRealisation){
			return (ContinuousRealisation)uncertResult;
		}
		else if (uncertResult instanceof RandomSample){
			RandomSample rs = (RandomSample)uncertResult;
			List<AbstractRealisation> absRealList = rs.getRealisations();
			List<Double> values = new ArrayList<Double>(absRealList.size());
			for (AbstractRealisation absReal:absRealList){
				if (absReal instanceof ContinuousRealisation){
					values.addAll(((ContinuousRealisation) absReal).getValues());
				}
			}
			return new ContinuousRealisation(values);
		}
		else {
			throw new RuntimeException("Uncertainty type " + uncertResult.getClass() + " not supported for aggregation processes!");
		}
	}

	/**
	 * methods creates RandomSample from single ContinuousRealisation that contains more than one value in its values array.
	 *
	 * @param aggResult
	 * 			ContinuousRealisation that contains more than one value in its values array.
	 * @return RandomSample that contains a single Realisation element for each realisation value
	 */
	public static RandomSample getRandomSample4Real(ContinuousRealisation aggResult) {
		List<Double> values = aggResult.getValues();
		List<AbstractRealisation> realisations = new ArrayList<AbstractRealisation>(values.size());
		for (Double value:values){
			List<Double> singleVal = new ArrayList<Double>(1);
			singleVal.add(value);
			realisations.add(new ContinuousRealisation(singleVal));
		}
		return new RandomSample(realisations);
	}

}
