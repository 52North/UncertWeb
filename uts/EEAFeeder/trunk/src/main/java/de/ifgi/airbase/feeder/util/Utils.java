package de.ifgi.airbase.feeder.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.data.EEAMeasurementType;
import de.ifgi.airbase.feeder.io.filter.ComponentFileFilter;
import de.ifgi.airbase.feeder.io.filter.CompositeFileFilter;
import de.ifgi.airbase.feeder.io.filter.MeasurementTypeFileFilter;
import de.ifgi.airbase.feeder.io.filter.StationFileFilter;
import de.ifgi.airbase.feeder.io.filter.TimeRangeFilter;

/**
 * Utility class.
 * 
 * @author Christian Autermann
 * 
 */
public class Utils {

	public static final DateTimeFormatter NORMAL_DATE_FORMAT = DateTimeFormat
			.forPattern("dd-MM-yyyy");
	public static final DateTimeFormatter REVERSE_DATE_FORMAT = DateTimeFormat
			.forPattern("yyyy-MM-dd");
	public static final DateTimeFormatter ISO8601_DATETIME_FORMAT = ISODateTimeFormat
			.dateTime();
	private static final String STATIONS_TO_PARSE_PROPERTY = "eea.stationsToParse";
	private static final String TYPES_TO_PARSE_PROPERTY = "eea.typesToParse";
	private static final String TIMES_TO_PARSE_PROPERTY = "eea.timesToParse";
	private static final String COMPONENTS_TO_PARSE_PROPERTY = "eea.componentsToParse";

	private static final String FILE_NAME = "/config.properties";
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	private static Properties props = null;
	private static Set<EEAMeasurementType> typesToParse = null;
	private static Set<String> stationsToParse = null;
	private static Collection<Integer> components = null;
	private static TimeRangeFilter trf = null;

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
		if (props == null) {
			log.info("Loading Properties");
			props = new Properties();
			try {
				InputStream is = Utils.class.getResourceAsStream(FILE_NAME);
				if (is == null)
					throw new FileNotFoundException();
				props.load(is);
			} catch (IOException e) {
				log.error("Failed to load properties", e);
			}
		}
	}

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
            return (int) (sec / 3600)  + " h";
		return (int) Math.floor(sec / 60) + " m " + (int) (sec % 60) + " s";
	}

	/**
	 * Loads a configuration property.
	 * 
	 * @param key
	 *            the property key
	 * @return the property
	 */
	public static String get(String key) {
		return props.getProperty(key);
	}

	public static Set<EEAMeasurementType> getTypesToParse() {
		if (typesToParse == null) {
			String prop = Utils.get(TYPES_TO_PARSE_PROPERTY);
			if (prop != null) {
				typesToParse = new HashSet<EEAMeasurementType>();
				for (String type : prop.split(",")) {
					typesToParse.add(EEAMeasurementType.getValue(type.trim()));
				}
			}
		}
		return typesToParse;
	}

	public static boolean shouldBeIgnored(int componentCode) {
		return !Utils.getComponentsToParse().contains(new Integer(componentCode));
	}
	
	private static Set<String> getStationsToParse() {
		if (stationsToParse == null) {
			String prop = Utils.get(STATIONS_TO_PARSE_PROPERTY);
			if (prop != null) {
				stationsToParse = new HashSet<String>();
				for (String s : prop.split(",")) {
					stationsToParse.add(s.trim());
				}
				log.info("Stations to parse: {}", stationsToParse);
			}
		}
		return stationsToParse;
	}

	public static FileFilter getRawDataFileFilter() {
		Set<String> stations = getStationsToParse();
		Set<EEAMeasurementType> types = getTypesToParse();
		Collection<Integer> comps = getComponentsToParse();
		CompositeFileFilter cff = new CompositeFileFilter();
		TimeRangeFilter trfilter = getTimeRangeFilter();
		if (stations != null) {
			cff.addFilter(new StationFileFilter(stations));
		}
		if (types != null) {
			cff.addFilter(new MeasurementTypeFileFilter(types));
		}
		if (comps != null) {
			cff.addFilter(new ComponentFileFilter(comps));
		}
		if (trfilter != null) {
			cff.addFilter(trfilter);
		}
		return cff;
	}

	private static Collection<Integer> getComponentsToParse() {
		if (components == null) {
			String property = get(COMPONENTS_TO_PARSE_PROPERTY);
			if (property != null) {
				components = new LinkedList<Integer>();
				for (String comp : property.split(",")) {
					if (comp.contains("-")) {
						String[] split = comp.split("-");
						int begin = Integer.valueOf(split[0]).intValue();
						int end = Integer.valueOf(split[0]).intValue();
						if (end > begin) 
							throw new Error("Can not parse component range. First value has to be smaller than the second.");
						for (int i = begin; i <= end; i++)
							components.add(Integer.valueOf(i));
					} else {
						components.add(Integer.valueOf(comp));
					}
				}
			}
		}
		return components;
	}

	public static TimeRangeFilter getTimeRangeFilter() {
		String NULL = "null";
		if (trf == null) {
			String property = get(TIMES_TO_PARSE_PROPERTY);
			if (property != null) {
				trf = new TimeRangeFilter();
				for (String range : property.split(";")) {
					String[] split = range.split(",");
					if (split[0].equalsIgnoreCase(NULL)) {
						if (split[1].equalsIgnoreCase(NULL)) {
							throw new NullPointerException(
									"Begin and end date are null");
						}
						DateTime end = ISO8601_DATETIME_FORMAT
								.parseDateTime(split[1]);
						trf.addEnd(end);
					} else {
						if (split[1].equalsIgnoreCase(NULL)) {
							DateTime begin = ISO8601_DATETIME_FORMAT
									.parseDateTime(split[0]);
							trf.addStart(begin);
						} else {
							DateTime begin = ISO8601_DATETIME_FORMAT
									.parseDateTime(split[0]);
							DateTime end = ISO8601_DATETIME_FORMAT
									.parseDateTime(split[1]);
							trf.addStartEndRange(begin, end);
						}
					}
				}
			}
		}
		return trf;
	}

	public static DateTime parseDate(String date) {
		return NORMAL_DATE_FORMAT.parseDateTime(date);
	}

	public static DateTime parseDateReverse(String date) {
		return REVERSE_DATE_FORMAT.parseDateTime(date.split(" ")[0]);
	}

	public static boolean isAcceptableStation(String stationId) {
		Set<String> stations = getStationsToParse();
		if (stations == null) {
			return true;
		}
		for (String s : stations) {
			if (stationId.matches(s))
				return true;
		}
		return false;
	}
    
	private static final String AIRBASE_TEMP_FOLDER = "AirBase_Feeder_TEMP";
    private static String tempDir = null;
    private static File failedRequest = null;

    public static String createTempDir() throws IOException {
	        File temp = new File(System.getProperty("java.io.tmpdir") + File.separator + AIRBASE_TEMP_FOLDER);

	        if (temp.exists()) {
	            log.info("Temp dir already exists, reusing " + temp.getAbsolutePath());
	        }
	        else {
	            temp.mkdir();
	            log.info("Created temp directory " + temp.getAbsolutePath());
	        }

	        return tempDir = temp.getAbsolutePath();
	    }
	public static String getFailedRequestPrintPath() {
		if (failedRequest == null) {
			if (tempDir == null) {
				try {
					createTempDir();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			failedRequest = new File(tempDir + File.separator + "FailedRequests");
			if (!failedRequest.exists()) failedRequest.mkdir();
		}
		return failedRequest.getAbsolutePath();
	}
}
