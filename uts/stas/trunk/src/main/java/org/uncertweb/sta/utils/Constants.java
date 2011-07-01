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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.wps.GenericObservationAggregationProcess;
import org.uncertweb.sta.wps.RequestCache;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;

/**
 * Constants.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class Constants extends org.uncertweb.intamap.utils.Constants {

	public static interface MethodNames {
		public static interface Aggregation {
			public static interface Temporal {
				static final String TEMPORAL_PREFIX = "temporal";
				public static final String ARITHMETIC_MEAN = TEMPORAL_PREFIX + "Mean";
				public static final String MINIMUM = TEMPORAL_PREFIX + "Min";
				public static final String MAXIMUM = TEMPORAL_PREFIX + "Max";
				public static final String MEDIAN = TEMPORAL_PREFIX + "Median";
				public static final String SUM = TEMPORAL_PREFIX + "Sum";
				
			}

			public static interface Spatial {
				static final String SPATIAL_PREFIX = "spatial";
				public static final String ARITHMETIC_MEAN = SPATIAL_PREFIX + "Mean";
				public static final String MINIMUM = SPATIAL_PREFIX + "Min";
				public static final String MAXIMUM = SPATIAL_PREFIX + "Max";
				public static final String MEDIAN = SPATIAL_PREFIX + "Median";
				public static final String SUM = SPATIAL_PREFIX + "Sum";
				
			}
			
		}
		public static interface Grouping {
			public static interface Temporal {
				public static final String TEMPORAL_GRIDDING = "temporalGridding";
				public static final String NO_GROUPING = "noPartitioning";
				public static final String ONE_CONTAINING_TIME_RANGE = "oneContainingTimeRange";
			}
			public static interface Spatial {
				public static final String POLYGON_CONTAINMENT = "polygonContainment";
				public static final String NO_GROUPING = "noPartitioning";
				public static final String CONVEX_HULL = "convexHull";
			}
			
			
		}
	}
	
	/** The Logger. */
	protected static final Logger log = LoggerFactory.getLogger(Constants.class);

	/** {@link GenericObservationAggregationProcess} related constants. */
	public static interface Process {
		
		public static final String PROCESS_PREFIX = get("process.urn.prefix");

		/** Description of all {@link GenericObservationAggregationProcess}. */
		public static final String DESCRIPTION = get("process.description");

		/** Process inputs. */
		public static interface Inputs {
			public static final String OBSERVATION_COLLECTION_INPUT_ID = "observationCollectionCompositeInput";
			public static final String FEATURE_COLLECTION_INPUT_ID = "featureCollectionCompositeInput";

			public static final String SPATIAL_BEFORE_TEMPORAL = "spatialFirst";
			

			public static final String GROUP_BY_OBSERVED_PROPERTY_ID = "GroupByObservedProperty";
			public static final String SOS_DESTINATION_URL_ID = "SOSDestinationUrl";
			public static final String FEATURE_COLLECTION_ID = "FeatureCollection";
			public static final String TIME_RANGE_ID = "TimeRange";
			public static final String SOS_SOURCE_URL_ID = "SOSSourceUrl";
			public static final String SOS_REQUEST_ID = "SOSRequest";
			public static final String WFS_REQUEST_ID = "WFSRequest";
			public static final String WFS_URL_ID = "WFSUrl";

		}

		/** Process outputs. */
		public static interface Outputs {

			public static final String AGGREGATED_OBSERVATIONS_ID = "AggregatedObservations";
			public static final String AGGREGATED_OBSERVATIONS_REFERENCE_ID = "AggregatedObservationsReference";
			public static final String VISUALIZATION_LINK_ID = "VisualizationLink";
		}
	}

	/** HTTP related constants. */
	public static interface Http {

		/** Timeout properties. */
		public static interface Timeout {

			/** Timeout property name for connecting. */
			public static final String CONNECTION_PROPERTY = "sun.net.client.defaultConnectTimeout";

			/** Timeout property name for reading. */
			public static final String READ_PROPERTY = "sun.net.client.defaultReadTimeout";

			/** Timeout for connecting. */
			public static final int CONNECTION = getInt(CONNECTION_PROPERTY);

			/** Timeout for reading. */
			public static final int READ = getInt(READ_PROPERTY);
		}

		/** HTTP header. */
		public static interface Header {

			/** Content-Type header. */
			public static final String CONTENT_TYPE = "Content-Type";
		}

		/** HTTP methods. */
		public enum Method {
			/** GET method. */
			GET,
			/** POST method. */
			POST,
			/** HEAD method. */
			HEAD,
			/** PUT method. */
			PUT,
			/** DELETE method. */
			DELETE,
			/** OPTIONS method. */
			OPTIONS;
		}
	}

	/**
	 * STAS version that will be inserted in URN's
	 */
	public static final String STAS_VERSION = get("stas.version");

	/** SOS related constants. */
	public static interface Sos {

		/**
		 * The offering id for which the aggregated observations will be
		 * registered.
		 */
		public static final String AGGREGATION_OFFERING_ID = get("stas.sos.offeringId");

		/**
		 * The offering name for which the aggregated observations will be
		 * registered.
		 */
		public static final String AGGREGATION_OFFERING_NAME = get("stas.sos.offeringName", AGGREGATION_OFFERING_ID);

		/** The URN prefix of registered virtual sensor processes. */
		public static final String AGGREGATED_PROCESS = get("stas.sos.urn.process")
				+ STAS_VERSION + ":";

		/** Indicates that we want an <om:Observation> */
		public static final QName OBSERVATION_RESULT_MODEL = Namespace.OM
				.q("Observation");

		/** Indicates that we want an <om:Measurement> */
		public static final QName MEASUREMENT_RESULT_MODEL = Namespace.OM
				.q("Measurement");

		/** The service type of the SOS: "SOS". */
		public static final String SERVICE_NAME = get("stas.sos.service.name");

		/** The service version used for SOS requests. */
		public static final String SERVICE_VERSION = get("stas.sos.service.version");

		/** SensorML 1.0.1 output format. */
		public static final String SENSOR_OUTPUT_FORMAT = get("stas.sos.ourputFormat.sensor");

		/** O&M 1.0.0 output format. */
		public static final String OBSERVATION_OUTPUT_FORMAT = get("stas.sos.outputFormat.observation");

		/** SOS operations. */
		public enum Operation {
			/** DescribeSensor operation. */
			DESCRIBE_SENSOR,
			/** GetObservationById operation. */
			GET_OBSERVATION_BY_ID,
			/** GetObservation operation. */
			GET_OBSERVATION,
			/** GetCapabilities operation. */
			GET_CAPABILITIES,
			/** GetFeatureOfInterest operation. */
			GET_FEATURE_OF_INTEREST,
			/** RegisterSensor operation. */
			REGISTER_SENSOR,
			/** GetFeatureOfInterestTime operation. */
			GET_FEATURE_OF_INTEREST_TIME,
			/** GetResult operation. */
			GET_RESULT,
			/** InstertObservation operation. */
			INSERT_OBSERVATION,
			/** DescribeFeatureOfInterest operation. */
			DESCRIBE_FEATURE_OF_INTEREST;

			private String camelcase;

			/**
			 * {@inheritDoc}
			 */
			public String toString() {
				if (this.camelcase == null) {
					this.camelcase = Utils.camelize(this.name(), false);
				}
				return this.camelcase;
			}
		}

		/** Parameter names for SOS GET-requests. */
		public enum Parameter {
			/** "request"-parameter. */
			REQUEST,
			/** "service"-parameter. */
			SERVICE,
			/** "version"-parameter. */
			VERSION,
			/** "outputFormat"-parameter. */
			OUTPUT_FORMAT,
			/** "procedure"-parameter. */
			PROCEDURE,
			/** "ObservationId"-parameter. */
			OBSERVATION_ID;

			private String camelcase;

			/**
			 * {@inheritDoc}
			 */
			public String toString() {
				if (this.camelcase == null) {
					this.camelcase = Utils.camelize(this.name(), false);
				}
				return this.camelcase;
			}

		}

		/** Constants for the SensorML description of registered process. */
		public static interface ProcessDescription {

			/** Overall description of the process. */
			public static final String SENSOR_DESCRIPTION = get("stas.sos.sensorDescription");

			/** Parameter names of used inputs. */
			public static interface Parameter {

				/** URN prefix for parameters. */
				public static final String URN_PREFIX = get("stas.sos.urn.param")
						+ STAS_VERSION + ":";

				/**
				 * Parameter to indicate which {@link SpatialGrouping} was used.
				 */
				public static final String SPATIAL_GROUPING_METHOD = get("stas.sos.param.spatialGroupingMethod");

				/**
				 * Parameter to indicate which {@link TemporalGrouping} was
				 * used.
				 */
				public static final String TEMPORAL_GROUPING_METHOD = get("stas.sos.param.temporalGroupingMethod");

				/**
				 * Parameter to indicate which {@link AggregationMethod} was
				 * used for spatial aggregation.
				 */
				public static final String SPATIAL_AGGREGATION_METHOD = get("stas.sos.param.spatialAggregationMethod");

				/**
				 * Parameter to indicate which {@link AggregationMethod} was
				 * used for temporal aggregation.
				 */
				public static final String TEMPORAL_AGGREGATION_METHOD = get("stas.sos.param.temporalAggregationMethod");

				/**
				 * Parameter to indicate if the the process grouped first
				 * temporally.
				 */
				public static final String SPATIAL_BEFORE_TEMPORAL_AGGREGATION = get("stas.sos.param.spatialFirst");

				/**
				 * Parameter to indicate if the observation were grouped by
				 * ObservedProperty.
				 */
				public static final String GROUPED_BY_OBSERVED_PROPERTY = get("stas.sos.param.groupedByObservedProperty");
			}

			/** Names of capabilities. */
			public static interface Capabilities {

				/** URN prefix for capabilities. */
				public static final String URN_PREFIX = get("stas.sos.urn.caps")
						+ STAS_VERSION + ":";

				/** Property to indicate the time of aggregation. */
				public static final String TIME_OF_AGGREGATION = get("stas.sos.caps.timeOfAggregation");
			}
		}
	}

	/** Number of threads to fetch process inputs. */
	public static final int THREADS_TO_FETCH_INPUTS = getInt("stas.threadsToFetchInputs");
	
	/**
	 * Numbers of cached SOS requests. 
	 * @see RequestCache
	 */
	public static final int MAX_CACHED_REQUESTS = getInt("stas.requestCache.max");

	/** Properties file name for common properties. */
	private static final String COMMON_PROPERTIES = "/sta.properties";

	/** Common properties. */
	private static Properties props = null;

	/** Properties file for process related properties like descriptions */
	private static final String PROCESS_PROPERTIES = "/process.properties";

	/** Process related properties. */
	private static Properties processProps = null;

	/** Don't need to be instantiated. */
	private Constants() {
		super();
	}

	/**
	 * Loads a configuration property.
	 * 
	 * @param key the property key
	 * @return the property
	 */
	public static String get(String key) {
		String prop = null;
		if (key.startsWith("process")) {
			prop = getProcessProperty(key);
		} else {
			prop = getCommonProperty(key);
		}
		if (prop == null || prop.trim().equals("")) {
			log.warn("Property '{}' not set.", key);
		}
		return prop;
	}
	
	/**
	 * Loads a configuration property.
	 * 
	 * @param key the property key
	 * @param defaultValue the default value
	 * @return the property or <code>defaultValue</code> if the property is
	 *         <code>null</code> or empty
	 */
	public static String get(String key, String defaultValue) {
		String prop = null;
		if (key.startsWith("process")) {
			prop = getProcessProperty(key);
		} else {
			prop = getCommonProperty(key);
		}
		if (prop == null || prop.trim().isEmpty()) {
			log.warn("Property '{}' not set.", key);
			return defaultValue;
		}
		return prop;
	}

	/**
	 * Loads a boolean configuration property.
	 * 
	 * @param key the property key
	 * @return the property
	 */
	public static boolean getDefaultFlag(String input) {
		String key = "stas.default." + input;
		String s = get(key);
		if (s == null) {
			throw new RuntimeException("Can not parse property: {}" + key);
		}
		return Boolean.parseBoolean(s);
	}

	/**
	 * Loads a integer configuration property.
	 * 
	 * @param key the property key
	 * @return the property
	 */
	protected static int getInt(String key) {
		String s = get(key);
		if (s == null) {
			throw new RuntimeException("Can not parse property: {}" + key);
		}
		return Integer.parseInt(s);
	}

	/**
	 * Loads a property from {@link Constants#COMMON_PROPERTIES}
	 * 
	 * @param key the property key
	 * @return the property
	 */
	protected static String getCommonProperty(String key) {
		if (props == null) {
			log.info("Loading Common Properties");
			props = new Properties();
			try {
				InputStream is = Constants.class
						.getResourceAsStream(COMMON_PROPERTIES);
				if (is == null) {
					throw new FileNotFoundException(
							"Common Properties not found.");
				}
				props.load(is);
			} catch (IOException e) {
				log.error("Failed to load common properties", e);
				throw new RuntimeException(e);
			}
		}
		return props.getProperty(key);
	}

	/**
	 * Loads a property from {@link Constants#PROCESS_PROPERTIES}
	 * 
	 * @param key the property key
	 * @return the property
	 */
	protected static String getProcessProperty(String key) {
		if (processProps == null) {
			log.info("Loading Process Properties");
			processProps = new Properties();
			try {
				InputStream is = Constants.class
						.getResourceAsStream(PROCESS_PROPERTIES);
				if (is == null) {
					throw new FileNotFoundException(
							"Process Properties not found.");
				}
				processProps.load(is);
			} catch (IOException e) {
				log.error("Failed to load Process properties", e);
				throw new RuntimeException(e);
			}
		}
		return processProps.getProperty(key);
	}

}
