package org.uncertml.distribution.randomvariable;

import java.net.URL;

public class NormalSpatioTemporalField extends AbstractSpatioTemporalField{

	/** covariance parameter*/
	private IGaussianCovarianceParameter covarianceParameter;
	
	/**
	 * 
	 * 
	 * @param sampleReference
	 * 
	 * @param covarianceParameter
	 */
	public NormalSpatioTemporalField(URL sampleReference,IGaussianCovarianceParameter covarianceParameter){
		super.samples=sampleReference;
		this.covarianceParameter=covarianceParameter;
	}
}
