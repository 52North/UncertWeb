package org.uncertweb.sta.utils;

import static org.uncertweb.sta.utils.Utils.get;

/**
 * @author Christian Autermann
 */
public class Constants extends org.uncertweb.intamap.utils.Constants {
	private Constants(){ super(); }
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
	public static final String XML_MIME_TYPE = get("mime.xml");
	public static final String UTF8_ENCODING = get("encoding.utf8");
	
	public static final String SOS_SERVICE_NAME = get("sos.service");
	public static final String SOS_SERVICE_VERSION = get("sos.version");
	public static final String SOS_DESCRIBE_SENSOR_OPERATION = get("sos.describeSensor");
	public static final String SOS_SENSOR_OUTPUT_FORMAT = get("sos.sensorOutputFormat");
	public static final String SOS_OBSERVATION_OUTPUT_FORMAT = get("sos.observationOutputFormat");
	public static final String SOS_GET_OBSERVATION_BY_ID_OPERATION = get("sos.getObservationById");
	
	public static final String AGGREGATION_OFFERING_ID = get("sos.aggregationOffering.id");
	public static final String AGGREGATION_OFFERING_NAME = get("sos.aggregationOffering.name");

}
