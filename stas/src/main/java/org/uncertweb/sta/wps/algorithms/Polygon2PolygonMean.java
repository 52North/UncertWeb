package org.uncertweb.sta.wps.algorithms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.sos.x10.GetFeatureOfInterestDocument.GetFeatureOfInterest;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.opengis.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Variance;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.ObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.FeatureCache;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.AggregationInputs;
import org.uncertweb.sta.wps.STASException;
import org.uncertweb.sta.wps.UncertainAggregationInputs;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.ProcessOutput;
import org.uncertweb.sta.wps.api.SingleProcessInput;
import org.uncertweb.sta.wps.method.aggregation.AggregationUncertMLUtils;

import com.vividsolutions.jts.geom.Geometry;


/**
 * algorithm that implements a weighted sum of a polygon to polygon aggregation. The intersection area of polygons serves
 * as weight.
 * 
 * @author staschc
 *
 */
public class Polygon2PolygonMean extends AbstractUncertainAggregationProcess{
	
	
	
	
	///////////////////////
	// supported uncertainty types
	private static final String CREAL_URI = UncertML.getURI(ContinuousRealisation.class);
	private static final String MEAN_URI = UncertML.getURI(Mean.class);
	private static final String VARIANCE_URI = UncertML.getURI(Variance.class);
	private static final String RANDOMSAMPLE_URI = UncertML
			.getURI(RandomSample.class);
	
	//indicates the number of digits for the aggregates
	private static final int NUMBER_OF_DIGITS=4;
	
	
	//default SRS EPSG Code
	private static final int DEFAULT_SRS = 27700;
	
	//used for retrieving FOIS in observations from WFS
	private FeatureCache featureCache = new FeatureCache();
	
	private String wfsURL = "";
	private String typeName = "";
	
	
	
	/**
	 * Input parameter which contains the input regions
	 */
	public static final SingleProcessInput<GTVectorDataBinding> INPUT_REGIONS = new SingleProcessInput<GTVectorDataBinding>(
			"InputRegions",
			GTVectorDataBinding.class, 0, 1, null, null);
	
	/**
	 * Input parameter which contains the scale factor for the weights calculated from areas
	 */
	public static final SingleProcessInput<LiteralStringBinding> PERM_INPUT_REFERENCE = new SingleProcessInput<LiteralStringBinding>(
			"PermanentInputReference",
			LiteralStringBinding.class, 0, 1, null, null);
	
	/**
	 * The URL of the SOS in which the aggregated observations will be inserted.
	 */
	public static final SingleProcessInput<String> INPUT_DATA = new SingleProcessInput<String>(
			Constants.Process.Inputs.INPUT_DATA, "@variable-uncertainty-types "+CREAL_URI+","+MEAN_URI+","+VARIANCE_URI + " \n @om-types OM_UncertaintyObservation",
			OMBinding.class, 1, 1, null, null);
	
	/**
	 * Process output that contains a {@code GetObservation} request to fetch
	 * the aggregated observations from a SOS.
	 * 
	 * @see Constants.Process.Inputs.Common#SOS_DESTINATION_URL
	 */
	public static final ProcessOutput AGGREGATED_OUTPUT = new ProcessOutput(
			Constants.Process.Outputs.AGGREGATED_DATA,
			OMBinding.class);
	
	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(Polygon2PolygonMean.class);
	
	/**
	 * identifier of aggregation process
	 */
	public static final String IDENTIFIER = "urn:ogc:def:aggregationProcess:sIntersection:sMean:noTG:noTA";


	/**
	 * constructor
	 */
	public Polygon2PolygonMean(){
		super();
		Set<String> allowedUncertaintyTypes = new HashSet<String>();
		allowedUncertaintyTypes.add(CREAL_URI);
		allowedUncertaintyTypes.add(MEAN_URI);
		allowedUncertaintyTypes.add(VARIANCE_URI);
		super.OUTPUT_UNCERTAINTY_TYPE.setAllowedValues(allowedUncertaintyTypes);
		log.debug("Aggregation process " + IDENTIFIER+" has been initialized");
	}
	
	

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
	
	@Override
	public String getTitle() {
		return "Weighted Sum Aggregation from polygons to polygons.";
	}

	@Override
	protected Set<AbstractProcessInput<?>> getInputs() {
		Set<AbstractProcessInput<?>> inputs = super.getCommonProcessInputs();
		inputs.add(INPUT_REGIONS);
		inputs.add(INPUT_DATA);
		inputs.add(PERM_INPUT_REFERENCE);
		return inputs;
	}

	@Override
	protected Set<ProcessOutput> getOutputs() {
		Set<ProcessOutput> result = new HashSet<ProcessOutput>();
		result.add(AGGREGATED_OUTPUT);
		return result;
	}
	
	/**
	 * 
	 * 
	 */
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		Map<String, IData> result = new HashMap<String,IData>();
		
		//get common Inputs
		AggregationInputs commonInputs = super.getAggregationInputs4Inputs(inputData);
		UncertainAggregationInputs uncertInputs = super.getUncertainAggregationInputs4Inputs(inputData);
		
		if (uncertInputs.getNumberOfRealisations()!=1){
			return runMonteCarlo(inputData);
		}
		else {
			//TODO implement for non-monte carlo case!!
			return runMonteCarlo(inputData);
		}
	}
	
	@Override
	public Map<String, IData> runMonteCarlo(Map<String, List<IData>> inputData) {
		
		Map<String, IData> result = new HashMap<String,IData>();
		
		//get common Inputs
		AggregationInputs commonInputs = super.getAggregationInputs4Inputs(inputData);
		UncertainAggregationInputs uncertInputs = super.getUncertainAggregationInputs4Inputs(inputData);
		
		IObservationCollection originalObs = null;
		FeatureCollection targetRegions = null;
		
		//get scale factor for area weights
		double scaleFactor = 1.0;
		
		
		//get observation input
		List<IData> inputDataInput = inputData.get(INPUT_DATA.getId());
		if (inputDataInput!=null&&inputDataInput.size()==1){
			originalObs = ((OMBinding)inputDataInput.get(0)).getPayload();
		}
		
		List<IData> inputRegionsInput = inputData.get(INPUT_REGIONS.getId());
		if (inputRegionsInput!=null&&inputRegionsInput.size()==1){
			targetRegions = ((GTVectorDataBinding)inputRegionsInput.get(0)).getPayload();
		}
		
		/*
		 * observations are first sorted by time and then for each time stamp, the aggregation is done
		 */
		Map<TimeObject,IObservationCollection> obs4time = sortObsByTime(originalObs);
		Iterator<TimeObject> timeIter = obs4time.keySet().iterator();
		UncertaintyObservationCollection resultObsCol = new UncertaintyObservationCollection();
		while (timeIter.hasNext()){
			TimeObject time = timeIter.next();
			try {
				resultObsCol.addObservationCollection(runAggregation4TimeObject(time,originalObs,targetRegions,uncertInputs.getNumberOfRealisations(),uncertInputs.getOutputUncertaintyTypes(),scaleFactor));
			} catch (STASException e) {
				log.info("Error while execution of aggregation process "+IDENTIFIER+" :"+e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
		}
		if (resultObsCol.getObservations().size()==0){
			throw new RuntimeException("No source observations intersect with the aggregation regions!");
		}
		result.put(AGGREGATED_OUTPUT.getId(), new OMBinding(resultObsCol));
		return result;
	}


	/**
	 * helper method that actually runs the agggregation
	 * 
	 * @param originalObs
	 * @param targetRegions
	 * @return
	 * @throws STASException 
	 */
	private IObservationCollection runAggregation4TimeObject(TimeObject time, 
			IObservationCollection originalObs, FeatureCollection targetRegions, int numberOfRealisations,List<String> uncertaintyTypes, double scaleFactor) throws STASException {
		//Map<SpatialSamplingFeature, IObservationCollection> obsCols4Fois = sortObsByFoi(originalObs);
		
		
		
		//Iterator<SpatialSamplingFeature> foiIter = obsCols4Fois.keySet().iterator();
		Map<Feature,Map<URI,RegionAggregates>> regionsAggCache = new HashMap<Feature,Map<URI,RegionAggregates>>(1000);
		
		/*
		 * iterate over fois and for each target region, check intersection; if FOI intersects, store obsCol with intersection are 
		 */
		Iterator<? extends AbstractObservation> obsIter = originalObs.getObservations().iterator();
		while (obsIter.hasNext()){
			//TODO add type check!!
			UncertaintyObservation uncertObs = (UncertaintyObservation) obsIter.next();
			URI observedProperty = uncertObs.getObservedProperty();
			Geometry foiGeom = null;
			String foiID = null;
			if (uncertObs.getFeatureOfInterest().getHref()!=null){
				String href = uncertObs.getFeatureOfInterest().getHref().toString();
				try {
					foiGeom = this.featureCache.getFeatureFromWfs(href).getShape();
				} catch (MalformedURLException e) {
					log.info("Error while getting URL from href attribute in feature of interest: "+e.getMessage());
					throw new RuntimeException("Error while getting URL from href attribute in feature of interest: "+e.getMessage());
				}
			}
			else {
				foiGeom = uncertObs.getFeatureOfInterest().getShape();
			}
			//iterate over target regions
			FeatureIterator features = targetRegions.features();
			
			while (features.hasNext()){
				Feature targetRegion = features.next();
				
				
				Geometry regionGeom = (Geometry)targetRegion.getDefaultGeometryProperty().getValue();
				if (regionGeom.intersects(foiGeom)){
					
					//TODO add type check
					IUncertainty uncertResult = uncertObs.getResult().getUncertaintyValue();
					ContinuousRealisation realisations = Utils.getRealisation4uncertResult(uncertResult);
					if (realisations.getValues().size()>0){
						if (regionsAggCache.containsKey(targetRegion)){
							Map<URI, RegionAggregates> targetRegionMap = regionsAggCache.get(targetRegion);
							if (targetRegionMap.containsKey(observedProperty)){
								targetRegionMap.get(observedProperty).addRealisation(realisations);
							}
							else {
								List<ContinuousRealisation> realList = new ArrayList<ContinuousRealisation>();
								realList.add(realisations);
								targetRegionMap.put(observedProperty, new RegionAggregates(targetRegion,realList,observedProperty));
							}
						}
						else {
							List<ContinuousRealisation> realList = new ArrayList<ContinuousRealisation>();
							realList.add(realisations);
							Map<URI,Polygon2PolygonMean.RegionAggregates> aggs4ObsProps = new HashMap<URI, Polygon2PolygonMean.RegionAggregates>(10000);
							aggs4ObsProps.put(observedProperty,new RegionAggregates(targetRegion,realList,observedProperty));
							regionsAggCache.put(targetRegion, aggs4ObsProps);
						}
					}
				}
				
				
			}
			
		}
		
		
		//iterate over regions aggregate cache and run aggregations
		IObservationCollection result = new UncertaintyObservationCollection();
		Iterator<Map<URI, RegionAggregates>> regionsAggIter = regionsAggCache.values().iterator();
		while (regionsAggIter.hasNext()){
			Map<URI, RegionAggregates> regionAgg = regionsAggIter.next();
			Iterator<RegionAggregates> obsPropsAggIter = regionAgg.values().iterator();
			while (obsPropsAggIter.hasNext()){
				RegionAggregates agg = obsPropsAggIter.next();
				URI observedProperty = agg.getObservedProperty();
				ContinuousRealisation aggResult = agg.aggregate(numberOfRealisations);
				SpatialSamplingFeature foi = null;
				try {
					int srs=((Geometry)agg.getRegion().getDefaultGeometryProperty().getValue()).getSRID();
					if (srs==0){
						srs=DEFAULT_SRS;
					}
					foi = Utils.createSF4GTFeature(agg.getRegion(),srs);
				} catch (URISyntaxException e) {
					throw new STASException(e.getLocalizedMessage());
				}
				try {
					
					if (uncertaintyTypes==null||uncertaintyTypes.contains(CREAL_URI)){
						UncertaintyResult obsResult = new UncertaintyResult(aggResult);
						UncertaintyObservation obs = new UncertaintyObservation(time,time,new URI(IDENTIFIER),observedProperty,foi,obsResult);
						result.addObservation(obs);
					}
					if (uncertaintyTypes != null && uncertaintyTypes.contains(RANDOMSAMPLE_URI)){
						UncertaintyResult obsResult = new UncertaintyResult(Utils.getRandomSample4Real(aggResult));
						UncertaintyObservation obs = new UncertaintyObservation(
								time, time, new URI(IDENTIFIER),
								observedProperty, foi, obsResult);
						result.addObservation(obs);
					}
					if (uncertaintyTypes!=null&&uncertaintyTypes.contains(MEAN_URI)){
						UncertaintyResult obsResult = new UncertaintyResult(AggregationUncertMLUtils.computeMean(aggResult));
						UncertaintyObservation obs = new UncertaintyObservation(time,time,new URI(IDENTIFIER),observedProperty,foi,obsResult);
						result.addObservation(obs);
					}
					if (uncertaintyTypes!=null&&uncertaintyTypes.contains(VARIANCE_URI)){
						UncertaintyResult obsResult = new UncertaintyResult(AggregationUncertMLUtils.computeVariance(aggResult));
						UncertaintyObservation obs = new UncertaintyObservation(time,time,new URI(IDENTIFIER),observedProperty,foi,obsResult);
						result.addObservation(obs);
					}
				} catch (URISyntaxException e) {
					throw new STASException(e.getLocalizedMessage());
				}
			}
		}
		return result;
	}
	
	
	
	/**
	 * sorts the input observations by time and provides a map containing the times as keys and the corresponding observation collections as values
	 * 
	 * @param obsCol
	 * 			observation collection that should be sorted by time
	 * @return
	 * 			map containing the times as keys and the corresponding observation collections as values
	 */
	private Map<TimeObject,IObservationCollection> sortObsByTime(IObservationCollection obsCol){
		
		Map<TimeObject,IObservationCollection> result = new HashMap<TimeObject,IObservationCollection>();
		Iterator<? extends AbstractObservation> obsIter = obsCol.getObservations().iterator();
		while (obsIter.hasNext()){
			AbstractObservation obs = obsIter.next();
			TimeObject to = obs.getPhenomenonTime();
			if (result.containsKey(to)){
				result.get(to).addObservation(obs);
			}
			else {
				IObservationCollection obsColNew = new ObservationCollection();
				obsColNew.addObservation(obs);
				result.put(to, obsColNew);
			}
		}
		return result;
	}
	
	
	private class RegionAggregates{
		private URI observedProperty;
		private Feature region;
		private List<ContinuousRealisation> originalObsResults;
		
		
		public RegionAggregates(Feature region,
				List<ContinuousRealisation> originalObsCols, URI observedProperty) {
			this.region = region;
			this.originalObsResults = originalObsCols;
			this.observedProperty=observedProperty;
		}
		
		public Feature getRegion() {
			return region;
		}
		
		public void addRealisation(ContinuousRealisation realisation){
			this.originalObsResults.add(realisation);
		}
		
		public ContinuousRealisation aggregate(int numberOfRealisations){
			List<Double> aggregatedValues = new ArrayList<Double>(numberOfRealisations);
			//iterate over each realisation and take i-th element to aggregate on each set of i-th realisations within a region
			for (int i=0;i<numberOfRealisations;i++){
				double sum = 0;
				for (ContinuousRealisation cr:originalObsResults){
					if (i<cr.getValues().size()){
						sum+=cr.getValues().get(i);
					} else {
						throw new RuntimeException(
							"Number of realisations in input data to aggregation has to contain as least as many realisations as are requested for the output. Either reduce the value of the numberOfRealisations parameter or make sure that input data contains enough realisations.");
					}
				}
				sum= sum/originalObsResults.size();
				//rounding to 
				BigDecimal bd = new BigDecimal(sum).setScale(NUMBER_OF_DIGITS, RoundingMode.HALF_EVEN);
				aggregatedValues.add(bd.doubleValue());
			}
			return new ContinuousRealisation(aggregatedValues);
		}
		public URI getObservedProperty(){
			return this.observedProperty;
		}
	}


	@Override
	public List<String> getSupportedUncertaintyTypes() {
		List<String> result = new ArrayList<String>();
		result.add(CREAL_URI);
		result.add(MEAN_URI);
		result.add(VARIANCE_URI);
		return result;
	}

}
