package org.uncertweb.austalwps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.WebProcessingService;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.uncertweb.austalwps.util.AustalOutputReader;
import org.uncertweb.austalwps.util.Point;
import org.uncertweb.austalwps.util.StreamGobbler;
import org.uncertweb.austalwps.util.Value;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class Austal2000Algorithm extends AbstractObservableAlgorithm{

//	private static Logger LOGGER = Logger.getLogger(Austal2000Algorithm.class);
//	private final String inputIDXQ = "XQ";
//	private final String inputIDYQ = "YQ";
	private final String inputIDMeteorology = "meteorology";
	private final String inputIDStreetEmissions = "street-emissions";
	private final String inputIDReceptorPoints = "receptor-points";
	private final String inputIDStartTime = "start-time";
	private final String inputIDEndTime = "end-time";
	private final String outputIDResult = "result";
	private final String fileSeparator = System.getProperty("file.separator");
	private final String lineSeparator = System.getProperty("line.separator");
	private final String logFileMarkerBeginningEnglish = "File";
	private final String logFileMarkerEndEnglish = "written.";
	private final String logFileMarkerBeginningGerman = "Datei";
	private final String logFileMarkerEndGerman = "ausgeschrieben.";
	private final CharSequence pointsXMarker = "xp";
	private final CharSequence pointsYMarker = "yp";
	private final CharSequence pointsHMarker = "hp";
	private final CharSequence gxMarker = "gx";
	private final CharSequence gyMarker = "gy";
	private final String hghbVar = "hghb";
	private final String countVar = "count";
	private final String timeperiodFileName = "zeitreihe.dmna";
	private final String austal2000FileName = "austal2000.txt";
	private final String tmpDir = System.getenv("TMP");
//	private final String tmpDir = System.getProperty("java.io.tmpdir");//TODO: maybe use apache temp. for that remove white space from path
	private String workDirPath = tmpDir + fileSeparator + "PO" + fileSeparator;
	private String austalHome = "";
	private List<String> errors = new ArrayList<String>();
	private GeometryFactory geomFactory;
	public static final int BUFFER = 2048;
	
	public Austal2000Algorithm(){		
		geomFactory = JTSFactoryFinder.getGeometryFactory(null);
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("Austal_Home")){
				austalHome = property.getStringValue();
				System.out.println(austalHome);
				break;
			}
		}		
	}
	
	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(id.equals(inputIDStreetEmissions)){
			return LiteralDoubleBinding.class;
		}else if(id.equals(inputIDStartTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDReceptorPoints)){
			return GTVectorDataBinding.class;
		}else{
			return GenericFileDataBinding.class;			
		}
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		return GTVectorDataBinding.class;
//		return GenericFileDataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		//1.a get input
		Map<String, IData> result = new HashMap<String, IData>();			

		String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
		String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
		
		String endpointURL = "http://" + host + ":" + hostPort+ "/" + 
		//WPSConfiguration.getInstance().getProperty(WebProcessingService.PROPERTY_NAME_HOST_PORT) + "/" + 
		WebProcessingService.WEBAPP_PATH + "/";
		
		File workDir = new File(workDirPath);
		
		if(!workDir.exists()){
			workDir.mkdir();
		}
		
		List<IData> receptorPointsDataList = inputData.get(inputIDReceptorPoints);
		if(receptorPointsDataList == null || receptorPointsDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = receptorPointsDataList.get(0);
		
		File austal2000File = null;
		
		if(firstInputData instanceof GenericFileDataBinding){
			GenericFileData genericInputData = ((GenericFileDataBinding)firstInputData).getPayload();
		
			String fileName = genericInputData.writeData(workDir);
			
			File tmpFile = new File(fileName);
			
			String newFileName = fileName.substring(0, fileName.lastIndexOf(fileSeparator));
			
			newFileName = newFileName.concat(fileSeparator + "austal2000.txt");
			
			austal2000File = new File(newFileName);
			
			boolean success = tmpFile.renameTo(austal2000File);
			
			System.out.println("created austal2000.txt " + success);
			
		} else if (firstInputData instanceof GTVectorDataBinding) {
			
			//insert point coordinates in austal2000.txt
			String output = "";

			int coordinateCount = 0;
			
			try {
				
				
				URL austal2000FileURL = new URL(
						endpointURL + "res/" + austal2000FileName);

				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(austal2000FileURL.openStream()));

				String line = bufferedReader.readLine();

				int gx = 0;
				int gy = 0;

				String xp = "";
				String yp = "";
				// String hp = "";
				
				while (line != null) {

//					System.out.println(line);

					if (line.contains(pointsXMarker)) {

						FeatureCollection<?, ?> featColl = ((GTVectorDataBinding) firstInputData)
								.getPayload();

						FeatureIterator<?> iterator = featColl.features();//Differentiate between point and line
												
						while (iterator.hasNext()) {

							SimpleFeature feature = (SimpleFeature) iterator.next();

							
							if(feature.getDefaultGeometry() instanceof com.vividsolutions.jts.geom.Point){
							
							Coordinate coord = ((Geometry)feature.getDefaultGeometry())
									.getCoordinate();

							xp = xp.concat("" + (coord.x - gx) + " ");
							yp = yp.concat("" + (coord.y - gy) + " ");
							// hp = hp.concat("" + coord.z + " ");
							}else if(feature.getDefaultGeometry() instanceof MultiLineString){
								
								MultiLineString lineString = (MultiLineString) feature
										.getDefaultGeometry();


								coordinateCount = lineString.getCoordinates().length;
								
								for (int i = 0; i < lineString.getCoordinates().length; i++) {
									
									if(i == 20){
										coordinateCount = 20;
										break;//TODO: check if we can go higher.
									}
									
									Coordinate coord = lineString
											.getCoordinates()[i];

									xp = xp.concat("" + (coord.x - gx) + " ");
									yp = yp.concat("" + (coord.y - gy) + " ");
								}
							}else if(feature.getDefaultGeometry() instanceof LineString){
								
								LineString lineString = (LineString) feature
										.getDefaultGeometry();

								coordinateCount = lineString.getCoordinates().length;
								
								for (int i = 0; i < lineString.getCoordinates().length; i++) {
									
									if(i == 20){
										coordinateCount = 20;
										break;//TODO: check if we can go higher. corresponding to the count of points
												//we have to add heigh values in austal2000.txt
									}
									
									Coordinate coord = lineString
											.getCoordinates()[i];

									xp = xp.concat("" + (coord.x - gx) + " ");
									yp = yp.concat("" + (coord.y - gy) + " ");
								}
							}

						}

						line = line.concat(" " + xp);
					} else if (line.contains(pointsYMarker)) {
						line = line.concat(" " + yp);
					}
					else if (line.contains(pointsHMarker)) {
						
						String hp = "";
						
						for (int i = 0; i < coordinateCount; i++) {
							hp = hp.concat(" 2");//for now just add height of two meters
						}
						
						line = line.concat(" " + hp);
					} 
					else if (line.contains(gxMarker)) {
						gx = Integer.valueOf(line.replace(gxMarker, "").trim());
					} else if (line.contains(gyMarker)) {
						gy = Integer.valueOf(line.replace(gyMarker, "").trim());
					}
					output = output.concat(line + "\n");
					line = bufferedReader.readLine();
				}

				bufferedReader.close();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// write zeitreihe.dmna to tmpDir

			austal2000File = new File(workDirPath + fileSeparator
					+ austal2000FileName);

			try {
				BufferedWriter bufferedWriter = new BufferedWriter(
						new FileWriter(austal2000File));

				bufferedWriter.write(output);

				bufferedWriter.flush();
				bufferedWriter.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		List<IData> streetEmissionDataList = inputData.get(inputIDStreetEmissions);
		
		if(!(streetEmissionDataList == null) && streetEmissionDataList.size() != 1){

//			IData secondInputData = streetEmissionDataList.get(0);
			
		}
		
		List<IData> meteorologyDataList1 = inputData.get(inputIDMeteorology);
		
		if(!(meteorologyDataList1 == null) && meteorologyDataList1.size() != 1){

//			IData secondInputData = meteorologyDataList1.get(0);
			
		}
		
		List<IData> startTimeDataList = inputData.get(inputIDStartTime);
		if(startTimeDataList == null || startTimeDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		String startTime = "";
		String endTime = "";
		
		long startTimeMillis = 0;
		long endTimeMillis = 0;
		
		IData startTimeData = startTimeDataList.get(0);
		
		if(startTimeData instanceof LiteralDateTimeBinding){
			
			LiteralDateTimeBinding dateTimeBinding = (LiteralDateTimeBinding)startTimeData;
			
			startTimeMillis = dateTimeBinding.getDate().getTime();
			
			String timeStamp = dateTimeBinding.getTimestamp().toString();
			
			timeStamp = timeStamp.substring(0, timeStamp.lastIndexOf('.'));
			
			timeStamp = timeStamp.trim();
			
			timeStamp = timeStamp.replace(" ", ".");
			
			startTime = timeStamp;
			
		}
		
		List<IData> endTimeDataList = inputData.get(inputIDEndTime);
		if(endTimeDataList == null || endTimeDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		IData endTimeData = endTimeDataList.get(0);
		
		if(endTimeData instanceof LiteralDateTimeBinding){
			
			LiteralDateTimeBinding dateTimeBinding = (LiteralDateTimeBinding)endTimeData;
			
			endTimeMillis = dateTimeBinding.getDate().getTime();
			
			String timeStamp = dateTimeBinding.getTimestamp().toString();
			
			timeStamp = timeStamp.substring(0, timeStamp.lastIndexOf('.'));
			
			timeStamp = timeStamp.trim();
			
			timeStamp = timeStamp.replace(" ", ".");
			
			endTime = timeStamp;
			
		}
		
		//compute number of ours of the timeperiod
		
		long diff = endTimeMillis - startTimeMillis;
		
		long diffHours = diff / (60 * 60 * 1000);
		
		
		//cut zeitreihe.txt to timeperiod starttime - endtime
		
		if(meteorologyDataList1 == null || meteorologyDataList1.size() == 0){
			
			String output = "";
			
			try {
				URL timeperiodFileURL = new URL( endpointURL + "res/" + timeperiodFileName);//TODO: make configurable
				
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(timeperiodFileURL.openStream()));
				
				String line = bufferedReader.readLine();	
				
				boolean withinTimeperiod = false;
				boolean variables = true;//file always starts with some variables
				
				while(line != null){
					
//					System.out.println(line);
					
					if(variables){
						
						if(line.contains(hghbVar)){
							
							output = output.concat(line.replace(countVar, "" + diffHours) + lineSeparator);
							
							output = output.concat("*" + lineSeparator);//add one asterisk
							output = output.concat("" + lineSeparator);//add one empty line
							variables = false;
							line = bufferedReader.readLine();
							continue;
						}else{						
							output = output.concat(line + lineSeparator);
						}	
						
					}
					
					if (line.contains(startTime)) {
						
						withinTimeperiod = true;
					}
					
					if(withinTimeperiod){
						
						if(!line.contains(endTime)){
						output = output.concat(line + lineSeparator);
						}else{
							output = output.concat("***");//end of file
							break;
						}
					}
					
					line = bufferedReader.readLine();
				}
				
				bufferedReader.close();
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//write zeitreihe.dmna to tmpDir
			
			File timeperiodFile = new File(workDirPath + fileSeparator + timeperiodFileName);
			
			try {
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(timeperiodFile));
				
				bufferedWriter.write(output);
				
				bufferedWriter.flush();
				bufferedWriter.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		//2. execute austal2000
		
		try {

//			File parentFile = workDir.getParentFile();
			
//			String path = parentFile.getAbsolutePath() + fileSeparator;
			
			String command = austalHome + fileSeparator + "austal2000.exe " + workDir.getAbsolutePath();
			
			Runtime rt = Runtime.getRuntime();
			
			Process proc = rt.exec(command);
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc
					.getErrorStream(), "ERROR");
			
			errorGobbler.setSubject(this);
			
			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(proc
					.getInputStream(), "OUTPUT");

			outputGobbler.setSubject(this);
			
			// kick them off
			errorGobbler.start();
			outputGobbler.start();
			
			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//parse logfile and extract filenames TODO: maybe better list files in directory...			
		
		ArrayList<String> fileList = new ArrayList<String>();
		
		try{
			
			File logFile = new File(workDirPath + fileSeparator + "austal2000.log");
			
			BufferedReader bufferedFileReader = new BufferedReader(new FileReader(logFile));
			
			String line = bufferedFileReader.readLine();
			
			while(line != null){
				
				if(line.contains(logFileMarkerBeginningEnglish)&&line.contains(logFileMarkerEndEnglish)){
					
					int beginning = line.indexOf(logFileMarkerBeginningEnglish);
					int end = line.indexOf(logFileMarkerEndEnglish);
					
					String fileName = line.substring(beginning, end);
					
					fileName = fileName.replace("\"", "");
					
					fileName = fileName.replace(logFileMarkerBeginningEnglish, "");
					
					fileName = fileName.trim();
					
					fileName = fileName.concat(".dmna");
					
					// store in list
					if (!fileList.contains(fileName)) {

						fileList.add(fileName);
					}
					
				}else if(line.contains(logFileMarkerBeginningGerman)&&line.contains(logFileMarkerEndGerman)){
					
					int beginning = line.indexOf(logFileMarkerBeginningGerman);
					int end = line.indexOf(logFileMarkerEndGerman);
					
					String fileName = line.substring(beginning, end);
					
					fileName = fileName.replace("\"", "");
					
					fileName = fileName.replace(logFileMarkerBeginningGerman, "");
					
					fileName = fileName.trim();
					
					fileName = fileName.concat(".dmna");
					
					// store in list
					if (!fileList.contains(fileName)) {

						fileList.add(fileName);
					}
				}
				
				line = bufferedFileReader.readLine();
			}
			
			bufferedFileReader.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}		
		
		//3. return output
		
		try {			
			
			//zip the output
			
//			String[] files = fileList.toArray(new String[fileList.size()]);
			
			parseAustalOutput();
			
			AustalOutputReader austal = new AustalOutputReader();
			
			ArrayList<Point[]> points = austal.createPoints(workDirPath, true);

			FeatureCollection<?, SimpleFeature> fOut = createFeatureCollection(points);

			result.put(outputIDResult,
					new GTVectorDataBinding(fOut));
			
//			// loop through point arrays
//			for (int j = 0; j < points.size(); j++) {
//				Point[] p = points.get(j);
//				for (int i = 0; i < p.length; i++) {
//					ArrayList<Value> vals = p[i].values();				
//					
//					for (int k = 0; k < vals.size(); k++) {
//
//						// add values from timeseries
//						// out.write(p[i].get_fid() + "," +
//						// vals.get(k).TimeStamp()
//						// + "," + vals.get(k).PM10val());
//						// out.newLine();
//					}
//					
//					double[] coords = p[i].coordinates();
//					
//					Coordinate coord = new Coordinate(coords[0], coords[1]);
//					
//					
////					out.write(p[i].get_fid() + "," + coords[0] + "," + coords[1]);
////					out.newLine();
//					
//				}
//
//			}		
			
									
			
//			File zipFile = zipFiles(files);			
//			
//			GenericFileData outputData_2 = new GenericFileData(zipFile, GenericFileDataConstants.MIME_TYPE_ZIP);
//
//			GenericFileDataBinding outputBinding_2 = new GenericFileDataBinding(outputData_2);
//
//			result.put(outputID_ZIP, outputBinding_2);
//			
//			File[] tmpFiles = workDir.listFiles();
//			
//			for (File file : tmpFiles) {
//				System.out.println("deleted " + file.getAbsolutePath() + " " + file.delete());
//			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private FeatureCollection<?, SimpleFeature> createFeatureCollection(ArrayList<Point[]> pois) {
		FeatureCollection<?, SimpleFeature> collection = FeatureCollections.newCollection();
		//SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		//typeBuilder.setName("gmlPacketFeatures");
		SimpleFeatureType featType = null;
		SimpleFeature feature = null;
			
		// build attributes for all timestamps
		ArrayList<Value> allValues = pois.get(0)[0].values();//take values from first point...hopefully they are all the same				
		String atts = "";
		for (Value value : allValues) {
			atts += (", "+value.TimeStamp()+":String");
			//attributeType = org.geotools.feature.AttributeTypeFactory
			//.newAttributeType(value.TimeStamp(), String.class);//TODO: hier die timestamps als feldnamen!?
			//typeFactory.addType(attributeType);
		}
		
		try {
			featType = DataUtilities.createType("Point", "geom:Point, f_id:String"+atts);
		} catch (SchemaException e) {
			e.printStackTrace();
		}	
		SimpleFeatureBuilder featBuilder = new SimpleFeatureBuilder(featType);
		
		//geom = org.geotools.feature.AttributeTypeFactory.newAttributeType(
		//		"Point", com.vividsolutions.jts.geom.Point.class);
		//typeFactory.addType(geom);	
		//AttributeType attributeType;	
		//attributeType = org.geotools.feature.AttributeTypeFactory
		//.newAttributeType("f_id", String.class);
		//typeFactory.addType(attributeType);
					
		
		// loop through point arrays
		for (int j = 0; j < pois.size(); j++) {
			Point[] p = pois.get(j);
			for (int i = 0; i < p.length; i++) {
				ArrayList<Value> vals = p[i].values();				
				double[] coords = p[i].coordinates();
				
				// get coordinates and create point
				Coordinate coord = new Coordinate(coords[0], coords[1]);				
				com.vividsolutions.jts.geom.Point point = geomFactory.createPoint(coord);				
				
				// collect properties for attributes
				ArrayList<Object> properties = new ArrayList<Object>(allValues.size());				
				properties.add(point);				
				properties.add(p[i].get_fid());
		
				for (int k = 0; k < vals.size(); k++) {								
					properties.add(vals.get(k).PM10val());					
				}

				try {
					featBuilder.add(properties);
					feature = featBuilder.buildFeature(null);
					collection.add(feature);
					
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}

		}
		return collection;
	}
	
	private void parseAustalOutput(){
		
		AustalOutputReader austal = new AustalOutputReader();
		String destination = workDirPath + fileSeparator + "csvOutput";//"C:/SOS/AUSTAL/values";
		
		File destinationDir = new File(destination);
		
		if(!destinationDir.exists()){
			destinationDir.mkdir();
		}
		
		austal.readAustalFiles(workDirPath, true, destination);		
	}
	
	
	public File zipFiles(String[] files){
		
//		String result = "c:\\temp\\work\\result.zip";
		String result = tmpDir + fileSeparator + "result.zip";
		
		 try {
	         BufferedInputStream origin = null;
	         FileOutputStream dest = new 
	           FileOutputStream(result);
	         ZipOutputStream out = new ZipOutputStream(new 
	           BufferedOutputStream(dest));
	         //out.setMethod(ZipOutputStream.DEFLATED);
	         byte data[] = new byte[BUFFER];

	         for (int i=0; i<files.length; i++) {
	            System.out.println("Adding: "+files[i]);
	            
	            File f = new File(files[i]);
	            
	            FileInputStream fi = new 
	              FileInputStream(f);
	            origin = new 
	              BufferedInputStream(fi, BUFFER);
	            	            
	            ZipEntry entry = new ZipEntry(f.getName());
	            out.putNextEntry(entry);
	            int count;
	            while((count = origin.read(data, 0, 
	              BUFFER)) != -1) {
	               out.write(data, 0, count);
	            }
	            origin.close();
	         }
	         out.close();
	      } catch(Exception e) {
	         e.printStackTrace();
	      }

	      File resultingFile = new File(result);
	      
	      if(!resultingFile.exists()){
	    	  return null;
	      }
	      
	      return resultingFile;		
	}
}
