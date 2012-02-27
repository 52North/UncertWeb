package org.uncertml.distribution.randomvariable;

import org.uncertml.distribution.IDistribution;

/**
 * 
 * implementation of a Gaussian spatial random field
 * 
 * @author staschc
 *
 */
public class NormalSpatialField extends AbstractSpatialField implements IDistribution{
	
	/** covariance parameter*/
	private INormalCovarianceParameter covarianceParameter;
	
	/**coefficients of a spatial trend polynom*/
	private double[] spatialTrend;
	
	/**
	 * 
	 * 
	 * @param sampleReference
	 * 
	 * @param covarianceParameter
	 * 
	 * @param spatialTrend
	 */
	public NormalSpatialField(SampleReference sampleReference,INormalCovarianceParameter covarianceParameter, double[] spatialTrend){
		super.samples=sampleReference;
		this.covarianceParameter=covarianceParameter;
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
	
}
