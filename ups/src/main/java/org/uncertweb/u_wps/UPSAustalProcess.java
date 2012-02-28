package org.uncertweb.u_wps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.n52.wps.server.WebProcessingService;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.XMLEncoder;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.gml.Identifier;
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

import com.vividsolutions.jts.geom.Envelope;

public class UPSAustalProcess extends AbstractObservableAlgorithm {

	private static Logger logger = Logger.getLogger(UPSAustalProcess.class);

	// Path to resources
//	private String localPath = "D:\\JavaProjects\\ups";
//	private String resPath = localPath
//			+ "\\src\\main\\resources\\austalResources";	
	private String utsAddress = "http://localhost:8080/uts/WebProcessingService";

	/*
	 * static inputs
	 */	
	private List<IData> staticInputsList = null;	
	/*
	 * how often to call the WPS
	 */	
	private int numberOfRealisations = -999;	
	/*
	 * URL of the WPS
	 */
	private String serviceURL = ""; 
	/*
	 * identifier for number of realisations
	 */
	private String inputIDNumberOfRealisations = "NumberOfRealisations";
	/*
	 * identifier for model service URL
	 */
	private String inputIDServiceURL = "ServiceURL";	
	/*
	 * identifier for process identifier of the simulated process/model
	 */
	private String inputIDIdentifierSimulatedProcess = "IdentifierSimulatedProcess"; 
	/*
	 * identifier for static inputs
	 */	
	private String inputIDStaticProcessInputs = "receptor-points"; 
	/*
	 * identifier for requested output uncertainty type
	 */	
	private String inputIDOutputUncertaintyType = "OutputUncertaintyType";
	/*
	 * identifier for the uncertain output
	 */	
	private String outputIDUncertainProcessOutputs = "UncertainProcessOutputs";
	/*
	 * prefix for uncertain inputs
	 */
	private String uncertaintyPrefix = "u_";
	
	private Map<String, List<IObservationCollection>> uncertainInputSamplesMap;
	private int globalSampleCountforMultiVariantNormalDistributions = 0;
	private String sampleNameforMultiVariantNormalDistributions = "Emission";

	private String modelIdentifier;

	private List<IObservationCollection> sampleObservationCollections;
	private String resourceURL;
	private String referenceURL;
	
	public UPSAustalProcess(){
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
		for(Property property : propertyArray){
			// check the name and active state
//			if(property.getName().equalsIgnoreCase("localPath") && property.getActive()){
//				localPath = property.getStringValue();
//				resPath = localPath
//				+ "\\src\\main\\resources\\austalResources";
//			}else 
				if(property.getName().equalsIgnoreCase("FullUTSAddress") && property.getActive()){
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
		
//		// Retrieve number of Realisations from input. Must exist!
//		list = inputMap.get(inputIDUncertainStreetEmissions);
//		IData uncertainStreetEmissions = list.get(0);
//		UncertaintyObservationCollection uncertainStreetEmissionsUObsColl = (UncertaintyObservationCollection) uncertainStreetEmissions.getPayload();
		
		/* Treat static inputs */
		staticInputsList = inputMap.get(inputIDStaticProcessInputs);
		
		Map<String, IData> uncertainInputs = filterUncertainInputs(inputMap);			
		
		for (String id : uncertainInputs.keySet()) {

			UncertaintyObservationCollection uobsColl = (UncertaintyObservationCollection)uncertainInputs.get(id).getPayload();
			/*
			 * TODO: what about netCDF!?
			 */			
			sampleDistributions(uobsColl, id);
			
		}

		/*
		 * now request the model wps the specified number of times
		 */
//		for (int i = 0; i < numberOfRealisations; i++) {
//			makeRequestAndRunAustal(i);
//		}
		
//		sampleDistributions(uncertainStreetEmissionsUObsColl);
		
		ArrayList<IObservationCollection> resultObservationList  = new ArrayList<IObservationCollection>(3);
		
		/* Make Austal requests and run Austal n times */
		for (int i = 0; i < numberOfRealisations; i++) {
			IObservationCollection tmpioc = makeRequestAndRunAustal(i);
			resultObservationList.add(tmpioc);
		}

//		HashMap<String, IData> resultMap = new HashMap<String, IData>();
//		
//		OMBinding omd = new OMBinding(sampleObservationCollections.get(0));
//		
//		resultMap.put(outputIDUncertainProcessOutputs, omd);
//		
//		return resultMap;
		
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

				HashMap<DateTime, ArrayList<Double>> tmpList = mightyMap
						.get(id);

				SpatialSamplingFeature tmpSF = idSpSFMap.get(id);

				for (DateTime dateTime : tmpList.keySet()) {

					TimeObject newT = new TimeObject(dateTime);

					ArrayList<Double> values = tmpList.get(dateTime);

					ContinuousRealisation r = new ContinuousRealisation(values);

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

//			try{
//			
//			BufferedReader r = new BufferedReader(new FileReader(xmlFile));
//			
//			String content = "";
//			
//			String line = "";
//			
//			while ((line = r.readLine()) != null) {
//				content = content.concat(line);
//			}
//			
//			IObservationCollection mcoll = new XBObservationParser().parseObservationCollection(content);
			
			// make UPS result
			HashMap<String, IData> resultMap = new HashMap<String, IData>();
			
			OMBinding omd = new OMBinding(mcoll);
			
			resultMap.put(outputIDUncertainProcessOutputs, omd);

			logger.debug("End of process"); // for debugging
			return resultMap;
//			}catch(Exception e){
//				throw new RuntimeException(
//				"An Exception occurred while creating the result collection.");
//				
//			}
			
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
		} else if (id.equals(inputIDStaticProcessInputs)) {
			//TODO: could be something else, too?!
			return LiteralStringBinding.class;
		} else if (id.equals(inputIDServiceURL)) {
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
	
	/**
	 * This method creates an <c>ExecuteDocument</c>.
	 * 
	 * @param url The url of the WPS the ExecuteDocument 
	 * @param processID The id of the process the ExecuteDocument
	 * @param inputs A map holding the identifiers of the inputs and the values
	 * @return
	 * @throws Exception
	 */
	public ExecuteDocument createExecuteDocument(String url, String processID,
			Map<String, Object> inputs) throws Exception {
		

		ProcessDescriptionType processDescription = WPSClientSession.getInstance().getProcessDescription(url, processID);
		
		org.n52.wps.client.ExecuteRequestBuilder executeBuilder = new org.n52.wps.client.ExecuteRequestBuilder(
				processDescription);

		for (InputDescriptionType input : processDescription.getDataInputs()
				.getInputArray()) {
			String inputName = input.getIdentifier().getStringValue();
			Object inputValue = inputs.get(inputName);
			if (input.getLiteralData() != null) {
				if (inputValue instanceof String) {
					executeBuilder.addLiteralData(inputName,
							(String) inputValue);
				}
			} else if (input.getComplexData() != null) {
				
				String schema = input.getComplexData().getDefault().getFormat().getSchema();
				
				logger.debug(schema);				
				
				String mimetype = input.getComplexData().getDefault().getFormat().getMimeType();
				
				logger.debug(mimetype);
				
				// Complexdata by value
				if (inputValue instanceof IUncertainty) {
					
					UncertMLBinding d = new UncertMLBinding((IUncertainty) inputValue);
					
					executeBuilder
							.addComplexData(
									inputName,
									d, schema, "UTF-8", mimetype);
				}
//				// Complexdata Reference
				else if (inputValue instanceof String) {
					executeBuilder
							.addComplexDataReference(
									inputName,
									(String) inputValue,
									schema,
									"UTF-8", mimetype);
				}else if (inputValue instanceof IObservationCollection) {
					
					OMBinding d = new OMBinding((IObservationCollection) inputValue);
					
					executeBuilder
							.addComplexData(
									inputName,
									d, "http://schemas.opengis.net/om/2.0/observation.xsd", "UTF-8", "application/x-om-u+xml");//TODO: don't hardcode schema and mimetype
				}

				if (inputValue == null && input.getMinOccurs().intValue() > 0) {
					throw new IOException("Property not set, but mandatory: "
							+ inputName);
				}
			}
		}
		
		OutputDescriptionType outType = processDescription.getProcessOutputs().getOutputArray(0);
		
		String outputIdentifier = outType.getIdentifier().getStringValue();
		
		String outputSchema = outType.getComplexOutput().getDefault().getFormat().getSchema();
		
		String outputMimeType = outType.getComplexOutput().getDefault().getFormat().getMimeType();
		
		logger.debug(outputSchema);
		
		logger.debug(outputMimeType);
		
		logger.debug(outputIdentifier);
		
		executeBuilder.setSchemaForOutput(
				outputSchema,
				outputIdentifier);
		executeBuilder.setMimeTypeForOutput(outputMimeType, outputIdentifier);
		
		logger.debug(executeBuilder.getExecute());
		
		return executeBuilder.getExecute();
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
	
	private void sampleDistributions(UncertaintyObservationCollection uobsColl, String uncertainInputId){
		
		/*
		 * iterate over uncertain observations of UncertaintyObservationCollection
		 * send distributions of uncertain observation to UTS
		 * create new measurement out of uncertain observation 
		 * with the sample as result and store in measurement collection
		 */

		Iterator<? extends AbstractObservation> observationIterator = uobsColl.getObservations().iterator();
		
		int counter = 0;
		
		while (observationIterator.hasNext()) {
			
//			if(counter == 100){
//				break;
//			}
			
			UncertaintyObservation abstractObservation = (UncertaintyObservation) observationIterator
					.next();
			
			UncertaintyResult result = abstractObservation.getResult();
			
			IUncertainty resultUncertainty = result.getUncertaintyValue();
							
			try {
				
				handleUncertainty(resultUncertainty, abstractObservation, uncertainInputId);				
									
			} catch (Exception e) {
				e.printStackTrace();				
				counter++;
//				testremoveunvalidcovariancematrices(abstractObservation);
//				continue;
			}
//			latestWorkingCovMatrice = ((MultivariateNormalDistribution)resultUncertainty).getCovarianceMatrix().getValues();
//			latestWorkingMean = ((MultivariateNormalDistribution)resultUncertainty).getMean();
//			
//			newUncertainObsColl.addObservation(abstractObservation);
		}
	
}
	
	private void handleUncertainty(IUncertainty resultUncertainty, UncertaintyObservation abstractObservation, String uncertainInputId) throws IllegalArgumentException{
		
		if(resultUncertainty instanceof NormalDistribution){
			
			try {
				double[] samples = handleNormalDistribution((NormalDistribution) resultUncertainty);
				putResultSampleAndAddMeasurementToCollection(samples, abstractObservation, uncertainInputId);
			} catch (UnsupportedUncertaintyTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UncertaintyEncoderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else if(resultUncertainty instanceof MultivariateNormalDistribution){
			
			MultivariateNormalDistribution distribution = (MultivariateNormalDistribution)resultUncertainty;
			
			double[][] samples = handleMultivariateDistribution(distribution);
								
			putSamplesinMeasurement(uncertainInputId, abstractObservation, samples, distribution.getCovarianceMatrix().getDimension());
		}
		/*
		 * TODO: handle other types of distributions
		 */
	}
	
	private void putResultSampleAndAddMeasurementToCollection(double[] samples, UncertaintyObservation uo, String uncertainInputId){
		
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
	
	private void putSamplesinMeasurement(String uncertainInputId, UncertaintyObservation uo, double[][] samples, int dimension){		
		
		for (int i = 0; i < numberOfRealisations; i++) {
			
		double[] currentEmissions = samples[i];
		
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
			
			DateTime hourDate = uo.getPhenomenonTime().getDateTime().plusHours(hour);
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
	
	private double[] handleNormalDistribution(NormalDistribution distribution) throws UnsupportedUncertaintyTypeException, UncertaintyEncoderException, IOException{
		
		// to be returned
		double[] samples = new double[numberOfRealisations];

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		ExecuteDocument execDocUTS = null;

		try {
			session.connect(utsAddress);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
		
//			NormalDistribution distrib = (NormalDistribution) distribution;
//
//			Map<String, Object> inputs = new HashMap<String, Object>();
//			
//			inputs.put("distribution", distrib);
//			inputs.put("numbReal", numberOfRealisations);
//			
//			// Make execute request
//			try {
//				execDocUTS = createExecuteDocument(utsAddress, "org.uncertweb.wps.UnivGaussianDist2Realisations", inputs);
//			} catch (XmlException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			
			NormalDistribution distrib = (NormalDistribution) distribution;

			String dist = new XMLEncoder().encode(distrib);
			
			// parameters
			String mean = distrib.getMean().get(0).toString();
			String variance = distrib.getVariance().get(0).toString();

			String templatePath = WPSConfig.getConfigPath().substring(0,WPSConfig.getConfigPath().indexOf("config/")).concat("resources/request_templates/");
			
			File f = new File(templatePath + "NormalRequest.xml");
			
			BufferedReader bread = new BufferedReader(new FileReader(f));
			
			String content = "";
			
			String line = "";
			
			while ((line = bread.readLine()) != null) {
				if(line.contains("%$dist$%")){
					line = line.replace("%$dist$%", dist);
				}else
					if(line.contains("%$numbReal$%")){
						line = line.replace("%$numbReal$%", "" + numberOfRealisations);
					}
				content = content.concat(line);				
			}
			
			// Make execute request
			try {
				// execDocUTSday = ExecuteDocument.Factory.parse(new
				// File("D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\request_templates\\LogNormalRequest.xml"));
				execDocUTS = ExecuteDocument.Factory.parse(content);
			} catch (XmlException e) {
				e.printStackTrace();
			} 
			
			// Run UTS and get output (=Realisation object)
			ExecuteResponseDocument responseUTSday = null;
			try {
				responseUTSday = (ExecuteResponseDocument) session.execute(
						utsAddress,
						execDocUTS);
			} catch (WPSClientException e) {
				logger.debug(e);
			}
			

		// Get realisations out of response xml.
		// TODO This could be done more elegantly, maybe?
		OutputDataType oType = responseUTSday.getExecuteResponse()
				.getProcessOutputs().getOutputArray(0); // all output elements
		Node wpsComplexData = oType.getData().getComplexData().getDomNode(); // the
																				// complex
																				// data
																				// node
		Node unRealisation = wpsComplexData.getChildNodes().item(0); // the
																		// realisation
																		// node
		Node unValues = unRealisation.getChildNodes().item(3); // the values
																// node
		Node realisationsValueNode = unValues.getChildNodes().item(0); // the
																		// node
																		// with
																		// the
																		// contents
																		// of
																		// the
																		// value
																		// node
		String realisationsValue = realisationsValueNode.getNodeValue(); // the
																			// value
																			// of
																			// the
																			// contents

		// put realisations into double array and return:
		String[] blub = realisationsValue.split(" ");
		for (int a = 0; a < blub.length; a++) {
			samples[a] = Double.parseDouble(blub[a]);
			System.out.println(samples[a]);
		}
			return samples;
	}
	
	/**
	 * This methods calls the UTS and returns samples.
	 * @throws Exception 
	 * @throws UnsupportedUncertaintyTypeException 
	 */
	private double[] handleLognormalDistribution(IUncertainty distribution) throws UnsupportedUncertaintyTypeException, Exception {

		// to be returned
		double[] samples = new double[numberOfRealisations];

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		ExecuteDocument execDocUTS = null;

		try {
			session.connect(utsAddress);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}

			LogNormalDistribution distrib = (LogNormalDistribution) distribution;

			// parameters
			String logScale = distrib.getLogScale().get(0).toString();
			String shape = distrib.getShape().get(0).toString();

			// Make execute request
			try {
				// execDocUTSday = ExecuteDocument.Factory.parse(new
				// File("D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\request_templates\\LogNormalRequest.xml"));
				
				String templatePath = WPSConfig.getConfigPath().substring(0,WPSConfig.getConfigPath().indexOf("config/")).concat("resources/request_templates/");
				
				execDocUTS = ExecuteDocument.Factory.parse(new File(templatePath + "LogNormalRequest.xml"));
			} catch (XmlException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Inserting values into request
			InputType[] types = execDocUTS.getExecute().getDataInputs()
					.getInputArray();
			for (InputType inputType : types) {
				String id = inputType.getIdentifier().getStringValue();
				if (id.equals("distribution")) {

					// Set new logscale
					Node wpsComplexData = inputType.getData().getComplexData()
							.getDomNode(); // Element wps:ComplexData
					Node unLogNormalDistribution = wpsComplexData
							.getChildNodes().item(1); // Element
														// un:LogNormalDistribution
					Node unlogScale = unLogNormalDistribution.getChildNodes()
							.item(1); // Element un:LogScale
					Node logScaleValueNode = unlogScale.getChildNodes().item(0); // Contents
																					// of
																					// it
					logScaleValueNode.setNodeValue(logScale);

					// Set new shape
					Node unShape = unLogNormalDistribution.getChildNodes()
							.item(3); // Element un:LogScale
					// System.out.println(unShape.getNodeName());
					Node shapeValueNode = unShape.getChildNodes().item(0); // Contents
																			// of
																			// it
					shapeValueNode.setNodeValue(shape);

					// set new number of realisations
				} else if (id.equals("numbReal")) {
					Node wpsLiteralData = inputType.getData().getLiteralData()
							.getDomNode();
					Node wpsLiteralDataValueNode = wpsLiteralData
							.getChildNodes().item(0);
					String newNumbReal = ((Integer) numberOfRealisations)
							.toString();
					wpsLiteralDataValueNode.setNodeValue(newNumbReal);
				}
			}



		// The following is the same for all distribution types:

		// Run UTS and get output (=Realisation object)
		ExecuteResponseDocument responseUTSday = null;
		try {
			responseUTSday = (ExecuteResponseDocument) session.execute(
					utsAddress,
					execDocUTS);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}

		// Get realisations out of response xml.
		// TODO This could be done more elegantly, maybe?
		OutputDataType oType = responseUTSday.getExecuteResponse()
				.getProcessOutputs().getOutputArray(0); // all output elements
		Node wpsComplexData = oType.getData().getComplexData().getDomNode(); // the
																				// complex
																				// data
																				// node
		Node unRealisation = wpsComplexData.getChildNodes().item(0); // the
																		// realisation
																		// node
		Node unValues = unRealisation.getChildNodes().item(3); // the values
																// node
		Node realisationsValueNode = unValues.getChildNodes().item(0); // the
																		// node
																		// with
																		// the
																		// contents
																		// of
																		// the
																		// value
																		// node
		String realisationsValue = realisationsValueNode.getNodeValue(); // the
																			// value
																			// of
																			// the
																			// contents

		// put realisations into double array and return:
		String[] blub = realisationsValue.split(" ");
		for (int a = 0; a < blub.length; a++) {
			samples[a] = Double.parseDouble(blub[a]);
			System.out.println(samples[a]);
		}
		return samples;

	} // end of method

	private double[][] handleMultivariateDistribution(MultivariateNormalDistribution distrib){

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		ExecuteDocument execDocUTS = null;

		try {
			session.connect(utsAddress);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
		
		String meanVector = distrib.getMean().toString(); // "[0.0046, 0.0029, 0.0023, 0.0028, ... 0.0105]"
		meanVector = meanVector.substring(1, meanVector.length() - 1); // to
																		// remove
																		// "[",
																		// "]"
		meanVector = meanVector.replace(",", ""); // to remove ","
//		System.out.println("Meanvector elements: " + meanVector.split(" ").length);
		// cov matrix as String
		String covMatrix = distrib.getCovarianceMatrix().getValues()
				.toString(); // This should look like [x, y, z]
		covMatrix = covMatrix.substring(1, covMatrix.length() - 1); // Now
																	// like
																	// this:
																	// x, y,
																	// z
		covMatrix = covMatrix.replace(",", ""); // to remove ","
		
//		System.out.println("Matrix elements: " + covMatrix.split(" ").length);
		
		// Make execute request
		try {
			// execDocUTShours = ExecuteDocument.Factory.parse(new
			// File("D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\request_templates\\MultivariateRequest.xml"));
			
			String templatePath = WPSConfig.getConfigPath().substring(0,WPSConfig.getConfigPath().indexOf("config/")).concat("resources/request_templates/");
			
			execDocUTS = ExecuteDocument.Factory
					.parse(new File(templatePath + "MultivariateRequest.xml"));
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputType[] types = execDocUTS.getExecute().getDataInputs()
				.getInputArray();

		// Change parameters in request
		for (InputType inputType : types) {
			String id = inputType.getIdentifier().getStringValue();

			// Set new number of realisations
			if (id.equals("numbReal")) {
				Node wpsLiteralData = inputType.getData().getLiteralData()
						.getDomNode();
				Node wpsLiteralDataValueNode = wpsLiteralData
						.getChildNodes().item(0);
				String newNumbReal = ((Integer) numberOfRealisations)
						.toString();
				wpsLiteralDataValueNode.setNodeValue(newNumbReal);
			} else if (id.equals("distribution")) {

				// Set new mean vector
				Node wpsComplexData = inputType.getData().getComplexData()
						.getDomNode(); // Element wps:ComplexData
				Node unMultivariateNormalDistribution = wpsComplexData
						.getChildNodes().item(1); // Element
													// un:MultivariateNormalDistributio
				Node unMean = unMultivariateNormalDistribution
						.getChildNodes().item(1); // Element un:Mean
				Node meanValueNode = unMean.getChildNodes().item(0); // its
																		// contents
				meanValueNode.setNodeValue(meanVector);

				// Set new covariance matrix
				Node unCovMat = unMultivariateNormalDistribution
						.getChildNodes().item(3); // Element
													// un:CovarianceMatrix
				Node unValues = unCovMat.getChildNodes().item(1); // Element
																	// values
				Node covmatValueNode = unValues.getChildNodes().item(0);
				covmatValueNode.setNodeValue(covMatrix);

				// Set attribute "dimension"
				NamedNodeMap attrib1 = unCovMat.getAttributes();
				Node attrib = attrib1.item(0);
				Integer dim = 24; // should always be 24
				attrib.setNodeValue(dim.toString());
			}
		}
		
		// The following is the same for all distribution types:

		// Run UTS and get output (=Realisation object)
		ExecuteResponseDocument responseUTSday = null;
		try {
			responseUTSday = (ExecuteResponseDocument) session.execute(
					utsAddress,
					execDocUTS);
		} catch (WPSClientException e) {
			e.printStackTrace();
//			System.out.println(execDocUTS);
		}

		// Get realisations out of response xml.
		// TODO This could be done more elegantly, maybe?
		OutputDataType oType = responseUTSday.getExecuteResponse()
				.getProcessOutputs().getOutputArray(0); // all output elements
		Node wpsComplexData = oType.getData().getComplexData().getDomNode(); // the
																				// complex
																				// data
																				// node
		Node unRealisation = wpsComplexData.getChildNodes().item(0); // the
																		// realisation
																		// node
		Node unValues = unRealisation.getChildNodes().item(3); // the values
																// node
		Node realisationsValueNode = unValues.getChildNodes().item(0); // the
																		// node
																		// with
																		// the
																		// contents
																		// of
																		// the
																		// value
																		// node
		String realisationsValue = realisationsValueNode.getNodeValue(); // the
																			// value
																			// of
																			// the
																			// contents

		double[][] samples = new double[numberOfRealisations][distrib.getCovarianceMatrix().getDimension()];
		
			// Put raw values into double[]:
			// (All values of all variables into one array)
			String[] blub = realisationsValue.split(" ");
			List<Double> utsResult = new LinkedList<Double>();
			for (int a = 0; a < blub.length; a++) {
				utsResult.add(Double.parseDouble(blub[a]));
			}

			// Put values into a double[][]
			// (the first 24 values are all hours of day for the first austal run,
			// ...)
			int hour = 0;
			int austalrun = 0;
			for (int i = 0; i < utsResult.size(); i++) { // immer 24 hintereinander
				samples[austalrun][hour] = utsResult.get(i);
				hour++;
				if (hour == 24) {
					hour = 0;
					austalrun++;
				}
			}
		
		return samples;
		
	}
	
	
	/** 
	 * 
	 * This method creates an execute request for the Austal WPS and runs it. At
	 * the end, the outputs should be stored and/or processed.
	 * 
	 */
	private IObservationCollection makeRequestAndRunAustal(int runNumber) {

		// connect to austal WPS
		WPSClientSession session = WPSClientSession.getInstance();

		try {
			session.connect(serviceURL);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
		}
		
		Map<String, Object> inputs = new HashMap<String, Object>();
		
		for (String id : uncertainInputSamplesMap.keySet()) {
			
			File f = new File("c:/temp/uom" + System.currentTimeMillis() + ".xml");
			
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
		}
		inputs.put("receptor-points", staticInputsList.get(0).getPayload());
		
		// Make execute request
		ExecuteDocument execDoc = null;
		try {
			execDoc = createExecuteDocument(serviceURL, modelIdentifier, inputs);
		} catch (XmlException e) {
			logger.debug(e);
		} catch (IOException e) {
			logger.debug(e);
		} catch (Exception e) {
			logger.debug(e);
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
	
	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		return stringWriter.toString();
	}
	


}
