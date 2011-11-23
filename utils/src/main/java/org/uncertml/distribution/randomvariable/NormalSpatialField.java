package org.uncertml.distribution.randomvariable;

import java.net.URL;

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
	private IGaussianCovarianceParameter covarianceParameter;
	
	/**
	 * 
	 * 
	 * @param sampleReference
	 * 
	 * @param covarianceParameter
	 */
	public NormalSpatialField(URL sampleReference,IGaussianCovarianceParameter covarianceParameter){
		super.samples=sampleReference;
		this.covarianceParameter=covarianceParameter;
	}
	
}
