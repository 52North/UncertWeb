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
import org.uncertweb.sta.wps.ClassProcessInputHandler;
import org.uncertweb.sta.wps.CompositeProcessInput;
import org.uncertweb.sta.wps.IProcessInput;
import org.uncertweb.sta.wps.PeriodProcessInputHandler;
import org.uncertweb.sta.wps.ProcessOutput;
import org.uncertweb.sta.wps.SOSProcessInputHandler;
import org.uncertweb.sta.wps.SingleProcessInput;
import org.uncertweb.sta.wps.WFSProcessInputHandler;
import org.uncertweb.sta.wps.method.MethodFactory;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.aggregation.ArithmeticMeanAggregation;
import org.uncertweb.sta.wps.xml.binding.GetFeatureRequestBinding;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

/**
 * @author Christian Autermann
 */
public class Constants extends org.uncertweb.intamap.utils.Constants {
	
	public static interface Process {
		
		public static final String DESCRIPTION = get("process.description");

		public static interface Inputs {
		
			public static interface Defaults {
				public static final Class<? extends AggregationMethod> SPATIAL_AGGREGATION_METHOD = ArithmeticMeanAggregation.class;
				public static final Class<? extends AggregationMethod> TEMPORAL_AGGREGATION_METHOD = ArithmeticMeanAggregation.class;
				public static final boolean GROUP_BY_OBSERVED_PROPERTY = true;
				public static final boolean TEMPORAL_BEFORE_SPATIAL_GROUPING = false;
			}
			
			public static interface Common {
			
				public static final SingleProcessInput<String> SOS_URL = new SingleProcessInput<String>(
						get("process.input.sos.source.url.id"),
						get("process.input.sos.source.url.title"),
						get("process.input.sos.source.url.desc"),
						LiteralStringBinding.class, 0, 1, null, null);
				
				public static final SingleProcessInput<String> SOS_DESTINATION_URL = new SingleProcessInput<String>(
						get("process.input.sos.destination.url.id"), get("process.input.sos.destination.url.title"), get("process.input.sos.destination.url.desc"),
						LiteralStringBinding.class, 0, 1, null, null);
				
				public static final SingleProcessInput<Class<?>> SPATIAL_AGGREGATION_METHOD = new SingleProcessInput<Class<?>>(
						get("process.input.aggregationMethod.spatial.id"), get("process.input.aggregationMethod.spatial.title"), get("process.input.aggregationMethod.spatial.desc"),
						LiteralStringBinding.class, 0, 1, 
						MethodFactory.getInstance().getAggregationMethods(),
						Inputs.Defaults.SPATIAL_AGGREGATION_METHOD, new ClassProcessInputHandler());
				
				public static final SingleProcessInput<Class<?>> TEMPORAL_AGGREGATION_METHOD = new SingleProcessInput<Class<?>>(
						get("process.input.aggregationMethod.temporal.id"),
						get("process.input.aggregationMethod.temporal.title"),
						get("process.input.aggregationMethod.temporal.desc"),
						LiteralStringBinding.class, 0, 1,
						MethodFactory.getInstance().getAggregationMethods(),
						Inputs.Defaults.TEMPORAL_AGGREGATION_METHOD, new ClassProcessInputHandler());
				
				public static final SingleProcessInput<Boolean> TEMPORAL_BEFORE_SPATIAL_GROUPING = new SingleProcessInput<Boolean>(
						get("process.input.temporalBeforeSpatial.id"),
						get("process.input.temporalBeforeSpatial.title"),
						get("process.input.temporalBeforeSpatial.desc"),
						LiteralBooleanBinding.class, 0, 1, null,
						Inputs.Defaults.TEMPORAL_BEFORE_SPATIAL_GROUPING);
				
				public static final SingleProcessInput<Boolean> GROUP_BY_OBSERVED_PROPERTY = new SingleProcessInput<Boolean>(
						get("process.input.groupingMethod.obsProp.id"),
						get("process.input.groupingMethod.obsProp.title"),
						get("process.input.groupingMethod.obsProp.desc"),
						LiteralBooleanBinding.class, 0, 1, null,
						Inputs.Defaults.GROUP_BY_OBSERVED_PROPERTY);
				
				public static final SingleProcessInput<GetObservationDocument> SOS_REQUEST = new SingleProcessInput<GetObservationDocument>(
						get("process.input.sos.request.id"), get("process.input.sos.request.title"), get("process.input.sos.request.desc"),
						GetObservationRequestBinding.class, 0, 1, null, null);
				public static final IProcessInput<ObservationCollection> OBSERVATION_COLLECTION_INPUT 
				= new CompositeProcessInput<ObservationCollection>("ObservationCollectionCompositeInput", 
						new SOSProcessInputHandler(), Inputs.Common.SOS_URL, Inputs.Common.SOS_REQUEST);
			}
			
			public static final SingleProcessInput<FeatureCollection<FeatureType, Feature>> FEATURE_COLLECTION = new SingleProcessInput<FeatureCollection<FeatureType, Feature>>(
					get("process.input.featureCollection.id"),
					get("process.input.featureCollection.title"),
					get("process.input.featureCollection.desc"),
					GTVectorDataBinding.class, 0, 1, null, null);
			
			public static final SingleProcessInput<String> WFS_URL = new SingleProcessInput<String>(
					get("process.input.wfs.url.id"),
					get("process.input.wfs.url.title"),
					get("process.input.wfs.url.desc"),
					LiteralStringBinding.class, 0, 1, null, null);
			
			public static final SingleProcessInput<GetFeatureDocument> WFS_REQUEST = new SingleProcessInput<GetFeatureDocument>(
					get("process.input.wfs.request.id"),
					get("process.input.wfs.request.title"),
					get("process.input.wfs.request.desc"),
					GetFeatureRequestBinding.class, 0, 1, null, null);
	
			public static final SingleProcessInput<Period> TIME_RANGE = new SingleProcessInput<Period>(
					get("process.input.timeRange.id"),
					get("process.input.timeRange.title"),
					get("process.input.timeRange.desc"),
					LiteralStringBinding.class, 1, 1, null, null, new PeriodProcessInputHandler());
			
			public static final IProcessInput<FeatureCollection<FeatureType, Feature>> FEATURE_COLLECTION_INPUT 
					= new CompositeProcessInput<FeatureCollection<FeatureType, Feature>>("FeatureCollectionCompositeInput",
						new WFSProcessInputHandler(), Constants.Process.Inputs.FEATURE_COLLECTION,
							Constants.Process.Inputs.WFS_URL, Constants.Process.Inputs.WFS_REQUEST);
		}
		
		public static interface Outputs {
			
			public static final ProcessOutput AGGREGATED_OBSERVATIONS = new ProcessOutput(
					get("process.output.aggregatedObservations.id"),
					get("process.output.aggregatedObservations.title"),
					get("process.output.aggregatedObservations.desc"),
					ObservationCollectionBinding.class);
			
			public static final ProcessOutput AGGREGATED_OBSERVATIONS_REFERENCE = new ProcessOutput(
					get("process.output.aggregatedObservationsReference.id"),
					get("process.output.aggregatedObservationsReference.title"),
					get("process.output.aggregatedObservationsReference.desc"),
					GetObservationRequestBinding.class);
		}
	}
	
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

	public static final String STAS_VERSION = "1.0.0";
	
	
	
	public static interface Sos {
		
		public static final QName OBSERVATION_RESULT_MODEL = new QName(Namespace.OM.URI, "Observation", "om");
		public static final QName MEASUREMENT_RESULT_MODEL = new QName(Namespace.OM.URI, "Measurement", "om");
		
		public static final String AGGREGATION_OFFERING_ID = "AGGREGATION";
		public static final String AGGREGATION_OFFERING_NAME = AGGREGATION_OFFERING_ID;
		
		public static final String SERVICE_NAME = "SOS";
		public static final String SERVICE_VERSION = "1.0.0";
		public static final String SENSOR_OUTPUT_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";
		public static final String OBSERVATION_OUTPUT_FORMAT = "text/xml;subtype=\"om/1.0.0\"";
		
		public static interface Operation {
			public static final String DESCRIBE_SENSOR = "DescribeSensor";
			public static final String GET_OBSERVATION_BY_ID = "GetObservationById";
		}
		
		public static interface Parameter {
			public static final String REQUEST = "request";
			public static final String SERVICE = "service";
			public static final String VERSION = "version";
			public static final String OUTPUT_FORMAT = "outputFormat";
			public static final String PROCEDURE = "procedure";
			public static final String OBSERVATION_ID = "ObservationId";
			
			
		}
		public static interface URN {
			public static final String AGGREGATED_PROCESS = "urn:ogc:object:sensor:STAS:" + STAS_VERSION + ":";
		}
		
		public static interface ProcessDescription {
			public static final String SENSOR_DESCRIPTION = "Virtual process for aggregated observations.";
			
			
			public static interface Parameter {
				public static final String URN_PREFIX = "urn:ogc:def:parameter:STAS:" + STAS_VERSION + ":";
				public static final String SPATIAL_GROUPING_METHOD = "spatialGroupingMethod";
				public static final String TEMPORAL_GROUPING_METHOD = "temporalGroupingMethod";
				public static final String SPATIAL_AGGREGATION_METHOD = "spatialAggregationMethod";
				public static final String TEMPORAL_AGGREGATION_METHOD = "temporalAggregationMethod";
				public static final String TEMPORAL_BEFORE_SPATIAL_AGGREGATION = "temporalBeforeSpatialAggregation";
				public static final String GROUPED_BY_OBSERVED_PROPERTY = "groupedByObservedProperty";
			}
			
			public static interface Capabilities {
				public static final String URN_PREFIX = "urn:ogc:def:property:STAS:" + STAS_VERSION + ":";
				public static final String TIME_OF_AGGREGATION = "timeOfAggregation";
			}
		}
	}
	
	
	
	public static final int MAX_CACHED_REQUESTS = 20;

	private Constants(){ super(); }

	static String get(String key) {
		String prop = null;
		if (key.startsWith("process")) {
			prop = getProcessProperty(key);
		} else {
			prop = getCommonProperty(key);
		}
		if (prop == null || prop.trim().equals("")) {
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
