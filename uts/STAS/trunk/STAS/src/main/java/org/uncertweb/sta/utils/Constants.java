package org.uncertweb.sta.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.utils.Namespace;

/**
 * @author Christian Autermann
 */
public class Constants extends org.uncertweb.intamap.utils.Constants {
	
	private static final Logger log = LoggerFactory.getLogger(Constants.class);
	private static final String PROPERTIES_FILE = "/sta.properties";
	private static final String PROCESS_PROPERTIES_FILE = "/process.properties";
	private static Properties props = null;
	private static Properties processProps = null;

	/*
	 * Connection Settings
	 */
	public static final String CONNECTION_TIMEOUT_PROPERTY = "sun.net.client.defaultConnectTimeout";
	public static final String READ_TIMEOUT_PROPERTY = "sun.net.client.defaultReadTimeout";
	public static final int CONNECTION_TIMEOUT = Integer.parseInt(get(CONNECTION_TIMEOUT_PROPERTY));
	public static final int READ_TIMEOUT = Integer.parseInt(get(READ_TIMEOUT_PROPERTY));

	/*
	 * Process
	 */
	public static final String PROCESS_DESCRIPTION = get("process.aggregation.vector.description");

	/*
	 * Process Inputs
	 */
	public static final String SOURCE_SOS_URL_INPUT_ID = get("process.aggregation.vector.input.sos.source.url.identifier");
	public static final String SOURCE_SOS_URL_INPUT_TITLE = get("process.aggregation.vector.input.sos.source.url.title");
	public static final String SOURCE_SOS_URL_INPUT_DESCRIPTION = get("process.aggregation.vector.input.sos.source.url.description");
	
	public static final String DESTINATION_SOS_URL_INPUT_ID = get("process.aggregation.vector.input.sos.destination.url.identifier");
	public static final String DESTINATION_SOS_URL_INPUT_TITLE = get("process.aggregation.vector.input.sos.destination.url.title");
	public static final String DESTINATION_SOS_URL_INPUT_DESCRIPTION = get("process.aggregation.vector.input.sos.destination.url.description");

	public static final String SPATIAL_AGGREGATION_METHOD_INPUT_ID = get("process.aggregation.vector.input.aggregationMethod.spatial.identifier");
	public static final String SPATIAL_AGGREGATION_METHOD_INPUT_TITLE = get("process.aggregation.vector.input.aggregationMethod.spatial.title");
	public static final String SPATIAL_AGGREGATION_METHOD_INPUT_DESCRIPTION = get("process.aggregation.vector.input.aggregationMethod.spatial.description");

	public static final String TEMPORAL_AGGREGATION_METHOD_INPUT_ID = get("process.aggregation.vector.input.aggregationMethod.temporal.identifier");
	public static final String TEMPORAL_AGGREGATION_METHOD_INPUT_TITLE = get("process.aggregation.vector.input.aggregationMethod.temporal.title");
	public static final String TEMPORAL_AGGREGATION_METHOD_INPUT_DESCRIPTION = get("process.aggregation.vector.input.aggregationMethod.temporal.description");
	
	public static final String SPATIAL_GROUPING_METHOD_INPUT_ID = get("process.aggregation.vector.input.groupingMethod.spatial.identifier");
	public static final String SPATIAL_GROUPING_METHOD_INPUT_TITLE = get("process.aggregation.vector.input.groupingMethod.spatial.title");
	public static final String SPATIAL_GROUPING_METHOD_INPUT_DESCRIPTION = get("process.aggregation.vector.input.groupingMethod.spatial.description");

	public static final String TEMPORAL_GROUPING_METHOD_INPUT_ID = get("process.aggregation.vector.input.groupingMethod.temporal.identifier");
	public static final String TEMPORAL_GROUPING_METHOD_INPUT_TITLE = get("process.aggregation.vector.input.groupingMethod.temporal.title");
	public static final String TEMPORAL_GROUPING_METHOD_INPUT_DESC = get("process.aggregation.vector.input.groupingMethod.temporal.description");
	
	public static final String GROUP_BY_OBSERVED_PROPERTY_INPUT_ID = get("process.aggregation.vector.input.groupingMethod.obsProp.identifier");
	public static final String GROUP_BY_OBSERVED_PROPERTY_INPUT_TITLE = get("process.aggregation.vector.input.groupingMethod.obsProp.title");
	public static final String GROUP_BY_OBSERVED_PROPERTY_INPUT_DESCRIPTION = get("process.aggregation.vector.input.groupingMethod.obsProp.description");
	
	public static final String TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_ID = get("process.aggregation.vector.input.temporalBeforeSpatial.identifier");
	public static final String TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_TITLE = get("process.aggregation.vector.input.temporalBeforeSpatial.title");
	public static final String TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_DESC = get("process.aggregation.vector.input.temporalBeforeSpatial.description");
	
	public static final String SOURCE_SOS_REQUEST_INPUT_ID = get("process.aggregation.vector.input.sos.request.identifier");
	public static final String SOURCE_SOS_REQUEST_INPUT_TITLE = get("process.aggregation.vector.input.sos.request.title");
	public static final String SOURCE_SOS_REQUEST_INPUT_DESCRIPTION = get("process.aggregation.vector.input.sos.request.description");
	
	public static final String WFS_URL_INPUT_ID = get("process.aggregation.vector.input.wfs.url.identifier");
	public static final String WFS_URL_INPUT_TITLE = get("process.aggregation.vector.input.wfs.url.title");
	public static final String WFS_URL_INPUT_DESC = get("process.aggregation.vector.input.wfs.url.description");
	
	public static final String WFS_REQUEST_INPUT_ID = get("process.aggregation.vector.input.wfs.request.identifier");
	public static final String WFS_REQUEST_INPUT_TITLE = get("process.aggregation.vector.input.wfs.request.title");
	public static final String WFS_REQUEST_INPUT_DESCRIPTION = get("process.aggregation.vector.input.wfs.request.description");
	
	public static final String FEATURE_COLLECTION_INPUT_ID = get("process.aggregation.vector.input.featureCollection.identifier");
	public static final String FEATURE_COLLECTION_INPUT_TITLE = get("process.aggregation.vector.input.featureCollection.title");
	public static final String FEATURE_COLLECTION_INPUT_DESCRIPTION = get("process.aggregation.vector.input.featureCollection.description");
	
	public static final String TIME_RANGE_INPUT_ID = get("process.aggregation.vector.input.timeRange.identifier");
	public static final String TIME_RANGE_INPUT_TITLE = get("process.aggregation.vector.input.timeRange.title");
	public static final String TIME_RANGE_INPUT_DESCRIPTION = get("process.aggregation.vector.input.timeRange.description");

	
	/*
	 * Process Output
	 */
	public static final String OBSERVATION_COLLECTION_OUTPUT_ID = get("process.aggregation.vector.output.aggregatedObservations.identifier");
	public static final String OBSERVATION_COLLECTION_OUTPUT_TITLE = get("process.aggregation.vector.output.aggregatedObservations.title");
	public static final String OBSERVATION_COLLECTION_OUTPUT_DESCRIPTION = get("process.aggregation.vector.output.aggregatedObservations.description");

	public static final String OBSERVATION_COLLECTION_REFERENCE_OUTPUT_ID = get("process.aggregation.vector.output.aggregatedObservationsReference.identifier");
	public static final String OBSERVATION_COLLECTION_REFERENCE_OUTPUT_TITLE = get("process.aggregation.vector.output.aggregatedObservationsReference.title");
	public static final String OBSERVATION_COLLECTION_REFERENCE_OUTPUT_DESCRIPTION = get("process.aggregation.vector.output.aggregatedObservationsReference.description");

    /*
	 * Various
	 */
	public static final String SOS_SERVICE_NAME = get("sos.service");
	public static final String SOS_SERVICE_VERSION = get("sos.version");
	public static final String SOS_DESCRIBE_SENSOR_OPERATION = get("sos.describeSensor");
	public static final String SOS_SENSOR_OUTPUT_FORMAT = get("sos.sensorOutputFormat");
	public static final String SOS_OBSERVATION_OUTPUT_FORMAT = get("sos.observationOutputFormat");
	public static final String SOS_GET_OBSERVATION_BY_ID_OPERATION = get("sos.getObservationById");
	
	public static final QName OBSERVATION_RESULT_MODEL = new QName(Namespace.OM.URI, "Observation", "om");
	public static final QName MEASUREMENT_RESULT_MODEL = new QName(Namespace.OM.URI, "Measurement", "om");
	
	public static final String AGGREGATION_OFFERING_ID = get("sos.aggregationOffering.id");
	public static final String AGGREGATION_OFFERING_NAME = get("sos.aggregationOffering.name");
	
	private Constants(){ super(); }

	
	static String get(String key) {
		String prop = null;
		if (key.startsWith("process"))
			prop = getProcessProperty(key);
		else {
			prop = getCommonProperty(key);
		}
		if (prop == null) {
			log.warn("Property '{}' not set." , key);
		}
		return prop;
	}

	/**
	 * Loads a configuration property.
	 * 
	 * @param key
	 *            the property key
	 * @return the property
	 */
	public static String getCommonProperty(String key) {
		if (props == null) {
			log.info("Loading Common Properties");
			props = new Properties();
			try {
				InputStream is = Constants.class.getResourceAsStream(PROPERTIES_FILE);
				if (is == null)
					throw new FileNotFoundException("Common Properties not found.");
				props.load(is);
			} catch (IOException e) {
				log.error("Failed to load common properties", e);
				throw new RuntimeException(e);
			}
		}
		return props.getProperty(key);
	}
	
	private static String getProcessProperty(String key) {
		if (processProps == null) {
			log.info("Loading Process Properties");
			processProps = new Properties();
			try {
				InputStream is = Constants.class.getResourceAsStream(PROCESS_PROPERTIES_FILE);
				if (is == null)
					throw new FileNotFoundException("Process Properties not found.");
				processProps.load(is);
			} catch (IOException e) {
				log.error("Failed to load Process properties", e);
				throw new RuntimeException(e);
			}
		}
		return processProps.getProperty(key);
	}

	public static void main(String[] args) {
		new Constants();
	}
}
