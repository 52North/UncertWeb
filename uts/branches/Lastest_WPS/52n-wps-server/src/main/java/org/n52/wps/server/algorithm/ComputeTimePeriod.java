package org.n52.wps.server.algorithm;

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

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.datahandler.xml.GTHelper;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.util.StreamGobbler;
import org.n52.wps.util.austal2.AustalOutputReader;
import org.n52.wps.util.austal2.Point;
import org.n52.wps.util.austal2.Value;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class ComputeTimePeriod extends AbstractAlgorithm{

	private static Logger LOGGER = Logger.getLogger(ComputeTimePeriod.class);
//	private final String inputIDXQ = "XQ";
//	private final String inputIDYQ = "YQ";
//	private final String inputIDHQ = "HQ";
	private final String inputIDPQ = "PQ";
	private final String inputIDFeatures = "FEATURES";
	private final String inputIDTimeperiod = "TIMEPERIOD";
	private final String inputIDStartTime = "START_TIME";
	private final String inputIDEndTime = "END_TIME";
	private final String outputID_1 = "RESULT_1";
	private final String outputID_2 = "RESULT_2";
	private final String outputID_ZIP = "RESULT_ZIP";
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
	
	public ComputeTimePeriod(){		
		geomFactory = new GeometryFactory();
		
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
	public Class getInputDataType(String id) {
		if(id.equals(inputIDPQ)){
			return LiteralDoubleBinding.class;
		}else if(id.equals(inputIDStartTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDEndTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDFeatures)){
			return GTVectorDataBinding.class;
		}else{
			return GenericFileDataBinding.class;			
		}
	}

	@Override
	public Class getOutputDataType(String id) {
		return GTVectorDataBinding.class;
//		return GenericFileDataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		//1.a get input
		Map<String, IData> result = new HashMap<String, IData>();		
		
		File workDir = new File(workDirPath);
		
		if(!workDir.exists()){
			workDir.mkdir();
		}
		
		List<IData> firstDataList = inputData.get(inputIDFeatures);
		if(firstDataList == null || firstDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = firstDataList.get(0);
		
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

			try {
				URL austal2000FileURL = new URL(
						"http://localhost:8080/wps/res/" + austal2000FileName);

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

						FeatureCollection featColl = ((GTVectorDataBinding) firstInputData)
								.getPayload();

						FeatureIterator iterator = featColl.features();//Differentiate between point and line

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

								for (int i = 0; i < lineString.getCoordinates().length; i++) {
									
									if(i == 20){
										break;//TODO: check if we can go higher. corresponding to the count of points
												//we have to add heigh values in austal2000.txt
									}
									
									Coordinate coord = lineString
											.getCoordinates()[i];

									xp = xp.concat("" + (coord.x - gx) + " ");
									yp = yp.concat("" + (coord.y - gy) + " ");
								}
							}else if(feature.getDefaultGeometry() instanceof LineString){
								
								LineString lineString = (LineString) feature
										.getDefaultGeometry();

								for (int i = 0; i < lineString.getCoordinates().length; i++) {
									
									if(i == 20){
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
//					else if (line.contains(pointsHMarker)) {
//						line = line.concat(" " + hp);
//					} 
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
		
		List<IData> secondDataList = inputData.get(inputIDTimeperiod);
		
		if(!(secondDataList == null) && secondDataList.size() != 1){

			IData secondInputData = secondDataList.get(0);
			
			File timeperiodFile = null;
			
			if(secondInputData instanceof GenericFileDataBinding){
				GenericFileData genericInputData = ((GenericFileDataBinding)secondInputData).getPayload();

				String fileName = genericInputData.writeData(workDir);
				
				File tmpFile = new File(fileName);
				
				String newFileName = fileName.substring(0, fileName.lastIndexOf(fileSeparator));
				
				newFileName = newFileName.concat(fileSeparator + timeperiodFileName);
				
				timeperiodFile = new File(newFileName);
				
				boolean success = tmpFile.renameTo(timeperiodFile);
				
				System.out.println("created " + timeperiodFileName + " " + success);
				
			}
			
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
		
		if(secondDataList == null || secondDataList.size() == 0){
			
			String output = "";
			
			try {
				URL timeperiodFileURL = new URL("http://localhost:8080/wps/res/" + timeperiodFileName);//TODO: make configurable
				
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

			File parentFile = workDir.getParentFile();
			
			String path = parentFile.getAbsolutePath() + fileSeparator;
			
			String command = austalHome + fileSeparator + "austal2000.exe " + workDir.getAbsolutePath();
			
			Runtime rt = Runtime.getRuntime();
			
			Process proc = rt.exec(command);
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc
					.getErrorStream(), "ERROR");
			
			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(proc
					.getInputStream(), "OUTPUT");

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal;
			try {
				exitVal = proc.waitFor();
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
			
			String[] files = fileList.toArray(new String[fileList.size()]);
			
			parseAustalOutput();
			
			AustalOutputReader austal = new AustalOutputReader();
			
			ArrayList<Point[]> points = austal.createPoints(workDirPath, true);

			FeatureCollection fOut = createFeatureCollection(points);

			result.put("RESULT_ZIP",
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
	
	private FeatureCollection createFeatureCollection(ArrayList<Point[]> pois) {
		
		SchemaRepository.registerSchemaLocation("http://schemas.opengis.net/gml/", "http://schemas.opengis.net/gml/2.1.2/feature.xsd");
		
		FeatureCollection collection = DefaultFeatureCollections.newCollection();
		
//		try {
//		
//		GTHelper.createFeatureType(attributes, newGeometry, uuid, coordinateReferenceSystem);
//		
//			CRS.decode("EPSG:31467");
//		} catch (NoSuchAuthorityCodeException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (FactoryException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		SimpleFeatureBuilder featureBuilder; 
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
//		typeBuilder.setName("featureMember");
		
		typeBuilder.setNamespaceURI("http://www.opengis.net/gml/");
		Name nameType = new NameImpl("http://www.opengis.net/gml/", "Feature");
		typeBuilder.setName(nameType);
		
		
		typeBuilder.add( "Point", com.vividsolutions.jts.geom.Point.class);
		typeBuilder.add( "f_id", String.class);
		
//		DefaultFeatureTypeFactory typeFactory = new DefaultFeatureTypeFactory();
//		typeFactory.setName("gmlPacketFeatures");
//		AttributeType geom;
		SimpleFeatureType fType = null;
				
		SimpleFeature feature = null;
//		
//		geom = org.geotools.feature.AttributeTypeFactory.newAttributeType(
//				"Point", com.vividsolutions.jts.geom.Point.class);
//		typeFactory.addType(geom);
//		
		ArrayList<Value> allValues = pois.get(0)[0].values();//take values from first point...hopefully they are all the same
//		AttributeType attributeType;
//		
//		attributeType = org.geotools.feature.AttributeTypeFactory
//		.newAttributeType("f_id", String.class);
		
//		typeFactory.addType(attributeType);
		
		for (Value value : allValues) {
//			attributeType = org.geotools.feature.AttributeTypeFactory
//			.newAttributeType(value.TimeStamp(), String.class);//TODO: hier die timestamps als feldnamen!?
//			typeFactory.addType(attributeType);
			typeBuilder.add( "D" +value.TimeStamp().replace(" ", "T").replace(":", "-"), String.class);
		}
		
//		try {
//			fType = typeFactory.getFeatureType();
//		} catch (SchemaException e) {
//			e.printStackTrace();
//		}				
		
		fType = typeBuilder.buildFeatureType();
		
//		 QName qname = GTHelper.createGML3SchemaForFeatureType(fType);
//		 SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());
		
		featureBuilder = new SimpleFeatureBuilder(fType);
		
		// loop through point arrays
		for (int j = 0; j < pois.size(); j++) {
			Point[] p = pois.get(j);
			for (int i = 0; i < p.length; i++) {
				ArrayList<Value> vals = p[i].values();
				
				double[] coords = p[i].coordinates();
				
				Coordinate coord = new Coordinate(coords[0], coords[1]);
				
				Geometry geom1 = geomFactory.createPoint(coord);
				
				ArrayList<Object> properties = new ArrayList<Object>(allValues.size());
				
				properties.add(geom1);
				
//				properties.add(p[i].get_fid());
				
				for (int k = 0; k < vals.size(); k++) {			
					
					properties.add(vals.get(k).PM10val());					
				}

				try {
					
					feature = featureBuilder.buildFeature(p[i].get_fid(), properties.toArray());//scheint so korrekt gemapped zu werden TODO: evtl. sicherere methode wählen
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
	
	
	private File zipFiles(String[] files){
		
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

//	@Override
//	public List<String> getInputIdentifiers() {
//		List<String> identifierList =  new ArrayList<String>();
//		identifierList.add(inputIDFeatures);
//		identifierList.add(inputIDPQ);
//		identifierList.add(inputIDStartTime);
//		identifierList.add(inputIDEndTime);
//		return identifierList;
//	}
//
//	@Override
//	public List<String> getOutputIdentifiers() {
//
//		List<String> identifierList =  new ArrayList<String>();
//		identifierList.add(outputID_ZIP);
//		return identifierList;
//	}
}
