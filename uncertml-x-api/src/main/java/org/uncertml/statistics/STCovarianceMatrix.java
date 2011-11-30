package org.uncertml.statistics;

import java.util.Collection;
import java.util.Date;

import org.uncertml.distribution.randomvariable.IGaussianCovarianceParameter;
import org.uncertml.statistic.CovarianceMatrix;

import com.vividsolutions.jts.geom.Geometry;

/**
 * covariance matrix with optional spatial and/or temporal reference
 * 
 * @author staschc
 *
 */
public class STCovarianceMatrix extends CovarianceMatrix implements IGaussianCovarianceParameter{

	
	public Collection<Geometry> spatialReference;
	public Collection<Date> temporalReferences;
	
	/**
	 * constructor for covariance matrix without spatial or temporal reference
	 * 
	 * @param dimensionp
	 * @param values
	 */
	public STCovarianceMatrix(int dimensionp, double[] values) {
		super(dimensionp, values);
	}
	
	/**
	 * constructor for covariance matrix without spatial or temporal reference
	 * 
	 * @param dimensionp
	 * @param values
	 */
	public STCovarianceMatrix(CovarianceMatrix m) {
		super(m.getDimension(), m.getValues());
	}
}
