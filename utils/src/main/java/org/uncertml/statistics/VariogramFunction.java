package org.uncertml.statistics;

import org.uncertml.distribution.IDistribution;
import org.uncertml.distribution.randomvariable.IGaussianCovarianceParameter;
import org.uncertml.statistic.IStatistic;

/**
 * class represents a variogram
 * 
 * @author staschc
 *
 */
public class VariogramFunction implements IStatistic, IGaussianCovarianceParameter{
	
	/**sill value of variogram*/
	private double sill;
	
	/**range value of variogram*/
	private double range;
	
	/**nugget of variogram*/
	private double nugget;
	
	/**kappa value; only used if model is MATERN*/
	private double kappa = Double.NaN;
	
	/**model of th variogram*/
	private Model model;
	
	/**anistropy of the variogram*/
	private Anisotropy anis;
	
	/**
	 * enumeration that represents the different models for the variogram
	 * 
	 * @author staschc
	 *
	 */
	public enum Model{
		GAUSSIAN,SPHERICAL,EXPONENTIAL,LINEAR,MATERN
	}
	
	/**
	 * constructor with all parameters
	 * 
	 * @param sill
	 * @param range
	 * @param nugget
	 * @param kappa
	 * @param model
	 * @param anis
	 */
	public VariogramFunction(double sill, double range, double nugget, double kappa, Model model, Anisotropy anis){
		this.sill=sill;
		this.range = range;
		this.nugget = nugget;
		this.kappa = kappa;
		this.model = model;
		this.anis = anis;
	}
	
	/**
	 * constructor with mandatory parameters
	 * 
	 * @param sill
	 * @param range
	 * @param nugget
	 * @param model
	 */
	public VariogramFunction(double sill, double range, double nugget, Model model){
		this.sill=sill;
		this.range = range;
		this.nugget = nugget;
		this.model = model;
	}
	
	/**
	 * constructor with mandatory parameters and anisotropy
	 * 
	 * @param sill
	 * @param range
	 * @param nugget
	 * @param model
	 * @param anis
	 */
	public VariogramFunction(double sill, double range, double nugget, Model model, Anisotropy anis){
		this.sill=sill;
		this.range = range;
		this.nugget = nugget;
		this.model = model;
		this.anis = anis;
	}

	/**
	 * @return the sill
	 */
	public double getSill() {
		return sill;
	}

	/**
	 * @return the range
	 */
	public double getRange() {
		return range;
	}

	/**
	 * @return the nugget
	 */
	public double getNugget() {
		return nugget;
	}

	/**
	 * @return the kappa
	 */
	public double getKappa() {
		return kappa;
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @return the anis
	 */
	public Anisotropy getAnis() {
		return anis;
	}
	
}
