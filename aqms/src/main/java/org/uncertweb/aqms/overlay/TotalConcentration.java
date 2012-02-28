package org.uncertweb.aqms.overlay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.XMLParser;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.ISample;
import org.uncertml.sample.RandomSample;
import org.uncertml.statistic.CredibleInterval;
import org.uncertml.statistic.IStatistic;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Quantile;
import org.uncertml.statistic.StandardDeviation;
import org.uncertml.statistic.StatisticCollection;
import org.uncertml.x20.StatisticsCollectionDocument.StatisticsCollection;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.aqms.interpolation.PolygonKriging;
import org.uncertweb.aqms.util.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

public class TotalConcentration {
 
	private static Logger LOGGER = Logger.getLogger(PolygonKriging.class);
	// NetCDF variables	
		private final static String TIME_VAR_NAME = "time";
		private final static String OLD_MAIN_VAR_NAME = "PM10_Austal2000";
		private final static String MAIN_VAR_NAME = "PM10";
		private final static String OLD_MAIN_VAR_LONG_NAME = "Particulate matter smaller 10 um diameter";
		private final static String X_VAR_NAME = "x";
		private final static String Y_VAR_NAME = "y";
		private final static String UNITS_ATTR_NAME = "units";
		private final static String MISSING_VALUE_ATTR_NAME = "missing_value";
		private final static String LONG_NAME_ATTR_NAME = "long_name";
		//private final static String GRID_MAPPING_VAR_NAME = "gauss_krueger_3";
		private final static String GRID_MAPPING_VAR_NAME = "crs";
		private final static String GRID_MAPPING_ATTR_NAME = "grid_mapping";	
		private final static String MAIN_VAR_UNITS = "ug m-3";
		
		private List<String> statParams;
		
		private static Logger logger = Logger.getLogger(TotalConcentration.class);
		
		private String utsAddress = "";
	//	private String resPath = "D:\\JavaProjects\\aqMS-wps\\src\\main\\resources";
	//	private int numberOfRealisations;
		private DateTime startDate, endDate;
		private boolean bg = false;
		private boolean austal = false;
		WPSClientSession session = null;
		
		
	public TotalConcentration(DateTime start, DateTime end){
		startDate = start;
		endDate = end;
	}
	
	public TotalConcentration(String utsURL, DateTime start, DateTime end){
		startDate = start;
		endDate = end;
		utsAddress = utsURL;
		
		// connect to UTS
		session = WPSClientSession.getInstance();
		try {
			session.connect(utsAddress);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
		}		
	}
	
	/*
	 * METHODS FOR OM
	 */
	
	// Method for OM
		public UncertaintyObservationCollection overlayOM(UncertaintyObservationCollection uColl, UncertaintyObservationCollection austalColl, int numberOfBackgroundSamples, List<String> stats){	
			statParams = stats;
			
			// get samples for background
			//HashMap<Integer, Double[]>  samplesTS = getBackgroundSamplesTS(uColl, numberOfRealisations);
			HashMap<Integer, Double[]>  samplesTS = callUTS4Samples(uColl, numberOfBackgroundSamples);
			
			// add samples to NetCDF realisations of Austal
			UncertaintyObservationCollection resultColl = addSamples2RealisationsOM(austalColl, samplesTS, numberOfBackgroundSamples);
			
			return resultColl;
		}
	
	// Method for OM
		private UncertaintyObservationCollection addSamples2RealisationsOM(UncertaintyObservationCollection austalColl, HashMap<Integer, Double[]> samplesTS, int numberOfRealisations){
			UncertaintyObservationCollection resultColl = new UncertaintyObservationCollection();
			UncertaintyObservationCollection realisationColl = new UncertaintyObservationCollection();
			ExtendedRConnection c = null;	
			DateTime startTemp = startDate.minusHours(1);
			boolean useUTS = true;
//			HashMap<String,HashMap<String, Double[]>> receptorPointResults = new HashMap<String,HashMap<String, Double[]>>();

			try {		
					// establish connection to Rserve running on localhost
					c = new ExtendedRConnection("127.0.0.1");
					if (c.needLogin()) {
						// if server requires authentication, send one
						c.login("rserve", "aI2)Jad$%");
					}
					
					int hours = -1;
					URI procedure = new URI("http://www.uncertweb.org/models/aqms");
					URI observedProperty = new URI("http://www.uncertweb.org/phenomenon/pm10");
					// loop through observation collection
					for (AbstractObservation obs : austalColl.getObservations()) {  		
						if(obs instanceof UncertaintyObservation){
							// get result and information from observation
							UncertaintyResult uResult = (UncertaintyResult) obs.getResult();
							IUncertainty uncertainty = uResult.getUncertaintyValue();
							SpatialSamplingFeature tmpSF = obs.getFeatureOfInterest();
							DateTime dateTime = obs.getPhenomenonTime().getDateTime();
							if(dateTime.isAfter(startTemp)&&!dateTime.isAfter(endDate))
								hours = Hours.hoursBetween(startTemp, dateTime).getHours();
							else
								hours = -1;
							
							// only continue if data is after startDate
							if(hours>0){
								// first check if samples are available for background
								Double[] bgSamples = samplesTS.get(hours);	
								if(bgSamples==null){
									bgSamples = new Double[numberOfRealisations];
								}
								
								// get Austal samples for this observation
								ContinuousRealisation real = null;						
								if(uncertainty instanceof ContinuousRealisation){	
									real = (ContinuousRealisation) uncertainty;
								}else if(uncertainty instanceof ISample){
									AbstractSample sample = (AbstractSample) uncertainty;
									real = (ContinuousRealisation) sample.getRealisations().get(0);	
								}
								
								if(real!=null){
									List<Double> austalSamples = real.getValues();
									
									// check if UTS should be used or if sample size is too large
									//TODO: Adapt thresholds?
									if(useUTS){
										int sampleSize = austalSamples.size()*bgSamples.length;
										int tsSize = austalColl.getObservations().size();
										if(sampleSize>500||tsSize>500)
											useUTS = false;
									}
									
									// create total concentration
									double[] newRealisations = new double[austalSamples.size()*bgSamples.length];
									// add values and calculate mean
									for(int rA=0; rA<austalSamples.size(); rA++){
										for(int rB=0; rB<bgSamples.length; rB++){
//											if(bg)
//												newRealisations[count] = bgSamples[rB];
//											else if(austal)
//												newRealisations[count] = austalSamples.get(rA);
//											else
											try{
												if(bgSamples[rB]==null)
													bgSamples[rB] = 0.0;
												newRealisations[rA*bgSamples.length+rB] = austalSamples.get(rA) + bgSamples[rB];
											}catch(Exception e){
												e.printStackTrace();
											}
										}
									}									
									// add results to HashMap
//									String fid = tmpSF.getIdentifier().getIdentifier();									
									
									TimeObject newT = new TimeObject(dateTime);
									
									if(!useUTS){
										REXPDouble d = new REXPDouble(newRealisations);
										c.assign("samples", d);	
										// write samples to workspace
										//c.tryVoidEval("save(samples, file=)");
										double mean =  c.tryEval("mean(samples)").asDouble();
										double sd =  c.tryEval("sd(samples)").asDouble();
										double[] ci95 = c.tryEval("quantile(samples,c(0.025,0.975))").asDoubles();	
										
										// create results									
										Mean m = new Mean(mean);								
										StandardDeviation s = new StandardDeviation(sd);
										CredibleInterval ci = new CredibleInterval(new Quantile(0.025, ci95[0]), new Quantile(0.975, ci95[1]));
										
										StatisticCollection statColl = new StatisticCollection();
										statColl.add(m);
										statColl.add(s);
										statColl.add(ci);									
										
										UncertaintyResult newResult = new UncertaintyResult(statColl, "ug/m3");
										UncertaintyObservation uObs = new UncertaintyObservation(
												newT, newT, procedure, observedProperty, tmpSF, newResult);
										resultColl.addObservation(uObs);
									}else{
										ContinuousRealisation r = new ContinuousRealisation(newRealisations, -1.0d, "id");
										UncertaintyResult realisationResult = new UncertaintyResult(r, "ug/m3");
										UncertaintyObservation realisationObs = new UncertaintyObservation(
												newT, newT, procedure, observedProperty, tmpSF, realisationResult);
										realisationColl.addObservation(realisationObs);
									}
									
//									if(receptorPointResults.containsKey(fid)){
////										receptorPointResults.get(fid).put(dateTime.toString(ISODateTimeFormat.dateTime()), newRealisations);
//										c.tryVoidEval(fid+"_"+startDate.toString("yyyy_MM")+"<-rbind("+fid+"_"+startDate.toString("yyyy_MM")+",samples)");
//										c.tryVoidEval("dates <- c(dates, \""+dateTime.toString(ISODateTimeFormat.dateTime())+"\")");
////										
//									}else{
//										//"AQMS_bg_"+startDate.toString("yyyy-MM")+"_"+fid
//										receptorPointResults.put(fid, null);
//										String cmd = fid+"_"+startDate.toString("yyyy_MM")+"<- NULL";
//										c.tryVoidEval(cmd);
//										cmd = fid+"_"+startDate.toString("yyyy_MM")+"<-rbind("+fid+"_"+startDate.toString("yyyy_MM")+",samples)";
//										c.tryVoidEval(cmd);
//										c.tryVoidEval("dates <- \""+dateTime.toString(ISODateTimeFormat.dateTime())+"\"");
////										HashMap<String, double[]> realisations = new HashMap<String, double[]>();
////										realisations.put(dateTime.toString(ISODateTimeFormat.dateTime()), newRealisations);
////										receptorPointResults.put(fid, realisations);
//									}
																								
								}								
							}							
						}
					}
					// get statistics
					//TODO: Call UTS here
					if(useUTS)
						resultColl = this.callUTS4Statistics(realisationColl);
					
//					// write data.frames
//					Set<String> keys = receptorPointResults.keySet();
//					String cmd1 = "save(dates, file=\"D:/PhD/WP1.1_AirQualityModel/WebServiceChain/results/dates_"+startDate.toString("yyyy_MM")+".RData\")";
//					c.tryVoidEval(cmd1);
//					for(String fid : keys){
//						String cmd = "save("+fid+"_"+startDate.toString("yyyy_MM")+", file=\"D:/PhD/WP1.1_AirQualityModel/WebServiceChain/results/"+fid+"_"+startDate.toString("yyyy_MM")+".RData\")";
//						c.tryVoidEval(cmd);
//					}
					
				}catch (Exception e) {
					LOGGER
					.debug("Error while calculating Total Concentration: "
							+ e.getMessage());
					throw new RuntimeException(
					"Error while calculating Total Concentration "
							+ e.getMessage(), e);
				} 
				finally {
					if (c != null) {
						c.close();
					}
				}		
			
			return resultColl;
		}
		
	

	/*
	 * METHODS FOR NetCDF
	 */
		
	// Method for NetCDF
	public NetcdfUWFile overlayNetCDF(UncertaintyObservationCollection uColl, NetcdfUWFile austalNcdf, String filepath, int numberOfBackgroundSamples){	
		// get samples for background
		HashMap<Integer, Double[]>  samplesTS = getBackgroundSamplesTS(uColl, numberOfBackgroundSamples);
		
		// add samples to NetCDF realisations of Austal
		NetcdfUWFile resultNetcdf = addSamples2RealisationsNetCDF(austalNcdf, samplesTS, filepath, false);
		
		return resultNetcdf;
	}
	
	
	// Method for NetCDF
	private NetcdfUWFileWriteable addSamples2RealisationsNetCDF(NetcdfUWFile austalNcdf, HashMap<Integer, Double[]> samplesTS, String filepath, boolean keepRealisations){
		NetcdfUWFileWriteable resultNetcdf = null;
		ExtendedRConnection c = null;	
		
		int xi=0;
		int yi=0;
		int r=0;
		int t=0;
		try {		
			// establish connection to Rserve running on localhost
			//c = new ExtendedRConnection("giv-uw.uni-muenster.de");
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
			
			// prepare new netCDF file for realisations
			NetcdfFileWriteable resultFile = NetcdfUWFileWriteable.createNew(
					filepath, true);
			resultNetcdf = new NetcdfUWFileWriteable(resultFile);
			
			// get realisation and time dimension
			Dimension xDim = null;
			Dimension yDim = null;
			Dimension rDim = null;
			Dimension tDim = null;
			Iterator<Dimension> dimensions = austalNcdf.getNetcdfFile()
					.getDimensions().iterator();
			
			// add dimensions to result file
			while (dimensions.hasNext()) {
				Dimension dim = dimensions.next();
				
				if (dim.getName().equals("realisation")){
					rDim=dim;
				}else if (dim.getName().equals(TIME_VAR_NAME)){
					tDim=dim;
					if (!resultFile.isDefineMode()) {
						resultFile.setRedefineMode(true);
					}
					resultFile.addDimension(null, dim);
				}else if (dim.getName().equals(X_VAR_NAME)){
					xDim=dim;
					if (!resultFile.isDefineMode()) {
						resultFile.setRedefineMode(true);
					}
					resultFile.addDimension(null, dim);
				}else if (dim.getName().equals(Y_VAR_NAME)){
					yDim=dim;
					if (!resultFile.isDefineMode()) {
						resultFile.setRedefineMode(true);
					}
					resultFile.addDimension(null, dim);
				}
			}
			
			// Set dimensions for value variable		
			ArrayList<Dimension> dims = new ArrayList<Dimension>(3);
			dims.add(xDim);
			dims.add(yDim);
			dims.add(tDim);
			
			String TIME_VAR_UNITS = "";
			
			// Add old variables to resultfile
			Iterator<Variable> vars = austalNcdf.getNetcdfFile().getVariables().iterator();
			while (vars.hasNext()) {
				Variable var = vars.next();
				// add all variables except data and realisation variable
				if(!var.getName().equals(OLD_MAIN_VAR_NAME)&&!var.getName().equals("realisation")){
					resultFile.setRedefineMode(true);
					resultFile.addVariable(null, var);
					// for time get attribute
					if(var.getName().equals("time")){
						Attribute timeUnits = var.findAttribute(UNITS_ATTR_NAME);
						TIME_VAR_UNITS = timeUnits.getStringValue();
					}
					resultFile.setRedefineMode(false);					
					resultFile.write(var.getName(), var.read());
				}
			}
			
			// get start time
			//String TIME_VAR_UNITS = "hours since "+dateFormat.format(minDate)+ " 00:00";
//			DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss 00:00");
//			String substring1 = TIME_VAR_UNITS.replace("hours since ", "");
//			DateTime startDate = dateFormat.parseDateTime(substring1);					
			
			//TODO: Add handler for keepRealisations==true
			// add new variables to resultfile
			resultFile.setRedefineMode(false);
			Variable meanVar = resultNetcdf
					.addStatisticVariable(MAIN_VAR_NAME+"_mean", DataType.DOUBLE, dims, org.uncertml.statistic.Mean.class);
			Variable sdVar = resultNetcdf
					.addStatisticVariable(MAIN_VAR_NAME+"_standard-deviation", DataType.DOUBLE, dims, org.uncertml.statistic.StandardDeviation.class);
			Variable varVar = resultNetcdf
					.addStatisticVariable(MAIN_VAR_NAME+"_variance", DataType.DOUBLE, dims, org.uncertml.statistic.Variance.class);
			
			// add Attributes to variables
			meanVar.addAttribute(new Attribute(UNITS_ATTR_NAME, MAIN_VAR_UNITS));
			meanVar.addAttribute(new Attribute(MISSING_VALUE_ATTR_NAME, -9999F));
			meanVar.addAttribute(new Attribute(LONG_NAME_ATTR_NAME, OLD_MAIN_VAR_LONG_NAME));
			meanVar.addAttribute(new Attribute(GRID_MAPPING_ATTR_NAME, GRID_MAPPING_VAR_NAME));
			
			sdVar.addAttribute(new Attribute(UNITS_ATTR_NAME, MAIN_VAR_UNITS));
			sdVar.addAttribute(new Attribute(MISSING_VALUE_ATTR_NAME, -9999F));
			sdVar.addAttribute(new Attribute(GRID_MAPPING_ATTR_NAME, GRID_MAPPING_VAR_NAME));
			
			varVar.addAttribute(new Attribute(UNITS_ATTR_NAME, MAIN_VAR_UNITS+"^2"));
			varVar.addAttribute(new Attribute(MISSING_VALUE_ATTR_NAME, -9999F));
			varVar.addAttribute(new Attribute(GRID_MAPPING_ATTR_NAME, GRID_MAPPING_VAR_NAME));
			
			resultNetcdf.setPrimaryVariable(meanVar);
					
			// get data variable
			Variable dataVariable = austalNcdf.getPrimaryVariable();
			Array samplesArray = dataVariable.read();
			ArrayDouble meanArray = new ArrayDouble.D3(xDim.getLength(), yDim.getLength(),tDim.getLength());
			ArrayDouble sdArray = new ArrayDouble.D3(xDim.getLength(), yDim.getLength(),tDim.getLength());
			ArrayDouble varArray = new ArrayDouble.D3(xDim.getLength(), yDim.getLength(),tDim.getLength());		
			
			Index samplesIndex = samplesArray.getIndex();
			Index statsIndex = meanArray.getIndex();
			
			// loop through cells
			for (xi = 0; xi < xDim.getLength(); xi++) {
				for (yi = 0; yi < yDim.getLength(); yi++) {
					
					// get time series of realisations
					for(t=0; t<tDim.getLength(); t++){
						// get actual time
						//DateTime date = startDate.plusHours(t+1);
						
						// collect new realisations
						double[] newRealisations = new double[rDim.getLength()*samplesTS.get(samplesTS.keySet().toArray()[0]).length];
						int count = 0;
						
						// for each time step, loop through realisations
						for (r=0; r<rDim.getLength(); r++){					
							// get Austal result
							samplesIndex.set(r, xi, yi, t);
							double austalValue = samplesArray.getDouble(samplesIndex);
							
							// loop through Background samples
							for(int s=0; s<samplesTS.get(samplesTS.keySet().toArray()[0]).length; s++){
								
								// check if value is available for current date
								if(samplesTS.containsKey(t+1)){
									double backgroundValue = samplesTS.get(t+1)[s];
									newRealisations[count] = backgroundValue + austalValue;
									count++;
								}else{
								//	System.out.println(date.toString());
								}							
							}
						}
						// get statistics from new realisations and fill new variables
						REXPDouble d = new REXPDouble(newRealisations);
						c.assign("samples", d);
						statsIndex.set(xi,yi,t);
						REXP mean =  c.tryEval("mean(samples)");
						meanArray.setDouble(statsIndex, mean.asDouble());
						REXP sd =  c.tryEval("sd(samples)");
						sdArray.setDouble(statsIndex, sd.asDouble());
						REXP var =  c.tryEval("var(samples)");
						varArray.setDouble(statsIndex, var.asDouble());
					}
				}
			}
			
			// add CF conventions
			resultNetcdf.getNetcdfFileWritable().setRedefineMode(true);
			Attribute conventions = resultNetcdf.getNetcdfFileWritable().findGlobalAttribute("Conventions");
			String newValue =  conventions.getStringValue() + " CF-1.5";
			resultNetcdf.getNetcdfFileWritable().deleteGlobalAttribute("Conventions");
			resultNetcdf.getNetcdfFileWritable().addGlobalAttribute("Conventions", newValue);
					
			//write result array to NetCDF file
			resultNetcdf.getNetcdfFileWritable().setRedefineMode(false);
			resultNetcdf.getNetcdfFileWritable().write(MAIN_VAR_NAME+"_mean",meanArray);
			resultNetcdf.getNetcdfFileWritable().write(MAIN_VAR_NAME+"_standard-deviation",sdArray);
			resultNetcdf.getNetcdfFileWritable().write(MAIN_VAR_NAME+"_variance",varArray);			
			resultNetcdf.getNetcdfFile().close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("y: "+yi+", x: "+xi+", t: "+t);
		}
		
		return resultNetcdf;
	}
		
	
	/*
	 * SAMPLING METHODS
	 */
	
			// Method to get samples from background time series
			// advantage: RServe is called only once
			private HashMap<Integer, Double[]> getBackgroundSamplesTS(UncertaintyObservationCollection uColl, int numberOfRealisations){
				 HashMap<Integer, Double[]>  samplesTS = new  HashMap<Integer, Double[]>();
				 ExtendedRConnection c = null;	
				 
					try {		
						// establish connection to Rserve running on localhost
						c = new ExtendedRConnection("127.0.0.1");
						if (c.needLogin()) {
							// if server requires authentication, send one
							c.login("rserve", "aI2)Jad$%");
						}
						
						// loop through observation collection
						for (AbstractObservation obs : uColl.getObservations()) {  		
							if(obs instanceof UncertaintyObservation){
								UncertaintyResult uResult = (UncertaintyResult) obs.getResult();
								IUncertainty distribution = uResult.getUncertaintyValue();
									
								// get samples for this observation
								if(distribution instanceof NormalDistribution){
									NormalDistribution normDist = (NormalDistribution) distribution;
									List<Double> mean = normDist.getMean();
									List<Double> var = normDist.getVariance();
									
									// define parameters in R
									c.tryVoidEval("i <- " + numberOfRealisations);
									c.tryVoidEval("m.gauss <- " + mean.get(0));
									c.tryVoidEval("var.gauss <- " + var.get(0));
									c.tryVoidEval("sd.gauss <- sqrt(var.gauss)");		
						    		
									// perform sampling
									REXP rSamples =  c.tryEval("round(rnorm(i, m.gauss, sd.gauss),digits=2)");			
									double[] samples = rSamples.asDoubles();
									Double[] samplesD = new Double[samples.length];
									for(int i=0; i<samples.length; i++){
										samplesD[i] = (Double) samples[i];
									}
									
									// get sampling time
						    		DateTime d = obs.getResultTime().getDateTime();
						    		if(d.isAfter(startDate)){
						    			Hours h = Hours.hoursBetween(startDate, d);
						    			samplesTS.put(h.getHours(), samplesD);
						    		}
									
								}					
							}					
						}			
						
						
					}catch (Exception e) {
						LOGGER
						.debug("Error while getting random samples for Gaussian distribution: "
								+ e.getMessage());
						throw new RuntimeException(
						"Error while getting random samples for Gaussian distribution: "
								+ e.getMessage(), e);
					} 
					finally {
						if (c != null) {
							c.close();
						}
					}
				
				return samplesTS;
			}
	
			
			/**
			 * uses UTS to get samples from OM document with Normal Distribution observations
			 * @param uColl
			 * @param numberOfRealisations
			 * @return
			 */
			private  HashMap<Integer, Double[]>  callUTS4Samples(UncertaintyObservationCollection uColl, int numberOfRealisations){
				// to be returned
				HashMap<Integer, Double[]>  samples = new  HashMap<Integer, Double[]> ();
				IObservationCollection iobs = null;

				// add inputs for request
				Map<String, Object> inputs = new HashMap<String, Object>();
				inputs.put("distribution", uColl);
				inputs.put("numbReal", ""+numberOfRealisations);
				
				// Make execute request
				ExecuteDocument execDoc = null;
				try {
					execDoc = Utils.createExecuteDocumentManually(utsAddress, "org.uncertweb.wps.Gaussian2Samples", inputs, UncertWebDataConstants.MIME_TYPE_OMX_XML);
				} catch (Exception e) {
					logger.debug(e);
				}

				// Run WPS and get output (= Realisation object)
				ExecuteResponseDocument responseDoc = null;
				try {
					responseDoc = (ExecuteResponseDocument) session.execute(
						utsAddress, execDoc);
					
					OutputDataType oType = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray(0);
					// all output elements
					Node wpsComplexData = oType.getData().getComplexData().getDomNode();
					// the complex data node
					Node unRealisation = wpsComplexData.getChildNodes().item(0); 
					// the realisation node			 
					iobs = new XBObservationParser().parseObservationCollection(nodeToString(unRealisation));
			
				} catch (WPSClientException e) {// Auto-generated catch block
						e.printStackTrace();
				} catch (OMParsingException e) {
					e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				
				if(iobs!=null){
					// get samples from Collection
					for (AbstractObservation obs : iobs.getObservations()) {  		
						if(obs instanceof UncertaintyObservation){
							UncertaintyResult uResult = (UncertaintyResult) obs.getResult();
							IUncertainty uncertainty = uResult.getUncertaintyValue();
							
							ContinuousRealisation realisations = null;
							
							// get samples for this distribution
							if(uncertainty instanceof ISample){
								AbstractSample sample = (AbstractSample) uncertainty;
								realisations = (ContinuousRealisation) sample.getRealisations().get(0);		
							}else if(uncertainty instanceof ContinuousRealisation){
									realisations = (ContinuousRealisation) uncertainty;
							}
							if(realisations!=null){
								Double[] values = (Double[]) realisations.getValues().toArray(new Double[0]);

								// get sampling time
					    		DateTime d = obs.getResultTime().getDateTime();
					    		if(d.isAfter(startDate)){
					    			Hours h = Hours.hoursBetween(startDate, d);
					    			samples.put(h.getHours(), values);
					    		}
							}
						}
					}
				}
				return samples;
			}

			
			//TODO: Implement UTS statistics estimation
			/**
			 * uses UTS to get statistics for OM document with realisations as observations
			 * @param uColl
			 * @return
			 */
			private UncertaintyObservationCollection callUTS4Statistics(UncertaintyObservationCollection uColl){
				UncertaintyObservationCollection resultColl = null;			
				
				// add inputs for request
				Map<String, Object> inputs = new HashMap<String, Object>();
				inputs.put("samples", uColl);
				String[] stats = (String[]) statParams.toArray(new String[0]);
				inputs.put("statistics", stats);
				
				// Make execute request
				ExecuteDocument execDoc = null;
				try {
					execDoc = Utils.createExecuteDocumentManually(utsAddress, "org.uncertweb.wps.Samples2Statistics", inputs, UncertWebDataConstants.MIME_TYPE_OMX_XML);
				} catch (Exception e) {
					logger.debug(e);
				}
				
				// save result locally
				try {
					String filepath = "D:\\PhD\\WP1.1_AirQualityModel\\WebServiceChain\\results\\UTS_statistics_request.xml";
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(filepath)));
					out.write(execDoc.xmlText());
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// Run WPS and get output
				ExecuteResponseDocument responseDoc = null;
				try {
					responseDoc = (ExecuteResponseDocument) session.execute(
						utsAddress, execDoc);
					
					OutputDataType oType = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray(0);
					// all output elements
					Node wpsComplexData = oType.getData().getComplexData().getDomNode();
					// the complex data node
					Node unStatistics = wpsComplexData.getChildNodes().item(0); 
					// Observation Collection
					resultColl = (UncertaintyObservationCollection) new XBObservationParser().parseObservationCollection(nodeToString(unStatistics));
					
				} catch (WPSClientException e) {// Auto-generated catch block
						e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					e.printStackTrace();
				} catch (OMParsingException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				} 
				
				return resultColl;
			}
		
			//TODO: Implement UTS statistics estimation
			private StatisticCollection callUTS4Statistics(double[] realisations){
				StatisticCollection statColl = null;			
			
				// add inputs for request
				Map<String, Object> inputs = new HashMap<String, Object>();
				inputs.put("samples", new ContinuousRealisation(realisations));
				String[] stats = (String[]) statParams.toArray(new String[0]);
				inputs.put("statistics", stats);
				
				// Make execute request
				ExecuteDocument execDoc = null;
				try {
					execDoc = Utils.createExecuteDocumentManually(utsAddress, "org.uncertweb.wps.Samples2Statistics", inputs, UncertWebDataConstants.MIME_TYPE_UNCERTML_XML);
				} catch (Exception e) {
					logger.debug(e);
				}
				
//				// save result locally
//				try {
//					String filepath = "D:\\PhD\\WP1.1_AirQualityModel\\WebServiceChain\\results\\UTS_statistics_request.xml";
//					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
//							new FileOutputStream(filepath)));
//					out.write(execDoc.xmlText());
//					out.close();
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				
				// Run WPS and get output
				ExecuteResponseDocument responseDoc = null;
				try {
					responseDoc = (ExecuteResponseDocument) session.execute(
						utsAddress, execDoc);
					
					OutputDataType oType = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray(0);
					// all output elements
					Node wpsComplexData = oType.getData().getComplexData().getDomNode();
					// the complex data node
					Node unStatistics = wpsComplexData.getChildNodes().item(0); 
					//TODO check if this works or if parameters have to be parsed separately and assembled afterwards to StatisticCollection
					statColl = (StatisticCollection) new XMLParser().parse(unStatistics.toString());					
					
				} catch (WPSClientException e) {// Auto-generated catch block
						e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					e.printStackTrace();
				} catch (UncertaintyParserException e) {
					e.printStackTrace();
				}
				
				return statColl;
			}
			

			private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
				StringWriter stringWriter = new StringWriter();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
				
				return stringWriter.toString();
			}
}
