package org.uncertweb.ems.exposuremodelling;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.n52.wps.util.r.process.RProcessException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertweb.ems.activityprofiles.Profile;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Estimates indoor concentration of air pollutants by using a mass balance model and the respective outdoor concentration
 * @author Lydia Gerharz
 *
 */
public class IndoorModel {
	
	private HashMap<String, IndoorModelParameters> microenvParams;
	private static Logger LOGGER = Logger.getLogger(IndoorModel.class);
	
	public IndoorModel(){
		
	}	
	
	
	public Profile runModel(Profile profile, int numberOfIterations, int minInterval, boolean useIndoorSources){
		ExtendedRConnection c = null;		
		int count = 0;
		try {		
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}			
			
			//TODO: get the microenvironment and activity list from linked data
			String lastME = "", lastActivity = "", lastWindowOpen = "0";
			double[] a = new double[numberOfIterations], 
					p = new double[numberOfIterations], 
					k = new double[numberOfIterations], 
					v = new double[numberOfIterations], 
					s_bgr = new double[numberOfIterations],
					s_cig = new double[numberOfIterations], 
					s_hum = new double[numberOfIterations], 
					s_cook = new double[numberOfIterations];
			
			// loop through diary entries
			for(int i=0; i<profile.getSize(); i++){
				// get attributes for this entry
				Map<String, String> attributes = profile.getAttributesEntry(i);
				
				// get visited microenvironment for the model
				String me = attributes.get("microenvironment");
				String me2 = attributes.get("microenvironmentDetail");
				if(me2.contains("home"))
					me = "home";
				
				// (1) check if parameters are available for this microenvironment and which model to use
				IndoorModelParameters params = null;
				if(microenvParams.containsKey(me2)){
					params = microenvParams.get(me2);									
				}				
				else if(microenvParams.containsKey(me)){
					params = microenvParams.get(me);						
				}
				else{
					// else: has to be outdoor or transport environment outside which is treated as outdoor
					lastME = me;
					continue;
				}
				
				if(params!=null){
					// (2) decide which model to use
					// (2.1) pub, disco, restaurant: concentration model
					if(params.getParameters().contains("c")){		
						double[] c_in = sampling(params.getParameterValue("c"), numberOfIterations*profile.getOutConcRealisations(i).length, c);
						if(!useIndoorSources){
							double[] c_out = profile.getOutConcRealisations(i);
							c_in = new double[numberOfIterations*c_out.length];
							for(int j=0; j<numberOfIterations; j++){
								for(int l=0; l<c_out.length; l++){
									c_in[j*c_out.length+l] = c_out[l];								
								}
							}
						}
						// add concentration to profile
						profile.setInConcRealisations(i, c_in);
					}
					
					// (2.2) bus, car: ratio model
					else if(params.getParameters().contains("r")){
						// get outdoor concentration
						double[] c_out = profile.getOutConcRealisations(i);
						
						// estimate ratio and apply to outdoor concentration
						double[] r = sampling(params.getParameterValue("r"), numberOfIterations, c);
						double[] c_in = new double[r.length*c_out.length];
						if(useIndoorSources){
							for(int j=0; j<r.length; j++){
								for(int l=0; l<c_out.length; l++){
									c_in[j*c_out.length+l] = c_out[l] * r[j];								
								}
							}
						}
						else if(!useIndoorSources){
							for(int j=0; j<r.length; j++){
								for(int l=0; l<c_out.length; l++){
									c_in[j*c_out.length+l] = c_out[l];								
								}
							}
						}
						// add concentration to profile
						profile.setInConcRealisations(i, c_in);
					}
					
					// (2.3) home, work, other indoor: normal model
					else{
						// (A) get model parameters
						// get outdoor concentration
						double[] c_out = profile.getOutConcRealisations(i);
					
						// get activities to add indoor sources
						String activity = attributes.get("activity");
						String smoker = attributes.get("smoker");
						//TODO: include emissions per person and change in air exchange for open windows
						String noOfPersons = attributes.get("noOfPersons");				
						String windowOpen = attributes.get("windowOpen");
						
						// make the entering human activity emission zero
						s_hum = new double[numberOfIterations];
						
						// if this is a new ME: sample indoor parameters 
						if(!lastME.equals(me2)){
							a = sampling(params.getParameterValue("a"), numberOfIterations, c, minInterval/60d);
							k = sampling(params.getParameterValue("k"), numberOfIterations, c, minInterval/60d);					
							//TODO: what to do with p>1?
							p = sampling(params.getParameterValue("p"), numberOfIterations, c);
							v = sampling(params.getParameterValue("v"), numberOfIterations, c);		
							
//							// when entering a new ME, add human walking emissions
//							if(params.getParameters().contains("s_human"))
//								s_hum = sampling(params.getParameterValue("s_human"), numberOfIterations, c, 2);							
//							else
//								s_hum = new double[numberOfIterations];
						}
						
						// for new ME OR new activity: change emissions
						if(!lastME.equals(me2)||!lastActivity.equals(activity)){
							// DAY/NIGHT BACKGROUND
							// night: no background emission rate
							if((activity.equals("sleeping")||activity.contains("resting")||activity.contains("relaxing"))&&params.getParameters().contains("s_bgr")){
								s_bgr = new double[numberOfIterations];
							}
							// day
							else if(params.getParameters().contains("s_bgr")){
								s_bgr = sampling(params.getParameterValue("s_bgr"), numberOfIterations, c, minInterval/60d);												
							}	
							
							// HUMAN ACTIVITY
							// if people walk around
							if((activity.equals("getting up")||activity.equals("making bed")||activity.contains("cleaning")||
									activity.equals("laundry")||activity.equals("sports")||activity.equals("dressing")||activity.contains("walk")||
									(!lastME.equals(me2))&&i>0)&&params.getParameters().contains("s_human")){
								// person emissions are in µg/(person*min)!
								s_hum = sampling(params.getParameterValue("s_human"), numberOfIterations, c, 2);
							}else{
								s_hum = new double[numberOfIterations];
							}
							
							// COOKING
							// add cooking emissions if they occurred
							if(activity.equals("cooking")&&params.getParameters().contains("s_cook")){
								// cooking emissions are in µg/min!
								s_cook = sampling(params.getParameterValue("s_cook"), numberOfIterations, c, minInterval);
							}else{
								s_cook = new double[numberOfIterations];
							}
						
							lastActivity = activity;
						}
						
						// SMOKING
						// add smoking emissions
						smoker.replace(">", "");
						smoker.replace("<", "");
						try{
							int nSmoker = Integer.parseInt(smoker);
							if(nSmoker>0){
								// smoking emissions are in µg/cig!
								s_cig = sampling(params.getParameterValue("s_cig"), numberOfIterations, c, nSmoker*minInterval/10d);
							//	double[] n_cig = lognormalSampling((LogNormalDistribution)params.getParameterValue("n_cig"), numberOfIterations, c);
							}else{
								s_cig = new double[numberOfIterations];
							}
						}catch(NumberFormatException e){
							
						}
							
						//TODO add biomass burning activity
						// NO OF PERSONS			
						
						// WINDOW OPEN
						if(!lastME.equals(me2)||!lastWindowOpen.equals(windowOpen)){							
							if(windowOpen.equals("1")&&params.getParameters().contains("a_aer")){
								// sample from the aerating parameter
								a = sampling(params.getParameterValue("a_aer"), numberOfIterations, c, minInterval/60d);
							}
							else{
								a = sampling(params.getParameterValue("a"), numberOfIterations, c, minInterval/60d);
							}
							lastWindowOpen = windowOpen;
						} 
								
						// if only outdoor contribution should be modelled
						if(!useIndoorSources){
							s_bgr = new double[numberOfIterations];
							s_hum = new double[numberOfIterations];
							s_cook = new double[numberOfIterations];
							s_cig = new double[numberOfIterations];
						}
						
						// (B) check if this is a new me to decide which model to use
						if(!lastME.equals(me2)){						
							// use static model
							double[] c_in = staticModel(a, k, p, v, Arrays.asList(s_bgr, s_hum, s_cook, s_cig), c_out);
							
							// add concentration to profile
							profile.setInConcRealisations(i, c_in);
							
							lastME = me2;			
						}else{
							// use dynamic model
							double[] c_in_t0 = profile.getInConcRealisations(i-1);
							int t = 1;
							while(c_in_t0==null||c_in_t0.length==0){
								t++;
								c_in_t0 = profile.getInConcRealisations(i-t);
							}
							double[] c_in = dynamicModel(a, k, p, v, Arrays.asList(s_bgr, s_hum, s_cook, s_cig), c_out, c_in_t0);
							
							// add concentration to profile
							profile.setInConcRealisations(i, c_in);
						}	
					}	
					
				}
					
	
			//	System.out.println("Run "+i);
			}
			
			
		}catch (Exception e) {
			LOGGER.debug("Error while running indoor model: "
					+ e.getMessage());
			e.printStackTrace();
		throw new RuntimeException(
				"Error while running indoor model: "
						+ e.getMessage(), e);
		} 
		finally {
			if (c != null) {
				c.close();
			}
		}
		
		return profile;
	}
	
	
	/*
	 * Sampling
	 */
	private double[] sampling(IUncertainty dist, int numberOfSamples, ExtendedRConnection c, double proportion) throws RProcessException, REXPMismatchException{
		if(dist instanceof LogNormalDistribution){
			return lognormalSampling((LogNormalDistribution)dist, numberOfSamples, c, proportion);
		}else if(dist instanceof NormalDistribution){
			return normalSampling((NormalDistribution)dist, numberOfSamples, c, proportion);
		}else{
			return null;
		}
	}
	
	private double[] sampling(IUncertainty dist, int numberOfSamples, ExtendedRConnection c) throws RProcessException, REXPMismatchException{
		if(dist instanceof LogNormalDistribution){
			return lognormalSampling((LogNormalDistribution)dist, numberOfSamples, c);
		}else if(dist instanceof NormalDistribution){
			return normalSampling((NormalDistribution)dist, numberOfSamples, c);
		}else{
			return null;
		}
	}
	
	private double[] lognormalSampling(LogNormalDistribution dist, int numberOfSamples, ExtendedRConnection c, double proportion) throws RProcessException, REXPMismatchException{		
		String cmd = "rlnorm("+numberOfSamples+","+dist.getLogScale().get(0)+",sqrt("+dist.getShape().get(0)+"))*"+proportion;
		REXP res = c.tryEval(cmd);
		return res.asDoubles();
	}
	
	private double[] lognormalSampling(LogNormalDistribution dist, int numberOfSamples, ExtendedRConnection c) throws RProcessException, REXPMismatchException{
		String cmd = "rlnorm("+numberOfSamples+","+dist.getLogScale().get(0)+",sqrt("+dist.getShape().get(0)+"))";
		REXP res = c.tryEval(cmd);
		return res.asDoubles();
	}

	private double[] normalSampling(NormalDistribution dist, int numberOfSamples, ExtendedRConnection c, double proportion) throws RProcessException, REXPMismatchException{		
		String cmd = "rnorm("+numberOfSamples+","+dist.getMean().get(0)+",sqrt("+dist.getVariance().get(0)+"))*"+proportion;
		REXP res = c.tryEval(cmd);
		return res.asDoubles();
	}
	
	private double[] normalSampling(NormalDistribution dist, int numberOfSamples, ExtendedRConnection c) throws RProcessException, REXPMismatchException{
		String cmd = "rnorm("+numberOfSamples+","+dist.getMean().get(0)+",sqrt("+dist.getVariance().get(0)+"))";
		REXP res = c.tryEval(cmd);
		return res.asDoubles();
	}
	
	
	/*
	 * Model equations
	 */
	private double[] staticModel(double[] a, double[] k, double[] p, double[] v, List<double[]> s, double[] c_out) {
		double[] c_in_t0 = new double[a.length*c_out.length];
		for(int i=0; i<a.length; i++){
			double si = 0;
			for(double[] source : s){
				si += source[i];
			}
			for(int j=0; j<c_out.length; j++){
				c_in_t0[i*c_out.length+j] = (c_out[j]*p[i]*a[i] +si/v[i])/(a[i] + k[i]);
			}			
		}
		//c_{in}(t_0) = (c_{out}(t_0)pa +\frac{\sum^{n}_{i=1} s_{i}(t_0)}{v})/(a + k)
		return c_in_t0;
	}
	
	private double[] dynamicModel(double[] a, double[] k, double[] p, double[] v, List<double[]> s, double[] c_out, double[] c_in_t0){
		double[] c_in_ti = null;
		try{
			c_in_ti = new double[a.length*c_out.length];
			for(int i=0; i<a.length; i++){
				double si = 0;
				for(double[] source : s){
					si += source[i];
				}
				for(int j=0; j<c_out.length; j++){
					c_in_ti[i*c_out.length+j] = c_in_t0[i*c_out.length+j] + c_out[j]*p[i]*a[i] + si/v[i] - c_in_t0[i*c_out.length+j]*(a[i] + k[i]);
				}			
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//c_{in}(t_i) = c_{in}(t_{i-1}) +  c_{out}(t_i)pa + \frac{\sum^{n}_{i=1} s_{i}(t_i)}{v}-c_{in}(t_{i-1})(a + k)
		return c_in_ti;
	}
	
	
	private double[] staticModelIndoorContribution(double[] a, double[] k,  double[] v, double[] s){
		double[] c_in_t0 = new double[a.length];
		for(int i=0; i<a.length; i++){
			c_in_t0[i] = (s[i]/v[i])/(a[i] + k[i]);
						
		}
		//c_{in|sources}(t_0) = \frac{\sum^{n}_{i=1} s_{i}(t_0)}{v(a + k)}
		return c_in_t0;
	}
	
	private double[] staticModelOutdoorContribution(double[] a, double[] k, double[] p, double[] c_out){
		double[] c_in_t0 = new double[a.length*c_out.length];
		for(int i=0; i<a.length; i++){
			for(int j=0; j<c_out.length; j++){
				c_in_t0[i*c_out.length+j] = (c_out[j]*p[i]*a[i])/(a[i] + k[i]);
			}			
		}
		//c_{in|out}(t_0) = \frac{c_{out}(t_0)pa}{a + k}
		return c_in_t0;
	}
	
	private double[] dynamicModelIndoorContribution(double[] a, double[] k,  double[] v, double[] s, double[] c_inSources_t0){
		double[] c_in_ti = new double[a.length];
		for(int i=0; i<a.length; i++){
			c_in_ti[i] = c_inSources_t0[i]  + s[i]/v[i] -  c_inSources_t0[i]*(a[i] + k[i]);		
		}
		//c_{in|sources}(t_i) = c_{in|sources}(t_{i-1}) + \frac{\sum^{n}_{i=1} s_{i}(t_i)}{v} - c_{in|sources}(t_{i-1})(a + k)
		return c_in_ti;
	}
	
	private void dynamicModelOutdoorContribution(double[] a, double[] k, double[] p, double[] c_out, double[] c_inOut_t0){
		double[] c_in_ti = new double[a.length*c_out.length];
		for(int i=0; i<a.length; i++){
			for(int j=0; j<c_out.length; j++){
				c_in_ti[i*c_out.length+j] = c_inOut_t0[i] + c_out[j]*p[i]*a[i] -  c_inOut_t0[i]*(a[i] + k[i]);
			}			
		}
		//c_{in|out}(t_i) = c_{in|out}(t_{i-1})+c_{out}(t_i)pa - c_{in|out}(t_{i-1})(a + k)
	}
	
	/*
	 * Model parameters
	 */
	public void readParametersFile(String countryID, String pollutantID,  String path){
		microenvParams = new HashMap<String, IndoorModelParameters>();
		try{
			CSVReader reader = new CSVReader(new FileReader(path));
			// for the first row fill the header	
			String[] header = reader.readNext();
			HashMap<String, Integer> columnIndices = new HashMap<String, Integer>();
			for(int i=0; i<header.length; i++){
				columnIndices.put(header[i], i);
			}
			
			String[] line;	
					
			// loop through lines
			while ((line = reader.readNext()) != null) {
				// only add parameters for the requested country
				if(line[columnIndices.get("country_id")].equals(countryID)&&line[columnIndices.get("pollutant_id")].equals(pollutantID)){
					String me = line[columnIndices.get("me")];
					
					// if the microenvironment is new, add new IndoorParameters to HashMap
					if(!microenvParams.containsKey(me)){
						IndoorModelParameters indoorParams = new IndoorModelParameters(me);
						microenvParams.put(me, indoorParams);
					}
					
					// add parameter information to IndoorParameters
					microenvParams.get(me).setParameterValue(line[columnIndices.get("parameter_id")], 
							 line[columnIndices.get("distribution_parameter")], line[columnIndices.get("distribution")],
							 Double.parseDouble(line[columnIndices.get("value")]));
				}
			   			   
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
