package org.uncertweb.ups.austal;

import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.multivariate.IMultivariateDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;

public class HourDistribution {
	
	// attributes
	private TimeObject day = null;
	private Identifier sourceIdentifier = null;
	private IMultivariateDistribution unc = null;
	private double[][] samples = null;
//	private Source source = null;
	
	/**
	 * Constructor
	 * @param id = id of the source
	 * @param day = which day
	 * @param iUn = distribution, should be MultivariateNormalDistribution
	 * @param sourcy 
	 */
	public HourDistribution(Identifier id, TimeObject day, IMultivariateDistribution iUn) {
		this.sourceIdentifier = id;
		this.day = day;
//		this.source = source;
		this.unc = iUn;

	}
	
	/**
	 * Add samples
	 * @param samples
	 */
	public void putSamples(double[][] samples) {
		this.samples = samples;		
	}

	public Identifier getSourceID() {
		return sourceIdentifier;
	}

	public TimeObject getTime() {
		return day;
	}

	
	public double[][] getSamples() {
		return samples;
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
			String zwischenstring = "\n\t[" + samples[i][0] ;
			for (int j = 1; j < samples[i].length; j++){
				zwischenstring = zwischenstring.concat(", " + samples[i][j]);
			}
			zwischenstring = zwischenstring.concat("]");
			if (i == samples.length-1){
				sampleString = sampleString.concat("]");
			} else {
				sampleString = sampleString.concat(zwischenstring + ", ");	
			}
			
		}
		
		// mean
		String mean = "[ " +
			((MultivariateNormalDistribution) unc).getMean().get(0) + "; " +
			((MultivariateNormalDistribution) unc).getMean().get(1) + "; " +
			((MultivariateNormalDistribution) unc).getMean().get(2) + "; " +
			", ... ]";
		// covmat
		String covmat = "[ " + 
		((MultivariateNormalDistribution) unc).getCovarianceMatrix().getValues().get(0) + "; " +
		((MultivariateNormalDistribution) unc).getCovarianceMatrix().getValues().get(1) + "; " +
		((MultivariateNormalDistribution) unc).getCovarianceMatrix().getValues().get(2) + "; " +
		", ... ]";
		
		
		String toBeReturned = 
			"HourDistribution: Source " + sourceIdentifier.getIdentifier() 
			+ "; Day " + date 
			+ "; Mean = " + mean
		    + "; Shape = " + covmat 
		    + ";\n\tSamples: " + sampleString +"\n";
		return toBeReturned;
	}
	
	
	
	public IMultivariateDistribution getDistribution() {
		return unc;
	}

	


}
