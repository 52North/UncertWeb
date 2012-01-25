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
package org.uncertweb.sta.wps.algorithms;

import static org.uncertweb.utils.UwCollectionUtils.list;
import static org.uncertweb.utils.UwCollectionUtils.map;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.ObservationContextType;
import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GetObservationRequestBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.opengis.observation.ObservationCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.EncoderHook;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.RandomStringGenerator;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ObservationCollectionInputHandler;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.CompositeProcessInput;
import org.uncertweb.sta.wps.api.ExtendedSelfDescribingAlgorithm;
import org.uncertweb.sta.wps.api.ProcessOutput;
import org.uncertweb.sta.wps.api.SingleProcessInput;
import org.uncertweb.sta.wps.method.MethodFactory;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.GroupingMethod;
import org.uncertweb.sta.wps.method.grouping.ObservationMapping;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.NoSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.NoTemporalGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.ObservedPropertyGrouping;
import org.uncertweb.sta.wps.om.Origin;
import org.uncertweb.sta.wps.sos.SOSClient;
import org.uncertweb.utils.UwConstants;

/**
 * Algorithm that encapsulates input/ouput handling and execution of
 * {@link GroupingMethod}s and {@link AggregationMethod}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class GenericObservationAggregationProcess extends
		ExtendedSelfDescribingAlgorithm {
	
	static {
		XBObservationEncoder.registerHook(new EncoderHook() {
			
			@Override
			public void encode(AbstractObservation ao, OMObservationDocument xml) {
				Origin o = (Origin) ao.getParameter(Constants.OBSERVATION_PARAMETER_AGGREGATED_OF);
				if (o != null) {
					String url = Utils.getObservationByIdUrl(o.getSourceUrl(), o.getSourceObservations());
					ObservationContextType oct = xml.getOMObservation().addNewRelatedObservation().addNewObservationContext();
					oct.addNewRelatedObservation().setHref(url);
					oct.addNewRole().setType(Constants.OBSERVATION_PARAMETER_AGGREGATED_OF);
				}
			}
			
		});
	}

	/**
	 * The URL of the SOS from which the {@link ObservationCollection} will be
	 * fetched. Can also be a GET request.
	 */
	public static final SingleProcessInput<String> SOS_URL = new SingleProcessInput<String>(
			Constants.Process.Inputs.SOS_SOURCE_URL_ID,
			LiteralStringBinding.class, 0, 1, null, null);

	/**
	 * The URL of the SOS in which the aggregated observations will be inserted.
	 */
	public static final SingleProcessInput<String> SOS_DESTINATION_URL = new SingleProcessInput<String>(
			Constants.Process.Inputs.SOS_DESTINATION_URL_ID,
			LiteralStringBinding.class, 0, 1, null, null);

	/**
	 * Indicates if the temporal aggregation should run before the spatial
	 * aggregation.
	 */
	public static final SingleProcessInput<Boolean> SPATIAL_BEFORE_TEMPORAL = new SingleProcessInput<Boolean>(
			Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL,
			LiteralBooleanBinding.class, 0, 1, null,
			Constants.getDefaultFlag(Constants.Process.Inputs.SPATIAL_BEFORE_TEMPORAL, true));

	/**
	 * Indicates if the observations should be grouped by ObservedProperty.
	 */
	public static final SingleProcessInput<Boolean> GROUP_BY_OBSERVED_PROPERTY = new SingleProcessInput<Boolean>(
			Constants.Process.Inputs.GROUP_BY_OBSERVED_PROPERTY_ID,
			LiteralBooleanBinding.class, 0, 1, null,
			Constants.getDefaultFlag(Constants.Process.Inputs.GROUP_BY_OBSERVED_PROPERTY_ID, true));

	/**
	 * The {@code GetObservation} request which will be postet to the SOS
	 * 
	 * @see #SOS_URL
	 */
	public static final SingleProcessInput<GetObservationDocument> SOS_REQUEST = new SingleProcessInput<GetObservationDocument>(
			Constants.Process.Inputs.SOS_REQUEST_ID,
			GetObservationRequestBinding.class, 0, 1, null, null);
	
	
	public static final SingleProcessInput<String> SOS_REQUEST_RETURN_MIME_TYPE = new SingleProcessInput<String>(
			Constants.Process.Inputs.SOS_REQUEST_RETURN_MIME_TYPE,
			LiteralStringBinding.class, 0, 1, null, null);
	
	public static final SingleProcessInput<String> SOS_REQUEST_RETURN_SCHEMA = new SingleProcessInput<String>(
			Constants.Process.Inputs.SOS_REQUEST_RETURN_SCHEMA,
			LiteralStringBinding.class, 0, 1, null, null);

	/**
	 * Composite input which combines {@link #SOS_URL} and {@link #SOS_REQUEST}.
	 * 
	 * @see ObservationCollectionInputHandler
	 */
	public static final AbstractProcessInput<IObservationCollection> OBSERVATION_COLLECTION_INPUT = 
			new CompositeProcessInput<IObservationCollection>(
			Constants.Process.Inputs.OBSERVATION_COLLECTION_INPUT_ID,
			new ObservationCollectionInputHandler(SOS_URL, SOS_REQUEST, SOS_REQUEST_RETURN_MIME_TYPE, SOS_REQUEST_RETURN_SCHEMA));

	/** Process output that contains the aggregated observations. */
	public static final ProcessOutput AGGREGATED_OBSERVATIONS = new ProcessOutput(
			Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_ID,
			OMBinding.class);

	/**
	 * Process output that contains a {@code GetObservation} request to fetch
	 * the aggregated observations from a SOS.
	 * 
	 * @see Constants.Process.Inputs.Common#SOS_DESTINATION_URL
	 */
	public static final ProcessOutput AGGREGATED_OBSERVATIONS_REFERENCE = new ProcessOutput(
			Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_REFERENCE_ID,
			GetObservationRequestBinding.class);

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(GenericObservationAggregationProcess.class);

	/**
	 * Number to generate (runtime-)unique observation id's.
	 */
	private static long observationIdCount = 0;

	/**
	 * The {@link AbstractProcessInput}s for all
	 * {@link GenericObservationAggregationProcess}.
	 */
	private static final Set<AbstractProcessInput<?>> COMMON_INPUTS = getCommonInputs();

	/**
	 * {@code ExecutorService} to process inputs.
	 */
	private static final ExecutorService INPUT_FETCHER = Executors.newFixedThreadPool(Constants.THREADS_TO_FETCH_INPUTS);

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
	 * The {@link AggregationMethod} to for spatial aggregation.
	 */
	private Class<? extends AggregationMethod> sam;

	/**
	 * The {@link AggregationMethod} to for temporal aggregation.
	 */
	private Class<? extends AggregationMethod> tam;
	
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
	private static Set<AbstractProcessInput<?>> getCommonInputs() {
		Set<AbstractProcessInput<?>> inputs = set();
		inputs.add(OBSERVATION_COLLECTION_INPUT);
		inputs.add(GROUP_BY_OBSERVED_PROPERTY);
		inputs.add(SOS_DESTINATION_URL);
		inputs.add(SPATIAL_BEFORE_TEMPORAL);
		return inputs;
	}

	/**
	 * Creates a new {@code GenericObservationAggregationProcess}.
	 * 
	 * @param identifier identifier of the process
	 * @param title title of the process
	 * @param sg spatial grouping method
	 * @param tg temporal grouping method
	 */
	public GenericObservationAggregationProcess(String identifier,
			String title, 
			Class<? extends SpatialGrouping> sg,
			Class<? extends TemporalGrouping> tg,
			Class<? extends AggregationMethod> sam,
			Class<? extends AggregationMethod> tam) {
		this.identifier = identifier;
		this.title = title;
		this.sg = sg;
		this.tg = tg;
		this.sam = sam;
		this.tam = tam;
		this.sgInputs = newSpatialGrouping(null, null)
				.getAdditionalInputDeclarations();
		this.tgInputs = newTemporalGrouping(null, null)
				.getAdditionalInputDeclarations();
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
		MethodFactory mf = MethodFactory.getInstance();
		return new StringBuffer()
				//@formatter off
				.append(Constants.Process.DESCRIPTION).append("\n")
				.append("SpatialPartitioningPredicate: ").append(mf.getMethodDescription(sg)).append(" \n")
				.append("SpatialAggregationFunction: ").append(mf.getMethodDescription(sam)).append(" \n")
				.append("TemporalPartitioningPredicate: ").append(mf.getMethodDescription(tg)).append(" \n")
				.append("TemporalAggregationFunction: ").append(mf.getMethodDescription(tam)).append(" \n")
				.toString();
				//@formatter on
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<AbstractProcessInput<?>> getInputs() {
		if (tgInputs == null)
			throw new NullPointerException();
		if (sgInputs == null)
			throw new NullPointerException();
		Set<AbstractProcessInput<?>> set = set();
		set.addAll(COMMON_INPUTS);
		set.addAll(tgInputs);
		set.addAll(sgInputs);
		return set;
	}

	@Override
	protected Set<ProcessOutput> getOutputs() {
		return set(AGGREGATED_OBSERVATIONS, 
						 AGGREGATED_OBSERVATIONS_REFERENCE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, IData> run(final Map<String, List<IData>> inputs) {
		try {
			return run1(inputs);
		} catch (Error e) {
			log.warn("Error while executing process", e);
			throw e;
		} catch (RuntimeException e) {
			log.warn("Error while executing process", e);
			throw e;
		}
	}


	private Map<String, IData> run1(final Map<String, List<IData>> inputs) {
		final long start = System.currentTimeMillis();
		final String random = RandomStringGenerator.getInstance().generate(20);
		final URI process = URI.create(Constants.Sos.AGGREGATED_PROCESS	+ random);

		//@formatter off
		try {
			/* Helper class to fetch the inputs asynchronously. */
			class Mapper implements Callable<Mapper> {
				AbstractProcessInput<?> i; Object o; 
				Mapper(AbstractProcessInput<?> t) { this.i = t; }
				@Override public Mapper call() {
					this.o = i.handle(inputs);
					return this; 
				}
				boolean is(AbstractProcessInput<?> t) {
					return this.i.equals(t); }
			}
			
			/* creating tasks */
			Set<Mapper> tasks = set();
			for (AbstractProcessInput<?> i : COMMON_INPUTS) { tasks.add(new Mapper(i)); }
			for (AbstractProcessInput<?> i : sgInputs) { tasks.add(new Mapper(i)); }
			for (AbstractProcessInput<?> i : tgInputs) { tasks.add(new Mapper(i)); }
			
			/* execute tasks */
			List<Future<Mapper>> futures =  INPUT_FETCHER.invokeAll(tasks);
			if (futures.size() != tasks.size()) {
				throw new RuntimeException("Not all input processing tasks could be completed.");
			}
			
			/* input declarations */
			Map<AbstractProcessInput<?>, Object>  sgInputMap = map();
			Map<AbstractProcessInput<?>, Object>  tgInputMap = map();
			IObservationCollection observations = null;
			Boolean groupByObservedProperty = null;
			Boolean spatialBeforeTemporal = null;
			
			/* extracting of inputs from the tasks */
			for (Future<Mapper> future : futures) {
				Mapper m = future.get();
				if (m.is(OBSERVATION_COLLECTION_INPUT)) {
					observations = (IObservationCollection)m.o;
				} else if (m.is(GROUP_BY_OBSERVED_PROPERTY)) {
					groupByObservedProperty = (Boolean) m.o;
				} else if (m.is(SPATIAL_BEFORE_TEMPORAL)) {
					spatialBeforeTemporal = (Boolean) m.o;
				}
				if (tgInputs.contains(m.i)) { tgInputMap.put(m.i, m.o); }
				if (sgInputs.contains(m.i)) { sgInputMap.put(m.i, m.o); }
			}

			/* give some status */
			log.info("Using Process URN: {}", process);
			log.info("Using Spatial Grouping Method: {}", sg.getName());
			log.info("Using Spatial Aggregation Method: {}", sam.getName());
			log.info("Using Temporal Grouping Method: {}", tg.getName());
			log.info("Using Temporal Aggregation Method: {}", tam.getName());
			log.info("Input: {} Observations.", observations.getObservations().size());
			
			/* sort by observed property or set it to null */
			Map<URI, List<? extends AbstractObservation>> obs = map();
			if (groupByObservedProperty) {
				ObservedPropertyGrouping obsPropMethod = new ObservedPropertyGrouping(observations.getObservations());
				for (ObservationMapping<URI> obsPropMap : obsPropMethod) {
					obs.put(obsPropMap.getKey(), obsPropMap.getObservations());
				}
				log.info("Grouping by ObservedProperty. Got {} distinct.", obs.size());
			} else {
				obs.put(UwConstants.URN.NULL.uri, observations.getObservations());
			}
			
			List<AbstractObservation> result = list();
	
			String sosUrl = SOS_URL.handle(inputs);
			int i = sosUrl.lastIndexOf('?');
			if (i > 0) {
				sosUrl = sosUrl.substring(0, i);
			}
			
			for (Entry<URI, List<? extends AbstractObservation>> e : obs.entrySet()) {
				if (spatialBeforeTemporal) {
					List<AbstractObservation> firstResult = doSpatialAggregation(process, sosUrl, e.getKey(), sgInputMap, e.getValue());
					result.addAll(doTemporalAggregation(process, sosUrl, e.getKey(), tgInputMap, firstResult));
				} else {
					List<AbstractObservation> firstResult = doTemporalAggregation(process, sosUrl, e.getKey(), tgInputMap, e.getValue());
					result.addAll(doSpatialAggregation(process, sosUrl, e.getKey(), sgInputMap, firstResult));
				}
				
			}
			
			log.info("Output: Generated {} Observations in {}.", result.size(), Utils.timeElapsed(start));
			
			/* Outputs */
			Map<String, IData> response = map();
			response.put(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_ID,
					new OMBinding(asIObservationCollection(result)));
	
			String destinationUrl = SOS_DESTINATION_URL.handle(inputs);
			if (destinationUrl != null) {
				GetObservationRequestBinding b;
				try {
					long insertStart = System.currentTimeMillis();
					Map<String, Object> meta = map();
					
					/* TODO add inputs of methods... like time range */
					meta.put(Constants.Sos.ProcessDescription.Parameter.GROUPED_BY_OBSERVED_PROPERTY, groupByObservedProperty);
					meta.put(Constants.Sos.ProcessDescription.Parameter.SPATIAL_BEFORE_TEMPORAL_AGGREGATION, spatialBeforeTemporal);
					meta.put(Constants.Sos.ProcessDescription.Parameter.SPATIAL_GROUPING_METHOD, this.sg);
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_GROUPING_METHOD, this.tg);
					meta.put(Constants.Sos.ProcessDescription.Parameter.SPATIAL_AGGREGATION_METHOD, this.sam);
					meta.put(Constants.Sos.ProcessDescription.Parameter.TEMPORAL_AGGREGATION_METHOD, this.tam);
					
					log.info("Inserting Observations into SOS: {}", destinationUrl);
					b = new SOSClient().registerAggregatedObservations(result, destinationUrl, process, meta);
					log.info("Registered {} Observations in {}.", result.size(), Utils.timeElapsed(insertStart));
				
					response.put(Constants.Process.Outputs.AGGREGATED_OBSERVATIONS_REFERENCE_ID, b);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
			}
	
			log.info("Served Request in {}.", Utils.timeElapsed(start));
	
			return response;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		//@formatter on
	}

	public static IObservationCollection asIObservationCollection(Collection<? extends AbstractObservation> obs) {
		IObservationCollection col = null;
		for (AbstractObservation ao : obs) {
			if (col == null) {
				if (ao instanceof BooleanObservation) {
					col = new BooleanObservationCollection();
				} else if (ao instanceof CategoryObservation) {
					col = new CategoryObservationCollection();
				} else if (ao instanceof DiscreteNumericObservation) {
					col = new DiscreteNumericObservationCollection();
				} else if (ao instanceof Measurement) {
					col = new MeasurementCollection();
				} else if (ao instanceof ReferenceObservation) {
					col = new ReferenceObservationCollection();
				} else if (ao instanceof TextObservation) {
					col = new TextObservationCollection();
				} else if (ao instanceof UncertaintyObservation) {
					col = new UncertaintyObservationCollection();
				}
			}
			col.addObservation(ao);
		}
		return col;
	}
	
	/**
	 * Aggregates a collection of {@code Observation}s with the
	 * {@code TemporalGrouping} method of this class. Observations will first be
	 * sorted by their FeatureOfInterest.
	 * 
	 * @param process the process URN
	 * @param sourceUrl the URL of source SOS
	 * @param m the {@code AggregationMethod}
	 * @param obsProp the ObservedProperty
	 * @param inputs the {@code IProcessInput} for {@link #tg}
	 * @param obs the {@code Observation}s to aggregate
	 * @return the aggregated {@code Observation}s
	 */
	protected List<AbstractObservation> doTemporalAggregation(URI process,
			String sourceUrl, URI obsProp,
			Map<AbstractProcessInput<?>, Object> inputs, List<? extends AbstractObservation> obs) {
		long start = System.currentTimeMillis();
		AggregationMethod m;
		try {
			m = this.tam.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		List<AbstractObservation> result = list();
		/* only process observations with equal FeatureOfInterest */

		for (ObservationMapping<SpatialSamplingFeature> fMap : new NoSpatialGrouping(obs)) {
			if (!fMap.getObservations().isEmpty()) {
				TemporalGrouping temporalMethod = newTemporalGrouping(fMap.getObservations(), inputs);
				for (ObservationMapping<TimeObject> tMap : temporalMethod) {
					if (!tMap.getObservations().isEmpty())
						result.add(aggregate(process, sourceUrl, m, obsProp, fMap
								.getKey(), tMap.getKey(), tMap
								.getObservations()));
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
	 * @param process the process URN
	 * @param sourceUrl the URL of source SOS
	 * @param m the {@code AggregationMethod}
	 * @param obsProp the ObservedProperty
	 * @param inputs the {@code IProcessInput} for {@link #sg}
	 * @param obs the {@code Observation}s to aggregate
	 * @return the aggregated {@code Observation}s
	 */
	protected List<AbstractObservation> doSpatialAggregation(URI process,
			String sourceUrl, URI obsProp,
			Map<AbstractProcessInput<?>, Object> inputs, List<? extends AbstractObservation> obs) {
		long start = System.currentTimeMillis();
		AggregationMethod m;
		try {
			m = this.sam.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		List<AbstractObservation> result = list();
		/* only process observations with equal ObservationTime */
		for (ObservationMapping<TimeObject> tMap : new NoTemporalGrouping(obs)) {
			if (!tMap.getObservations().isEmpty()) {
				SpatialGrouping spatialMethod = newSpatialGrouping(tMap.getObservations(), inputs);
				for (ObservationMapping<SpatialSamplingFeature> fMap : spatialMethod) {
					if (!fMap.getObservations().isEmpty())
						result.add(aggregate(process, sourceUrl, m, obsProp, fMap
								.getKey(), tMap.getKey(), fMap
								.getObservations()));
				}
			}
		}
		log.info("Spatial aggregation took {}", Utils.timeElapsed(start));
		return result;
	}

	/**
	 * Instantiates the {@code SpatialGrouping} method and set the given inputs.
	 * 
	 * @param obs the {@code Observation} collection to set
	 * @param inputs the inputs for the {@code SpatialGrouping}
	 * @return the instantiated {@code SpatialGrouping}
	 * @see SpatialGrouping#setInputs(List, Map)
	 */
	protected SpatialGrouping newSpatialGrouping(List<? extends AbstractObservation> obs,
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
	 * @param obs the {@code Observation} collection to set
	 * @param inputs the inputs for the {@code TemporalGrouping}
	 * @return the instantiated {@code TemporalGrouping}
	 * @see TemporalGrouping#setInputs(List, Map)
	 */
	protected TemporalGrouping newTemporalGrouping(List<? extends AbstractObservation> obs,
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
	 * @param process the process URN to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param sourceUrl the URL of the SOS from which the original
	 *            {@code Observation} s were fetched
	 * @param method the {@code AggregationMethod} which is used to aggregate
	 *            {@code obs}
	 * @param observedProperty the ObservedProperty to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param f the FeatureOfInterest to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param time the SamplingTime to set on the aggregated
	 *            {@code OriginAwareObservation}
	 * @param obs the {@code Observation} to aggregate
	 * @return the aggregated {@code OriginAwareObservation}
	 */
	protected AbstractObservation aggregate(URI process,
			String sourceUrl, AggregationMethod method,
			URI observedProperty, SpatialSamplingFeature f, TimeObject time,
			List<? extends AbstractObservation> obs) {
		Set<AbstractObservation> sourceObservations = set();
		for (AbstractObservation o : obs) {
			Origin origin = (Origin) o.getParameter(Constants.OBSERVATION_PARAMETER_AGGREGATED_OF);
			if (origin != null) {
				sourceObservations.addAll(origin.getSourceObservations());
			} else {
				sourceObservations.add(o);
			}
		}
		Identifier id = new Identifier(UwConstants.URL.INAPPLICABLE.uri, "ao_"
				+ String.valueOf(observationIdCount++));
		MeasureResult result = (MeasureResult) method.aggregate(obs);
		Measurement m = new Measurement(id, null, time, time, null, process,
				observedProperty, f, null, result);
		m.addParameter(Constants.OBSERVATION_PARAMETER_AGGREGATED_OF, 
				new Origin(sourceUrl, sourceObservations));
		return m;
	}
}
