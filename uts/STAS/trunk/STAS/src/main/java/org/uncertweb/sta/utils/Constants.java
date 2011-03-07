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

import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.wfs.GetFeatureDocument;

import org.geotools.feature.FeatureCollection;
import org.joda.time.Period;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.wps.ClassInputHandler;
import org.uncertweb.sta.wps.FeatureCollectionInputHandler;
import org.uncertweb.sta.wps.GenericObservationAggregationProcess;
import org.uncertweb.sta.wps.ObservationCollectionInputHandler;
import org.uncertweb.sta.wps.PeriodInputHandler;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.CompositeProcessInput;
import org.uncertweb.sta.wps.api.ProcessOutput;
import org.uncertweb.sta.wps.api.SingleProcessInput;
import org.uncertweb.sta.wps.method.MethodFactory;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.aggregation.impl.ArithmeticMeanAggregation;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.CoverageGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.TimeRangeGrouping;
import org.uncertweb.sta.wps.sos.GetObservationRequestCache;
import org.uncertweb.sta.wps.xml.binding.GetFeatureRequestBinding;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

/**
 * Constants for all other classes.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class Constants extends org.uncertweb.intamap.utils.Constants {

	/**
	 * {@link GenericObservationAggregationProcess} related constants.
	 */
	public static interface Process {

		/**
		 * Description of all {@link GenericObservationAggregationProcess}.
		 */
		public static final String DESCRIPTION = get("process.description");

		/**
		 * Process inputs.
		 */
		public static interface Inputs {

			/**
			 * Input defaults.
			 */
			public static interface Defaults {

				/**
				 * The default {@link AggregationMethod} used for spatial
				 * aggregation.
				 */
				public static final Class<? extends AggregationMethod> SPATIAL_AGGREGATION_METHOD = ArithmeticMeanAggregation.class;

				/**
				 * The default {@link AggregationMethod} used for temporal
				 * aggregation.
				 */
				public static final Class<? extends AggregationMethod> TEMPORAL_AGGREGATION_METHOD = ArithmeticMeanAggregation.class;

				/**
				 * Indicates if observations will be grouped by ObservedProperty
				 * by default.
				 */
				public static final boolean GROUP_BY_OBSERVED_PROPERTY = true;

				/**
				 * Indicates if temporal aggregation should take place before
				 * spatial aggregation.
				 */
				public static final boolean TEMPORAL_BEFORE_SPATIAL_GROUPING = false;
			}

			/**
			 * Common inputs.
			 */
			public static interface Common {

				/**
				 * The URL of the SOS from which the
				 * {@link ObservationCollection} will be fetched. Can also be a
				 * GET request.
				 */
				public static final SingleProcessInput<String> SOS_URL = new SingleProcessInput<String>(
						get("process.input.sos.source.url.id"),
						get("process.input.sos.source.url.title"),
						get("process.input.sos.source.url.desc"),
						LiteralStringBinding.class, 0, 1, null, null);

				/**
				 * The URL of the SOS in which the aggregated observations will
				 * be inserted.
				 */
				public static final SingleProcessInput<String> SOS_DESTINATION_URL = new SingleProcessInput<String>(
						get("process.input.sos.destination.url.id"),
						get("process.input.sos.destination.url.title"),
						get("process.input.sos.destination.url.desc"),
						LiteralStringBinding.class, 0, 1, null, null);

				/**
				 * The spatial {@link AggregationMethod}.
				 */
				public static final SingleProcessInput<Class<?>> SPATIAL_AGGREGATION_METHOD = new SingleProcessInput<Class<?>>(
						get("process.input.aggregationMethod.spatial.id"),
						get("process.input.aggregationMethod.spatial.title"),
						get("process.input.aggregationMethod.spatial.desc"),
						LiteralStringBinding.class, 0, 1, MethodFactory
								.getInstance().getAggregationMethods(),
						Inputs.Defaults.SPATIAL_AGGREGATION_METHOD,
						new ClassInputHandler());
				/**
				 * The temporal {@link AggregationMethod}.
				 */
				public static final SingleProcessInput<Class<?>> TEMPORAL_AGGREGATION_METHOD = new SingleProcessInput<Class<?>>(
						get("process.input.aggregationMethod.temporal.id"),
						get("process.input.aggregationMethod.temporal.title"),
						get("process.input.aggregationMethod.temporal.desc"),
						LiteralStringBinding.class, 0, 1, MethodFactory
								.getInstance().getAggregationMethods(),
						Inputs.Defaults.TEMPORAL_AGGREGATION_METHOD,
						new ClassInputHandler());

				/**
				 * Indicates if the temporal aggregation should run before the
				 * spatial aggregation.
				 * 
				 * @see Defaults#TEMPORAL_BEFORE_SPATIAL_GROUPING
				 */
				public static final SingleProcessInput<Boolean> TEMPORAL_BEFORE_SPATIAL_GROUPING = new SingleProcessInput<Boolean>(
						get("process.input.temporalBeforeSpatial.id"),
						get("process.input.temporalBeforeSpatial.title"),
						get("process.input.temporalBeforeSpatial.desc"),
						LiteralBooleanBinding.class, 0, 1, null,
						Inputs.Defaults.TEMPORAL_BEFORE_SPATIAL_GROUPING);

				/**
				 * Indicates if the observations should be grouped by
				 * ObservedProperty.
				 * 
				 * @see Defaults#GROUP_BY_OBSERVED_PROPERTY
				 */
				public static final SingleProcessInput<Boolean> GROUP_BY_OBSERVED_PROPERTY = new SingleProcessInput<Boolean>(
						get("process.input.groupingMethod.obsProp.id"),
						get("process.input.groupingMethod.obsProp.title"),
						get("process.input.groupingMethod.obsProp.desc"),
						LiteralBooleanBinding.class, 0, 1, null,
						Inputs.Defaults.GROUP_BY_OBSERVED_PROPERTY);

				/**
				 * The {@code GetObservation} request which will be postet to
				 * the SOS
				 * 
				 * @see #SOS_URL
				 */
				public static final SingleProcessInput<GetObservationDocument> SOS_REQUEST = new SingleProcessInput<GetObservationDocument>(
						get("process.input.sos.request.id"),
						get("process.input.sos.request.title"),
						get("process.input.sos.request.desc"),
						GetObservationRequestBinding.class, 0, 1, null, null);

				/**
				 * Composite input which combines {@link #SOS_URL} and
				 * {@link #SOS_REQUEST}.
				 * 
				 * @see ObservationCollectionInputHandler
				 */
				public static final AbstractProcessInput<ObservationCollection> OBSERVATION_COLLECTION_INPUT = new CompositeProcessInput<ObservationCollection>(
						"ObservationCollectionCompositeInput",
						new ObservationCollectionInputHandler(
								Inputs.Common.SOS_URL,
								Inputs.Common.SOS_REQUEST));
			}

			/**
			 * The {@link FeatureCollection} which will be merged with the
			 * {@code FeatureCollection} fetched from {@link #WFS_URL}.
			 * 
			 * @see CoverageGrouping
			 */
			public static final SingleProcessInput<FeatureCollection<FeatureType, Feature>> FEATURE_COLLECTION = new SingleProcessInput<FeatureCollection<FeatureType, Feature>>(
					get("process.input.featureCollection.id"),
					get("process.input.featureCollection.title"),
					get("process.input.featureCollection.desc"),
					GTVectorDataBinding.class, 0, 1, null, null);

			/**
			 * The URL of the WFS from which the {@link FeatureCollection} will
			 * be fetched. Can also be a GET request.
			 * 
			 * @see CoverageGrouping
			 */
			public static final SingleProcessInput<String> WFS_URL = new SingleProcessInput<String>(
					get("process.input.wfs.url.id"),
					get("process.input.wfs.url.title"),
					get("process.input.wfs.url.desc"),
					LiteralStringBinding.class, 0, 1, null, null);

			/**
			 * The request which will be posted to {@link #WFS_URL}.
			 * 
			 * @see CoverageGrouping
			 */
			public static final SingleProcessInput<GetFeatureDocument> WFS_REQUEST = new SingleProcessInput<GetFeatureDocument>(
					get("process.input.wfs.request.id"),
					get("process.input.wfs.request.title"),
					get("process.input.wfs.request.desc"),
					GetFeatureRequestBinding.class, 0, 1, null, null);

			/**
			 * The {@link Period} of time in which observations will be grouped.
			 * 
			 * @see TimeRangeGrouping
			 */
			public static final SingleProcessInput<Period> TIME_RANGE = new SingleProcessInput<Period>(
					get("process.input.timeRange.id"),
					get("process.input.timeRange.title"),
					get("process.input.timeRange.desc"),
					LiteralStringBinding.class, 1, 1, null, null,
					new PeriodInputHandler());

			/**
			 * {@link CompositeProcessInput} to combine
			 * {@link #FEATURE_COLLECTION}, {@link #WFS_URL} and
			 * {@link #WFS_REQUEST}.
			 * 
			 * @see FeatureCollectionInputHandler
			 * @see CoverageGrouping
			 */
			public static final AbstractProcessInput<FeatureCollection<FeatureType, Feature>> FEATURE_COLLECTION_INPUT = new CompositeProcessInput<FeatureCollection<FeatureType, Feature>>(
					"FeatureCollectionCompositeInput",
					new FeatureCollectionInputHandler(
							Constants.Process.Inputs.FEATURE_COLLECTION,
							Constants.Process.Inputs.WFS_URL,
							Constants.Process.Inputs.WFS_REQUEST));
		}

		/**
		 * Process outputs.
		 */
		public static interface Outputs {

			/**
			 * Process output that contains the aggregated observations.
			 */
			public static final ProcessOutput AGGREGATED_OBSERVATIONS = new ProcessOutput(
					get("process.output.aggregatedObservations.id"),
					get("process.output.aggregatedObservations.title"),
					get("process.output.aggregatedObservations.desc"),
					ObservationCollectionBinding.class);

			/**
			 * Process output that contains a {@code GetObservation} request to
			 * fetch the aggregated observations from a SOS.
			 * 
			 * @see Constants.Process.Inputs.Common#SOS_DESTINATION_URL
			 */
			public static final ProcessOutput AGGREGATED_OBSERVATIONS_REFERENCE = new ProcessOutput(
					get("process.output.aggregatedObservationsReference.id"),
					get("process.output.aggregatedObservationsReference.title"),
					get("process.output.aggregatedObservationsReference.desc"),
					GetObservationRequestBinding.class);
			
			/**
			 * Process output that contains a link to visualize the aggregated observations.
			 */
			public static final ProcessOutput VISUALIZATION_LINK = new ProcessOutput(
					get("process.output.visualizationLink.id"),
					get("process.output.visualizationLink.title"),
					get("process.output.visualizationLink.desc"),
					LiteralStringBinding.class);
		}
	}

	/**
	 * HTTP related constants.
	 */
	public static interface Http {
		
		/**
		 * Timeout properties.
		 */
		public static interface Timeout {
			
			/**
			 * Timeout property names.
			 */
			public static interface Property {
				public static final String CONNECTION = "sun.net.client.defaultConnectTimeout";
				public static final String READ = "sun.net.client.defaultReadTimeout";
			}
			
			/**
			 * Timeout for connecting.
			 */
			public static final int CONNECTION = Integer.parseInt(get(Property.CONNECTION));
			
			/**
			 * Timout for reading.
			 */
			public static final int READ = Integer.parseInt(get(Property.READ));
		}

		/**
		 * HTTP header.
		 */
		public static interface Header {
			/**
			 * Content-Type header.
			 */
			public static final String CONTENT_TYPE = "Content-Type";
			
		}
		
		/**
		 * HTTP methods.
		 */
		public static interface Method {

			/**
			 * GET method.
			 */
			public static final String GET = "GET";

			/**
			 * POST method.
			 */
			public static final String POST = "POST";

			/**
			 * HEAD method.
			 */
			public static final String HEAD = "HEAD";

			/**
			 * PUT method.
			 */
			public static final String PUT = "PUT";

			/**
			 * DELETE method.
			 */
			public static final String DELETE = "DELETE";

			/**
			 * OPTIONS method.
			 */
			public static final String OPTIONS = "OPTIONS";
		}
	}
	
	/**
	 * STAS version that will be inserted in URN's
	 */
	public static final String STAS_VERSION = "1.0.0";

	/**
	 * SOS related constants.
	 */
	public static interface Sos {

		/**
		 * Indicates that we want an <om:Observation>
		 */
		public static final QName OBSERVATION_RESULT_MODEL = new QName(
				Namespace.OM.URI, "Observation", "om");

		/**
		 * Indicates that we want an <om:Measurement>
		 */
		public static final QName MEASUREMENT_RESULT_MODEL = new QName(
				Namespace.OM.URI, "Measurement", "om");

		/**
		 * The offering id for which the aggregated observations will be
		 * registered.
		 */
		public static final String AGGREGATION_OFFERING_ID = "AGGREGATION";

		/**
		 * The offering name for which the aggregated observations will be
		 * registered.
		 */
		public static final String AGGREGATION_OFFERING_NAME = AGGREGATION_OFFERING_ID;

		/**
		 * The service type of the SOS: "SOS".
		 */
		public static final String SERVICE_NAME = "SOS";

		/**
		 * The service version used for SOS requests.
		 */
		public static final String SERVICE_VERSION = "1.0.0";

		/**
		 * SensorML 1.0.1 output format.
		 */
		public static final String SENSOR_OUTPUT_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";

		/**
		 * O&M 1.0.0 output format.
		 */
		public static final String OBSERVATION_OUTPUT_FORMAT = "text/xml;subtype=\"om/1.0.0\"";

		/**
		 * SOS operations.
		 */
		public static interface Operation {

			/**
			 * DescribeSensor operation.
			 */
			public static final String DESCRIBE_SENSOR = "DescribeSensor";

			/**
			 * GetObservationById operation.
			 */
			public static final String GET_OBSERVATION_BY_ID = "GetObservationById";

			/**
			 * GetObservation operation.
			 */
			public static final String GET_OBSERVATION = "GetObservation";

			/**
			 * GetCapabilities operation.
			 */
			public static final String GET_CAPABILITIES = "GetCapabilities";

			/**
			 * GetFeatureOfInterest operation.
			 */
			public static final String GET_FEATURE_OF_INTEREST = "GetFeatureOfInterest";

			/**
			 * RegisterSensor operation.
			 */
			public static final String REGISTER_SENSOR = "RegisterSensor";

			/**
			 * GetFeatureOfInterestTime operation.
			 */
			public static final String GetFeatureOfInterestTime = "GetFeatureOfInterestTime";

			/**
			 * GetResult operation.
			 */
			public static final String GET_RESULT = "GetResult";

			/**
			 * InstertObservation operation.
			 */
			public static final String INSERT_OBSERVATION = "InstertObservation";

			/**
			 * DescribeFeatureOfInterest operation.
			 */
			public static final String DESCRIBE_FEATURE_OF_INTEREST = "DescribeFeatureOfInterest";

		}

		/**
		 * Parameter names for SOS GET-requests.
		 */
		public static interface Parameter {

			/**
			 * "request"-parameter.
			 */
			public static final String REQUEST = "request";

			/**
			 * "service"-parameter.
			 */
			public static final String SERVICE = "service";

			/**
			 * "version"-parameter.
			 */
			public static final String VERSION = "version";

			/**
			 * "outputFormat"-parameter.
			 */
			public static final String OUTPUT_FORMAT = "outputFormat";

			/**
			 * "procedure"-parameter.
			 */
			public static final String PROCEDURE = "procedure";

			/**
			 * "ObservationId"-parameter.
			 */
			public static final String OBSERVATION_ID = "ObservationId";

		}

		/**
		 * URN's
		 */
		public static interface URN {

			/**
			 * The URN prefix of registered virtual sensor processes.
			 * 
			 * @see Constants#STAS_VERSION
			 */
			public static final String AGGREGATED_PROCESS = "urn:ogc:object:sensor:STAS:"
					+ STAS_VERSION + ":";
		}

		/**
		 * Constants for the SensorML description of registered process.
		 */
		public static interface ProcessDescription {

			/**
			 * Overall description of the process.
			 */
			public static final String SENSOR_DESCRIPTION = "Virtual process for aggregated observations.";

			/**
			 * Parameter names of used inputs.
			 */
			public static interface Parameter {

				/**
				 * URN prefix for parameters.
				 * 
				 * @see Constants#STAS_VERSION
				 */
				public static final String URN_PREFIX = "urn:ogc:def:parameter:STAS:"
						+ STAS_VERSION + ":";

				/**
				 * Parameter to indicate which {@link SpatialGrouping} was used.
				 */
				public static final String SPATIAL_GROUPING_METHOD = "spatialGroupingMethod";

				/**
				 * Parameter to indicate which {@link TemporalGrouping} was
				 * used.
				 */
				public static final String TEMPORAL_GROUPING_METHOD = "temporalGroupingMethod";

				/**
				 * Parameter to indicate which {@link AggregationMethod} was
				 * used for spatial aggregation.
				 */
				public static final String SPATIAL_AGGREGATION_METHOD = "spatialAggregationMethod";

				/**
				 * Parameter to indicate which {@link AggregationMethod} was
				 * used for temporal aggregation.
				 */
				public static final String TEMPORAL_AGGREGATION_METHOD = "temporalAggregationMethod";

				/**
				 * Parameter to indicate if the the process grouped first
				 * temporally.
				 */
				public static final String TEMPORAL_BEFORE_SPATIAL_AGGREGATION = "temporalBeforeSpatialAggregation";

				/**
				 * Parameter to indicate if the observation were grouped by
				 * ObservedProperty.
				 */
				public static final String GROUPED_BY_OBSERVED_PROPERTY = "groupedByObservedProperty";
			}

			/**
			 * Names of capabilities
			 */
			public static interface Capabilities {
				/**
				 * URN prefix for capabilities.
				 * 
				 * @see Constants#STAS_VERSION
				 */
				public static final String URN_PREFIX = "urn:ogc:def:property:STAS:"
						+ STAS_VERSION + ":";

				/**
				 * Property to indicate the time of aggregation.
				 */
				public static final String TIME_OF_AGGREGATION = "timeOfAggregation";
			}
		}
	}

	/**
	 * Numbers of cached SOS requests.
	 * 
	 * @see GetObservationRequestCache
	 */
	public static final int MAX_CACHED_REQUESTS = 20;
	
	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory.getLogger(Constants.class);
	
	/**
	 * Configuration files.
	 */
	public static interface Files {
		
		/**
		 * Method configurations.
		 */
		public static final String METHODS_CONFIG = "/methods.conf";

		/**
		 * Properties file name for common properties.
		 */
		public static final String COMMON_PROPERTIES = "/sta.properties";
		
		/**
		 * Properties file for process related properties like descriptions, id's.
		 */
		public static final String PROCESS_PROPERTIES = "/process.properties";
		
		/**
		 * The directory name in which GetObservation requests are saved.
		 */
		public static final String REQUEST_SAVE_DIRECTORY_NAME = "requests";
		
		/**
		 * The directory that contains the OpenLayers client.
		 */
		public static final String OLC_PATH = "olc";
	}
	
	/**
	 * Common properties.
	 */
	private static Properties props = null;
	
	/**
	 * Process related properties.
	 */
	private static Properties processProps = null;

	/**
	 * Don't need to be instantiated.
	 */
	private Constants() {
		super();
	}
	
	/**
	 * Loads a configuration property.
	 * 
	 * @param key
	 *            the property key
	 * @return the property
	 */
	static String get(String key) {
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
	 * Loads a property from {@link Constants#COMMON_PROPERTIES}
	 * 
	 * @param key
	 *            the property key
	 * @return the property
	 */
	protected static String getCommonProperty(String key) {
		if (props == null) {
			log.info("Loading Common Properties");
			props = new Properties();
			try {
				InputStream is = Constants.class.getResourceAsStream(Files.COMMON_PROPERTIES);
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

	/**
	 * Loads a property from {@link Constants#PROCESS_PROPERTIES}
	 * 
	 * @param key
	 *            the property key
	 * @return the property
	 */
	protected static String getProcessProperty(String key) {
		if (processProps == null) {
			log.info("Loading Process Properties");
			processProps = new Properties();
			try {
				InputStream is = Constants.class.getResourceAsStream(Files.PROCESS_PROPERTIES);
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
	
}
