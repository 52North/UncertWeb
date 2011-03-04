package org.uncertweb.sta.wps;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Constants.Process.Inputs;
import org.uncertweb.sta.utils.Constants.Process.Outputs;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.ObservedPropertyGrouping;
import org.uncertweb.sta.wps.method.grouping.spatial.NoSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.spatial.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.NoTemporalGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.TemporalGrouping;
import org.uncertweb.sta.wps.om.OriginAwareObservation;
import org.uncertweb.sta.wps.sos.SOSClient;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

/**
 * Super class for all Observation Aggregation Processes
 * 
 * @author Christian Autermann
 */
public class GenericObservationAggregationProcess extends ExtendedSelfDescribingAlgorithm {

	protected static final Logger log = LoggerFactory.getLogger(GenericObservationAggregationProcess.class);
	protected static final Set<IProcessInput<?>> commonInputs = getCommonInputs();

	protected static long observationIdCount = 0;

	protected Class<? extends SpatialGrouping> sg;
	protected Class<? extends TemporalGrouping> tg;
	protected String identifier, title;
	protected Set<IProcessInput<?>> tgInputs;
	protected Set<IProcessInput<?>> sgInputs;

	private static HashSet<IProcessInput<?>> getCommonInputs() {
		HashSet<IProcessInput<?>> inputs = new HashSet<IProcessInput<?>>();
		inputs.add(Inputs.Common.OBSERVATION_COLLECTION_INPUT);
		inputs.add(Inputs.Common.SPATIAL_AGGREGATION_METHOD);
		inputs.add(Inputs.Common.GROUP_BY_OBSERVED_PROPERTY);
		inputs.add(Inputs.Common.SOS_DESTINATION_URL);
		inputs.add(Inputs.Common.TEMPORAL_AGGREGATION_METHOD);
		inputs.add(Inputs.Common.TEMPORAL_BEFORE_SPATIAL_GROUPING);
		return inputs;
	}
	
	/**
	 * constructor
	 * 
	 * @param identifier
	 *            identifier of the process
	 * @param title
	 *            title of the process
	 * @param sg
	 *            spatial grouping method
	 * @param tg
	 *            temporal grouping method
	 */
	public GenericObservationAggregationProcess(String identifier,
			String title, Class<? extends SpatialGrouping> sg,
			Class<? extends TemporalGrouping> tg) {
		this.identifier = identifier;
		this.title = title;
		this.sg = sg;
		this.tg = tg;
		this.sgInputs = newSpatialGrouping(null,null).getAdditionalInputDeclarations();
		this.tgInputs = newTemporalGrouping(null,null).getAdditionalInputDeclarations();
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
		return new StringBuffer("\n")
				.append(Constants.Process.DESCRIPTION).append("\n")
				.append("Spatial grouping method: ").append(sg.getName()).append("\n")
				.append(Utils.getMethodDescription(sg)).append("\n")
				.append("\n")
				.append("Temporal grouping method: ").append(tg.getName()).append("\n")
				.append(Utils.getMethodDescription(tg)).append("\n")
				.toString();
	}
	
	
	@Override
	protected Set<IProcessInput<?>> getInputs() {
		if (tgInputs == null) throw new NullPointerException();
		if (sgInputs == null) throw new NullPointerException();
		Set<IProcessInput<?>> set = new HashSet<IProcessInput<?>>();
		set.addAll(commonInputs);
		set.addAll(tgInputs);
		set.addAll(sgInputs);
		return set;
	}

	@Override
	protected Set<ProcessOutput> getOutputs() {
		return Utils.set(Outputs.AGGREGATED_OBSERVATIONS, 
						 Outputs.AGGREGATED_OBSERVATIONS_REFERENCE);
	}

	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputs) {
		try {
	
			final String process = Utils.generateRandomProcessUrn();
			
			long start = System.currentTimeMillis();
	
			/* get the inputs */
			
			/* TODO do the following synchronously */
			ObservationCollection observations = Inputs.Common.OBSERVATION_COLLECTION_INPUT.handle(inputs);
			boolean groupByObservedProperty = Inputs.Common.GROUP_BY_OBSERVED_PROPERTY.handle(inputs);
			boolean temporalBeforeSpatial = Inputs.Common.TEMPORAL_BEFORE_SPATIAL_GROUPING.handle(inputs);
			AggregationMethod temporalAggregationMethod = (AggregationMethod) Inputs.Common.TEMPORAL_AGGREGATION_METHOD.handle(inputs).newInstance();
			AggregationMethod spatialAggregationMethod = (AggregationMethod) Inputs.Common.SPATIAL_AGGREGATION_METHOD.handle(inputs).newInstance();

			/* get the grouping method inputs */
	
			HashMap<IProcessInput<?>, Object>  sgInputMap = new HashMap<IProcessInput<?>, Object>();
			for (IProcessInput<?> i : sgInputs) { sgInputMap.put(i, i.handle(inputs)); }
			
			HashMap<IProcessInput<?>, Object>  tgInputMap = new HashMap<IProcessInput<?>, Object>();
			for (IProcessInput<?> i : tgInputs) { tgInputMap.put(i, i.handle(inputs)); }
			
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
				obs.put(Constants.NULL_URN, observations);
			}
			
			LinkedList<Observation> result = new LinkedList<Observation>();
	
			String sosUrl = Inputs.Common.SOS_URL.handle(inputs);
			int i = sosUrl.lastIndexOf('?');
			if (i > 0) {
				sosUrl = sosUrl.substring(0, i);
			}
			
			for (Entry<String, List<Observation>> e : obs.entrySet()) {
				if (temporalBeforeSpatial) {
					List<Observation> firstResult = doTemporalAggregation(process, sosUrl, temporalAggregationMethod, e.getKey(), tgInputMap, e.getValue());
					result.addAll(doSpatialAggregation(process, sosUrl, spatialAggregationMethod, e.getKey(), sgInputMap, firstResult));
				} else {
					List<Observation> firstResult = doSpatialAggregation(process, sosUrl, spatialAggregationMethod, e.getKey(), sgInputMap, e.getValue());
					result.addAll(doTemporalAggregation(process, sosUrl, temporalAggregationMethod, e.getKey(),	tgInputMap, firstResult));
				}
				
			}
			
			log.info("Output: Generated {} Observations in {}.", result.size(), Utils.timeElapsed(start));
			
			/* Outputs */
			Map<String, IData> response = new HashMap<String, IData>();
			response.put(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS.getId(),
					new ObservationCollectionBinding(new ObservationCollection(result)));
	
			String destinationUrl = Inputs.Common.SOS_DESTINATION_URL.handle(inputs);
			if (destinationUrl != null) {
				GetObservationRequestBinding b;
				try {
					long insertStart = System.currentTimeMillis();
					Map<String,String> meta = new HashMap<String, String>();
					
					/* TODO add inputs of methods... like time range */
					meta.put(Constants.Sos.ProcessDescription.Parameter.GROUPED_BY_OBSERVED_PROPERTY, String.valueOf(groupByObservedProperty));
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_BEFORE_SPATIAL_AGGREGATION, String.valueOf(temporalBeforeSpatial));
					meta.put(Constants.Sos.ProcessDescription.Parameter.SPATIAL_GROUPING_METHOD, this.sg.getName());
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_GROUPING_METHOD, this.tg.getName() );
					meta.put(Constants.Sos.ProcessDescription.Parameter.SPATIAL_AGGREGATION_METHOD, spatialAggregationMethod.getClass().getName());
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_AGGREGATION_METHOD, temporalAggregationMethod.getClass().getName());
					
					log.info("Inserting Observations into SOS: {}", destinationUrl);
					b = new SOSClient().registerAggregatedObservations(result, destinationUrl, process, meta);
					log.info("Registered {} Observations in {}.", result.size(), Utils.timeElapsed(insertStart));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	
				response.put(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_REFERENCE.getId(), b);
			}
	
			log.info("Served Request in {}.", Utils.timeElapsed(start));
	
			return response;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	protected List<Observation> doTemporalAggregation(String process,
			String sourceUrl, AggregationMethod m, String obsProp,
			Map<IProcessInput<?>, Object> inputs, List<Observation> obs) {
		long start = System.currentTimeMillis();
		List<Observation> result = new LinkedList<Observation>();
		/* only process observations with equal FeatureOfInterest */

		for (ObservationMapping<ISamplingFeature> fMap : new NoSpatialGrouping(obs)) {
			if (!fMap.getObservations().isEmpty()) {
				TemporalGrouping temporalMethod = newTemporalGrouping(fMap.getObservations(), inputs);
				for (ObservationMapping<ObservationTime> tMap : temporalMethod) {
					if(!tMap.getObservations().isEmpty())
						result.add(aggregate(process, sourceUrl, m, obsProp, fMap.getKey(), tMap.getKey(), tMap.getObservations()));
				}
			}
		}
		log.info("Temporal aggregation took {}", Utils.timeElapsed(start));
		return result;
	}
	
	protected List<Observation> doSpatialAggregation(String process,
			String sourceUrl, AggregationMethod m, String obsProp,
			Map<IProcessInput<?>, Object> inputs, List<Observation> obs) {
		long start = System.currentTimeMillis();
		List<Observation> result = new LinkedList<Observation>();
		/* only process observations with equal ObservationTime */
		for (ObservationMapping<ObservationTime> tMap : new NoTemporalGrouping(obs)) {
			if (!tMap.getObservations().isEmpty()) {
				SpatialGrouping spatialMethod = newSpatialGrouping(tMap.getObservations(), inputs);
				for (ObservationMapping<ISamplingFeature> fMap : spatialMethod) {
					if (!fMap.getObservations().isEmpty())
						result.add(aggregate(process, sourceUrl, m, obsProp, fMap.getKey(), tMap.getKey(), fMap.getObservations()));
				}
			}
		}
		log.info("Spatial aggregation took {}", Utils.timeElapsed(start));
		return result;
	}
	
	protected SpatialGrouping newSpatialGrouping(List<Observation> obs,
			Map<IProcessInput<?>, Object> inputs) {
		try {
			SpatialGrouping g = sg.newInstance();
			g.setInputs(obs, inputs);
			return g;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected TemporalGrouping newTemporalGrouping(List<Observation> obs,
			Map<IProcessInput<?>, Object> inputs) {
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
				null, observedProperty, process, time, uom, sourceObservations, sourceUrl);
	}
}
