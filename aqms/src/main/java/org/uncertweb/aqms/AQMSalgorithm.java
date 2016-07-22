package org.uncertweb.aqms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.OutputDataType;

import org.geotools.feature.FeatureCollection;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.aqms.austal.AustalInputs;
import org.uncertweb.aqms.austal.AustalModelRun;
import org.uncertweb.aqms.austal.AustalProperties;
import org.uncertweb.aqms.overlay.TotalConcentration;
import org.uncertweb.aqms.util.Utils;
import org.w3c.dom.Node;

import ucar.nc2.NetcdfFile;


/**
 * Class to execute Air Quality Model Service for Münster
 * @author l_gerh01
 *
 */

public class AQMSalgorithm extends AbstractObservableAlgorithm{

	private List<String> errors = new ArrayList<String>();
	
	// WPS inputs & outputs
	private final String inputIDReceptorPoints = "receptor-points";
	private final String inputIDStartTime = "start-time";
	private final String inputIDEndTime = "end-time";
	private final String inputIDAustalRealisations = "NumbAustalRuns";
	private final String inputIDBackgroundSamples = "NumbBackgroundSamples";
	private final String inputIDOutputUncertainty = "OutputUncertaintyType";
	private final String outputIDResult = "result";
		
	// input variables
	private DateTime startDate, endDate, preStartDate;
	private DateTime minDate = ISODateTimeFormat.dateTime().parseDateTime("2008-07-01T01:00:00.000+01");
	private DateTime maxDate = ISODateTimeFormat.dateTime().parseDateTime("2010-07-01T00:00:00.000+01");
	private int numberOfAustalRealisations, numberOfBackgroundSamples;
	private boolean netcdf = false;
	private List<String> statList = new ArrayList<String>();
	
	// system variables
	private static String resourcesPath = "C:\\WebResources\\AQMS";	
	private static String resultsPath = "C:\\WebResources\\AQMS\\outputs";		
	
	private static String sosURL = "http://localhost:8080/ubaSOS/sos";
	private static String utsURL = "http://giv-uw2.uni-muenster.de:8080/uts/WebProcessingService";
	private static String upsURL = "http://localhost:8080/ups/WebProcessingService";
	private static String austalURL = "http://localhost:8080/austalWPS/WebProcessingService";
	private static String interpolationURL = "http://localhost:8080/aqms/WebProcessingService";	
	
	private int austalThr = 10;
	private int bgThr = 10;
	private int dayThr = 14;
	
	public AQMSalgorithm(){
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			if (property.getName().equalsIgnoreCase("Resources")){
				resourcesPath = property.getStringValue();
				resultsPath = resourcesPath + "\\outputs";
				break;
			}
		}	
	}
	
	
	public List<String> getErrors() {
		return errors;
	}

	public Class<?> getInputDataType(String id) {
		if(id.equals(inputIDStartTime)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDEndTime)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDAustalRealisations)){
			return LiteralIntBinding.class;
		}else if(id.equals(inputIDBackgroundSamples)){
			return LiteralIntBinding.class;
		}else if(id.equals(inputIDOutputUncertainty)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDReceptorPoints)){
			return GTVectorDataBinding.class;
		}else{
			return GenericFileDataBinding.class;			
		}
	}

	public Class<?> getOutputDataType(String arg0) {
		return UncertWebIODataBinding.class;
	}

	public Map<String, IData> run(Map<String, List<IData>> inputMap) {
		// process parameters
		boolean useUPS = false;
		boolean saveOutputs = true;
		
		//1) INPUT HANDLING
		DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
		
		// 1.1) start and end date
		List<IData> startList = inputMap.get(inputIDStartTime);		
		String startTime = "2010-03-01T01:00:00.000+01";
		if(startList!=null){
			startTime = ((IData)startList.get(0)).getPayload().toString();
		}
		
		List<IData> endList = inputMap.get(inputIDEndTime);
		String endTime = "2010-03-05T00:00:00.000+01";
		if(endList!=null){
			endTime = ((IData)endList.get(0)).getPayload().toString();
		}
		
		startDate = dateFormat.parseDateTime(startTime);
		preStartDate = startDate.minusDays(1);	
		endDate = dateFormat.parseDateTime(endTime);	
		
		// 1.2) realisation numbers
		List<IData> austalNumbList = inputMap.get(inputIDAustalRealisations);
		if(austalNumbList!=null){
			numberOfAustalRealisations = Integer.parseInt(((IData)austalNumbList.get(0)).getPayload().toString());
		}
		
		List<IData> backgroundNumbList = inputMap.get(inputIDBackgroundSamples);
		if(austalNumbList!=null){
			numberOfBackgroundSamples = Integer.parseInt(((IData)backgroundNumbList.get(0)).getPayload().toString());
		}
		
		// 1.3) receptor points
		List<IData> receptorPointsDataList = inputMap.get(inputIDReceptorPoints);	
		FeatureCollection<?,?> receptorPoints = null;
		if(!(receptorPointsDataList == null) && receptorPointsDataList.size() != 0){
			netcdf = false;
			IData receptorPointsData = receptorPointsDataList.get(0);	
			if(receptorPointsData instanceof GTVectorDataBinding){			
				receptorPoints = (FeatureCollection<?, ?>)receptorPointsData.getPayload();
			}
		}else{
			netcdf = true;	
			try {
				throw new IOException("NetCDF is currently not supported. Please provide receptor points for model run.");
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		// 1.4) output statistic parameters
		List<IData> statisticsList = inputMap.get(inputIDOutputUncertainty);
		if(!(statisticsList == null) && statisticsList.size() != 0){
			statList = extractStatisticsFromRequest(statisticsList);			
		}		
		
		// objects for processing
		AustalModelRun austal = new AustalModelRun(austalURL, resourcesPath+"\\Austal");	
		TotalConcentration totalConc = new TotalConcentration(utsURL, startDate, endDate);	
		
		Map<String, IData> result = new HashMap<String, IData>();
		UncertaintyObservationCollection backgroundColl = null;
		UncertaintyObservationCollection austalColl= null;
		NetcdfUWFile austalNCFile = null;
		
		
		// 2) BACKGROUND INTERPOLATION 
		// call interpolation algorithm	   
    	backgroundColl = callInterpolationAlgorithm();
    	if(saveOutputs)
    		Utils.writeObsColl(backgroundColl, resultsPath + "\\UBA_"+startDate.toString("yyyy-MM")+".xml"); 		
		//uncomment for local use
    	//backgroundColl = (UncertaintyObservationCollection) Utils.readObsColl(resultsPath + "\\UBA_"+startDate.toString("yyyy-MM")+".xml");		
		
    	     			     	
		// 3) AUSTAL2000 RUN  
    	// do not use UPS
    	if(!useUPS){
    		 // Austal execution
            if(!netcdf){
            	// A) OM case  
            	austal.setReceptorPoints(receptorPoints);
            	austalColl = austal.executeU_AustalWPSOM(startDate, endDate, numberOfAustalRealisations);                         
            	if(saveOutputs)
	    			Utils.writeObsColl(austalColl, resourcesPath + "\\Austal_"+startDate.toString("yyyy-MM")+".xml");        	
            	//uncomment for local use
            	//austalColl = (UncertaintyObservationCollection) Utils.readObsColl(resultsPath + "\\Austal_"+startDate.toString("yyyy-MM")+".xml");   	        
            }
            else{
              // B) NetCDF case
            	//TODO: implement NetCDF support
           //    austalNCFile = austal.executeU_AustalWPSNetCDF();             	
            	//uncomment for local use      
//    		  	try {
//    		  		NetcdfFile ncfile = NetcdfFile.open("C:/UncertWeb/Austal/austal.nc");
//    		  		austalNCFile = new NetcdfUWFile(ncfile);
//    		  	} catch (IOException ioe) {
//    		  		System.out.println("trying to open " + ""+ " " + ioe);
//    		  	} catch (NetcdfUWException e) {
//    				e.printStackTrace();
//    			  }  
    	    }    	            
    	}
    	
    	// use UPS
    	else{
    		// 3.1) prepare Austal inputs
    		AustalInputs austalInputs = new AustalInputs(preStartDate, endDate, resourcesPath+"\\Austal\\");
    		austalInputs.prepareUncertainInputs();		
    		austalInputs.prepareCertainInputs();   		
    		AustalProperties austalProps = null;
        	try {
    			austalProps = new AustalProperties(resourcesPath+"\\Austal\\"+"austal.props");
    		} catch (FileNotFoundException e1) {
    			e1.printStackTrace();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
        	
        	// 3.2) Austal execution
            if(!netcdf){
            	// A) OM case
            	austal.setReceptorPoints(receptorPoints);
                austalColl = austal.executeUPSOM(upsURL, numberOfAustalRealisations, austalProps);                               
                if(saveOutputs)
                	Utils.writeObsColl(austalColl, resourcesPath + "\\Austal_"+startDate.toString("yyyy-MM")+".xml");        	                
             }
            else{
              // B) NetCDF case  
            	//TODO: implement NetCDF support
         //    austalNCFile = austal.executUPSNetCDF();   	            	  	
        	  }
    	}
				 	
   	 
    	// Finally
        if(!netcdf){
        	 // 4) TOTAL CONCENTRATION
      	    UncertaintyObservationCollection resultColl = totalConc.overlayOM(backgroundColl, austalColl, numberOfBackgroundSamples, statList);               	
      	    if(saveOutputs)
      	    	Utils.writeObsColl(resultColl, resultsPath + "\\AQMS_"+startDate.toString("yyyy-MM")+".xml"); 		          	    
      
      	    // 5) OUTPUT
        	OMBinding omd = new OMBinding(resultColl);
        	result.put(outputIDResult, omd);	
        	return result;  
    	}else{
    		 // 4) TOTAL CONCENTRATION
    		NetcdfUWFile resultNCFile = totalConc.overlayNetCDF(backgroundColl, austalNCFile, "C:/UncertWeb/Austal/overlay.nc", numberOfBackgroundSamples);         	
        
         	// 5) OUTPUT
    		NetCDFBinding uwData = new NetCDFBinding(resultNCFile);
    		result.put(outputIDResult, uwData);
    		return result;
    	}
     	
	}

	
	/**
	 * Checks id selected dates are within ranges
	 * @throws IOException
	 */
	private void checkDates() throws IOException{
		// Check if period is at least 2 days
		if(Days.daysBetween(startDate, endDate).getDays()<2){
			endDate = startDate.plusDays(2);
			throw new IOException("Requested period must at least be 2 days!");
		}else if(Days.daysBetween(startDate, endDate).getDays()>dayThr){
			throw new IOException("Requested period cannot be more than "+dayThr+" days!");
		}
						
		// Check if dates are within available data range
		if(preStartDate.isBefore(minDate)){
			preStartDate = minDate;
			startDate = preStartDate.plusDays(1);
			if(Days.daysBetween(startDate, endDate).getDays()<2)
				endDate = startDate.plusDays(2);
			throw new IOException("Requested period must be at least one day after minimum date (2008-07-01!");
		}
		if(endDate.isAfter(maxDate)){
			endDate = maxDate;
			if(Days.daysBetween(startDate, endDate).getDays()<2)
				startDate = endDate.minusDays(2);
			throw new IOException("Requested period must be before maximum date (2010-06-30)!");
		}	
	}
	
	private UncertaintyObservationCollection callInterpolationAlgorithm(){
		UncertaintyObservationCollection uColl = null;
		// connect to Interpolation Service
		WPSClientSession session = WPSClientSession.getInstance();
		try {
			session.connect(interpolationURL);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
		}
		
		// change dates in the request	
		DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();

		// add inputs for request
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("start-time", startDate.toString(dateFormat));
		inputs.put("end-time", endDate.toString(dateFormat));
		inputs.put("sos-url", sosURL);
		
		// specify prediction polygon reference
		Map<String, String> predictionInput = new HashMap<String,String>();
		predictionInput.put("href", "file:///"+resourcesPath+"\\Background\\predictionPolygon.xml");
		predictionInput.put("schema", "http://schemas.opengis.net/gml/2.1.2/feature.xsd");
		predictionInput.put("mimeType", "text/xml");		
		inputs.put("prediction-area", predictionInput);
		
		// Make execute request
		ExecuteDocument execDoc = null;
		try {
			execDoc = Utils.createExecuteDocumentManually(interpolationURL, "org.uncertweb.aqms.PolygonInterpolationAlgorithm", 
					inputs, UncertWebDataConstants.MIME_TYPE_OMX_XML);
		} catch (Exception e) {
			//logger.debug(e);
		}
		
			
		// Run WPS and get output (= Realisation object)
		ExecuteResponseDocument responseDoc = null;
		try {
			responseDoc = (ExecuteResponseDocument) session.execute(
				interpolationURL, execDoc);
					
			OutputDataType oType = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray(0);
			// all output elements
			Node wpsComplexData = oType.getData().getComplexData().getDomNode();
			// the complex data node
			Node unRealisation = wpsComplexData.getChildNodes().item(0); 
			// the realisation node			 
			IObservationCollection iobs = new XBObservationParser().parseObservationCollection(nodeToString(unRealisation));
			uColl = (UncertaintyObservationCollection) iobs;
			return uColl;
					
		} catch (WPSClientException e) {// Auto-generated catch block
				e.printStackTrace();
		} catch (OMParsingException e) {
				e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
		} catch (TransformerException e) {
				e.printStackTrace();
		}
		
		return uColl;
	}

	private List<String> extractStatisticsFromRequest(List<IData> statParams){
		List<String> params = new ArrayList<String>(statParams.size());
		Iterator<IData> statParamsIter = statParams.iterator();
		while (statParamsIter.hasNext()){
			IData statParam = statParamsIter.next();
			String statistics = ((LiteralStringBinding) statParam)
			.getPayload();
			params.add(statistics);
		}		
		return params;
	}
	
	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		return stringWriter.toString();
	}
	
	
}
