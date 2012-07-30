package org.uncertweb.ems.exposuremodelling;


import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.ems.activityprofiles.Profile;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Allows the estimation of exposure by overlaying individual trajectories with outdoor concentration
 * @author Lydia Gerharz
 *
 */
public class OutdoorModel {

	private static Logger LOGGER = Logger.getLogger(OutdoorModel.class);
	private String resPath;
	
	public OutdoorModel(String resPath){
		this.resPath = resPath;
	}
	
	
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
			cmd = "load(\""+resPath+"/outdoorModel/overlay.RData\")";
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
			cmd = "load(\""+resPath+"/outdoorModel/COSP.RData\")";
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
	
	
	
	private void writeObservationCollection(IObservationCollection profile, String filepath){
		
	}
}
