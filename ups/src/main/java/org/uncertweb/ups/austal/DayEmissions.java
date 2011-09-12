package org.uncertweb.ups.austal;


import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.UncertaintyObservation;

/**
 * An object of class DayEmissions contains the emissions (realisations)
 * for one source and one day. It thus contains 24xN emission values, with
 * N being the number of austal runs.
 * 
 * The source is represented by the O&M Observation that is stored as an attribute.
 * The day is represented by a TimeObject.
 * 
 * @author m_buur01
 *
 */
public class DayEmissions {
	
	
	// Attributes
	private TimeObject day = null;
	private double[][] emissions = null; // [numberOfRealisations][24]
	private UncertaintyObservation uncObs = null;
	
	/**
	 * Constructor
	 * 
	 * @param uncertaintyObservation 
	 * @param emissions
	 * @param day
	 * @param identifier
	 */
	public DayEmissions(double[][] emissions, TimeObject day, Identifier identifier, UncertaintyObservation uncertaintyObservation) {
		this.day = day;
		this.emissions = emissions;
		this.uncObs = uncertaintyObservation;
	}

	
	/* Simple getters */
	
	
	/** getEmissions()
	 * 
	 * @return The emissions of that day as a double[][]. It contains the
	 * 24 hourly emissions for each austal run (double[numberOfRealisations][24]).
	 */
	public double[][] getEmissions(){
		return emissions;
	}
	
	
	/** getTime()
	 * 
	 * @return A TimeObject that identifies/describes the day of these
	 * 		   emissions
	 */
	public TimeObject getTime() {
		return day;
	}

	
	/** getUncertaintyObservation()
	 * 
	 * @return The UncertaintyObservation
	 */
	public UncertaintyObservation getUncertaintyObservation(){
		return uncObs;
	}
	


	
	/** getUnitOfMeasurement()
	 * 
	 * @return a String that is the correct unit of measurement
	 */
	public String getUnitOfMeasurement() {
//		return uncObs.getUnitOfMeasurement();
		// TODO once unit of measurement is included in API, enable this!
		return "g[PM10]/s";
	}


}
