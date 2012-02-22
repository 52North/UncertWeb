package org.uncertweb.ups;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ExecuteDocument.Execute;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.joda.time.DateTime;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.generator.SimpleGMLGenerator;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.WebProcessingService;
import org.opengis.feature.simple.SimpleFeature;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.XMLEncoder;
import org.uncertml.io.XMLParser;
import org.uncertml.sample.AbstractRealisation;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.ISample;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.io.XmlBeansGeometryEncoder;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class UPSGenericAustalProcess extends AbstractObservableAlgorithm {

	private static Logger logger = Logger.getLogger(UPSGenericAustalProcess.class);

	// Path to resources
	private String localPath = "D:\\JavaProjects\\ups";
	private String resPath = localPath
			+ "\\src\\main\\resources\\austalResources";	
	private String utsAddress = "http://localhost:8080/uts/WebProcessingService";

	private Map<String, IData> staticInputsList = null;	
	private int numberOfRealisations = -999;	
	private String serviceURL = ""; 
	
	// WPS inputs and outputs
	private String inputIDNumberOfRealisations = "NumberOfRealisations";
	private String inputIDServiceURL = "ServiceURL";	
	private String inputIDIdentifierSimulatedProcess = "IdentifierSimulatedProcess"; 
//	private String inputIDStaticProcessInputs = "receptor-points"; 
	private String inputIDOutputUncertaintyType = "OutputUncertaintyType";
	private String outputIDUncertainProcessOutputs = "UncertainProcessOutputs";
	private String uncertaintyPrefix = "u_";
	private String certaintyPrefix = "c_";
	private final String inputIDStabilityClass = "c_stability-class";
	private final String inputIDVariableEmissions = "c_variable-emissions";
	private final String inputIDStaticEmissions = "c_static-emissions";
	private final String inputIDCentralPoint = "c_central-point";
	private final String inputIDReceptorPoints = "c_receptor-points";
	
	private Map<String, List<IObservationCollection>> uncertainInputSamplesMap;
	private int globalSampleCountforMultiVariantNormalDistributions = 0;
	private String sampleNameforMultiVariantNormalDistributions = "Emission";

	private String modelIdentifier;

	private List<IObservationCollection> sampleObservationCollections;
	private String resourceURL;
	private String referenceURL;
	private WPSClientSession utsSession;
	
	public UPSGenericAustalProcess(){
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			// check the name and active state
			if(property.getName().equalsIgnoreCase("localPath") && property.getActive()){
				localPath = property.getStringValue();
				resPath = localPath
				+ "\\src\\main\\resources";
			}else if(property.getName().equalsIgnoreCase("FullUTSAddress") && property.getActive()){
				utsAddress = property.getStringValue();
			}
		}
		
		try {
			resourceURL = WPSConfig.getConfigPath().substring(0,WPSConfig.getConfigPath().indexOf("config/"));
			String randomUUID = UUID.randomUUID().toString();
			resourceURL = resourceURL.concat("resources/outputs/" + randomUUID + "/");
			
			File resourceFolder = new File(resourceURL);
			
			if(!resourceFolder.exists()){
				resourceFolder.mkdir();
			}
			
			String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
			String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
			if(host == null) {
				host = InetAddress.getLocalHost().getCanonicalHostName();
			}
			referenceURL = "http://" + host + ":" + hostPort+ "/" + 
					WebProcessingService.WEBAPP_PATH + "/" + 
					WebProcessingService.SERVLET_PATH + "/" + "resources/outputs/" + randomUUID + "/";
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputMap) {			
		// 1) get UPS inputs
		// Retrieve model service URL from input. Must exist!
		List<IData> list = inputMap.get(inputIDServiceURL);
		IData serviceURL_IData = list.get(0);
		serviceURL = (String) serviceURL_IData.getPayload();

		// Retrieve model process identifier URL from input. Must exist!
		list = inputMap.get(inputIDIdentifierSimulatedProcess);
		IData modelIdentifier_IData = list.get(0);
		modelIdentifier = (String) modelIdentifier_IData.getPayload();

		// Retrieve number of realisations from input. Must exist!
		list = inputMap.get(inputIDNumberOfRealisations);
		IData numberOfReal = list.get(0);
		numberOfRealisations = (Integer) numberOfReal.getPayload();
		
		// 2) treat static inputs 
		staticInputsList = filterCertainInputs(inputMap);	
		
		// 3) treat uncertain inputs
		
		Map<String, IData> uncertainInputs = filterUncertainInputs(inputMap);					
		for (String id : uncertainInputs.keySet()) {
			UncertaintyObservationCollection uobsColl = (UncertaintyObservationCollection)uncertainInputs.get(id).getPayload();
			sampleDistributions2(uobsColl, id);
		}
		
		// free memory
		uncertainInputs = null;
		System.gc();
		
		// 4) run Austal for the number of iterations
		ArrayList<IObservationCollection> resultObservationList  = new ArrayList<IObservationCollection>(3);
		
		Map<String, Object> iMap = this.addStaticInputs();
		/* Make Austal requests and run Austal n times */
		for (int i = 0; i < numberOfRealisations; i++) {
			IObservationCollection tmpioc = makeRequestAndRunAustalOM(i, iMap);
			resultObservationList.add(tmpioc);
		}
		
		// 5) treat Austal results
		// TODO: refine OM handling
		
		// TODO: add NetCDF handling
		
		/*
		 * Here, treat the austal results and make e.g. distribution out of
		 * them. Then make the OutputMap to return!
		 */
		HashMap<String, HashMap<DateTime, ArrayList<Double>>> mightyMap = new HashMap<String, HashMap<DateTime, ArrayList<Double>>>();		
		HashMap<String, SpatialSamplingFeature> idSpSFMap = new HashMap<String, SpatialSamplingFeature>();		
		for (IObservationCollection iObservationCollection : resultObservationList) {
			
			/*
			 * run through every collection
			 */
			for (AbstractObservation abob : iObservationCollection.getObservations()) {
				
				/*
				 * check spatialsamplingfeatures
				 */
				String id = abob.getFeatureOfInterest().getIdentifier().getIdentifier();
				
				if(!idSpSFMap.containsKey(id)){					
					idSpSFMap.put(id, abob.getFeatureOfInterest());
					
					/*
					 * create first entry in mightymap
					 */
					HashMap<DateTime, ArrayList<Double>> timeValueMap = new HashMap<DateTime, ArrayList<Double>>();					
					ArrayList<Double> values = new ArrayList<Double>();					
					DateTime ti = abob.getPhenomenonTime().getDateTime();
					Double d = (Double)abob.getResult().getValue();					
					values.add(d);					
					timeValueMap.put(ti, values);					
					mightyMap.put(id, timeValueMap);
					
				}else{
					
					/*
					 * spatialfeature exists
					 * now we have to check the datetime
					 */					
					HashMap<DateTime, ArrayList<Double>> timeValueMap = mightyMap.get(id);					
					DateTime ti = abob.getPhenomenonTime().getDateTime();					
					ArrayList<Double> values = new ArrayList<Double>(); 					
					if(timeValueMap.containsKey(ti)){						
						values = timeValueMap.get(ti);						
					}
					Double d = (Double)abob.getResult().getValue();					
					values.add(d);
					timeValueMap.put(ti, values);					
					mightyMap.put(id, timeValueMap);					
				}				
			}						
		}
		
		UncertaintyObservationCollection mcoll = new UncertaintyObservationCollection();
		
		try {
			URI procedure = new URI(
					"http://www.uncertweb.org/models/austal2000");
			URI observedProperty = new URI(
					"http://www.uncertweb.org/phenomenon/pm10");

			for (String id : mightyMap.keySet()) {
				HashMap<DateTime, ArrayList<Double>> tmpList = mightyMap.get(id);
				SpatialSamplingFeature tmpSF = idSpSFMap.get(id);

				for (DateTime dateTime : tmpList.keySet()) {
					TimeObject newT = new TimeObject(dateTime);
					ArrayList<Double> values = tmpList.get(dateTime);
					ContinuousRealisation r = new ContinuousRealisation(values, -1.0d, "id");
					UncertaintyResult uResult = new UncertaintyResult(r, "ug/m3");
					UncertaintyObservation uObs = new UncertaintyObservation(
							newT, newT, procedure, observedProperty, tmpSF,
							uResult);
					mcoll.addObservation(uObs);
				}
			}
			
			// write uncertainty observation as XML and JSON
//			String filepath = resPath + "\\output_om";
//			String xmlFilepath = filepath.concat("\\realisations.xml");
//			File xmlFile = new File(xmlFilepath);
//			String jsonFilepath = filepath.concat("\\realisations.json");
//			File jsonFile = new File(jsonFilepath);
//			// encode, store (for using in austal request later)
//			try {
//				new StaxObservationEncoder().encodeObservationCollection(mcoll,xmlFile);
//				new JSONObservationEncoder().encodeObservationCollection(mcoll, jsonFile);
//			} catch (OMEncodingException e) {
//				e.printStackTrace();
//			}

			// make UPS result
			HashMap<String, IData> resultMap = new HashMap<String, IData>();			
			OMBinding omd = new OMBinding(mcoll);			
			resultMap.put(outputIDUncertainProcessOutputs, omd);

			logger.debug("End of process"); // for debugging
			return resultMap;
			
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(
					"An Exception occurred while creating the result collection.");
		}
	}
	
	@Override
	public List<String> getErrors() {
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if (id.equals(inputIDIdentifierSimulatedProcess)) {
			return LiteralStringBinding.class;
		} else if (id.startsWith(uncertaintyPrefix)) {
			return UncertWebIODataBinding.class;
		} else if (id.equals(inputIDCentralPoint)||id.equals(inputIDReceptorPoints)) {
			return GTVectorDataBinding.class;
		} else if(id.equals(inputIDStaticEmissions)||id.equals(inputIDVariableEmissions)||id.equals(inputIDStabilityClass)){
			return UncertWebIODataBinding.class;
		} else if(id.startsWith(certaintyPrefix)){
			return LiteralStringBinding.class;
		}else if (id.equals(inputIDServiceURL)) {
			return LiteralStringBinding.class;
		} else if (id.equals(inputIDOutputUncertaintyType)) {
			return LiteralStringBinding.class;
		} else if (id.equals(inputIDNumberOfRealisations)) {
			return LiteralIntBinding.class;
		}
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		if (id.equals(outputIDUncertainProcessOutputs)) {
			return UncertWebIODataBinding.class;
		}
		return null;
	}
		
	private Map<String, IData> filterUncertainInputs(Map<String, List<IData>> inputMap){		
		uncertainInputSamplesMap = new HashMap<String, List<IObservationCollection>>();
		Map<String, IData> result = new HashMap<String, IData>();
		
		for (String id : inputMap.keySet()) {
			
			if(id.startsWith(uncertaintyPrefix)){
				/*
				 * TODO: what if there are multiple uncertain inputs for one 
				 * id?
				 */
				result.put(id, inputMap.get(id).get(0));
				List<IObservationCollection> tmpList = new ArrayList<IObservationCollection>(numberOfRealisations);
				for (int i = 0; i < numberOfRealisations; i++) {
					tmpList.add(new MeasurementCollection());
				}
				uncertainInputSamplesMap.put(id, tmpList);				
			}			
		}	
		return result;		
	}
	
	private Map<String, IData> filterCertainInputs(Map<String, List<IData>> inputMap){		
		Map<String, IData> result = new HashMap<String, IData>();		
		for (String id : inputMap.keySet()) {			
			if(id.startsWith(certaintyPrefix)){
				result.put(id.substring(2), inputMap.get(id).get(0));		
			}			
		}	
		return result;		
	}
	
	
	private void sampleDistributions2(UncertaintyObservationCollection uobsColl, String uncertainInputId){		
		/*
		 * iterate over uncertain observations of UncertaintyObservationCollection
		 * send distributions of uncertain observation to UTS
		 * create new measurement out of uncertain observation 
		 * with the sample as result and store in measurement collection
		 */
		
		// get samples for OM collection
		IObservationCollection iobs = null;
		if(uobsColl.getObservations().get(0).getResult().getValue() instanceof NormalDistribution)
				iobs = Gaussian2Samples(uobsColl);
		else if(uobsColl.getObservations().get(0).getResult().getValue() instanceof MultivariateNormalDistribution)
			iobs = MultivariateGaussian2Samples(uobsColl);
		
		// loop through observations with samples
		for (AbstractObservation obs : iobs.getObservations()) {	
			UncertaintyObservation abstractObservation = (UncertaintyObservation) obs;
			UncertaintyResult result = abstractObservation.getResult();			
			IUncertainty resultUncertainty = result.getUncertaintyValue();							
			try {		
				if(resultUncertainty instanceof AbstractSample){	
					AbstractSample abstractSample = (AbstractSample) resultUncertainty;
					ContinuousRealisation realisations = (ContinuousRealisation) abstractSample.getRealisations().get(0);				
					if(uobsColl.getObservations().get(0).getResult().getValue() instanceof NormalDistribution){
						Double[] samples = realisations.getValues().toArray(new Double[0]);
						samples2Measurement(samples, abstractObservation, uncertainInputId);
					}
					else if(uobsColl.getObservations().get(0).getResult().getValue() instanceof MultivariateNormalDistribution){
						Double[] r = realisations.getValues().toArray(new Double[0]);
						int sampleLength = r.length/numberOfRealisations;
						Double[][] samples = new Double[numberOfRealisations][sampleLength];
						
						int count = 0;
						for(int i = 0; i<numberOfRealisations; i++){
							for(int j=0; j<sampleLength;j++){
								samples[i][j] = r[count];
								count++;
							}							
						}
												
						multivariateSamples2Measurement(uncertainInputId, abstractObservation, samples, 
								sampleLength);
					}
						
				}else if(resultUncertainty instanceof AbstractRealisation){
					
				}
			} catch (Exception e) {
				e.printStackTrace();				
			}
		}	
	}
	
	private IObservationCollection Gaussian2Samples(UncertaintyObservationCollection uColl){
		IObservationCollection iobs = null;

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		ExecuteDocument execDocUTS = null;
		try {
			session.connect(utsAddress);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
		
		// add inputs for request
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("distribution", uColl);
		inputs.put("numbReal", ""+numberOfRealisations);
		
		// Make execute request
		ExecuteDocument execDoc = null;
		try {
			execDoc = createExecuteDocumentManually(utsAddress, "org.uncertweb.wps.Gaussian2Samples", inputs, UncertWebDataConstants.MIME_TYPE_OMX_XML);
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
		return iobs;
	}
	
	private IObservationCollection MultivariateGaussian2Samples(UncertaintyObservationCollection uColl){
		IObservationCollection iobs = null;

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		ExecuteDocument execDocUTS = null;
		try {
			session.connect(utsAddress);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
		
		// add inputs for request
		Map<String, Object> inputs = new HashMap<String, Object>();
		File f = new File("c:/temp/ucoll.xml");			
		try {
			String s = new XBObservationEncoder().encodeObservationCollection(uColl);				
			BufferedWriter b = new BufferedWriter(new FileWriter(f));				
			b.write(s);
			b.flush();
			b.close();
			
		} catch (OMEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		inputs.put("distribution", "file:///" + f.getAbsoluteFile());
	//	inputs.put("distribution", uColl);
		inputs.put("numbReal", ""+numberOfRealisations);
		
		// Make execute request
		ExecuteDocument execDoc = null;
		try {
			execDoc = createExecuteDocumentManually(utsAddress, "org.uncertweb.wps.MultivariateGaussian2Samples", inputs, UncertWebDataConstants.MIME_TYPE_OMX_XML);
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
		return iobs;
	}
		
	private void samples2Measurement(Double[] samples, UncertaintyObservation uo, String uncertainInputId){
		
		for (int i = 0; i < samples.length; i++) {
			MeasureResult result = new MeasureResult(samples[i],
					uo.getResult().getUnitOfMeasurement());			
			
			Identifier uoSAMIdentifier = null;
			
			Identifier uoIdentifier = uo.getIdentifier();
			if(uo.getFeatureOfInterest() != null){
				uoSAMIdentifier = uo.getFeatureOfInterest().getIdentifier();
			}
			URI uri = null;
			
			if(uoIdentifier != null){
				uri = uoIdentifier.getCodeSpace();
			}else if(uoSAMIdentifier != null){
				uri = uoSAMIdentifier.getCodeSpace();				
			}else{
			
			try {
				uri = new URI("http://uncertweb.org");
			} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			Identifier identifier = new Identifier(uri, "Meteo" + i);		
			
			uncertainInputSamplesMap.get(uncertainInputId).get(i).addObservation(createMeasurement(uo, result, identifier));
//			sampleObservationCollections.get(i).addObservation(createMeasurement(uo, result));					
		}
	}

	
	private Measurement createMeasurement(UncertaintyObservation uo, MeasureResult result, Identifier identifier){
		
		Measurement me = new Measurement(identifier, uo.getBoundedBy(), uo
				.getPhenomenonTime(), uo.getResultTime(),
				uo.getValidTime(), uo.getProcedure(), uo
						.getObservedProperty(), uo.getFeatureOfInterest(),
				uo.getResultQuality(), result);
		return me;
	}
	
	private void multivariateSamples2Measurement(String uncertainInputId, UncertaintyObservation uo, Double[][] samples, int dimension){		
		
		for (int i = 0; i < numberOfRealisations; i++) {
			
		Double[] currentEmissions = samples[i];
		
		// Make 24 results with one hour emission in each
		for (int hour = 0; hour < dimension; hour++) {

			// make result
			MeasureResult result = new MeasureResult(
					currentEmissions[hour], uo.getResult().getUnitOfMeasurement());

			// make identifier for this observation
			globalSampleCountforMultiVariantNormalDistributions++;
			String ident = sampleNameforMultiVariantNormalDistributions.concat(((Integer) globalSampleCountforMultiVariantNormalDistributions).toString());

			Identifier uoSAMIdentifier = null;
			
			Identifier uoIdentifier = uo.getIdentifier();
			if(uo.getFeatureOfInterest() != null){
				uoSAMIdentifier = uo.getFeatureOfInterest().getIdentifier();
			}
			URI uri = null;
			
			if(uoIdentifier != null){
				uri = uoIdentifier.getCodeSpace();
			}else if(uoSAMIdentifier != null){
				uri = uoSAMIdentifier.getCodeSpace();				
			}else{
			
			try {
				uri = new URI("http://uncertweb.org");
			} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			Identifier identifier = new Identifier(uri, ident);
			
			DateTime hourDate = uo.getPhenomenonTime().getDateTime().plusHours(hour+1);
			TimeObject hourOfDay = new TimeObject(hourDate);

			// make other measurement input
			Envelope boundedBy = null;
			if (uo.getBoundedBy() != null) {
				boundedBy = uo.getBoundedBy();
			}
			TimeObject validTime = null;
			if (uo.getValidTime() != null) {
				validTime = uo.getValidTime();
			}
			DQ_UncertaintyResult[] uncResu = null;
			if (uo.getResultQuality() != null) {
				uncResu = uo.getResultQuality();
			}
			SpatialSamplingFeature feat = uo.getFeatureOfInterest();
			URI proc = uo.getProcedure();
			URI obsProp = uo.getObservedProperty();
			Measurement me = new Measurement(identifier, boundedBy,
					hourOfDay, hourOfDay, validTime, proc, obsProp, feat,
					uncResu, result);
//			mec.addObservation(me);
			
			uncertainInputSamplesMap.get(uncertainInputId).get(i).addObservation(me);
			
//			try {
//				System.out.println(new XBObservationEncoder().encodeObservation(me));
//			} catch (OMEncodingException e) {
//				e.printStackTrace();
//			}
		}		
	}		
	}

	
	private Map<String, Object> addStaticInputs(){
		Map<String, Object> inputMap = new HashMap<String, Object>();
		// add all certain inputs
		for(String identifier : staticInputsList.keySet()){
			// write observation collections to files and provide only reference
			if(staticInputsList.get(identifier).getPayload() instanceof IObservationCollection){
				File f = new File("c:/temp/" + identifier + ".xml");			
				try {
					String s = new XBObservationEncoder().encodeObservationCollection((IObservationCollection)staticInputsList.get(identifier).getPayload());				
					BufferedWriter b = new BufferedWriter(new FileWriter(f));				
					b.write(s);
					b.flush();
					b.close();
					
				} catch (OMEncodingException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputMap.put(identifier, "file:///" + f.getAbsoluteFile());			
			}else if(staticInputsList.get(identifier).getPayload() instanceof FeatureCollection){
				// read template and change only coordinates	
				FeatureCollection<?,?> fc = (FeatureCollection<?,?>) staticInputsList.get(identifier).getPayload();
				FeatureIterator<?> iterator = fc.features();
				int srs = 31467;
				String coordinates = "";
				File f = new File("c:/temp/" + identifier + ".xml");	
				
				if(identifier.equals("receptor-points")){					
					//TODO: implement for different features
					while (iterator.hasNext()) {
						SimpleFeature feature = (SimpleFeature) iterator.next();
						if (feature.getDefaultGeometry() instanceof MultiLineString) {
							MultiLineString lineString = (MultiLineString) feature
									.getDefaultGeometry();
							if(lineString.getSRID()!=0)
								srs = lineString.getSRID();
							// loop through coordinates and add them to the string
							int coordinateCount = lineString.getCoordinates().length;							
							for (int i = 0; i < lineString.getCoordinates().length; i++) {
								Coordinate coord = lineString.getCoordinates()[i];
								coordinates = coordinates +coord.x + ","+coord.y+" ";
							}
						}
					}
				}else if(identifier.equals("central-point")){
						//TODO: implement for different features
						while (iterator.hasNext()) {
							SimpleFeature feature = (SimpleFeature) iterator.next();
							if (feature.getDefaultGeometry() instanceof Point) {
								Point point = (Point) feature.getDefaultGeometry();
								Coordinate coord = point.getCoordinate();
								coordinates = coordinates +coord.x + ","+coord.y;
								if(point.getSRID()!=0)
									srs = point.getSRID();
							}
						}
				}
				
				// read template file
				try {
					String templatePath = WPSConfig.getConfigPath().substring(0,WPSConfig.getConfigPath().indexOf("config/")).concat("resources/austal-inputs/");
					File fIn = new File(templatePath + identifier+".xml");					
					BufferedReader bread = new BufferedReader(new FileReader(fIn));				
					String content = "";					
					String line = "";								
						
					// substitute values
					while ((line = bread.readLine()) != null) {
						if(line.contains("srsName")){
							String[] s = line.split("#");
							if(s.length==2){
								s[1] = "#"+srs+"\">";
							}
							line = s[0] + s[1];
						}else if(line.contains("%$coords$%")){
							line = line.replace("%$coords$%", "" + coordinates);
						}
						content = content.concat(line);				
					}
						
					// write new file						
					BufferedWriter b = new BufferedWriter(new FileWriter(f));				
					b.write(content);
					b.flush();
					b.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				inputMap.put(identifier, "file:///" + f.getAbsoluteFile());
				
			}
			else{
				inputMap.put(identifier, staticInputsList.get(identifier).getPayload());
			}			
		}
		// free memory
		staticInputsList = null;
		System.gc();
		
		return inputMap;
	}
	
	/** 
	 * 
	 * This method creates an execute request for the Austal WPS and runs it. At
	 * the end, the outputs should be stored and/or processed.
	 * 
	 */
	private IObservationCollection makeRequestAndRunAustalOM(int runNumber, Map<String, Object> inputs) {

		// connect to austal WPS
		WPSClientSession session = WPSClientSession.getInstance();

		try {
			session.connect(serviceURL);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
		}
		
	//	Map<String, Object> inputs = new HashMap<String, Object>();
		
		// add all uncertain inputs
		for (String id : uncertainInputSamplesMap.keySet()) {			
			File f = new File("c:/temp/" + id.replace(uncertaintyPrefix, "")+ runNumber + ".xml");			
			try {
				String s = new XBObservationEncoder().encodeObservationCollection(uncertainInputSamplesMap.get(id).get(runNumber));				
				BufferedWriter b = new BufferedWriter(new FileWriter(f));				
				b.write(s);
				b.flush();
				b.close();
				
			} catch (OMEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			inputs.put(id.replace(uncertaintyPrefix, ""), "file:///" + f.getAbsoluteFile());
			
			// free memory
			uncertainInputSamplesMap.get(id).remove((runNumber));
		}
		System.gc();
		
		// Make execute request
		ExecuteDocument execDoc = null;
		try {
			//execDoc = createExecuteDocument(serviceURL, modelIdentifier, inputs);
			execDoc = createExecuteDocumentManually(serviceURL, modelIdentifier, inputs, UncertWebDataConstants.MIME_TYPE_OMX_XML);
		} catch (XmlException e) {
			logger.debug(e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.debug(e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.debug(e);
			e.printStackTrace();
		}
		
		 //Run austal WPS and get output (Realisation object)
		 ExecuteResponseDocument response1 = null;
		 try {
		 response1 = (ExecuteResponseDocument) session.execute(serviceURL,
		 execDoc);
		 
		 OutputDataType oType =
			 response1.getExecuteResponse().getProcessOutputs().getOutputArray(0);
			 // all output elements
			 Node wpsComplexData = oType.getData().getComplexData().getDomNode();
			 // the complex data node
			 Node unRealisation = wpsComplexData.getChildNodes().item(0); 
			 // the realisation node			 
			 IObservationCollection iobs = new XBObservationParser().parseObservationCollection(nodeToString(unRealisation));
		 
			 return iobs;
			 
		 } catch (WPSClientException e) {
			 logger.error(e);
		 } catch (OMParsingException e) {
			 logger.error(e);
		} catch (TransformerFactoryConfigurationError e) {
			 logger.error(e);
		} catch (TransformerException e) {
			 logger.error(e);
		}
		 
		return null;	
	}
	
	
	public static ExecuteDocument createExecuteDocumentManually(String url, String processID,
			Map<String, Object> inputs, String outputMimeType) throws Exception {
		// get process description to create execute document
		ProcessDescriptionType processDescription = WPSClientSession.getInstance().getProcessDescription(url, processID);		
		
		// create new ExecuteDocument
		ExecuteDocument execDoc = ExecuteDocument.Factory.newInstance();
		Execute ex = execDoc.addNewExecute();
		ex.setVersion("1.0.0");
		ex.addNewIdentifier().setStringValue(processDescription.getIdentifier().getStringValue());
		ex.addNewDataInputs();

		// loop through inputs in the process description and add inputs from the input map
		for (InputDescriptionType inputDescType : processDescription.getDataInputs()
				.getInputArray()) {
			// get respective input from map
			String inputName = inputDescType.getIdentifier().getStringValue();
			Object inputValue = inputs.get(inputName);
			
			// if input is Literal data
			if (inputDescType.getLiteralData() != null) {
				if (inputValue instanceof String) {				
					InputType input = execDoc.getExecute().getDataInputs().addNewInput();
					input.addNewIdentifier().setStringValue(inputName);
					input.addNewData().addNewLiteralData().setStringValue((String)inputValue);
				}else if(inputValue instanceof String[]){
					for(String inputType : (String[]) inputValue){
						InputType input = execDoc.getExecute().getDataInputs().addNewInput();
						input.addNewIdentifier().setStringValue(inputName);
						input.addNewData().addNewLiteralData().setStringValue((String)inputType);
					}
				}else if(inputValue instanceof Double){
					InputType input = execDoc.getExecute().getDataInputs().addNewInput();
					input.addNewIdentifier().setStringValue(inputName);
					input.addNewData().addNewLiteralData().setStringValue(((Double) inputValue)+"");
				}else if(inputValue instanceof Integer){
					InputType input = execDoc.getExecute().getDataInputs().addNewInput();
					input.addNewIdentifier().setStringValue(inputName);
					input.addNewData().addNewLiteralData().setStringValue(((Integer) inputValue)+"");
				}
			} 
			// if input is Complex data
			else if (inputDescType.getComplexData() != null) {
				// get supported mime types
				ComplexDataDescriptionType[] supportedTypes = inputDescType.getComplexData().getSupported().getFormatArray();
				List<String> inputMimeTypes = new ArrayList<String>();
				inputMimeTypes.add(inputDescType.getComplexData().getDefault().getFormat().getMimeType());
				for(ComplexDataDescriptionType type: supportedTypes){
					inputMimeTypes.add(type.getMimeType());
				}
				
				// Complex data UncertML
				if (inputValue instanceof IUncertainty) {		
					if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_UNCERTML_XML)){
						InputType input = execDoc.getExecute().getDataInputs().addNewInput();
						input.addNewIdentifier().setStringValue(inputName);
						try {
							String xmlString = new XMLEncoder().encode((IUncertainty) inputValue);
							ComplexDataType data = input.addNewData().addNewComplexData();
							data.set(XmlObject.Factory.parse(xmlString));
							data.setMimeType(UncertWebDataConstants.MIME_TYPE_UNCERTML_XML);
							data.setSchema(UncertWebDataConstants.SCHEMA_UNCERTML);
							data.setEncoding(UncertWebDataConstants.ENCODING_UTF_8);							
						} catch (XmlException e) {
							e.printStackTrace();
						}

					}
					else{
						throw new IllegalArgumentException("Given Input mime type is not supported: "
								+ inputName + ", "+ UncertWebDataConstants.MIME_TYPE_UNCERTML_XML);
					}
				}
				// Complex data Reference
				else if (inputValue instanceof String) {
					// OM reference
					if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_OMX_XML)){						
						InputType input = execDoc.getExecute().getDataInputs().addNewInput();
						input.addNewIdentifier().setStringValue(inputName);
						input.addNewReference().setHref((String)inputValue);
						input.getReference().setMimeType(UncertWebDataConstants.MIME_TYPE_OMX_XML);
						input.getReference().setSchema(UncertWebDataConstants.SCHEMA_OM_V2);
						input.getReference().setEncoding(UncertWebDataConstants.ENCODING_UTF_8);
					}
					// Feature Collection reference
					else if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_TEXT_XML)){
						InputType input = execDoc.getExecute().getDataInputs().addNewInput();
						input.addNewIdentifier().setStringValue(inputName);
						input.addNewReference().setHref((String)inputValue);
						input.getReference().setMimeType(UncertWebDataConstants.MIME_TYPE_TEXT_XML);
						input.getReference().setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
						input.getReference().setEncoding(UncertWebDataConstants.ENCODING_UTF_8);
					}
					else{
						throw new IllegalArgumentException("Given Input mime type is not supported: "
								+ inputName + ", "+ UncertWebDataConstants.MIME_TYPE_OMX_XML);
					}
				}else if(inputValue instanceof Map){
					Map<String, String> inputMap = (Map<String, String>) inputValue;
					if(inputMimeTypes.contains(inputMap.get("mimeType"))){
						InputType input = execDoc.getExecute().getDataInputs().addNewInput();
						input.addNewIdentifier().setStringValue(inputName);
						input.addNewReference().setHref(inputMap.get("href"));
						input.getReference().setMimeType(inputMap.get("mimeType"));
						input.getReference().setSchema(inputMap.get("schema"));
						input.getReference().setEncoding(UncertWebDataConstants.ENCODING_UTF_8);
					}
					
				}
				
				// Complex data OM
				else if (inputValue instanceof IObservationCollection) {
					if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_OMX_XML)){
						InputType input = execDoc.getExecute().getDataInputs().addNewInput();
						input.addNewIdentifier().setStringValue(inputName);
						try {
							String collString = new XBObservationEncoder().encodeObservationCollection((IObservationCollection)inputValue);
							ComplexDataType data = input.addNewData().addNewComplexData();
							data.set(XmlObject.Factory.parse(collString));
							data.setMimeType(UncertWebDataConstants.MIME_TYPE_OMX_XML);
							data.setSchema(UncertWebDataConstants.SCHEMA_OM_V2);
							data.setEncoding(UncertWebDataConstants.ENCODING_UTF_8);							
						} catch (OMEncodingException e) {
							e.printStackTrace();
						} catch (XmlException e) {
							e.printStackTrace();
						}
					}
					else{
						throw new IllegalArgumentException("Given Input mime type is not supported: "
								+ inputName + ", "+ UncertWebDataConstants.MIME_TYPE_OMX_XML);
					}
				}
				// Complex data GML
				else if (inputValue instanceof FeatureCollection) {
					if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_TEXT_XML)){
						// make String from GML object
						GTVectorDataBinding g = new GTVectorDataBinding((FeatureCollection) inputValue);						
						StringWriter buffer = new StringWriter();
						SimpleGMLGenerator generator = new SimpleGMLGenerator();
						generator.write(g, buffer);
						String fc = buffer.toString();
						
						// add data to request document
						InputType input = execDoc.getExecute().getDataInputs().addNewInput();
						input.addNewIdentifier().setStringValue(inputName);
						ComplexDataType data = input.addNewData().addNewComplexData();
						data.set(XmlObject.Factory.parse(fc));
						data.setMimeType(UncertWebDataConstants.MIME_TYPE_TEXT_XML);
						data.setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
						data.setEncoding(UncertWebDataConstants.ENCODING_UTF_8);	
					}
				}
				// if an input is missing
				if (inputValue == null && inputDescType.getMinOccurs().intValue() > 0) {
					throw new IOException("Property not set, but mandatory: "
							+ inputName);
				}
			}
		}
		
		// get output type from process description		
		OutputDescriptionType outDesc = processDescription.getProcessOutputs().getOutputArray(0);	
		String outputIdentifier = outDesc.getIdentifier().getStringValue();
		
		// prepare output type in execute document
		if (!execDoc.getExecute().isSetResponseForm()) {
			execDoc.getExecute().addNewResponseForm();
		}
		if (!execDoc.getExecute().getResponseForm().isSetResponseDocument()) {
			execDoc.getExecute().getResponseForm().addNewResponseDocument();
		}
		
		//TODO: implement handling for more than one output definition
		DocumentOutputDefinitionType outputDef = null;
		if(execDoc.getExecute().getResponseForm().getResponseDocument().getOutputArray().length>0)
			outputDef = execDoc.getExecute().getResponseForm().getResponseDocument().getOutputArray()[0];
		else{
			outputDef = execDoc.getExecute().getResponseForm().getResponseDocument().addNewOutput();
			outputDef.setIdentifier(outDesc.getIdentifier());			
		}
	
		// if not the default type is used, find the correct mime type and schema
		String defaultOutputMimeType = outDesc.getComplexOutput().getDefault().getFormat().getMimeType();		
		String outputSchema = outDesc.getComplexOutput().getDefault().getFormat().getSchema();	
		boolean mimeTypeExists = false;
		if(!defaultOutputMimeType.equals(outputMimeType)){
			ComplexDataDescriptionType[] supportedTypes = outDesc.getComplexOutput().getSupported().getFormatArray();
			for(ComplexDataDescriptionType type : supportedTypes){
				if(type.getMimeType().equals(outputMimeType)){
					outputSchema = type.getSchema();
					mimeTypeExists = true;
				}
			}			
		}
		else{
			mimeTypeExists = true;
		}
		
		if(mimeTypeExists){
			logger.debug(outputSchema);		
			logger.debug(outputMimeType);		
			logger.debug(outputIdentifier);
			outputDef.setSchema(outputSchema);
			outputDef.setMimeType(outputMimeType);
			
		}else{
			throw new IOException("Given Output mime type is not supported: "
					+ outputMimeType);
		}

	return execDoc;
	}
	

	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		return stringWriter.toString();
	}
}
