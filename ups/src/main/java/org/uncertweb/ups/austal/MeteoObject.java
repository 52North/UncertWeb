package org.uncertweb.ups.austal;

import org.uncertml.IUncertainty;
import org.uncertweb.api.om.observation.UncertaintyObservation;

/**
 * Class MeteoObject
 * 
 * A MeteoObject stores a meteorological observation, i.e. the information
 * about the meteorology at a certain moment and a certain place, represented
 * by a probability distribution. It also stores the realisations drawn from
 * this distribution. The time and place are stored as well (in the Uncertainty
 * Observation).
 * 
 * @author Merret Buurman, 17. August 2011
 *
 */
public class MeteoObject {
	
	
	
	// attributes
	private UncertaintyObservation uncObserv = null;
	private IUncertainty distribution = null;
	private double[] samples = null;
	
	
	
	/**
	 * Constructor
	 * 
	 * Creates a meteoobject from an UncertaintyObservation.
	 */
	public MeteoObject(UncertaintyObservation uncObs) {
		
		// Store UncertaintyObservation
		this.uncObserv = uncObs;
		
		// Extract and store distribution
		this.distribution = uncObs.getResult().getUncertaintyValue();

	}

	
	
	/* Simple getters */

	/**
	 * getDistribution()
	 * 
	 * This method returns the distribution of this meteo observation.
	 * @return A distribution.
	 */
	public IUncertainty getDistribution() {
		return distribution;
	}
	
	/**
	 * getUncertaintyObservation()
	 * 
	 * This method returns the UncertaintyObservation from which the meteo object
	 * was created.
	 * @return
	 */
	public UncertaintyObservation getUncertaintyObservation(){
		return uncObserv;
	}

	/**
	 * getSample()
	 * 
	 * This method returns the i-th sample/realisation of this observation,
	 * to be used with the i-th austal run.
	 * 
	 * @param j	number of the austal run
	 * @return A single realisation/sample
	 */
	public double getSample(int j) {
		return samples[j];
	}
	
	
	/* Setters */
	
	/** putSamples()
	 * 
	 * This method adds an array with samples to this meteo object.
	 * @param samples An array containing samples drawn from this object's distribution.
	 */
	public void putSamples(double[] samples) {
		this.samples = samples;	
	}



	
	/** getUnitOfMeasurement
	 * 
	 * @return Unit of Measurement from O&M document
	 */
	public String getUnitOfMeasurement() {
		return "Platzhalter_fuer_Unit_of_winddir/windspeed";
//		return uncObserv.getUnitOfMeasurement();
		// TODO once unit of measurement is included in API, enable this!
	}
	

	

}
