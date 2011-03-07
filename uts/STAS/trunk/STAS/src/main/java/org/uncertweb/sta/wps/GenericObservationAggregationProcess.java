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
package org.uncertweb.sta.wps;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.RandomStringGenerator;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.ExtendedSelfDescribingAlgorithm;
import org.uncertweb.sta.wps.api.ProcessOutput;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.GroupingMethod;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.NoSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.NoTemporalGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.ObservedPropertyGrouping;
import org.uncertweb.sta.wps.om.OriginAwareObservation;
import org.uncertweb.sta.wps.sos.SOSClient;
import org.uncertweb.sta.wps.xml.binding.GetObservationRequestBinding;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

/**
 * Algorithm that encapsulates input/ouput handling and execution of
 * {@link GroupingMethod}s and {@link AggregationMethod}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class GenericObservationAggregationProcess extends ExtendedSelfDescribingAlgorithm {

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory.getLogger(GenericObservationAggregationProcess.class);
	
	/**
	 * Number to generate (runtime-)unique observation id's.
	 */
	private static long observationIdCount = 0;
	
	/**
	 * The {@link AbstractProcessInput}s for all {@link GenericObservationAggregationProcess}.
	 */
	private static final Set<AbstractProcessInput<?>> commonInputs = getCommonInputs();
	
	/**
	 * The {@link AbstractProcessInput}s of the {@link TemporalGrouping} method.
	 */
	private Set<AbstractProcessInput<?>> tgInputs;
	
	/**
	 * The {@link AbstractProcessInput}s of the {@link SpatialGrouping} method.
	 */
	private Set<AbstractProcessInput<?>> sgInputs;
	
	/**
	 * The {@link SpatialGrouping} method of this algorithm.
	 */
	private Class<? extends SpatialGrouping> sg;
	
	/**
	 * The {@link TemporalGrouping} method of this algorithm.
	 */
	private Class<? extends TemporalGrouping> tg;
	
	/**
	 * The identifier of this algorithm.
	 */
	private String identifier;

	/**
	 * The title of this algorithm.
	 */
	private String title;

	/**
	 * Generates the common {@link AbstractProcessInput}s for all
	 * {@link GenericObservationAggregationProcess}.
	 * 
	 * @return the common inputs.
	 */
	private static HashSet<AbstractProcessInput<?>> getCommonInputs() {
		HashSet<AbstractProcessInput<?>> inputs = new HashSet<AbstractProcessInput<?>>();
		inputs.add(Constants.Process.Inputs.Common.OBSERVATION_COLLECTION_INPUT);
		inputs.add(Constants.Process.Inputs.Common.SPATIAL_AGGREGATION_METHOD);
		inputs.add(Constants.Process.Inputs.Common.GROUP_BY_OBSERVED_PROPERTY);
		inputs.add(Constants.Process.Inputs.Common.SOS_DESTINATION_URL);
		inputs.add(Constants.Process.Inputs.Common.TEMPORAL_AGGREGATION_METHOD);
		inputs.add(Constants.Process.Inputs.Common.TEMPORAL_BEFORE_SPATIAL_GROUPING);
		return inputs;
	}
	
	/**
	 * Creates a new {@code GenericObservationAggregationProcess}.
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getIdentifier() {
		return this.identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTitle() {
		return this.title;
	}

	/**
	 * {@inheritDoc}
	 */
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
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<AbstractProcessInput<?>> getInputs() {
		if (tgInputs == null) throw new NullPointerException();
		if (sgInputs == null) throw new NullPointerException();
		Set<AbstractProcessInput<?>> set = new HashSet<AbstractProcessInput<?>>();
		set.addAll(commonInputs);
		set.addAll(tgInputs);
		set.addAll(sgInputs);
		return set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<ProcessOutput> getOutputs() {
		return Utils.set(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS, 
						 Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_REFERENCE,
						 Constants.Process.Outputs.VISUALIZATION_LINK);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputs) {
		try {
			final String random = RandomStringGenerator.getInstance().generate(20);
			final String process = Constants.Sos.URN.AGGREGATED_PROCESS + random; 
			
			long start = System.currentTimeMillis();
	
			/* get the inputs */
			
			/* TODO do the following synchronously */
			ObservationCollection observations = Constants.Process.Inputs.Common.OBSERVATION_COLLECTION_INPUT.handle(inputs);
			boolean groupByObservedProperty = Constants.Process.Inputs.Common.GROUP_BY_OBSERVED_PROPERTY.handle(inputs);
			boolean temporalBeforeSpatial = Constants.Process.Inputs.Common.TEMPORAL_BEFORE_SPATIAL_GROUPING.handle(inputs);
			AggregationMethod temporalAggregationMethod = (AggregationMethod) Constants.Process.Inputs.Common.TEMPORAL_AGGREGATION_METHOD.handle(inputs).newInstance();
			AggregationMethod spatialAggregationMethod = (AggregationMethod) Constants.Process.Inputs.Common.SPATIAL_AGGREGATION_METHOD.handle(inputs).newInstance();
			
			/* get the grouping method inputs */
			HashMap<AbstractProcessInput<?>, Object>  sgInputMap = new HashMap<AbstractProcessInput<?>, Object>();
			for (AbstractProcessInput<?> i : sgInputs) { sgInputMap.put(i, i.handle(inputs)); }

			HashMap<AbstractProcessInput<?>, Object>  tgInputMap = new HashMap<AbstractProcessInput<?>, Object>();
			for (AbstractProcessInput<?> i : tgInputs) { tgInputMap.put(i, i.handle(inputs)); }
			
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
	
			String sosUrl = Constants.Process.Inputs.Common.SOS_URL.handle(inputs);
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
	
			String destinationUrl = Constants.Process.Inputs.Common.SOS_DESTINATION_URL.handle(inputs);
			if (destinationUrl != null) {
				GetObservationRequestBinding b;
				try {
					long insertStart = System.currentTimeMillis();
					Map<String, Object> meta = new HashMap<String, Object>();
					
					/* TODO add inputs of methods... like time range */
					meta.put(Constants.Sos.ProcessDescription.Parameter.GROUPED_BY_OBSERVED_PROPERTY, groupByObservedProperty);
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_BEFORE_SPATIAL_AGGREGATION, temporalBeforeSpatial);
					meta.put(Constants.Sos.ProcessDescription.Parameter.SPATIAL_GROUPING_METHOD, this.sg);
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_GROUPING_METHOD, this.tg);
					meta.put(Constants.Sos.ProcessDescription.Parameter.SPATIAL_AGGREGATION_METHOD, spatialAggregationMethod.getClass());
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_AGGREGATION_METHOD, temporalAggregationMethod.getClass());
					
					log.info("Inserting Observations into SOS: {}", destinationUrl);
					b = new SOSClient().registerAggregatedObservations(result, destinationUrl, process, meta);
					log.info("Registered {} Observations in {}.", result.size(), Utils.timeElapsed(insertStart));
				
					
					
					String visualizationLink = Utils.getClientUrlForRequest(random, destinationUrl, b.getPayload());
					log.info("Visualization URL: {}", visualizationLink);

					response.put(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_REFERENCE.getId(), b);
					response.put(Constants.Process.Outputs.VISUALIZATION_LINK.getId(), new LiteralStringBinding(visualizationLink));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
			}
	
			log.info("Served Request in {}.", Utils.timeElapsed(start));
	
			return response;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Aggregates a collection of {@code Observation}s with the
	 * {@code TemporalGrouping} method of this class. Observations will first be
	 * sorted by their FeatureOfInterest.
	 * 
	 * @param process
	 *            the process URN
	 * @param sourceUrl
	 *            the URL of source SOS
	 * @param m
	 *            the {@code AggregationMethod}
	 * @param obsProp
	 *            the ObservedProperty
	 * @param inputs
	 *            the {@code IProcessInput} for {@link #tg}
	 * @param obs
	 *            the {@code Observation}s to aggregate
	 * @return the aggregated {@code Observation}s
	 */
	protected List<Observation> doTemporalAggregation(String process,
			String sourceUrl, AggregationMethod m, String obsProp,
			Map<AbstractProcessInput<?>, Object> inputs, List<Observation> obs) {
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
	
	/**
	 * Aggregates a collection of {@code Observation}s with the
	 * {@code SpatialGrouping} method of this class. Observations will first be
	 * sorted by their SamplingTime.
	 * 
	 * @param process
	 *            the process URN
	 * @param sourceUrl
	 *            the URL of source SOS
	 * @param m
	 *            the {@code AggregationMethod}
	 * @param obsProp
	 *            the ObservedProperty
	 * @param inputs
	 *            the {@code IProcessInput} for {@link #sg}
	 * @param obs
	 *            the {@code Observation}s to aggregate
	 * @return the aggregated {@code Observation}s
	 */
	protected List<Observation> doSpatialAggregation(String process,
			String sourceUrl, AggregationMethod m, String obsProp,
			Map<AbstractProcessInput<?>, Object> inputs, List<Observation> obs) {
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
	
	/**
	 * Instantiates the {@code SpatialGrouping} method and set the given
	 * inputs.
	 * 
	 * @param obs
	 *            the {@code Observation} collection to set
	 * @param inputs
	 *            the inputs for the {@code SpatialGrouping}
	 * @return the instantiated {@code SpatialGrouping}
	 * @see SpatialGrouping#setInputs(List, Map)
	 */
	protected SpatialGrouping newSpatialGrouping(List<Observation> obs,
			Map<AbstractProcessInput<?>, Object> inputs) {
		try {
			SpatialGrouping g = sg.newInstance();
			g.setInputs(obs, inputs);
			return g;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Instantiates the {@code TemporalGrouping} method and set the given
	 * inputs.
	 * 
	 * @param obs
	 *            the {@code Observation} collection to set
	 * @param inputs
	 *            the inputs for the {@code TemporalGrouping}
	 * @return the instantiated {@code TemporalGrouping}
	 * @see TemporalGrouping#setInputs(List, Map)
	 */
	protected TemporalGrouping newTemporalGrouping(List<Observation> obs,
			Map<AbstractProcessInput<?>, Object> inputs) {
		try {
			TemporalGrouping g = tg.newInstance();
			g.setInputs(obs, inputs);
			return g;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Aggregates a collection of {@code Observation}s with the given paramters.
	 * 
	 * @param process
	 *            the process URN to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param sourceUrl
	 *            the URL of the SOS from which the original {@code Observation}
	 *            s were fetched
	 * @param method
	 *            the {@code AggregationMethod} which is used to aggregate
	 *            {@code obs}
	 * @param observedProperty
	 *            the ObservedProperty to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param f
	 *            the FeatureOfInterest to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param time
	 *            the SamplingTime to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param obs
	 *            the {@code Observation} to aggregate
	 * @return the aggregated {@code OriginAwareObservation}
	 */
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
						throw new RuntimeException(
								MessageFormat.format("Not matching UoM: {1} -  {2}. Unit conversation is not supported.",
												uom, o.getUom()));
					}
				}
			}
		}
		double aggregatedValue = method.aggregate(obs);
		return new OriginAwareObservation("ao_" + String.valueOf(observationIdCount++), aggregatedValue, f,
				null, observedProperty, process, time, uom, sourceObservations, sourceUrl);
	}
}
