package org.uncertweb.austalwps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.austalwps.util.AustalOutputReader;
import org.uncertweb.austalwps.util.Point;
import org.uncertweb.austalwps.util.Value;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Austal2000Algorithm extends AbstractObservableAlgorithm{

	private static Logger LOGGER = Logger.getLogger(Austal2000Algorithm.class);
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
			return OMDataBinding.class;
		}else if(id.equals(inputIDMeteorology)){
			return OMDataBinding.class;		}
		else if(id.equals(inputIDStartTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDEndTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDReceptorPoints)){
			return GTVectorDataBinding.class;
		}else{
			return GenericFileDataBinding.class;			
		}
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		return OMDataBinding.class;
//		return GTVectorDataBinding.class;
//		return GenericFileDataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {		

		XBObservationEncoder encoder = new XBObservationEncoder();
		
		//1.a get input
		Map<String, IData> result = new HashMap<String, IData>();	
		
		List<IData> streetEmissionDataList = inputData.get(inputIDStreetEmissions);
		
		if(!(streetEmissionDataList == null) && streetEmissionDataList.size() != 0){

			IData streetEmissionData = streetEmissionDataList.get(0);
			
			if(streetEmissionData instanceof OMDataBinding){
				
				OMData theData = (OMData) ((OMDataBinding)streetEmissionData).getPayload();
				
				List<? extends AbstractObservation> observations = theData.getObservationCollection().getObservations();
				
				for (AbstractObservation abstractObservation : observations) {
					Coordinate[] coordinates = abstractObservation.getFeatureOfInterest().getShape().getCoordinates();
					
					LOGGER.debug(coordinates[0] + " " + coordinates[1]);
					
					try {
						System.out.println(encoder.encodeObservation(abstractObservation));
					} catch (OMEncodingException e) {
						e.printStackTrace();
					}					
				}				
			}			
		}
		
		List<IData> meteorologyDataList = inputData.get(inputIDMeteorology);
		
		if(!(meteorologyDataList == null) && meteorologyDataList.size() != 0){

			IData meteorologyData = meteorologyDataList.get(0);
			
			if(meteorologyData instanceof OMDataBinding){
				
				OMData theData = (OMData) ((OMDataBinding)meteorologyData).getPayload();
				
				List<? extends AbstractObservation> observations = theData.getObservationCollection().getObservations();
				
				for (AbstractObservation abstractObservation : observations) {
					Coordinate[] coordinates = abstractObservation.getFeatureOfInterest().getShape().getCoordinates();
					
					LOGGER.debug(coordinates[0] + " " + coordinates[1]);
					
					try {
						System.out.println(encoder.encodeObservation(abstractObservation));
					} catch (OMEncodingException e) {
						e.printStackTrace();
					}					
				}				
			}			
		}
		
		List<IData> receptorPointsDataList = inputData.get(inputIDMeteorology);
		
		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){

			IData receptorPointsData = receptorPointsDataList.get(0);
			
			if(receptorPointsData instanceof GTVectorDataBinding){
				
				FeatureCollection<?, ?> theData = ((GTVectorDataBinding)receptorPointsData).getPayload();
				
				
			}			
		}
		
		try {

			IObservationCollection mcoll = createResultCollection();
			
			OMData omd = new OMData(mcoll);
			
			result.put(outputIDResult, new OMDataBinding(omd));
			
			return result;
		} catch (Exception e) {
			
		}		
				
		FeatureCollection<?, SimpleFeature> collection = FeatureCollections.newCollection();
		
		result.put("result", new GTVectorDataBinding(collection));
		
		return result;
	}
	
	private IObservationCollection createResultCollection() throws URISyntaxException{
		
		MeasurementCollection mcoll = new MeasurementCollection();			
		
		AustalOutputReader austal = new AustalOutputReader();
		
		ArrayList<Point[]> points = austal.createPoints("C:/UncertWeb/workspace/AustalWPS/src/test/resources", true);
		
		URI procedure = new URI("http://www.uncertweb.org/models/austal2000");
		URI observedProperty = new URI("http://www.uncertweb.org/phenomenon/pm10");
		
		URI codeSpace = new URI("");
		
		for (int j = 0; j < points.size(); j++) {
			
			Point[] p = points.get(j);
			
			for (int i = 0; i < p.length; i++) {
				
				ArrayList<Value> vals = p[i].values();				
				double[] coords = p[i].coordinates();
				
				// get coordinates and create point
				Coordinate coord = new Coordinate(coords[0], coords[1]);								
				
				PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);
				
				GeometryFactory geomFac = new GeometryFactory(pMod, 31467);
				SpatialSamplingFeature featureOfInterest = new SpatialSamplingFeature("sampledFeature", geomFac.createPoint(coord));
				featureOfInterest.setIdentifier(new Identifier(codeSpace, "point" + i));
				for (int k = 0; k < vals.size(); k++) {	
					Identifier identifier = new Identifier(codeSpace, "m" + k);
					
					String timeStamp = vals.get(k).TimeStamp().trim();
					
					timeStamp = timeStamp.replace(" ", "T");
					
					TimeObject phenomenonTime = new TimeObject(timeStamp);						
					MeasureResult resultm = new MeasureResult(vals.get(k).PM10val(), "");						
					Measurement m1 = new Measurement(identifier, null, phenomenonTime, phenomenonTime, null, procedure, observedProperty, featureOfInterest, null, resultm);
					mcoll.addObservation(m1);
				}					
			}
		}
		return mcoll;
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
		
		String result = tmpDir + fileSeparator + "result.zip";
		
		 try {
	         BufferedInputStream origin = null;
	         FileOutputStream dest = new 
	           FileOutputStream(result);
	         ZipOutputStream out = new ZipOutputStream(new 
	           BufferedOutputStream(dest));
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
