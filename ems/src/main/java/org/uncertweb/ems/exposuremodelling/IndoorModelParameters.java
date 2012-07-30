package org.uncertweb.ems.exposuremodelling;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Stores distribution parameters for the indoor model parameters for a specific microenvironment and country
 * @author Lydia Gerharz
 *
 */
public class IndoorModelParameters {

	private String microenvironment;
	
	// parameters: a, p, k, v, s_bgr, s_bio, s_ets
	private HashMap<String,IUncertainty> parameters;
	private HashMap<String,Double> paraTemp;

	
	public IndoorModelParameters(String me){
		microenvironment = me;
		parameters = new HashMap<String,IUncertainty>();
		paraTemp = new HashMap<String,Double>();
	}
	
	/*
	 * Setters
	 */
	public void setParameterValue(String parameterId, String distributionParameter, String distribution, double val){
		// check if parameter is already in the list
		if(!paraTemp.containsKey(parameterId)){
			paraTemp.put(parameterId, val);
		}else{
			// get existing value and convert to lognormal distribution
			Double sd, mean;
			if(distributionParameter.contains("mean")){
				sd = paraTemp.get(parameterId);
				mean = val;			
			}else{
				mean = paraTemp.get(parameterId);
				sd = val;	
			}
			
			double[] logParams = null;
			// transform values to lognormal distribution
			if(distributionParameter.contains("geometric")){				
				if(distribution.equals("LN")){
					logParams = geometric2lognormal(mean, sd);		
					// create lognormal distribution for this parameter
					parameters.put(parameterId, new LogNormalDistribution(logParams[0], logParams[1]));
				}else
					parameters.put(parameterId, new NormalDistribution(mean, Math.pow(sd,2)));
			}else if(distributionParameter.contains("normal")){
				if(distribution.equals("LN")){
					logParams = normal2lognormal(mean, sd);
					// create lognormal distribution for this parameter
					parameters.put(parameterId, new LogNormalDistribution(logParams[0], logParams[1]));
				}else
					parameters.put(parameterId, new NormalDistribution(mean, Math.pow(sd,2)));					
			}					
		}			
	}
	
	private double[] normal2lognormal(double nMean, double nSd){
		// get lognormal mean and sd
		double logMean = Math.log(Math.pow(nMean,2) / Math.sqrt(Math.pow(nSd,2)+Math.pow(nMean,2)));
		double logVar = Math.log(Math.pow(nSd,2)/Math.pow(nMean,2) + 1);
		
		return new double[]{logMean, logVar};
		
//		# lognormal mean and sd
//		mu = log(m^2 / sqrt(sd^2+m^2))
//		sigma = log(sd^2/m^2 + 1)
	}
	
	private double[] geometric2lognormal(double gMean, double gSd){
		// get normal mean and sd
		double nMean =  gMean/Math.exp(-Math.pow(Math.log(gSd),2)/2);
		double nSd = Math.sqrt(Math.exp(Math.log(gMean)+Math.pow(Math.log(gSd),2)/2)*Math.sqrt(Math.exp(Math.pow(Math.log(gSd),2))-1));
		
		// get lognormal mean and sd
		return normal2lognormal(nMean, nSd);
		
//		# normal mean and var
//		m = gm/exp(-log(gsd)^2/2);
//		v = exp(log(gm)+log(gsd)^2/2)*sqrt(exp(log(gsd)^2)-1);
	}
	
	private double[] geometric2normal(double gMean, double gSd){
		// get normal mean and sd
		double nMean =  gMean/Math.exp(-Math.pow(Math.log(gSd),2)/2);
		double nVar = Math.exp(Math.log(gMean)+Math.pow(Math.log(gSd),2)/2)*Math.sqrt(Math.exp(Math.pow(Math.log(gSd),2))-1);
		
		// get lognormal mean and sd
		return new double[]{nMean, nVar};
		
//		# normal mean and var
//		m = gm/exp(-log(gsd)^2/2);
//		v = exp(log(gm)+log(gsd)^2/2)*sqrt(exp(log(gsd)^2)-1);
	}
	
	/*
	 * Getters
	 */
	public IUncertainty getParameterValue(String parameterId){
		return parameters.get(parameterId);
	}
	
	public Set<String> getParameters(){
		//Set<String> params = parameters.keySet();
		//params.toArray(new String[0]);
		return parameters.keySet();
	}
	
}
