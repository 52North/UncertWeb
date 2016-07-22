package org.uncertweb.ems.exposuremodel;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.n52.wps.util.r.process.RProcessException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.ems.data.profiles.AbstractProfile;
import org.uncertweb.ems.data.profiles.Profile;
import org.uncertweb.ems.exceptions.EMSProcessingException;
import org.uncertweb.ems.util.ExposureModelConstants;
import org.uncertweb.netcdf.NcUwConstants;
import org.uncertweb.netcdf.NcUwFile;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Allows the estimation of exposure by overlaying individual trajectories with outdoor concentration
 * @author LydiaGerharz
 *
 */
public class OutdoorModel{

	private static Logger LOGGER = Logger.getLogger(OutdoorModel.class);
	private final String fileSeparator = System.getProperty("file.separator");
	private String resOMPath, resRPath, parameter;


	public OutdoorModel(String RresourcePath){
		if(System.getProperty("os.name").contains("Windows")){
			resOMPath = System.getenv("TMP");
		}else{
			resOMPath = System.getenv("CATALINA_TMPDIR");
		}
		resOMPath = resOMPath.replace("\\","/");
		resRPath = RresourcePath.replace("\\","/");
	}

	/**
	 * executes the outdoor model for the provided activity profile and the air quality NetCDF file
	 * @param profile
	 * @param ncFile
	 * @return
	 */
	public void run(List<AbstractProfile> profileList, NcUwFile ncFile) {
		// get necessary information from NetCDF file
		String ncFilePath = ncFile. getUnderlyingFile().getAbsolutePath();
		ncFilePath = ncFilePath.replace("\\","/");
		parameter = ncFile.getStringAttribute(NcUwConstants.Attributes.PRIMARY_VARIABLES, true).split(" ")[0];
		String uom = ncFile.getVariable(parameter).findAttribute("units").getStringValue();

		// perform overlay
		ArrayList<TreeMap<DateTime, double[]>> expoVals = null;

		// check geometry of the profile's observation collection to call the correct overlay method
		List<AbstractObservation> obsList = (List<AbstractObservation>) profileList.get(0).getObservationCollection().getObservations();
		SpatialSamplingFeature foi = obsList.get(0).getFeatureOfInterest();
		Geometry obsGeom = foi.getShape();
		try{
			if(obsGeom instanceof Point || obsGeom instanceof MultiPoint){
				expoVals = overlayRaster2Points(profileList, ncFilePath);
			}else if(obsGeom instanceof MultiPolygon || obsGeom instanceof Polygon){
				expoVals = overlayRaster2Polygons(profileList, ncFilePath);
			}
		}catch(Exception e){
			LOGGER.debug("Error while executing outdoor model: "
					+ e.getMessage());
			throw new EMSProcessingException("Error while executing outdoor model: "
					+ e.getMessage(), e);
		}

		// if no values were estimated throw an Exception
		if(expoVals.isEmpty()||expoVals==null||expoVals.size()<1)
			throw new EMSProcessingException("No exposure values could be estimated as the two datasets do not intersect in space and/or time!");

		// loop through profiles to add results
		for(int i=0; i<profileList.size(); i++) {
			AbstractProfile profile = profileList.get(i);

			// necessary to find respective Interval in the observation collection
			TreeMap<DateTime,Interval> dateMap = profile.getStartDatesMap();

			for(DateTime dt : dateMap.keySet()){
				double[] c = expoVals.get(i).get(dt);
				profile.setExposureValue(dateMap.get(dt), c, ExposureModelConstants.ExposureValueTypes.OUTDOOR_SOURCES, parameter, uom);
			}
		}
	}

	/**
	 * performs the overlay for a NetCDF-U raster and polygon locations in R by first disaggregating to points, overlay, and finally aggregate back to polygons
	 * @param omFilePath
	 * @param netcdfFilePath
	 * @return
	 * @throws RserveException
	 * @throws RProcessException
	 * @throws REXPMismatchException
	 */
	private ArrayList<TreeMap<DateTime, double[]>> overlayRaster2Polygons(List<AbstractProfile> profileList, String netcdfFilePath) throws RserveException, RProcessException, REXPMismatchException{
		ArrayList<TreeMap<DateTime, double[]>> expoValsList = new ArrayList<TreeMap<DateTime, double[]>>();
	//	HashMap<DateTime, double[]> expoVals = new HashMap<DateTime, double[]>();
		String cmd;
		ExtendedRConnection c = null;
		try {
		// establish connection to Rserve running on localhost
		c = new ExtendedRConnection("127.0.0.1");
		if (c.needLogin()) {
			// if server requires authentication, send one
			c.login("rserve", "aI2)Jad$%");
		}

		for (AbstractProfile profile : profileList) {
			// write geometry of the profile to csv file
			String omFilePath = resOMPath + "/om.csv";
			profile.writeObsCollGeometry2csv(omFilePath);

			// set R inputs
			c.voidEval("omFile <- \""+omFilePath+"\"");
			c.voidEval("rasterFile <- \""+netcdfFilePath+"\"");

			// run the prepared script
			//TODO: this solution does not work in R, so make sure the path is set correctly!
//			URL url = OutdoorModel.class
//					.getResource("overlay_utils.R");
//			cmd  = "source(\""+url+"\", echo=TRUE)";
			cmd  = "source(\""+resRPath+"/overlay_utils.R\", echo=TRUE)";
			c.voidEval(cmd);

			// load files
			c.voidEval("raster <-readNetCDFU(rasterFile)");
			c.voidEval("om <- readOMcsv(omFile)");

			// get pollutant
			parameter = c.tryEval("strsplit(colnames(raster@data)[1],\"_r\")[[1]][1]").asString();

			// transform OM data to NetCDF-U projection
			c.voidEval("om.sp <- spTransform(om@sp, CRS=CRS(proj4string(raster)))");
			c.voidEval("om.st <- STI(om.sp, om@time)");

			// 1) disaggregate polygons to points
			c.voidEval("points <- polygons2points(om.st, 0.00001, 50)");

			// 2) try to perform overlay
			try{
				c.voidEval("points.over <- over(points, raster, fn=mean, timeInterval = TRUE)");
				c.voidEval("points.stdf <- STIDF(points@sp, points@time, points.over)");
			}catch(Exception e){
				throw new EMSProcessingException("No exposure values could be estimated as the two datasets do not intersect in space and/or time!", e);
			}

			// 3) aggregate point values to polygons
			c.voidEval("overlay.mean <- aggregate(points.stdf, om.st, mean, na.rm=T)");
			c.voidEval("overlay.sd <- aggregate(points.stdf, om.st, sd, na.rm=T)");

			// get values and dates
			REXP vals = c.tryEval("as.matrix(overlay.mean@data)");
			double[][] valueMatrix = vals.asDoubleMatrix();
			REXP dates = c.tryEval("format(index(om@time),\"%Y-%m-%dT%H:%M:%OS3+00:00\")");
			String[] dateList = dates.asStrings();

			expoValsList.add(new TreeMap<DateTime, double[]>());
			for(int i=0; i<dateList.length; i++){
				expoValsList.get(expoValsList.size()-1).put(ISODateTimeFormat.dateTime().parseDateTime(dateList[i]), valueMatrix[i]);
			}
		}
		} catch (Exception e){
			LOGGER.info("Error while executing exposure model: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while executing exposure model: "+e.getLocalizedMessage());
		}
		finally{
			if (c!=null){
				c.close();
			}
		}

		return expoValsList;
	}

	/**
	 * performs the overlay for a NetCDF-U raster and point locations in R
	 * @param omFilePath
	 * @param netcdfFilePath
	 * @return
	 * @throws RserveException
	 * @throws REXPMismatchException
	 * @throws RProcessException
	 */
	private ArrayList<TreeMap<DateTime, double[]>> overlayRaster2Points(List<AbstractProfile> profileList, String netcdfFilePath) throws RserveException, REXPMismatchException, RProcessException{
		ArrayList<TreeMap<DateTime, double[]>> expoValsList = new ArrayList<TreeMap<DateTime, double[]>>();
	//	HashMap<DateTime, double[]> expoVals = new HashMap<DateTime, double[]>();

		ExtendedRConnection c = null;
		try {
		// establish connection to Rserve running on localhost
		c = new ExtendedRConnection("127.0.0.1");
		if (c.needLogin()) {
			// if server requires authentication, send one
			c.login("rserve", "aI2)Jad$%");
		}

		for (AbstractProfile profile : profileList) {
			// write geometry of the profile to csv file
			String omFilePath = resOMPath + "/om.csv";
			profile.writeObsCollGeometry2csv(omFilePath);

			// set R inputs
			c.voidEval("omFile <- \""+omFilePath+"\"");
			c.voidEval("rasterFile <- \""+netcdfFilePath+"\"");


			// run the prepared script
			c.voidEval("source(\""+resRPath+"/overlay_utils.R\", echo=TRUE)");

			// load files
			c.voidEval("raster <-readNetCDFU(rasterFile)");
			c.voidEval("om <- readOMcsv(omFile)");
			// get pollutant
			parameter = c.tryEval("strsplit(colnames(raster@data)[1],\"_r\")[[1]][1]").asString();

			// transform OM data to NetCDF-U projection
			c.voidEval("om.sp <- spTransform(om@sp, CRS=CRS(proj4string(raster)))");
			c.voidEval("om.st <- STI(om.sp, om@time)");

			// try to perform overlay, result is dataframe with ncol=numbReal, nrow=numbPoints
			try{
				c.voidEval("overlay.mean <- over(om.st, raster, fn=mean)");
			}catch(Exception e){
				throw new EMSProcessingException("No exposure values could be estimated as the two datasets do not intersect in space and/or time!", e);
			}

			// get values and dates
			REXP vals = c.tryEval("as.matrix(overlay.mean)");
			double[][] valueMatrix = vals.asDoubleMatrix();
			REXP dates = c.tryEval("format(index(om@time),\"%Y-%m-%dT%H:%M:%OS3+00:00\")");
			String[] dateList = dates.asStrings();

			expoValsList.add(new TreeMap<DateTime, double[]>());
			for(int i=0; i<dateList.length; i++){
				expoValsList.get(expoValsList.size()-1).put(ISODateTimeFormat.dateTime().parseDateTime(dateList[i]), valueMatrix[i]);
			}
		}
		} catch (Exception e){
			LOGGER.info("Error while running exposure model: "+e.getLocalizedMessage());
			throw new RuntimeException("Error while running exposure model: "+e.getLocalizedMessage());
		}
		finally {
			if (c!=null){
			c.close();
			}
		}

		return expoValsList;
	}


	/**
	 * Old method for outdoor modelling
	 * @deprecated
	 * @param profile
	 * @param ncFilePath
	 * @param omFilePath
	 * @param parameter
	 * @return
	 */
	public Profile performOutdoorOverlay(Profile profile, String ncFilePath, String omFilePath, String parameter) {
		//TODO: works only for points so far!

		ExtendedRConnection c = null;
		String cmd = "";
		try {
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}

			// load neccessary libraries
			c.voidEval("library(rgdal)");
			c.voidEval("library(spacetime)");
			c.voidEval("library(RNetCDF)");
			c.voidEval("library(rgeos)");
			c.voidEval("library(maptools)");
//			cmd = "source(\""+resPath+"/outdoorModel/overlay.R\")";
			cmd = "load(\""+resRPath+"/overlay.RData\")";
			c.voidEval(cmd);

			// load gps data
			c.voidEval("epsg_pm10 <- \"+init=epsg:31467\"");
			c.voidEval("gps.table <- read.csv(\""+omFilePath+"\")");
			c.voidEval("gps.geom <- readWKT(gps.table$WKTGeometry[1])");
			c.voidEval("for(i in 2:nrow(gps.table)) {gps.geom<-spRbind(gps.geom,readWKT(gps.table$WKTGeometry[i]))}");
			c.voidEval("row.names(gps.geom) <- 1:nrow(coordinates(gps.geom))");

			// transformations
			c.voidEval("proj4string(gps.geom) <- CRS(paste(\"+init=epsg:\",gps.table$EPSG[1],sep=\"\"))");
			c.voidEval("gps.t <- spTransform(gps.geom, CRS=CRS(epsg_pm10))");
			c.voidEval("time <- strsplit(as.character(gps.table$PhenomenonTime),\".000\")");
			c.voidEval("time.gps <- as.POSIXct(strptime(matrix(unlist(time),ncol=2,byrow=T)[,1], \"%Y-%m-%dT%H:%M:%S\"),tz=\"GMT\")");
			c.voidEval("gps.st <- STI(gps.t, time.gps)");

			// load NetCDF-U file
			cmd = "file <- \""+ncFilePath+"\"";
			c.voidEval(cmd);
			cmd = "nc <- readUNetCDF(file, x=\"x\", y=\"y\", time=\"time\", variables=\""+parameter+"\", projection=epsg_pm10, realisation=\"realisation\")";
			c.voidEval(cmd);

			// perform overlay
			c.voidEval("gps.over <- over(gps.st, nc, fn=mean)");

			// get values for each GPS point and add results to profile
			for(int i=1; i<=profile.getSize(); i++){
				REXP vals = c.tryEval("as.numeric(gps.over["+i+",])");
				double[] valsDouble = vals.asDoubles();

				// add values to profile
				profile.setOutConcRealisations(i-1, valsDouble);
			}

		}catch (Exception e) {
			LOGGER
				.debug("Error while preprocessing GPS tracks: "
						+ e.getMessage());
		throw new RuntimeException(
				"Error while preprocessing GPS tracks: "
						+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}
		return profile;
	}

	/**
	 * Old method for COSP uncertainty estimation
	 * @deprecated
	 * @param profile
	 * @param omFilePath
	 * @param nSim
	 * @return
	 */
	public Profile estimateCOSPUncertainty(Profile profile, String omFilePath, int nSim){
		ExtendedRConnection c = null;
		String cmd = "";
		try {
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}


			// load neccessary libraries
			c.voidEval("library(rgdal)");
			c.voidEval("library(gstat)");
			c.voidEval("library(rgeos)");
			c.voidEval("library(maptools)");
			cmd = "load(\""+resRPath+"/outdoorModel/COSP.RData\")";
			c.voidEval(cmd);

			// load gps data
			c.voidEval("gps.table <- read.csv(\""+omFilePath+"\")");
			c.voidEval("gps.geom <- readWKT(gps.table$WKTGeometry[1])");
			c.voidEval("for(i in 2:nrow(gps.table)) {gps.geom<-spRbind(gps.geom,readWKT(gps.table$WKTGeometry[i]))}");
			c.voidEval("row.names(gps.geom) <- 1:nrow(coordinates(gps.geom))");
			c.voidEval("gps.io <- gps.table$Result");

			// transformations
			c.voidEval("proj4string(gps.geom) <- CRS(paste(\"+init=epsg:\",gps.table$EPSG[1],sep=\"\"))");
			c.voidEval("gps.t <- spTransform(gps.geom, CRS=CRS(epsg_pm10))");
			c.voidEval("data <- data.frame(x=coordinates(gps.t)[,1],y=coordinates(gps.t)[,2])");
			c.voidEval("time <- strsplit(as.character(gps.table$PhenomenonTime),\".000\")");
			cmd = "data$datetime <- as.POSIXct(strptime(matrix(unlist(time),ncol=2,byrow=T)[,1], \"%Y-%m-%dT%H:%M:%S\"),tz=\"GMT\")";
			c.voidEval(cmd);

			// get meteorology data
			c.voidEval("meteo.sel <- meteo[which(meteo$datetime>=min(data$datetime)&meteo$datetime<=max(data$datetime)+600),]");
			c.voidEval("tmp <- apply(meteo.sel, 2, rep,each=600)");
			c.voidEval("row.names(tmp) <- 1:nrow(tmp)");
			c.voidEval("meteo.s <- as.data.frame(tmp)");
			c.voidEval("meteo.s$datetime <- as.POSIXct(strptime(as.character(meteo.s$date_time), \"%d.%m.%Y %H:%M\",tz=\"GMT\"))");
			c.voidEval("sec <- rep(-599:0, nrow(meteo.sel))");
			c.voidEval("meteo.s$datetime <- meteo.s$datetime + sec");
			c.voidEval("data.meteo <- merge(data, meteo.s, by.x=\"datetime\")");

			// get traffic data
			c.voidEval("weekend <- as.numeric(format(data$datetime[1], \"%w\"))==0|as.numeric(format(data$datetime[1], \"%w\"))==6");
			c.voidEval("trafficcount <- rep(traffic$Weekday*!weekend + traffic$Weekend*weekend, each=3600)");
			c.voidEval("datetime <- data$datetime[1]+0:((24*60*60)-1)");
			c.voidEval("data.all <- merge(data.meteo, as.data.frame(cbind(trafficcount,datetime)), by.x=\"datetime\")");

			// get spatial auxiliary data
			c.voidEval("data.all$clc250 <- over(gps.t, clc250, fn=mean)$band1");
			c.voidEval("data.all$lqMS<- over(gps.t, lqMS, fn=mean)$band1");
			c.voidEval("data.all$streetMS <- over(gps.t, streetMS, fn=mean)$band1");

			// group data
			c.voidEval("track.cells <- over(gps.t, cellIDs)$band1");
			c.voidEval("track.hours <- format(data.all$datetime, \"%H\")");
			c.voidEval("track.groups <- paste(track.hours,track.cells,sep=\"_\")");
			c.voidEval("group.names <- dimnames(table(track.groups))$track.groups");
			c.voidEval("pointsDF <- SpatialPointsDataFrame(gps.t, data.frame(io=gps.io, group=track.groups))");

			c.voidEval("winddir.groups <- unlist(lapply(split(as.numeric(as.character(data.all$wind_direction)), track.groups),mean,na.rm=T))");
			c.voidEval("windspeed.groups <- unlist(lapply(split(as.numeric(as.character(data.all$wind_speed)), track.groups),mean,na.rm=T))");
			c.voidEval("temp.groups <- unlist(lapply(split(as.numeric(as.character(data.all$temperature)), track.groups),mean,na.rm=T))");
			c.voidEval("humid.groups <- unlist(lapply(split(as.numeric(as.character(data.all$relative_humidity)), track.groups),mean,na.rm=T))");
			c.voidEval("trafficcount.groups <- unlist(lapply(split(data.all$trafficcount, track.groups),mean,na.rm=T))");
			c.voidEval("clc250.groups <- unlist(lapply(split(data.all$clc250, track.groups),mean,na.rm=T))");
			c.voidEval("lqMS.groups <- unlist(lapply(split(data.all$lqMS, track.groups),mean,na.rm=T))");
			c.voidEval("streetMS.groups <- unlist(lapply(split(data.all$streetMS, track.groups),mean,na.rm=T))");

			// predict variogram parameters with linear regression models
			c.voidEval("nugget <- lm_nugget$coefficients[1] + lm_nugget$coefficients[2]*winddir.groups + lm_nugget$coefficients[3]*windspeed.groups +lm_nugget$coefficients[4]*humid.groups + lm_nugget$coefficients[5]*temp.groups + lm_nugget$coefficients[6]*trafficcount.groups + lm_nugget$coefficients[7]*clc250.groups + lm_nugget$coefficients[8]*lqMS.groups + lm_nugget$coefficients[9]*streetMS.groups");
			c.voidEval("psill <- lm_psill$coefficients[1] + lm_psill$coefficients[2]*winddir.groups +  lm_psill$coefficients[3]*windspeed.groups +  lm_psill$coefficients[4]*humid.groups + lm_psill$coefficients[5]*temp.groups + lm_psill$coefficients[6]*trafficcount.groups + lm_psill$coefficients[7]*clc250.groups + lm_psill$coefficients[8]*lqMS.groups + lm_psill$coefficients[9]*streetMS.groups");
			c.voidEval("range <- lm_range$coefficients[1] + lm_range$coefficients[2]*winddir.groups + lm_range$coefficients[3]*windspeed.groups + lm_range$coefficients[4]*humid.groups + lm_range$coefficients[5]*temp.groups + lm_range$coefficients[6]*trafficcount.groups + lm_range$coefficients[7]*clc250.groups + lm_range$coefficients[8]*lqMS.groups + lm_range$coefficients[9]*streetMS.groups");

			// check for negative values
			c.voidEval("if(sum(nugget<0)>0){nugget[nugget<0] <- mean(nugget[nugget>0])}");
			c.voidEval("if(sum(psill<0)>0){psill[psill<0] <- mean(psill[psill>0])}");
			c.voidEval("if(sum(range<0)>0){range[range<0] <- mean(range[range>0])}");

			// perform unconditional simulation
			c.voidEval("cosp <- uSim(pointsDF, psill, range, nugget, "+nSim+")");

			// get values for each GPS point and add results to profile
			for(int i=1; i<=profile.getSize(); i++){
				REXP vals = c.tryEval("cosp["+i+",]");
				double[] valsDouble = vals.asDoubles();

				// only add cosp if it is not zero
				REXP sd= c.tryEval("sd(cosp["+i+",])");
				double sdDouble = sd.asDouble();
				if(sdDouble>0){
					profile.setCOSPrealisations(i-1, valsDouble);
				}
			}

		}catch (Exception e) {
			LOGGER
				.debug("Error while preprocessing GPS tracks: "
						+ e.getMessage());
		throw new RuntimeException(
				"Error while preprocessing GPS tracks: "
						+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}

		return null;
	}



}
