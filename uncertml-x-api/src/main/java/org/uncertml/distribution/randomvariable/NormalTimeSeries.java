package org.uncertml.distribution.randomvariable;

import org.uncertml.distribution.IDistribution;

/**
 * 
 * 
 * @author staschc
 *
 */
public class NormalTimeSeries extends AbstractTimeSeries implements IDistribution {
	
	/** covariance parameter*/
	private INormalCovarianceParameter covarianceParameter;
	
	/**coefficients of a temporal trend polynom*/
	private double[] temporalTrend;
	
	

	/**
	 * constructor
	 * 
	 * @param covarianceParameter
	 * 			covariance parameter; can be either variogram function or covariance matrix
	 * @param temporalTrend
	 * 			array containing the coefficients of a temporal trend polynom
	 */
	public NormalTimeSeries(SampleReference sampleReference, INormalCovarianceParameter covarianceParameter,
			double[] temporalTrend) {
		super();
		super.setSamples(sampleReference);
		this.covarianceParameter = covarianceParameter;
		this.temporalTrend = temporalTrend;
	}

	/**
	 * @return the covarianceParameter
	 */
	public INormalCovarianceParameter getCovarianceParameter() {
		return covarianceParameter;
	}

	/**
	 * @param covarianceParameter the covarianceParameter to set
	 */
	public void setCovarianceParameter(
			INormalCovarianceParameter covarianceParameter) {
		this.covarianceParameter = covarianceParameter;
	}

	/**
	 * @return the temporalTrend
	 */
	public double[] getTemporalTrend() {
		return temporalTrend;
	}

	/**
	 * @param temporalTrend the temporalTrend to set
	 */
	public void setTemporalTrend(double[] temporalTrend) {
		this.temporalTrend = temporalTrend;
	}
	
	
}
