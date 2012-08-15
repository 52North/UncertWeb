package org.uncertweb.sta.wps.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.opengis.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.ObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.netcdf.NcUwFile;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.AggregationInputs;
import org.uncertweb.sta.wps.UncertainAggregationInputs;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.ProcessOutput;
import org.uncertweb.sta.wps.api.SingleProcessInput;

import com.vividsolutions.jts.geom.Geometry;


/**
 * algorithm that implements a weighted sum of a polygon to polygon aggregation. The intersection area of polygons serves
 * as weight.
 * 
 * @author staschc
 *
 */
public class Polygon2PolygonWeightedSum extends AbstractUncertainAggregationProcess{
	
	/**
	 * Input parameter which contains the input regions
	 */
	public static final SingleProcessInput<GTVectorDataBinding> INPUT_REGIONS = new SingleProcessInput<GTVectorDataBinding>(
			"InputRegions",
			GTVectorDataBinding.class, 0, 1, null, null);
	
	/**
	 * The URL of the SOS in which the aggregated observations will be inserted.
	 */
	public static final SingleProcessInput<String> INPUT_DATA = new SingleProcessInput<String>(
			Constants.Process.Inputs.INPUT_DATA,
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
			.getLogger(Polygon2PolygonWeightedSum.class);
	
	/**
	 * identifier of aggregation process
	 */
	public static final String IDENTIFIER = "urn:ogc:def:aggregationProcess:sIntersection:sWeightedMean:noTG:noTA";


	/**
	 * constructor
	 */
	public Polygon2PolygonWeightedSum(){
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
		return inputs;
	}

	@Override
	protected Set<ProcessOutput> getOutputs() {
		Set<ProcessOutput> result = new HashSet<ProcessOutput>();
		result.add(AGGREGATED_OUTPUT);
		return result;
	}
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		Map<String, IData> result = new HashMap<String,IData>();
		
		//get common Inputs
		AggregationInputs commonInputs = super.getAggregationInputs4Inputs(inputData);
		UncertainAggregationInputs uncertInputs = super.getUncertainAggregationInputs4Inputs(inputData);
		IObservationCollection originalObs = null;
		FeatureCollection targetRegions = null;
		
		//get observation input
		List<IData> inputDataInput = inputData.get(INPUT_DATA.getId());
		if (inputDataInput!=null&&inputDataInput.size()==1){
			originalObs = ((OMBinding)inputDataInput.get(0)).getPayload();
		}
		
		List<IData> inputRegionsInput = inputData.get(INPUT_REGIONS.getId());
		if (inputRegionsInput!=null&&inputRegionsInput.size()==1){
			targetRegions = ((GTVectorDataBinding)inputRegionsInput.get(0)).getPayload();
		}
		
		Map<TimeObject,IObservationCollection> obs4time = sortObsByTime(originalObs);
		Iterator<TimeObject> timeIter = obs4time.keySet().iterator();
		UncertaintyObservationCollection resultObsCol = new UncertaintyObservationCollection();
		while (timeIter.hasNext()){
			TimeObject time = timeIter.next();
			runAggregation4TimeObject(time,originalObs,targetRegions,uncertInputs.getNumberOfRealisations());
		
		}
		
		
		return result;
	}


	/**
	 * helper method that actually runs the agggregation
	 * 
	 * @param originalObs
	 * @param targetRegions
	 * @return
	 */
	private IObservationCollection runAggregation4TimeObject(TimeObject time, 
			IObservationCollection originalObs, FeatureCollection targetRegions, int numberOfRealisations) {
		IObservationCollection targetObs = null;
		//Map<SpatialSamplingFeature, IObservationCollection> obsCols4Fois = sortObsByFoi(originalObs);
		
		
		
		//Iterator<SpatialSamplingFeature> foiIter = obsCols4Fois.keySet().iterator();
		Map<Feature,RegionAggregates> regionsAggCache = new HashMap<Feature,RegionAggregates>(targetRegions.size());
		
		/*
		 * iterate over fois and for each target region, check intersection; if FOI intersects, store obsCol with intersection are 
		 */
		Iterator<? extends AbstractObservation> obsIter = originalObs.getObservations().iterator();
		while (obsIter.hasNext()){
			//TODO add type check!!
			UncertaintyObservation uncertObs = (UncertaintyObservation) obsIter.next();
			Geometry foiGeom = uncertObs.getFeatureOfInterest().getShape();
			//iterate over target regions
			FeatureIterator features = targetRegions.features();
			while (features.hasNext()){
				Feature targetRegion = features.next();
				Geometry regionGeom = (Geometry)targetRegion.getDefaultGeometryProperty().getValue();
				if (regionGeom.intersects(foiGeom)){
					Geometry intersectionArea = regionGeom.intersection(foiGeom);
					double weight = intersectionArea.getArea();
					//TODO add type check
					ContinuousRealisation obsResult = (ContinuousRealisation)uncertObs.getResult().getUncertaintyValue();
					ContinuousRealisation weightedReal = new ContinuousRealisation(obsResult.getValues(),weight);
					if (regionsAggCache.containsKey(targetRegion)){
						regionsAggCache.get(targetRegion).addRealisation(weightedReal);
					}
					else {
						List<ContinuousRealisation> realList = new ArrayList<ContinuousRealisation>();
						realList.add(weightedReal);
						regionsAggCache.put(targetRegion, new RegionAggregates(targetRegion,realList));
					}
				}
				
			}
		}
		
		//iterate over regions aggregate cache and run aggregations
		IObservationCollection result = new UncertaintyObservationCollection();
		Iterator<RegionAggregates> regionsAggIter = regionsAggCache.values().iterator();
		while (regionsAggIter.hasNext()){
			RegionAggregates regionAgg = regionsAggIter.next();
			ContinuousRealisation aggResult = regionAgg.aggregate(numberOfRealisations);
			UncertaintyResult obsResult = new UncertaintyResult(aggResult);
			
			
		}
		
		
		return targetObs;
	}
	
	
	/**
	 * sorts the input observations by time and provides a map containing the times as keys and the corresponding observation collections as values
	 * 
	 * @param obsCol
	 * 			observation collection that should be sorted by time
	 * @return
	 * 			map containing the times as keys and the corresponding observation collections as values
	 */
	private Map<SpatialSamplingFeature,IObservationCollection> sortObsByFoi(IObservationCollection obsCol){
		
		Map<SpatialSamplingFeature,IObservationCollection> result = new HashMap<SpatialSamplingFeature,IObservationCollection>();
		Iterator<? extends AbstractObservation> obsIter = obsCol.getObservations().iterator();
		while (obsIter.hasNext()){
			AbstractObservation obs = obsIter.next();
			SpatialSamplingFeature foi = obs.getFeatureOfInterest();
			if (result.containsKey(foi)){
				result.get(foi).addObservation(obs);
			}
			else {
				IObservationCollection obsColNew = new ObservationCollection();
				obsColNew.addObservation(obs);
				result.put(foi, obsColNew);
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
		private Feature region;
		private List<ContinuousRealisation> originalObsResults;
		
		
		public RegionAggregates(Feature region,
				List<ContinuousRealisation> originalObsCols) {
			this.region = region;
			this.originalObsResults = originalObsCols;
		}
		
		public Feature getRegion() {
			return region;
		}
		public List<ContinuousRealisation> getOriginalObsCols() {
			return originalObsResults;
		}
		
		public void addRealisation(ContinuousRealisation realisation){
			this.originalObsResults.add(realisation);
		}
		
		
		public ContinuousRealisation aggregate(int numberOfRealisations){
			List<Double> aggregatedValues = new ArrayList<Double>(numberOfRealisations);
			for (int i=0;i<numberOfRealisations;i++){
				double sum = 0;
				for (ContinuousRealisation cr:originalObsResults){
					sum+=cr.getWeight()*cr.getValues().get(i);
				}
				aggregatedValues.add(sum);
			}
			return new ContinuousRealisation(aggregatedValues);
		}
	}
	
	private class WeightedObsCol{
		private double weight;
		private IObservationCollection obsCol;
		
		public WeightedObsCol(double weight, IObservationCollection obsCol){
			this.weight=weight;
			this.obsCol=obsCol;
		}
		public double getWeight() {
			return weight;
		}
		public IObservationCollection getObsCol() {
			return obsCol;
		}
	}

}
