package org.uncertweb.ups.austal;

import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.UncertaintyObservation;

public class DayDistribution {
	
	private TimeObject day = null;
	private Identifier sourceIdentifier = null;
	private double[] samples = null; // one sample per day, thus size of the array is the number of days
	private UncertaintyObservation uncObs = null;
	
	/**
	 * Constructor
	 * @param id
	 * @param day
	 * @param uncObs 
	 */
	public DayDistribution(Identifier id, TimeObject day, UncertaintyObservation uncObs) {
		this.sourceIdentifier = id;
		this.day = day;
		this.uncObs = uncObs;
	}
	
	/**
	 * to String
	 */
	public String toString(){
		// date
		String date = day.getDateTime().toString().substring(0, 10);
		// samples
		String sampleString = "[";
		for (int i = 0; i < samples.length; i++){
			if (i == samples.length - 1){
				sampleString = sampleString.concat(samples[i] + "]");
			} else if (i/2 == (double)i/2){
				sampleString = sampleString.concat(samples[i] + ",\n\t");
			} else {
				sampleString = sampleString.concat(samples[i] + ", ");
			}
			
		}
		
		LogNormalDistribution unc = (LogNormalDistribution) uncObs.getResult().getUncertaintyValue();
		String toBeReturned = "DayDistribution: Source " + sourceIdentifier.getIdentifier() 
		+ "; Day " + date 
		+ "; Logscale = " + unc.getLogScale() 
		+ "; Shape = " + unc.getShape() 
		+ ";\n\tSamples: " + sampleString +"\n";
		return toBeReturned;
	}
	
	/*  Simple getters and setters*/

	public void putSamples(double[] samples) {
		this.samples = samples;
	}

	public Identifier getSourceID() {
		return sourceIdentifier;
	}

	public TimeObject getTime() {
		return day;
	}

	public double[] getSamples() {
		return samples;
	}
	
	public IUncertainty getDistribution() {
		return uncObs.getResult().getUncertaintyValue();

	}
	

	public UncertaintyObservation getObservation() {
		return uncObs;
	}
	
	

}
