package org.uncertweb.ems;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import net.opengis.om.x20.impl.OMBooleanObservationCollectionDocumentImpl;
import net.opengis.om.x20.impl.OMCategoryObservationDocumentImpl;
import net.opengis.om.x20.impl.OMMeasurementCollectionDocumentImpl;
import net.opengis.om.x20.impl.OMTextObservationCollectionDocumentImpl;
import net.opengis.om.x20.impl.OMUncertaintyObservationCollectionDocumentImpl;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.CSVEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.ems.activityprofiles.Profile;
import org.uncertweb.ems.exposuremodelling.IndoorModel;
import org.uncertweb.ems.exposuremodelling.OutdoorModel;
import org.uncertweb.ems.util.Utils;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Class to test methodology of the service
 * @author Lydia Gerharz
 *
 */
public class EMSalgorithmControl {

	private static String resourcesPath = "C:/WebResources/EMS";
	//load("D:/PhD/WP3_ExposureModelling/Processing_scripts/overlay/overlay.RData")
	
	public EMSalgorithmControl(){
		
	}
	
	public static void main(String[] args) {
		EMSalgorithmControl control = new EMSalgorithmControl();
		control.run();

	}

	public void run(){		
		//"p1","p2","p4","p5","p6","p7","p9","p10","p12","p13"
		//"2010-03-18","2010-03-22","2010-03-26","2010-03-28","2010-03-30","2010-04-01","2010-04-05","2010-04-07","2010-04-11","2010-04-13"
		String[] profiles = {"p1","p2","p4","p5","p6","p7","p9","p10","p12","p13"};
		String[] dates = {"2010-03-18","2010-03-22","2010-03-26","2010-03-28","2010-03-30","2010-04-01","2010-04-05","2010-04-07","2010-04-11","2010-04-13"};
		
		
		/*
		 *  1) get input data
		 */
		for(int p=0; p<profiles.length; p++){
			System.out.println("Started exposure modelling for profile: "+profiles[p]);
			
			String parameter = "PM2_5";
			//String parameter = "PM10";
			String ncFile = "C:/WebResources/AQMS/outputs/"+parameter+"/aqms_"+dates[p]+".nc";
			String nctempFile = ncFile;
//			String nctempFile = "C:/WebResources/EMS/outdoorModel/aq.nc";
			String omFile = resourcesPath+"/profiles/profile_"+profiles[p]+".xml";
			int cospIterations = 100;
			int indoorIterations = 500;
			int minuteResolution = 5;
			boolean useIndoorSources = true;
			
			
			// air quality data
//			NetcdfUWFile aq = null;
//			try {
//		  		NetcdfFile ncfile = NetcdfFile.open(ncFile);
//		  		aq = new NetcdfUWFile(ncfile);
//		  		parameter = writeNetCDFfile(aq, nctempFile);
//		  	} catch (IOException ioe) {
//		  		System.out.println("trying to open " + ""+ " " + ioe);
//		  	} catch (NetcdfUWException e) {
//				e.printStackTrace();
//			} 
			
			// activity profile
			//TODO: implement realisations handling with UncertML doc containing href to realisation documents (Albatross data)
			//TODO: implement handling of more than one individual in the collection?
			Profile profile = new Profile(Utils.readObsColl(omFile));
			System.out.println("Created Profile.");
			
			/*
			 *  2) perform outdoor overlay
			 */
			OutdoorModel outdoor = new OutdoorModel(resourcesPath);
			
			// A) for the moment, do the overlay for GPS tracks in MS with the local version			
			// write NetCDF file locally			
			
			// write observation geometry file locally
			profile.writeObsCollGeometry2csv(resourcesPath+"/profiles/overlay/"+profiles[p]+".csv");
			
			// get outdoor concentration at profile locations
			outdoor.performOutdoorOverlay(profile, nctempFile, resourcesPath+"/profiles/overlay/"+profiles[p]+".csv",parameter);
			System.out.println("Finished outdoor model overlay.");
			
			// estimate COSP uncertainties for PM10
			if(parameter.equals("PM10")){
				outdoor.estimateCOSPUncertainty(profile, resourcesPath+"/profiles/overlay/"+profiles[p]+".csv", cospIterations);
				System.out.println("Finished outdoor model COSP estimation.");
			}
						
			// perform averaging of profile observations
			profile.aggregateProfile(minuteResolution);
			
			
			// B) TODO: later use STAS for overlay of Albatross data				
			
			
			/*
			 *  3) perform indoor model if activity data is available
			 */
			// create indoor model with parameters
			IndoorModel indoor = new IndoorModel();
			indoor.readParametersFile("DE", parameter.replace("_", "."), resourcesPath+"/indoorModel/parameters.csv");
			
			// estimate indoor concentration
			indoor.runModel(profile, indoorIterations, minuteResolution, useIndoorSources);
			System.out.println("Finished indoor model calculation.");
			
			/*
			 *  4) prepare result
			 */			
			// store csv files with exposure results to be used in R
			if(useIndoorSources){
				profile.writeObsCollRealisations2csv(resourcesPath+"/outputs/exposure_"+parameter+"_"+profiles[p]+".csv");			
			}else{
				profile.writeObsCollRealisations2csv(resourcesPath+"/outputs/exposure_"+parameter+"_out_"+profiles[p]+".csv");			
			}
			
			// get OM file
			IObservationCollection exposureProfile = profile.getExposureProfileObservationCollection("lognormal");
				
			// create a specific collection for visualisation
			//IObservationCollection 
			// loop through observations and set time to one time step
			
			// store OM file
			if(useIndoorSources){
				Utils.writeObsCollXML(exposureProfile, resourcesPath+"/outputs/exposure_"+parameter+"_"+profiles[p]+".xml");
				Utils.writeObsCollJSON(exposureProfile, resourcesPath+"/outputs/exposure_"+parameter+"_"+profiles[p]+".json");
			}else{
				Utils.writeObsCollXML(exposureProfile, resourcesPath+"/outputs/exposure_"+parameter+"_out_"+profiles[p]+".xml");
				Utils.writeObsCollJSON(exposureProfile, resourcesPath+"/outputs/exposure_"+parameter+"_out_"+profiles[p]+".json");
			}
			
			// store OM JSON file
			
			
			System.out.println("Finished exposure modelling.");
			System.out.println("---------------------");
		}
	}
	
	
	private String writeNetCDFfile(NetcdfUWFile aqNCfile, String filepath){
		String mainVariable = "";
		
		try{
			// get main variable as parameter
			mainVariable = aqNCfile.getPrimaryVariable().getName();
						
			//new NetCDF file
			NetcdfFileWriteable ncFile = NetcdfUWFileWriteable.createNew(
					filepath, true);
			NetcdfUWFileWriteable ncUWfile = new NetcdfUWFileWriteable(ncFile);
			
			// write attributes
			// not necessary here
			
			// add dimensions
			ncUWfile.getNetcdfFileWritable().setRedefineMode(false);
			List<Dimension> dims = aqNCfile.getNetcdfFile().getDimensions();
			for (Dimension d : dims){
				ncUWfile.getNetcdfFileWritable().addDimension(null, d);
			}

			// write variables
			List<Variable> varIter = aqNCfile.getNetcdfFile().getVariables();
			for (Variable var : varIter) {
				// add variable
				ncFile.setRedefineMode(true);
				ncFile.addVariable(null, var);
				// write variable
				ncFile.setRedefineMode(false);
				ncFile.write(var.getName(), var.read());
			}
			
			// write data
			ncUWfile.getNetcdfFileWritable().setRedefineMode(false);
			
			// close file
			ncUWfile.getNetcdfFile().close();
		}catch (IOException ioe) {
	  		System.out.println("trying to open " + ""+ " " + ioe);
	  	} catch (NetcdfUWException e) {
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		
		
		return mainVariable;
	}
}
