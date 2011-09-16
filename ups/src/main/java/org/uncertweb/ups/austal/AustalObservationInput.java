package org.uncertweb.ups.austal;

import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;

/**
 * wrapper class for UncertaintyObservationCollections containing
 * emission observations and meteorological observations
 * 
 * @author staschc
 *
 */
public class AustalObservationInput {

	private UncertaintyObservationCollection meteoObs;
	private UncertaintyObservationCollection emissionObs;
	
	public AustalObservationInput(UncertaintyObservationCollection meteoObsp, UncertaintyObservationCollection emissionObsp){
		this.meteoObs = meteoObsp;
		this.emissionObs = emissionObsp;
	}

	/**
	 * @return the meteoObs
	 */
	public UncertaintyObservationCollection getMeteoObs() {
		return meteoObs;
	}

	/**
	 * @return the emissionObs
	 */
	public UncertaintyObservationCollection getEmissionObs() {
		return emissionObs;
	}
	
	/**
	 * 
	 * @return Returns true if object contains Meteorological Observations
	 */
	public boolean hasMeteoObs(){
		if (meteoObs!=null){
			return true;
		}
		else return false;
	}

	/**
	 * 
	 * @return Returns true if object contains Emission Observations
	 */
	public boolean hasEmissionObs(){
		if (emissionObs!=null){
			return true;
		}
		else return false;
	}
	
	
	
}
