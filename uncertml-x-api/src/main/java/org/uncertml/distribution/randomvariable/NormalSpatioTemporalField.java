package org.uncertml.distribution.randomvariable;

import org.uncertml.distribution.IDistribution;

/**
 * class represents a normal spatio-temporal field
 * 
 * @author staschc
 *
 */
public class NormalSpatioTemporalField extends AbstractSpatioTemporalField implements IDistribution{

	/** covariance parameter*/
	private INormalCovarianceParameter covarianceParameter;
	
	/**coefficients of a spatial trend polynom*/
	private double[] spatialTrend;
	
	/**coefficients of a temporal trend polynom*/
	private double[] temporalTrend;
	
	/**
	 * constructor
	 * 
	 * @param sampleReference
	 * 			reference to samples
	 * @param covarianceParameter
	 * 			covariance parameter can be either variogram function or covariance matrix
	 * @param spatialTrend
	 * 			coefficients of spatial trend polynom
	 * @param temporalTrend
	 * 			coefficients of temporal trend polynom
	 * */
	public NormalSpatioTemporalField(SampleReference sampleReference,INormalCovarianceParameter covarianceParameter,double[] spatialTrend, double[] temporalTrend){
		super.samples=sampleReference;
		setCovarianceParameter(covarianceParameter);
		setSpatialTrend(spatialTrend);
		setTemporalTrend(temporalTrend);
	}

	/**
	 * @return the spatialTrend
	 */
	public double[] getSpatialTrend() {
		return spatialTrend;
	}

	/**
	 * @param spatialTrend the spatialTrend to set
	 */
	public void setSpatialTrend(double[] spatialTrend) {
		this.spatialTrend = spatialTrend;
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
}
