package org.uncertweb.aqms.austal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.uncertweb.api.om.converter.ShapeFileConverter;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.aqms.util.Utils;

/**
 * Class using the shp2om converter to create the OM documents with Austal2000 input distributions necessary for the UPS
 * @author l_gerh01
 *
 */
public class AustalInputs {

	private static String resourcesPath = "";
	private static String inputsPath = "";
	private static Logger LOGGER = Logger.getLogger(AustalInputs.class);
	private DateTime startDate, endDate;
//	public enum INPUTS{
//		c
//	};

	public AustalInputs(DateTime startDate, DateTime endDate, String resourcesPath){
		this.resourcesPath = resourcesPath.replace("\\", "/");
		this.inputsPath =  resourcesPath+"inputs\\";
		this.startDate = startDate;
		this.endDate = endDate;
		//TODO uncomment
		try {
			this.adaptPathInPropertiesFiles();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void prepareUncertainInputs(){
		//TODO uncomment
		// prepare emission data in R
		this.createUncertainEmissionFiles();
		this.createUncertainMeteorologyFiles();

		// run shp2om converter
		try {
			this.runSHP2OMConverter(resourcesPath+"largeStreets.props");
			this.runSHP2OMConverter(resourcesPath+"smallStreets.props");
			this.mergeEmisCollections(resourcesPath+"inputs\\streets.xml");

			this.runSHP2OMConverter(resourcesPath+"wd.props");
			this.runSHP2OMConverter(resourcesPath+"ws.props");

			// copy to inputs folder
//			UncertaintyObservationCollection wdColl = (UncertaintyObservationCollection) Utils.readObsColl(resourcesPath + "\\winddirection.xml");
//			Utils.writeObsColl(wdColl, austalInputsPath+"winddirection.xml");
//			UncertaintyObservationCollection wsColl = (UncertaintyObservationCollection) Utils.readObsColl(resourcesPath + "\\windsspeed.xml");
//			Utils.writeObsColl(wsColl, austalInputsPath+"windspeed.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void prepareUncertainInputsAsSamples(int NumberOfAustalRuns){
		//TODO implement sampling for meteorology and emissions here
	}

	public void prepareCertainInputs(){
		// prepare emission data in R
		this.createCertainInputFiles();

		// run shp2om converter
		try {
			// stability class
			this.runSHP2OMConverter(resourcesPath+"sc.props");
			// variable industry emissions
			this.runSHP2OMConverter(resourcesPath+"industry_variable.props");
			// variable offroad emissions
			this.runSHP2OMConverter(resourcesPath+"offroad.props");
			this.mergeVariableEmisCollections(resourcesPath+"inputs\\variableemissions.xml");
			// static industry emissions
			this.runSHP2OMConverter(resourcesPath+"industry_static.props");
			// static ship emissions
			this.runSHP2OMConverter(resourcesPath+"ship.props");
			// static railroad emissions
			this.runSHP2OMConverter(resourcesPath+"railroad.props");
			// static combustion emissions
			this.runSHP2OMConverter(resourcesPath+"combustion.props");
			this.mergeStaticEmisCollections(resourcesPath+"inputs\\staticemissions.xml");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void createCertainInputFiles(){
		ExtendedRConnection c = null;
		try {
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}

			// load dataframe with emission data
			String dfPath = resourcesPath + "staticInputs.RData";
			c.tryVoidEval("load(\""+dfPath+"\")");

			// 1) stability class
			// set variables
			String cmd = "start_date <- \""+startDate.toString("yyyy-MM-dd")+"\"";
			c.tryVoidEval(cmd);

			cmd = "end_date <- \""+endDate.toString("yyyy-MM-dd")+"\"";
			c.tryVoidEval(cmd);
			String start_day = startDate.toString("yyyy-MM-dd");
			int start_hour = Integer.parseInt(startDate.toString("HH"));
			String end_day = endDate.toString("yyyy-MM-dd");
			int end_hour = Integer.parseInt(endDate.toString("HH"));

			// get ids of start and end date
			c.tryVoidEval("days <- substring(as.character(stability$time),1,10)");
			c.tryVoidEval("hours <- as.numeric(substring(as.character(stability$time),12,13))");
			c.tryVoidEval("idStart <- which(days==\""+start_day+"\"&hours=="+start_hour+")");
			c.tryVoidEval("idEnd <- which(days==\""+end_day+"\"&hours=="+end_hour+")");

			// write csv file
			c.tryVoidEval("write.csv(stability[idStart:idEnd,], \""+resourcesPath+"stabilityclass.csv\", row.names=F)");

			// 2) emissions
			// prepare variables
			c.tryVoidEval("dayList <- dayTypeTS(start_date, end_date)");
			c.tryVoidEval("industry <- NULL");
			c.tryVoidEval("offroad <- NULL");
			c.tryVoidEval("ts <- NULL");

			// create new lists
			c.tryVoidEval("for(d in 1:nrow(dayList)){for(h in 1:24){ts <- c(ts, paste(dayList$date[d], \"T\", varEmis$time[h], \".000+01\", sep=\"\"))}}");
			c.tryVoidEval("for(d in 1:nrow(dayList)){if(dayList$dayType[d]==1){ industry <- rbind(industry, varEmis[varEmis$day==\"Weekday\",3:14]);offroad <- rbind(offroad, varEmis[varEmis$day==\"Weekday\",15:26])  }else{industry <- rbind(industry, varEmis[varEmis$day==\"Weekend\",3:14]);offroad <- rbind(offroad, varEmis[varEmis$day==\"Weekend\",15:26])}}");

			c.tryVoidEval("iTS <- NULL");
			c.tryVoidEval("for(c in 1:ncol(industry)){iTS <- c(iTS, industry[,c])}");
			c.tryVoidEval("industryTS <- data.frame(fid=rep(0:(ncol(industry)-1), each=length(ts)), time=rep(ts,ncol(industry)), EmisRealisation=iTS)");

			c.tryVoidEval("oTS <- NULL");
			c.tryVoidEval("for(c in 1:ncol(offroad)){oTS <- c(oTS, offroad[,c])}");
			c.tryVoidEval("offroadTS <- data.frame(fid=rep(0:(ncol(offroad)-1), each=length(ts)), time=rep(ts,ncol(offroad)), EmisRealisation=oTS)");

			c.tryVoidEval("write.csv(industryTS, file=\""+resourcesPath+"/industry.csv\", row.names=F)");
			c.tryVoidEval("write.csv(offroadTS, file=\""+resourcesPath+"/offroad.csv\", row.names=F)");

		}catch (Exception e) {
			LOGGER
			.debug("Error while preparing certain emission files: "
					+ e.getMessage());
			throw new RuntimeException(
			"Error while preparing certain emission files: "
					+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}
	}

	private void createUncertainEmissionFiles(){
		ExtendedRConnection c = null;
		UncertaintyObservationCollection uColl = null;
		try {
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}

			// load dataframe with emission data
			String dfPath = resourcesPath + "HE.RData";
			String cmd = "load(\""+dfPath+"\")";
			c.tryVoidEval(cmd);

			// set variables
			c.tryVoidEval("library(shapefiles)");
//			String start_date = startDate.toString("dd.MM.yyyy");
//			String end_date = endDate.toString("dd.MM.yyyy");
			cmd = "start_date <- \""+startDate.toString("dd.MM.yyyy")+"\"";
			c.tryVoidEval(cmd);
			if(endDate.getHourOfDay()==0)
				cmd = "end_date <- \""+endDate.minusDays(1).toString("dd.MM.yyyy")+"\"";
			else
				cmd = "end_date <- \""+endDate.toString("dd.MM.yyyy")+"\"";
			c.tryVoidEval(cmd);

			// 1) large streets
			// read street files
			cmd = "streets <- read.dbf(\""+resourcesPath+"shapefiles/large.dbf\")$dbf";
			c.tryVoidEval(cmd);
			cmd = "streetsize <- \"large\"";
			c.tryVoidEval(cmd);
		//	double[] largeDTVKM = c.tryEval("large$DTV_KM").asDoubles();

			// create tables with distributions
			c.tryVoidEval("df <- NULL");
			c.tryVoidEval("for (i in 1: nrow(streets)){street <- specificStreet(streetsize, streets$DTV_KM[i]);  ts <- specificTimeSeries(start_date, end_date, street);  df <- rbind(df, cbind(fid=rep(i-1, nrow(ts)), ts))}");

			// write results to csv file
			c.tryVoidEval("write.csv(df, \""+resourcesPath+"largeStreets_HE.csv\", row.names=F)");
//			write.csv(df, paste("D:/PhD/WP1.1_AirQualityModel/Traffic/traffic_sampling/shapes/",streetsize,"_HE_3days.csv", sep=""), row.names=F)

			// 2) small streets
			c.tryVoidEval("streets <- read.dbf(\""+resourcesPath+"shapefiles/small.dbf\")$dbf");
			// for small streets scale aadt*length up to total sum
			c.tryVoidEval("streets$DTV_KM <- streets$GRIDCODE/sum(streets$GRIDCODE)*99345");
			c.tryVoidEval("streetsize <- \"small\"");
			//double[] smallDTVKM = c.tryEval("small$DTV_KM").asDoubles();

			// create tables with distributions
			c.tryVoidEval("df <- NULL");
			c.tryVoidEval("for (i in 1: nrow(streets)){street <- specificStreet(streetsize, streets$DTV_KM[i]);  ts <- specificTimeSeries(start_date, end_date, street);  df <- rbind(df, cbind(fid=rep(i-1, nrow(ts)), ts))}");

			// write results to csv file
			c.tryVoidEval("write.csv(df, \""+resourcesPath+"smallStreets_HE.csv\", row.names=F)");

		}catch (Exception e) {
			LOGGER
			.debug("Error while preparing street emission files: "
					+ e.getMessage());
			throw new RuntimeException(
			"Error while preparing street emission files: "
					+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}
	}

	private void createUncertainMeteorologyFiles(){
		ExtendedRConnection c = null;
		UncertaintyObservationCollection uColl = null;
		try {
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}

			// load dataframe with emission data
			String dfPath = resourcesPath + "Meteo.RData";
			c.tryVoidEval("load(\""+dfPath+"\")");

			// set variables
			String start_day = startDate.toString("yyyy-MM-dd");
			int start_hour = Integer.parseInt(startDate.toString("HH"));
			String end_day = endDate.toString("yyyy-MM-dd");
			int end_hour = Integer.parseInt(endDate.toString("HH"));

			// get ids of start and end date
			c.tryVoidEval("idStart <- which(meteo$day==\""+start_day+"\"&meteo$hour=="+start_hour+")");
			c.tryVoidEval("idEnd <- which(meteo$day==\""+end_day+"\"&meteo$hour=="+end_hour+")");

			// write csv file
			c.tryVoidEval("write.csv(meteo[idStart:idEnd,], \""+resourcesPath+"meteorology.csv\", row.names=F)");

		}catch (Exception e) {
			LOGGER
			.debug("Error while getting Kriging results for observation collection: "
					+ e.getMessage());
			throw new RuntimeException(
			"Error while getting Kriging results for observation collection: "
					+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public void runSHP2OMConverter(String propsFile) throws Exception{
		//"src/main/resources/shp2om.props"
		ShapeFileConverter converter = new ShapeFileConverter(propsFile);
		converter.run();
	}


	private UncertaintyObservationCollection mergeEmisCollections(String mergedFilePath){
		// read emission collections
		UncertaintyObservationCollection largeColl = (UncertaintyObservationCollection) Utils.readObsColl(inputsPath + "/largeStreets.xml");
		UncertaintyObservationCollection smallColl = (UncertaintyObservationCollection) Utils.readObsColl(inputsPath + "/smallStreets.xml");

		UncertaintyObservationCollection uobsAll = largeColl;

		// loop through observations
		for (AbstractObservation obs : smallColl.getObservations()) {
			uobsAll.addObservation(obs);
		}

		// save merged observations
		Utils.writeObsColl(uobsAll, mergedFilePath);

		return uobsAll;
	}


	private MeasurementCollection mergeStaticEmisCollections(String mergedFilePath){
		// read emission collections
		MeasurementCollection industryColl = (MeasurementCollection) Utils.readObsColl(inputsPath + "/industry_static.xml");
		MeasurementCollection railroadColl = (MeasurementCollection) Utils.readObsColl(inputsPath + "/railroad.xml");
		MeasurementCollection shipColl = (MeasurementCollection) Utils.readObsColl(inputsPath + "/ship.xml");
		MeasurementCollection combustionColl = (MeasurementCollection) Utils.readObsColl(inputsPath + "/combustion.xml");

		MeasurementCollection uobsAll = industryColl;

		// loop through observations
		for (AbstractObservation obs : railroadColl.getObservations()) {
			uobsAll.addObservation(obs);
		}
		for (AbstractObservation obs : shipColl.getObservations()) {
			uobsAll.addObservation(obs);
		}
		for (AbstractObservation obs : combustionColl.getObservations()) {
			uobsAll.addObservation(obs);
		}

		// save merged observations
		Utils.writeObsColl(uobsAll, mergedFilePath);

		return uobsAll;
	}

	private MeasurementCollection mergeVariableEmisCollections(String mergedFilePath){
		// read emission collections
		MeasurementCollection industryColl = (MeasurementCollection) Utils.readObsColl(inputsPath + "industry_variable.xml");
		MeasurementCollection offroadColl = (MeasurementCollection) Utils.readObsColl(inputsPath + "offroad.xml");

		MeasurementCollection uobsAll = industryColl;

		// loop through observations
		for (AbstractObservation obs : offroadColl.getObservations()) {
			uobsAll.addObservation(obs);
		}

		// save merged observations
		Utils.writeObsColl(uobsAll, mergedFilePath);

		return uobsAll;
	}

	private void adaptPathInPropertiesFiles() throws FileNotFoundException, IOException{
		//TODO: change path in each properties file to the resources path
		File directory = new File(resourcesPath);

		// go through all properties files
		if (!directory.exists()) {
			System.out.println("Directory does not exist!");
		} else {
			File[] files = directory.listFiles();
			for (int i=0; i<files.length; i++){
					if (files[i].getPath().endsWith("props")&&!files[i].toString().equals("austal.props")) {
						List<String> rows = readPropertiesFile(files[i].toString());
						writePropertiesFile(files[i].toString(), rows);
					}
			}
		}
	}

	private List<String> readPropertiesFile(String filename) throws FileNotFoundException, IOException{
		// read file
		BufferedReader in = new BufferedReader(new FileReader(filename));
		List<String> rows = new ArrayList<String>();
		String row;

		while ((row = in.readLine()) != null) {
			// Separated by "whitespace"
			String[] s = row.trim().split("=");
			if(s.length>1){
				if(s[0].equals("SHPPATH")){
					String[] p = s[1].trim().split("/");
					String newPath = resourcesPath+"shapefiles/"+p[p.length-1];
					row = s[0] + "=" + newPath;
				}else if(s[0].equals("OMFILEPATH")){
					String[] p = s[1].trim().split("/");
					String newPath = resourcesPath+p[p.length-1];
					row = s[0] + "=" + newPath;
				}else if(s[0].equals("OUTFILEPATH")){
					String[] p = s[1].trim().split("/");
					String newPath = resourcesPath+"inputs/"+p[p.length-1];
					row = s[0] + "=" + newPath;
				}
			}

			rows.add(row);
		}
		in.close();
		return rows;
	}

	private void writePropertiesFile(String filename, List<String> rows) throws FileNotFoundException, IOException{
		 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename)));
		 for(String row : rows){
			 out.write(row);
			 out.newLine();
		 }
		 out.close();
	}
}
