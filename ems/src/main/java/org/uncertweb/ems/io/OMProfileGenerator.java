package org.uncertweb.ems.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.statistic.ContinuousStatistic;
import org.uncertml.statistic.IStatistic;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Variance;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.ems.data.exposure.ExposureValue;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.data.profiles.GeometryProfile;

/**
 * Creates an O&M document from the exposure profile, containing the exposure results as observations
 * @author LydiaGerharz
 *
 */

public class OMProfileGenerator {

	private final String UNCERTWEB_PHEN_PREFIX = "http://www.uncertweb.org/phenomenon/";
	
	/**
	 * Constructor
	 */
	public OMProfileGenerator(){
		
	}
	
	// Method for single ObservationCollections per individual
		public ArrayList<UncertaintyObservationCollection> createIndividualExposureProfileObservationCollections(List<AbstractProfile> profiles, List<String> statistics){		
			ArrayList<UncertaintyObservationCollection> individualResults = new ArrayList<UncertaintyObservationCollection>();
			
			for(AbstractProfile profile : profiles){
				IObservationCollection exposureObs = new UncertaintyObservationCollection();
				IObservationCollection rawObs = profile.getObservationCollection();
				
				// loop through original observation collection and add concentrations as results
				for(AbstractObservation obs : rawObs.getObservations()){	
					ArrayList<ExposureValue> vals = profile.getExposureValues(obs.getPhenomenonTime().getInterval());
					
					// add all different exposure values
					for(ExposureValue v : vals){
						// procedure id = individual, observed property = pollutant + type
						URI observedProperty = null;
						try {
							observedProperty = new URI(UNCERTWEB_PHEN_PREFIX + v.getPollutant() + "_" + v.getType());
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
						
						// add all statistics
						double[] realisations = v.getExposureValues();
						for(String stat : statistics){
							IUncertainty statVal = null;
							
							if(stat.contains("mean")){
								double[] mean = new double[1];
								for(int i=0; i<realisations.length; i++){
									mean[0] += realisations[i]/realisations.length;
								}
								statVal = new Mean(mean);
							}else if(stat.contains("variance")){
								double[] var = new double[1];
								double[] mean = new double[1];
								for(int i=0; i<realisations.length; i++){
									mean[0] += realisations[i]/realisations.length;
								}
								for(int i=0; i<realisations.length; i++){
									var[0] += Math.pow((realisations[i]-mean[0]),2)/realisations.length;
								}
								statVal = new Variance(var);
							}else if(stat.contains("deviation")){
								double[] sd = new double[1];
								double[] mean = new double[1];
								for(int i=0; i<realisations.length; i++){
									mean[0] += realisations[i]/realisations.length;
								}
								for(int i=0; i<realisations.length; i++){
									sd[0] += Math.sqrt(Math.pow((realisations[i]-mean[0]),2)/realisations.length);
								}
								statVal = new Variance(sd);
							}else if(stat.contains("realisation")){
								statVal = new ContinuousRealisation(realisations);
							}
							UncertaintyObservation newObs = new UncertaintyObservation(obs.getPhenomenonTime(), obs.getResultTime(), 
									obs.getProcedure(), observedProperty, obs.getFeatureOfInterest(), new UncertaintyResult(statVal,v.getUom()));
							exposureObs.addObservation(newObs);
						}				
					}			
				}
				
				individualResults.add((UncertaintyObservationCollection)exposureObs);
			}
			
			return individualResults;
		}
		
	// Method for one ObservationCollection containing all individuals
	public IObservationCollection createExposureProfileObservationCollection(AbstractProfile profile, List<String> statistics){		
		IObservationCollection exposureObs = new UncertaintyObservationCollection();				
		IObservationCollection rawObs = profile.getObservationCollection();
		
		// loop through original observation collection and add concentrations as results
		for(AbstractObservation obs : rawObs.getObservations()){	
			ArrayList<ExposureValue> vals = profile.getExposureValues(obs.getPhenomenonTime().getInterval());
			
			// add all different exposure values
			for(ExposureValue v : vals){
				// procedure id = individual, observed property = pollutant + type
				URI observedProperty = null;
				try {
					observedProperty = new URI(UNCERTWEB_PHEN_PREFIX + v.getPollutant() + "_" + v.getType());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				
				// add all statistics
				double[] realisations = v.getExposureValues();
				for(String stat : statistics){
					IUncertainty statVal = null;
					
					if(stat.contains("mean")){
						double[] mean = new double[1];
						for(int i=0; i<realisations.length; i++){
							mean[0] += realisations[i]/realisations.length;
						}
						statVal = new Mean(mean);
					}else if(stat.contains("variance")){
						double[] var = new double[1];
						double[] mean = new double[1];
						for(int i=0; i<realisations.length; i++){
							mean[0] += realisations[i]/realisations.length;
						}
						for(int i=0; i<realisations.length; i++){
							var[0] += Math.pow((realisations[i]-mean[0]),2)/realisations.length;
						}
						statVal = new Variance(var);
					}else if(stat.contains("deviation")){
						double[] sd = new double[1];
						double[] mean = new double[1];
						for(int i=0; i<realisations.length; i++){
							mean[0] += realisations[i]/realisations.length;
						}
						for(int i=0; i<realisations.length; i++){
							sd[0] += Math.sqrt(Math.pow((realisations[i]-mean[0]),2)/realisations.length);
						}
						statVal = new Variance(sd);
					}else if(stat.contains("realisation")){
						statVal = new ContinuousRealisation(realisations);
					}
					UncertaintyObservation newObs = new UncertaintyObservation(obs.getPhenomenonTime(), obs.getResultTime(), 
							obs.getProcedure(), observedProperty, obs.getFeatureOfInterest(), new UncertaintyResult(statVal,v.getUom()));
					exposureObs.addObservation(newObs);
				}				
			}			
		}
		return exposureObs;
	}
	
}
