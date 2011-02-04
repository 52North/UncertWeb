package org.uncertweb.sta.wps;

import static org.uncertweb.sta.utils.Constants.GROUP_BY_OBSERVED_PROPERTY_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.GROUP_BY_OBSERVED_PROPERTY_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.GROUP_BY_OBSERVED_PROPERTY_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.NULL_URN;
import static org.uncertweb.sta.utils.Constants.OBSERVATION_COLLECTION_OUTPUT_ID;
import static org.uncertweb.sta.utils.Constants.OBSERVATION_COLLECTION_OUTPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.OBSERVATION_COLLECTION_OUTPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.OBSERVATION_COLLECTION_REFERENCE_OUTPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.OBSERVATION_COLLECTION_REFERENCE_OUTPUT_ID;
import static org.uncertweb.sta.utils.Constants.OBSERVATION_COLLECTION_REFERENCE_OUTPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.PROCESS_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.DESTINATION_SOS_URL_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.DESTINATION_SOS_URL_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.DESTINATION_SOS_URL_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_REQUEST_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_REQUEST_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_REQUEST_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_URL_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_URL_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.SOURCE_SOS_URL_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.SPATIAL_AGGREGATION_METHOD_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.SPATIAL_AGGREGATION_METHOD_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.SPATIAL_AGGREGATION_METHOD_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_AGGREGATION_METHOD_INPUT_DESCRIPTION;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_AGGREGATION_METHOD_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_AGGREGATION_METHOD_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_DESC;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_ID;
import static org.uncertweb.sta.utils.Constants.TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_TITLE;
import static org.uncertweb.sta.utils.Constants.UTF8_ENCODING;
import static org.uncertweb.sta.utils.Constants.XML_MIME_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.n52.wps.server.AlgorithmParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.sta.wps.OriginAwareObservation;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.method.MethodFactory;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.aggregation.ArithmeticMeanAggregation;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.ObservedPropertyGrouping;
import org.uncertweb.sta.wps.method.grouping.spatial.NoSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.spatial.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.NoTemporalGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.TemporalGrouping;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

/**
 * Super class for all Observation Aggregation Processes
 * 
 * @author Christian Autermann
 */
public class GenericObservationAggregationProcess extends ExtendedSelfDescribingAlgorithm {

	private static final AggregationMethod DEFAULT_SPATIAL_AGGREGATION_METHOD = new ArithmeticMeanAggregation();
	private static final AggregationMethod DEFAULT_TEMPORAL_AGGREGATION_METHOD = new ArithmeticMeanAggregation();
	private static final Boolean GROUP_BY_OBSERVED_PROPERTY_DEFAULT = true;
	private static final boolean TEMPORAL_BEFORE_SPATIAL_GROUPING_DEFAULT = false;

	protected static final ProcessInput SOS_URL = new ProcessInput(
			SOURCE_SOS_URL_INPUT_ID, SOURCE_SOS_URL_INPUT_TITLE,
			SOURCE_SOS_URL_INPUT_DESCRIPTION, LiteralStringBinding.class, 1, 1);
	protected static final ProcessInput SOS_DESTINATION_URL = new ProcessInput(
			DESTINATION_SOS_URL_INPUT_ID, DESTINATION_SOS_URL_INPUT_TITLE,
			DESTINATION_SOS_URL_INPUT_DESCRIPTION, LiteralStringBinding.class);
	protected static final ProcessInput SPATIAL_AGGREGATION_METHOD = new ProcessInput(
			SPATIAL_AGGREGATION_METHOD_INPUT_ID,
			SPATIAL_AGGREGATION_METHOD_INPUT_TITLE,
			SPATIAL_AGGREGATION_METHOD_INPUT_DESCRIPTION,
			LiteralStringBinding.class, MethodFactory.getInstance()
					.getAggregationMethods(),
			DEFAULT_SPATIAL_AGGREGATION_METHOD.getClass().getName());
	protected static final ProcessInput TEMPORAL_AGGREGATION_METHOD = new ProcessInput(
			TEMPORAL_AGGREGATION_METHOD_INPUT_ID,
			TEMPORAL_AGGREGATION_METHOD_INPUT_TITLE,
			TEMPORAL_AGGREGATION_METHOD_INPUT_DESCRIPTION,
			LiteralStringBinding.class, MethodFactory.getInstance()
					.getAggregationMethods(),
			DEFAULT_TEMPORAL_AGGREGATION_METHOD.getClass().getName());
	protected static final ProcessInput TEMPORAL_BEFORE_SPATIAL_GROUPING = new ProcessInput(
			TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_ID,
			TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_TITLE,
			TEMPORAL_BEFORE_SPATIAL_GROUPING_INPUT_DESC,
			LiteralBooleanBinding.class, null,
			String.valueOf(TEMPORAL_BEFORE_SPATIAL_GROUPING_DEFAULT));
	protected static final ProcessInput GROUP_BY_OBSERVED_PROPERTY = new ProcessInput(
			GROUP_BY_OBSERVED_PROPERTY_INPUT_ID,
			GROUP_BY_OBSERVED_PROPERTY_INPUT_TITLE,
			GROUP_BY_OBSERVED_PROPERTY_INPUT_DESCRIPTION,
			LiteralBooleanBinding.class, null,
			String.valueOf(GROUP_BY_OBSERVED_PROPERTY_DEFAULT));
	protected static final ProcessInput SOS_REQUEST = new ProcessInput(
			SOURCE_SOS_REQUEST_INPUT_ID, SOURCE_SOS_REQUEST_INPUT_TITLE,
			SOURCE_SOS_REQUEST_INPUT_DESCRIPTION,
			GetObservationRequestBinding.class);

	protected static final ProcessOutput AGGREGATED_OBSERVATIONS = new ProcessOutput(
			OBSERVATION_COLLECTION_OUTPUT_ID,
			OBSERVATION_COLLECTION_OUTPUT_TITLE,
			OBSERVATION_COLLECTION_OUTPUT_DESCRIPTION,
			ObservationCollectionBinding.class);
	protected static final ProcessOutput AGGREGATED_OBSERVATIONS_REFERENCE = new ProcessOutput(
			OBSERVATION_COLLECTION_REFERENCE_OUTPUT_ID,
			OBSERVATION_COLLECTION_REFERENCE_OUTPUT_TITLE,
			OBSERVATION_COLLECTION_REFERENCE_OUTPUT_DESCRIPTION,
			GetObservationRequestBinding.class);

	private static final Logger log = LoggerFactory.getLogger(GenericObservationAggregationProcess.class);

	private Class<? extends SpatialGrouping> sg;
	private Class<? extends TemporalGrouping> tg;
	private String identifier, title;
	private Set<ProcessInput> commonInputs;
	private Set<ProcessInput> tgInputs;
	private Set<ProcessInput> sgInputs;
	private static long observationIdCount = 0;
	
	/**
	 * constructor
	 * 
	 * @param identifier
	 * 			identifier of the process
	 * @param title
	 * 			title of the process
	 * @param sg
	 * 			spatial grouping method
	 * @param tg
	 * 			temporal grouping method
	 */
	public GenericObservationAggregationProcess(String identifier, 
												String title, 
												Class<? extends SpatialGrouping> sg,
												Class<? extends TemporalGrouping> tg) {
		this.sg = sg;
		this.tg = tg;
		this.identifier = identifier;
		this.title = title;
		commonInputs = Utils.set(SPATIAL_AGGREGATION_METHOD,
				GROUP_BY_OBSERVED_PROPERTY, SOS_DESTINATION_URL, SOS_URL,
				SOS_REQUEST, TEMPORAL_AGGREGATION_METHOD,
				TEMPORAL_BEFORE_SPATIAL_GROUPING);

		sgInputs = newSpatialGrouping().getAdditionalInputDeclarations();
		tgInputs = newTemporalGrouping().getAdditionalInputDeclarations();
	}

	@Override
	protected String getIdentifier() {
		return this.identifier;
	}

	@Override
	protected String getTitle() {
		return this.title;
	}

	@Override
	protected String getAbstract() {
		return new StringBuffer("\n").append(PROCESS_DESCRIPTION).append("\n")
				.append("Spatial grouping method: ").append(sg.getName())
				.append("\n").append(newSpatialGrouping().getDescription())
				.append("\n").append("\n").append("Temporal grouping method: ")
				.append(tg.getName()).append("\n")
				.append(newTemporalGrouping().getDescription()).append("\n")
				.toString();
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	//method creates a multiset from the common inputs and the inputs of the spatial and temporal grouping
	protected Set<ProcessInput> getInputs() {
		//check whether inputs common process inputs are set
		if (commonInputs == null)
			throw new NullPointerException();
		//check inputs of temporal grouping
		if (tgInputs == null)
			throw new NullPointerException();
		//check inputs of spatial grouping
		if (sgInputs == null)
			throw new NullPointerException();
		//create multiset of inputs
		return Utils.multiSet(commonInputs, tgInputs, sgInputs);
	}

	@Override
	protected Set<ProcessOutput> getOutputs() {
		return Utils.set(AGGREGATED_OBSERVATIONS, AGGREGATED_OBSERVATIONS_REFERENCE);
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputs) {

		final String process = Utils.generateRandomProcessUrn();
		
		long start = System.currentTimeMillis();

		/* get the inputs */
		ObservationCollection observations = getObservationCollection(inputs);
		AggregationMethod spatialAggregationMethod = getSpatialAggregationMethod(inputs);
		AggregationMethod temporalAggregationMethod = getTemporalAggregationMethod(inputs);
		boolean groupByObservedProperty = getGroupByObservedPropertyFlag(inputs);
		boolean temporalBeforeSpatial = getTemporalBeforeSpatialGroupingFlag(inputs);

		/* get the grouping method inputs */
		Map<ProcessInput, List<IData>> sgInputMap = getMethodParameters(sgInputs, inputs);
		Map<ProcessInput, List<IData>> tgInputMap =  getMethodParameters(tgInputs, inputs);

		log.info("Using Process URN: {}", process);
		log.info("Using Spatial Grouping Method: {}", sg.getName());
		log.info("Using Spatial Aggregation Method: {}", spatialAggregationMethod.getClass().getName());
		log.info("Using Temporal Grouping Method: {}", tg.getName());
		log.info("Using Temporal Aggregation Method: {}", temporalAggregationMethod.getClass().getName());
		log.info("Input: {} Observations.", observations.size());
	
		/* sort by observed property or set it to null */
		HashMap<String, List<Observation>> obs = new HashMap<String, List<Observation>>();
		if (groupByObservedProperty) {
			ObservedPropertyGrouping obsPropMethod = new ObservedPropertyGrouping(observations);
			for (ObservationMapping<String> obsPropMap : obsPropMethod) {
				obs.put(obsPropMap.getKey(), obsPropMap.getObservations());
			}
			log.info("Grouping by ObservedProperty. Got {} distinct.", obs.size());
		} else {
			obs.put(NULL_URN, observations);
		}
		
		LinkedList<Observation> result = new LinkedList<Observation>();

		String sosUrl = getSOSUrl(inputs);
		int i = sosUrl.lastIndexOf('?');
		if (i > 0)
			sosUrl = sosUrl.substring(0, i);
		
		for (Entry<String, List<Observation>> e : obs.entrySet()) {
			if (temporalBeforeSpatial) {
				List<Observation> firstResult = doTemporalAggregation(process, sosUrl, temporalAggregationMethod, e.getKey(), tgInputMap, e.getValue());
				result.addAll(doSpatialAggregation(process, sosUrl, spatialAggregationMethod, e.getKey(), sgInputMap, firstResult));
			} else {
				List<Observation> firstResult = doSpatialAggregation(process, sosUrl, spatialAggregationMethod, e.getKey(), sgInputMap, e.getValue());
				result.addAll(doTemporalAggregation(process, sosUrl, temporalAggregationMethod, e.getKey(),	tgInputMap, firstResult));
			}
			
		}
		
		/* Outputs */
		Map<String, IData> response = new HashMap<String, IData>();
		response.put(AGGREGATED_OBSERVATIONS.getIdentifier(),
				new ObservationCollectionBinding(new ObservationCollection(result)));

		String destinationUrl = (String) Utils.getSingleParam(SOS_DESTINATION_URL, inputs);
		if (destinationUrl != null) {
			SOSRequestBuilder sos = new SOSRequestBuilder();
			GetObservationRequestBinding b;
			try {
				b = sos.registerAggregatedObservations(result, destinationUrl, process);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			response.put(AGGREGATED_OBSERVATIONS_REFERENCE.getIdentifier(), b);
		}

		log.info("Output: Generated {} Observations in {}.", result.size(), Utils.timeElapsed(start));

		return response;
	}

	protected List<Observation> doTemporalAggregation(String process,
			String sourceUrl, AggregationMethod m, String obsProp,
			Map<ProcessInput, List<IData>> inputs, List<Observation> obs) {
		List<Observation> result = new LinkedList<Observation>();
		/* only process observations with equal FeatureOfInterest */
		for (ObservationMapping<ISamplingFeature> fMap : new NoSpatialGrouping(obs)) {
			TemporalGrouping temporalMethod = newTemporalGrouping(fMap.getObservations(), inputs);
			for (ObservationMapping<ObservationTime> tMap : temporalMethod) {
				result.add(aggregate(process, sourceUrl, m, obsProp, fMap.getKey(), tMap.getKey(), tMap.getObservations()));
			}
		}
		return result;
	}
	
	protected List<Observation> doSpatialAggregation(String process,
			String sourceUrl, AggregationMethod m, String obsProp,
			Map<ProcessInput, List<IData>> inputs, List<Observation> obs) {
		List<Observation> result = new LinkedList<Observation>();
		/* only process observations with equal ObservationTime */
		for (ObservationMapping<ObservationTime> tMap : new NoTemporalGrouping(obs)) {
			SpatialGrouping spatialMethod = newSpatialGrouping(tMap.getObservations(), inputs);
			for (ObservationMapping<ISamplingFeature> fMap : spatialMethod) {
				result.add(aggregate(process, sourceUrl, m, obsProp, fMap.getKey(), tMap.getKey(), fMap.getObservations()));
			}
		}
		return result;
	}
	
	
	/**
	 * helper method for getting the observation collection from the process
	 * inputs; might be either contained in input parameter
	 * ObservationCollection or has to be queried from SOS; if all parameters
	 * are set, observations queried from external SOS are added to observations
	 * passed in the execute request
	 * 
	 * @param inputs
	 *            Map containing the process inputs
	 * @return Returns ObservationCollection which contains the observation to
	 *         be aggregated
	 */
	protected ObservationCollection getObservationCollection(Map<String, List<IData>> inputs) {
		
		//extract relevant parameters
		String sosUrl = getSOSUrl(inputs);
		GetObservationDocument sosReq = (GetObservationDocument) Utils
				.getSingleParam(SOS_REQUEST, inputs);
		
		//observations have to be queried from SOS
		if (sosUrl == null) {
			throw new AlgorithmParameterException("No Source SOS Url.");
		}
		AbstractXMLParser p = (AbstractXMLParser) ParserFactory
			.getInstance().getParser(Namespace.OM.SCHEMA,
					XML_MIME_TYPE, UTF8_ENCODING,
					ObservationCollectionBinding.class);
		try {
			InputStream is = null;
			if (sosReq != null) {
				is = Utils.sendPostRequest(sosUrl, sosReq.xmlText());
			} else {
				// URL encoded request
				is = Utils.sendGetRequest(sosUrl);
			}
			return ((ObservationCollectionBinding) p.parseXML(is)).getPayload();
		} catch (Exception e) {
			log.error("Error while retrieving ObservationCollection from " + sosUrl, e);
			throw new RuntimeException(e);
		}
	}
	
	protected String getSOSUrl(Map<String, List<IData>> inputs) {
		return (String) Utils.getSingleParam(SOS_URL, inputs);
	}
	
	protected boolean getTemporalBeforeSpatialGroupingFlag(Map<String, List<IData>> inputs) {
		Boolean temporalBeforeSpatial = (Boolean) Utils.getSingleParam(TEMPORAL_BEFORE_SPATIAL_GROUPING, inputs);
		return (temporalBeforeSpatial == null) ? TEMPORAL_BEFORE_SPATIAL_GROUPING_DEFAULT : temporalBeforeSpatial.booleanValue();
	}
	
	protected boolean getGroupByObservedPropertyFlag(Map<String, List<IData>> inputs) {
		Boolean groupByObservedProperty = (Boolean) Utils.getSingleParam(GROUP_BY_OBSERVED_PROPERTY, inputs);
		return (groupByObservedProperty == null) ? GROUP_BY_OBSERVED_PROPERTY_DEFAULT : groupByObservedProperty.booleanValue();
	}
	
	protected AggregationMethod getSpatialAggregationMethod(Map<String, List<IData>> inputs) {
		String spatialAggregationMethodName = (String) Utils.getSingleParam(SPATIAL_AGGREGATION_METHOD, inputs);
		return (spatialAggregationMethodName == null) ? DEFAULT_SPATIAL_AGGREGATION_METHOD : 
					MethodFactory.getInstance().getAggregationMethod(spatialAggregationMethodName);
	}
	
	protected AggregationMethod getTemporalAggregationMethod(Map<String, List<IData>> inputs) {
		String temporalAggregationMethodName = (String) Utils.getSingleParam(TEMPORAL_AGGREGATION_METHOD, inputs);
		return (temporalAggregationMethodName == null) ? DEFAULT_TEMPORAL_AGGREGATION_METHOD : 
					MethodFactory.getInstance().getAggregationMethod(temporalAggregationMethodName);
	}
	
	protected Map<ProcessInput, List<IData>> getMethodParameters(Set<ProcessInput> wanted, Map<String, List<IData>> inputs) {
		HashMap<ProcessInput, List<IData>> result = new HashMap<ProcessInput, List<IData>>();
		for (ProcessInput i : wanted) {
			List<IData> data = inputs.get(i.getIdentifier());
			if (data != null) result.put(i, data);
		}
		return result;
	}
	
	protected SpatialGrouping newSpatialGrouping() {
		return newSpatialGrouping(null, null);
	}

	protected SpatialGrouping newSpatialGrouping(List<Observation> obs,
			Map<ProcessInput, List<IData>> inputs) {
		try {
			SpatialGrouping g = sg.newInstance();
			g.setInputs(obs, inputs);
			return g;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected TemporalGrouping newTemporalGrouping() {
		return newTemporalGrouping(null, null);
	}

	protected TemporalGrouping newTemporalGrouping(List<Observation> obs,
			Map<ProcessInput, List<IData>> inputs) {
		try {
			TemporalGrouping g = tg.newInstance();
			g.setInputs(obs, inputs);
			return g;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected OriginAwareObservation aggregate(String process,
			String sourceUrl, AggregationMethod method,
			String observedProperty, ISamplingFeature f,
			ObservationTime time, List<Observation> obs) {
		String uom = null;
		HashSet<Observation> sourceObservations = new HashSet<Observation>();
		for (Observation o : obs) {
			if (o instanceof OriginAwareObservation) {
				sourceObservations.addAll(((OriginAwareObservation)o).getSourceObservations());
			} else {
				sourceObservations.add(o);
			}
			if (o.getUom() != null) {
			if (uom == null) {
					uom = o.getUom();
				} else {
					if (!uom.equals(o.getUom())) {
						throw new RuntimeException("Not matching UoM: " + uom
								+ " - " + o.getUom() + ". Unit conversation is not supported.");
					}
				}
			}
		}
		double aggregatedValue = method.aggregate(obs);
		return new OriginAwareObservation("ao_"
				+ String.valueOf(observationIdCount++), aggregatedValue, f,
				null, observedProperty, process, f.getLocation().getSRID(),
				time, uom, sourceObservations, sourceUrl);
	}
}
