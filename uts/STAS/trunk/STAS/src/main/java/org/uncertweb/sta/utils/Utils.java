package org.uncertweb.sta.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.wps.ProcessInput;
import org.uncertweb.sta.wps.method.grouping.GroupingMethod;


/**
 * @author Christian Autermann
 */
public class Utils extends org.uncertweb.intamap.utils.Utils {
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	private static final String MIME_TYPE_HTTP_HEADER = "Content-Type";
	private static final String POST_HTTP_METHOD = "POST";
	private static final String GET_HTTP_METHOD = "GET";
	
	public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.########", new DecimalFormatSymbols(Locale.US));
	
	/**
	 * Formats the time elapsed since @ start}
	 * 
	 * @param start
	 *            the start point
	 * @return a {@link String} describing the elapsed time
	 */
	public static String timeElapsed(long start) {
		double sec = ((double) (System.currentTimeMillis() - start)) / 1000;
		if (sec < 1)
			return (int) (sec * 1000) + " ms";
		if (sec < 60)
			return (int) sec + " s";
		if (sec > 3600)
			return (int) (sec / 3600) + " h";
		return (int) Math.floor(sec / 60) + " m " + (int) (sec % 60) + " s";
	}
	
	public static InputStream sendPostRequest(String url, String request) throws IOException {
        OutputStreamWriter wr = null;
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty(Constants.CONNECTION_TIMEOUT_PROPERTY, String.valueOf(Constants.CONNECTION_TIMEOUT));
        systemProperties.setProperty(Constants.READ_TIMEOUT_PROPERTY, String.valueOf(Constants.READ_TIMEOUT));
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(POST_HTTP_METHOD);
            conn.setRequestProperty(MIME_TYPE_HTTP_HEADER, IOHandler.DEFAULT_MIMETYPE);
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(request);
            wr.flush();
            return conn.getInputStream();
        } finally {
            if (wr != null)
                wr.close();
            System.getProperties().remove(Constants.CONNECTION_TIMEOUT_PROPERTY);
            System.getProperties().remove(Constants.READ_TIMEOUT_PROPERTY);
        }		
	}
	
	public static String buildGetRequest(String url, Map<?,?> props) {
		if (url == null || props == null) {
			throw new NullPointerException();
		}
		StringBuilder sb = new StringBuilder(url);
		if (!url.endsWith("?") && !props.isEmpty()) {
			sb.append("?");
		}
		boolean first = true;
		for (Map.Entry<?,?> e : props.entrySet()) {
			if (!first) {
				sb.append("&");
			} else {
				first = false;
			}
			sb.append(e.getKey()).append("=").append(e.getValue());
		}
		return sb.toString();
	}
	
	public static InputStream sendGetRequest(String url, Map<?,?> props) throws IOException {
		return sendGetRequest(buildGetRequest(url, props));
	}
	
	public static InputStream sendGetRequest(String url) throws IOException {
		Properties systemProperties = System.getProperties();
        systemProperties.setProperty(Constants.CONNECTION_TIMEOUT_PROPERTY, String.valueOf(Constants.CONNECTION_TIMEOUT));
        systemProperties.setProperty(Constants.READ_TIMEOUT_PROPERTY, String.valueOf(Constants.READ_TIMEOUT));
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(GET_HTTP_METHOD);
            log.debug("Sending Request...");
            return conn.getInputStream();
        } finally {
            System.getProperties().remove(Constants.CONNECTION_TIMEOUT_PROPERTY);
            System.getProperties().remove(Constants.READ_TIMEOUT_PROPERTY);
        }
	}


	public static <T> List<T> mutableSingletonList(T t) {
		LinkedList<T> l = new LinkedList<T>();
		l.add(t);
		return l;
	}
	
	/**
	 * 
	 * helper method for creating a collection
	 *  
	 * @param <T> 
	 * 			type of elements in collection
	 * @param ts
	 * 			elements of collection
	 * @return collection
	 */
	public static <T> Collection<T> collection(T... ts) {
		return list(ts);
	}
	
	/**
	 * helper method for creating a set
	 * 
	 * @param <T> 
	 * 				type of elements in set
	 * @param ts 
	 * 				elements of set
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
	 * helper method for creating a list 
	 * 
	 * @param <T>
	 * 			type of elements in list
	 * @param ts
	 * 			elements of list
	 * @return list containing the passed elements
	 */
	public static <T> List<T> list(T... ts) {
		LinkedList<T> list = new LinkedList<T>();
		if (ts != null) {
			for (int i = 0; i < ts.length; i++) {
				list.add(ts[i]);
			}
		}
		return list;
	}
	
	/**
	 * helper method for creating a multiset
	 * 
	 * @param <T>
	 * 			type of elements
	 * @param ts
	 * 			elements of multiset
	 * @return the multiset
	 */
	public static <T> Set<T> multiSet(Set<T>... ts) {
		HashSet<T> set = new HashSet<T>();
		if (ts != null) {
			for (int i = 0; i < ts.length; i++) {
				if (ts[i] != null)
					set.addAll(ts[i]);
			}
		}
		return set;
	}
	
	public static Object getSingleParam(ProcessInput input, Map<String, List<IData>> inputs) {
		List<IData> dataList = inputs.get(input.getIdentifier());
		if (dataList != null) {
			switch (dataList.size()) {
			case 0:
				return null;
			case 1:
				return dataList.get(0).getPayload();
			default:
				throw new RuntimeException("Not more then one input expected: "
						+ input.getIdentifier());
			}
		}
		return null;
	}
	
	public static String getMethodDescription(GroupingMethod<?> gm) {
		return Constants.get("process.aggregation.vector." + gm.getClass().getName() + ".description");
	}
	
	
	public static String getDescribeSensorUrl(String url, String sensorId) {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("request", Constants.SOS_DESCRIBE_SENSOR_OPERATION);
		props.put("service", Constants.SOS_SERVICE_NAME);
		props.put("version", Constants.SOS_SERVICE_VERSION);
		props.put("outputFormat", Constants.SOS_SENSOR_OUTPUT_FORMAT);
		props.put("procedure", sensorId);
		return buildGetRequest(url, props);
	}
	
	public static String getObservationByIdUrl(String url, List<String> observationIds) {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("request", Constants.SOS_GET_OBSERVATION_BY_ID_OPERATION);
		props.put("service", Constants.SOS_SERVICE_NAME);
		props.put("version", Constants.SOS_SERVICE_VERSION);
		props.put("outputFormat", Constants.SOS_SENSOR_OUTPUT_FORMAT);
		StringBuilder sb = new StringBuilder();
		int size = observationIds.size(), i = 1;
		for (String s : observationIds) {
			sb.append(s.trim());
			if (size != i) {
				sb.append(",");
			}
			i++;
		}
		props.put("ObservationId", sb.toString());
//		log.info("ObservationId's: {}",sb.toString());
		return buildGetRequest(url, props);
	}

	private static final Random random = new Random();
	public static double randomBetween(double min, double max) {
		return Math.min(min, max) + random.nextDouble() * Math.abs(max-min);
	}

	public static String generateRandomProcessUrn() {
		return Constants.URN_AGGREGATED_PROCESS_PREFIX 
				+ RandomStringGenerator.getInstance().generate(20);
	}
}
