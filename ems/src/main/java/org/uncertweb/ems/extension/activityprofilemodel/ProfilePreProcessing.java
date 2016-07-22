package org.uncertweb.ems.extension.activityprofilemodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;


import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.converter.ShapeFileConverter;
import org.uncertweb.api.om.converter.ShapeFileConverterProperties;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.CategoryResult;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Merge GPS tracks and diaries to provide full space-time activity profiles
 * @author Lydia Gerharz
 *
 */
public class ProfilePreProcessing {

	private static Logger log = Logger.getLogger(ProfilePreProcessing.class);
	private static String resourcesPath = "C:/WebResources/EMS/profiles";
	private static String profileLink = "http://giv-uw2.uni-muenster.de/activityprofiles";
	private static int gps_srid = 4326;
	
	
	public static void main(String[] args) {
		ProfilePreProcessing processing = new ProfilePreProcessing();
				
		// list of profiles ids
		//"p1","p2","p4","p5","p6","p7","p9","p10","p12","p13"
		String[] profiles = {"p1","p2","p4","p5","p6","p7","p9","p10","p12","p13"};
		int spatialThr = 100;
		int temporalThr = 300;
		int velocityThr = 1;
		int minuteResolution = 5; 
		int GPSpointRatio = 2;
		
		try{
			for(int p=0; p<profiles.length; p++){
				System.out.println("Started Profile Processing.");
				
				// 1) Use R preprocessing to create geometry for full 24 hours and remove outliers
		//		processing.gpsPreProcessing(profiles[p], spatialThr, temporalThr, velocityThr, minuteResolution, GPSpointRatio);
				System.out.println("Finished GPS preprocessing.");
				
				// 2) make OM documents from preprocessed GPS data
				// first adapt properties file
	//			processing.adaptPropertyFile(profiles[p]);
				
				// perform conversion
	//			processing.runSHP2OMConverter(resourcesPath+"/profile.props");
				
				//3) Add location and activity information from diary and adjust time spans  
				IObservationCollection profile = processing.mergeGPSandDiary(profiles[p], resourcesPath+"/gps/"+profiles[p]+"_om.xml",
						resourcesPath+"/diaries/"+profiles[p]+".csv", resourcesPath+"/profile.props", minuteResolution);
				System.out.println("Finished GPS and diary merging.");
				
				//4) (optional) read personal information table and add to the profile
				
				
				// save OM file
				new StaxObservationEncoder().encodeObservationCollection(profile, new File( resourcesPath+"/profile_"+profiles[p]+".xml"));		
		}}catch(Exception e){
			e.printStackTrace();
			log.info("Error while pre-processing profile data: " + e.getMessage());
		}
		
		
		/*
		 *  1) Use R preprocessing to create geometry for full 24 hours and remove outliers
		 *  2) make OM documents from preprocessed GPS data
		 *  3) Add location and activity information from diary and adjust time spans  
		 *  4) create new OM documents with locations and activity as observed properties
		 *  5) read personal information table and add to the profile
		 */
		
	}

	
	public ProfilePreProcessing(){		
	}
	
	/**
	 * Creates OM document from shapefile (GPS track)
	 * @param propsFile
	 * @throws Exception
	 */
	public void runSHP2OMConverter(String propsFile) throws Exception{
		ShapeFileConverter converter = new ShapeFileConverter(propsFile);
		converter.run();
	}
	
	/**
	 * Does GPS preprocessing in R
	 */
	public void gpsPreProcessing(String profileID, int spatialThreshold, int temporalThreshold, int velocityThreshold, int minuteResolution, int GPSratio){
		ExtendedRConnection c = null;		
		String cmd = "";
		try {		
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
					
			// set path and thresholds
			c.voidEval("library(rgdal)");
			c.voidEval("path <- \""+resourcesPath+"/gps\"");
			c.voidEval("name <- \""+profileID+"\"");
			c.voidEval("sThr <-"+spatialThreshold);
			c.voidEval("tThr <-"+temporalThreshold);
			c.voidEval("vThr <-"+velocityThreshold);
			c.voidEval("minInterval <- "+minuteResolution);
			c.voidEval("ratio <- "+GPSratio);
			
			// read GPS track and correct for time
			c.voidEval("data <- readOGR(paste(path,\"/\",name,\".gml\",sep=\"\"),layer=name)");
			c.voidEval("data$datetime <- as.POSIXct(strptime(as.character(data$datetime), \"%Y-%m-%d %H:%M:%S\"),tz=\"GMT\")");
			c.voidEval("data$datetime <- as.POSIXct(strptime(format(data$datetime-3600, \"%Y-%m-%d %H:%M:%S\",tz=\"Europe/Andorra\",usetz=TRUE), \"%Y-%m-%d %H:%M:%S\"))");
			
			// transform GPS track to projected CRS for distance measurements
			c.voidEval("proj4string(data) <- \"+init=epsg:4326\"");
			c.voidEval("data.t <- spTransform(data, CRS=CRS(\"+init=epsg:31467\"))");
			
			// (1) track analysis
			// spatial distances
			c.voidEval("sDist <- NULL");
			c.voidEval("sDist.2 <- NULL");
			c.voidEval("for(i in 1:(nrow(data.t)-1)){sDist <- c(sDist, spDistsN1(data.t[i+1,], data.t[i,]));if(i<(nrow(data.t)-1)){;sDist.2 <- c(sDist.2, spDistsN1(data.t[i+2,], data.t[i,]))}}");
			
			// temporal distances
			c.voidEval("tDist <- NULL");
			c.voidEval("for(i in 1:(nrow(data)-1)){tDist <- c(tDist, as.numeric(data$datetime[i+1]-data$datetime[i], units=\"secs\"))}");
			
			// velocity
			c.voidEval("velocity <- sDist/tDist* 3600/1000");
			
			// (2) Detect outliers
			// single outliers: points with lag(i,i+1) > 100 m & lag(i+1,i+2) > 100 m
			c.voidEval("outliers <- sDist[-length(sDist)]>sThr&sDist[-1]>sThr");
			
			// (3) Detect indoor and outdoor periods
			c.voidEval("timeLag <- which(tDist>tThr)");
			// a) if next two points lie within 100 m distance -> indoor stop
			c.voidEval("indoor.a <- sDist[timeLag]<sThr&sDist.2[timeLag]<sThr");
			// b) else if next two points lie outside 100 m distance and velocity is less 1 -> indoor stop
			c.voidEval("indoor.b <- velocity[timeLag] < vThr");
			
			// (4) Adapt GPS track
			// create new indoor points from last location of outdoor periods
			c.voidEval("indoor.start <- data[c(timeLag[indoor.a|indoor.b],nrow(data)),]");
			c.voidEval("indoor.end <- data[c(1,timeLag[indoor.a|indoor.b]+1),]");
			c.voidEval("sec <- 60-as.numeric(format(indoor.start$datetime,\"%S\"))");
			c.voidEval("sec[sec==0] <- 60");
			c.voidEval("min <- minInterval - as.numeric(format(indoor.start$datetime + sec, \"%M\"))%%minInterval");
			c.voidEval("min[min==minInterval] <- 0");
			c.voidEval("indoor.start$datetime <- indoor.start$datetime + sec + 60*min");

			// add first location as indoor points
			c.voidEval("dayString <- strsplit(as.character(data$datetime[1]),\" \")[[1]][1]");
			c.voidEval("indoor.start <- rbind(indoor.start[1,],indoor.start)");
			c.voidEval("indoor.start$datetime[1] <- as.POSIXct(strptime(paste(dayString,\"00:00:00\"), \"%Y-%m-%d %H:%M:%S\"),tz=\"CET\")");
			c.voidEval("indoor.end <- rbind(indoor.end, indoor.end[nrow(indoor.end),])");
			c.voidEval("indoor.end$datetime[nrow(indoor.end)] <- as.POSIXct(strptime(paste(dayString,\"00:00:00\"), \"%Y-%m-%d %H:%M:%S\"),tz=\"CET\")+24*60*60-minInterval*60");

			// get the number of minutes within each indoor period
			c.voidEval("indoor.min <- trunc(as.numeric(indoor.end$datetime-indoor.start$datetime, \"mins\")/minInterval)");
			
			// function to multiply points
			c.voidEval("copy.points <- function(spdf, nc){spdf.new <- spdf;if(nc>1){for(j in 1:(nc-1)){spdf.new <- rbind(spdf.new,spdf[1,])}};return(spdf.new)}");
			
			// fill points eacht minute in the indoor periods
			c.voidEval("indoor.points <- indoor.start");
			c.voidEval("for(i in 1:nrow(indoor.start)){if(indoor.min[i]>0){tmp <- copy.points(indoor.start[i,], indoor.min[i]);tmp$datetime <- indoor.start[i,]$datetime+(1:indoor.min[i])*60*minInterval;indoor.points <- rbind(indoor.points, tmp)}}");

			// set indoor parameter to each point
			c.voidEval("indoor.points$io <- \"indoor\"");

			// remove outliers
			c.voidEval("diary <- data[!outliers,]");

			// thin out GPS track
			c.tryVoidEval("diff.1 <- which(as.numeric(diff(diary$datetime))==1)");
			c.tryVoidEval("if(length(diff.1)>0){diff.2 <- diff.1[as.logical((1:length(diff.1))%%ratio)]; diary <- diary[-diff.2,]}");
			
			// add new indoor points and sort points data frame
			c.voidEval("diary$io<-\"outdoor\"");
			c.voidEval("diary <- rbind(diary, indoor.points)");
			
			// check for summertime
			c.voidEval("tz = \"01\"");
			c.voidEval("if(format(diary$datetime[1],\"%Z\")==\"CEST\"){tz=\"02\"}");
			c.voidEval("diary$time <- format(diary$datetime, paste(\"%Y-%m-%dT%H:%M:%S.000\",tz,sep=\"+\"))");
			c.voidEval("diary <- diary[order(diary@data$datetime),]");

			// write as shapefile
			c.voidEval("writeOGR(diary[,c(\"io\",\"time\")], dsn=path, layer=name, driver=\"ESRI Shapefile\",overwrite_layer=TRUE)");
			
		}catch (Exception e) {
				log
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
	}
	
	
	/**
	 * Merge diary and GPS
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	public IObservationCollection mergeGPSandDiary(String profileID, String gpsPath, String diaryPath, String propertiesPath, int minuteResolution) throws FileNotFoundException, IOException, URISyntaxException{	
		// get properties
		ShapeFileConverterProperties props = new ShapeFileConverterProperties(propertiesPath);
		
		// read GPS observation collection
		IObservationCollection gpsObs = null;
		try {
			XmlObject xml = XmlObject.Factory.parse(new FileInputStream(gpsPath));		
			gpsObs = (IObservationCollection) new XBObservationParser().parse(xml.xmlText());			
		} catch (Exception e) {		
			e.printStackTrace();
			log.info("Error while reading OM input: " + e.getMessage());
			throw new RuntimeException("Error while reading OM input: " + e.getMessage(), e);
		}
		// read diary file
		Diary diary = readDiary(diaryPath);
		
		// get start and endtime arrays
		ArrayList<DateTime> diaryEntryStart = diary.getStartTimes();
		ArrayList<DateTime> diaryEntryEnd = diary.getEndTimes();
		ArrayList<String> ioList = diary.getIOList();
		ArrayList<String> attributes = diary.getAttributeNames();
		
		// new observation collection
		IObservationCollection profile = new CategoryObservationCollection();
		
		// get times
		TreeMap<DateTime, Interval> times = getTimeIntervals(gpsObs, minuteResolution);
		
		// loop through observations
		for(AbstractObservation obs : gpsObs.getObservations()){
			// add domain feature (profile id)
	//		obs.getFeatureOfInterest().setSampledFeature(profileLink+"#"+profileID);
			obs.getFeatureOfInterest().getShape().setSRID(gps_srid);
			
			// add gps observation to new profile collection (optional)
	//		profile.addObservation(obs);
			
			// get time and activity
			DateTime resultTime = obs.getPhenomenonTime().getDateTime();
			String io = (String)obs.getResult().getValue();
			
			//TODO implement correct merging -> what to do if both do not match exactly?
			// determine which diary entry belongs to the observation
			for(int i=0; i<diaryEntryEnd.size(); i++){
				DateTime start = diaryEntryStart.get(i);
				DateTime end = diaryEntryEnd.get(i);
				boolean startBefore = start.isBefore(resultTime);
				boolean startEqual= start.isEqual(resultTime);
				boolean endAfter = end.isAfter(resultTime);
				boolean endBefore = end.isBefore(resultTime);
			
				// find the respective diary entry
				if((startBefore&&endAfter)||(startEqual&&endAfter)||(i==(diary.size()-1)&&endBefore)){
					int id = i;
					
					// if io is not the same, look at adjacent diary entries and check them with ascending time difference					
					if(!io.equals(ioList.get(i))){
						int p = 0, n = 0, diffP = -1, diffN = -1;
						
						// check previous diary entries and find the next one with the same io
						while(diffP<0){
							p++;
							if(i>=p){
								if(io.equals(ioList.get(i-p)))
									diffP = Seconds.secondsBetween(diaryEntryEnd.get(i-p),resultTime).getSeconds();									
							}else{
								diffP = 60*60*24;
							}							
						}				
							
						// check next diary entries and find the next one with the same io
						while(diffN<0){
							n++;
							if(i<(diary.size()-n-1)){
								if(io.equals(ioList.get(i+n)))
									diffN = Seconds.secondsBetween(resultTime, diaryEntryStart.get(i+n)).getSeconds();									
							}else{
								diffN = 60*60*24;
							}						
						}

						// choose the closest entry with same io
						if(p<n&&diffP<600){
							id = i-p;
						}else if(p>n&&diffN<600){
							id = i+n;
						}else if(diffP<diffN){
							id = i-p;
						}else if(diffP>diffN){
							id = i+n;
						}else if(p==n){
							id = i-p;
						}else{
							System.out.println("strange");
						}
						
						if(id>=ioList.size()){
							System.out.println("strange");
						}
					}					
					
					// add new observations 
					for(String att:attributes){
						CategoryResult res = new CategoryResult(diary.getEntry(id, att),props.getUom());
						//new CategoryObservation(TimeObject phenomenonTime, TimeObject resultTime, URI procedure,URI observedProperty, SpatialSamplingFeature featureOfInterest,CategoryResult result)						
						AbstractObservation attObs = new CategoryObservation(new TimeObject(times.get(resultTime)), new TimeObject(times.get(resultTime)),
								obs.getProcedure(), new URI(props
										.getObsPropsPrefix()+att),obs.getFeatureOfInterest(),res);							
						profile.addObservation(attObs);
					}
					// quit the loop here and go to next observation
					break;
				}			
			}
			
			// change result time to timeperiod (optional)
		
		}		
		return profile;
	}
	
	
	/**
	 * Parse diary
	 * @throws FileNotFoundException, IOException
	 */
	public Diary readDiary(String path) throws FileNotFoundException, IOException{
		CSVReader reader = new CSVReader(new FileReader(path));
		//TODO: create object to store diary entries
		Diary diary = new Diary("");
		
		// for the first row fill the header	
		String[] header = reader.readNext();
		String[] line;	
		
		// loop through lines
	    while ((line = reader.readNext()) != null) {
	    	//header: Date,Starttime,Endtime,Raw data,ME,ME2,Activity,No of persons,Smoker,Window open,Remarks
	    	//TODO: fill object
	    	diary.addEntries(header, line);
	    }
	    reader.close();
	    
	    return diary;	    
	}
	
	
	/**
	 * Read personal information file and add to profile
	 */
	public void readPersonalInformation(String path){
		
	}
	
	private TreeMap<DateTime, Interval> getTimeIntervals(IObservationCollection obsColl, int minuteInterval){
		// first get all phenomenon times to sort them
		TreeMap<DateTime, Interval> times = new TreeMap<DateTime, Interval>();
		for(AbstractObservation obs : obsColl.getObservations()){
			times.put(obs.getPhenomenonTime().getDateTime(), null);
		}
		
		// then estimate the intervals for each time
		TreeMap<DateTime, Interval> newTimes = new TreeMap<DateTime, Interval>();
		Iterator<DateTime> it = times.keySet().iterator();
		DateTime lastDt = null;
		while(it.hasNext()){
			DateTime currentDt = it.next();
			if(lastDt!=null){
				newTimes.put(lastDt, new Interval(lastDt, currentDt));
			}
			lastDt = currentDt;			
		}
		
		// for the last time step use overall resolution
		newTimes.put(lastDt, new Interval(lastDt,lastDt.plusMinutes(minuteInterval)));
		
		return(newTimes);
	} 
	
	
	private void adaptPropertyFile(String profileID) throws FileNotFoundException, IOException{
		// read file
		BufferedReader in = new BufferedReader(new FileReader(resourcesPath+"/profile.props"));
		List<String> rows = new ArrayList<String>();
		String row;
					
		while ((row = in.readLine()) != null) {
			// Separated by "whitespace"
			String[] s = row.trim().split("=");
			if(s.length>1){
				if(s[0].equals("SHPPATH")){
					String newPath = resourcesPath+"/gps/"+profileID+".shp";
					row = s[0] + "=" + newPath;					
				}else if(s[0].equals("OUTFILEPATH")){
					String newPath = resourcesPath+"/gps/"+profileID+"_om.xml";
					row = s[0] + "=" + newPath;		
				}else if(s[0].equals("FEATCLASSNAME")){
					row = s[0] + "=" + profileID;		
				}else if(s[0].equals("PROCID")){
					row = s[0] + "=" + profileID;	
				}
			}
			
			rows.add(row);
		}
		in.close();
		
	 // finally write new file
	 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(resourcesPath+"/profile.props")));
	 for(String r : rows){
		 out.write(r);
		 out.newLine();
	 }
	 out.close();
	}
	

}
