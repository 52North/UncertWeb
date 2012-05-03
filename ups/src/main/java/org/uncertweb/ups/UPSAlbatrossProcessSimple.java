package org.uncertweb.ups;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;

/**
 * @author t_ever02
 *
 */
public class UPSAlbatrossProcessSimple extends AbstractAlgorithm {

	private static Logger logger = Logger.getLogger(UPSAlbatrossProcessSimple.class);
	

	/**
	 * identifier for model service URL (Albatross)
	 */
	private static final String INPUT_ID_MODEL_SERVICE_URL = "ModelServiceURL";	
	
	
	/**
	 * identifier for model service URL (Albatross)
	 */
	private static final String INPUT_ID_POP_MODEL_SERVICE_URL = "ModelServiceURL";	
	

	/**
	 * identifier for process identifier of the simulated process/model
	 */
	private static final String INPUT_ID_SIMULATION_PROCESS_IDENTIFIER = "IdentifierSimulatedProcess"; 
	
	
	/**
	 * identifier for process identifier of the simulated process/model
	 */
	private static final String INPUT_ID_SYN_POP_PROCESS_IDENTIFIER = "IdentifierSimulatedProcess"; 
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_GENPOP = "genpop-households"; 
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_RWDATA = "rwdata-households"; 
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_POSTCODE_AREAS = "postcode-areas"; 
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_ZONES = "zones"; 
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_MUNICIPALITIES = "municipalities"; 
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_EXPORT_FILE= "export-file"; 
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_EXPORT_FILE_BIN = "export-file-bin";    
	
	
	/**
	 * identifier for static inputs
	 */	
	private static final String INPUT_ID_IS_BOOTSTRAPPING = "isBootstrapping";  
	
	
	/**
	 * List of errors that occurred during the execution of the process
	 */
	private List<String> errors;
	
//	private List<String> inputIDs;
	
	
//	/*
//	 * identifier for requested output uncertainty type
//	 */	
//	private static final String INPUT_ID_OUTPUT_UNCERT_TYPE = "OutputUncertaintyType";
//	
//	
//	/*
//	 * identifier for the uncertain output
//	 */	
//	private static final String OUTPUT_ID_UNCERT_PROCESS_OUTPUTS = "UncertainProcessOutputs";	
	
	
//	private ExecuteDocument exDoc = null;
//	
	
//	

//
//	List<MeasurementCollection> realisationCollection;
	
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		/*
		 * This UPS process will perform the following steps.
		 * 
		 * This is a simplified (testing) version without using 
		 * extra uncertainty parameters and just one realization run.
		 */
		
		//1 parse input data for the syn pop model WPS and Albatross model WPS
		//2 execute syn pop WPS
		//3 extract results
		//4 execute Albatross model WPS and return results
		
		
		/*
		 * 1: Parse input data
		 */
		
		HashMap<String, Object> inputs = new HashMap<String, Object>();
		
		// Retrieve syn pop model service URL from input. Must exist!
		IData iData = inputData.get(INPUT_ID_POP_MODEL_SERVICE_URL).get(0);
		String popModelServiceURL = (String) iData.getPayload();
		
		// Retrieve model service URL from input. Must exist!
		iData = inputData.get(INPUT_ID_MODEL_SERVICE_URL).get(0);
		String modelServiceURL = (String) iData.getPayload();
		
		
		// Retrieve syn pop model process identifier URL from input. Must exist!
		iData = inputData.get(INPUT_ID_SYN_POP_PROCESS_IDENTIFIER).get(0);
		String synPopProcessID = iData.getPayload().toString();
		
		// Retrieve model process identifier URL from input. Must exist!
		iData = inputData.get(INPUT_ID_SIMULATION_PROCESS_IDENTIFIER).get(0);
		String modelProcessID = iData.getPayload().toString();
		
		
		// Retrieve genpop-households from input. Must exist!
		iData = inputData.get(INPUT_ID_GENPOP).get(0);
		inputs.put(INPUT_ID_GENPOP, iData.getPayload());
		
		// Retrieve rwdata-households from input. Must exist!
		iData = inputData.get(INPUT_ID_RWDATA).get(0);
		inputs.put(INPUT_ID_RWDATA, iData.getPayload());
		
		// Retrieve postcode-areas from input. Must exist!
		iData = inputData.get(INPUT_ID_POSTCODE_AREAS).get(0);
		inputs.put(INPUT_ID_POSTCODE_AREAS, iData.getPayload());
		
		// Retrieve zones from input. Must exist!
		iData = inputData.get(INPUT_ID_ZONES).get(0);
		inputs.put(INPUT_ID_ZONES, iData.getPayload());
		
		// Retrieve municipalities from input. Must exist!
		iData = inputData.get(INPUT_ID_MUNICIPALITIES).get(0);
		inputs.put(INPUT_ID_MUNICIPALITIES, iData.getPayload());
		
		// set 'isBootstrapping' to false (as default)
		inputs.put(INPUT_ID_IS_BOOTSTRAPPING, new Boolean(false));
		
		
		/*
		 * 2: Execute syn pop WPS
		 */
		
		//build execute document
		ExecuteDocument execDoc = this.createExecuteDocument(popModelServiceURL, synPopProcessID, inputs);
		
		//execute operation
		ExecuteResponseDocument response = this.executeRequest(execDoc, popModelServiceURL);
		
		
		/*
		 * 3: extract results from syn pop model run
		 */
		
		String exportPath = this.extractProperty(response, INPUT_ID_EXPORT_FILE);
		String exportBinPath = this.extractProperty(response, INPUT_ID_EXPORT_FILE_BIN);
		
		//adjust inputs map
		inputs.put(INPUT_ID_EXPORT_FILE, exportPath);
		inputs.put(INPUT_ID_EXPORT_FILE_BIN, exportBinPath);
		inputs.remove(INPUT_ID_IS_BOOTSTRAPPING);
		
		
		/*
		 * 4: execute Albatross model and return results
		 */
		
		//build execute document
		execDoc = this.createExecuteDocument(modelServiceURL, modelProcessID, inputs);
		
		//execute request
		response = this.executeRequest(execDoc, modelServiceURL);
		
		//TODO: build result when the Albatross WPS works
		return null;
		


//		//TODO: remove array list? (size = 1 because of just one realization run)
//		ArrayList<MeasurementCollection> realisationCollection = new ArrayList<MeasurementCollection>(1);
//		
//		/*
//		 * fill the map that is required to create the executedocument
//		 */
//		Map<String, Object> idsAndValues = new HashMap<String, Object>(this.getInputIDs().size());
//		
//		for (String id : getInputIDs()) {
//			list = inputData.get(id);
//			IData data = list.get(0);			
//			if(data instanceof LiteralStringBinding){
//				idsAndValues.put(id,data.getPayload());				
//			}
//		}
//		
//		try {
//			this.exDoc = createExecuteDocument(this.modelServiceURL, this.modelIdentifier, idsAndValues, "indicators");
//		} catch (Exception e) {
//			this.errors.add(e.getMessage());
//			logger.error(e);
//			throw new RuntimeException(e);			
//		}
//		
//		for (int i = 0; i < this.numberOfRealisations; i++) {
//			handleResponse(i);
//		}
//		
//		/*
//		 *TODO: check output uncertainty type 
//		 */
//		
//		//TODO: write result as output
//		
////		UncertaintyObservationCollection resultCollection = createResultCollection();
////		
////		UncertaintyObservationCollection uobscoll = new UncertaintyObservationCollection();
////		
//		Map<String, IData> result = new HashMap<String, IData>();
////		
////		result.put(OUTPUT_ID_UNCERT_PROCESS_OUTPUTS, new OMBinding(resultCollection));
//		
//		return result;
//	}
//
//	private UncertaintyObservationCollection createResultCollection(){
//	
//		UncertaintyObservationCollection uobscoll = new UncertaintyObservationCollection();
//
//		/*
//		 * its the same satial and temporal extent for all measurements
//		 * so take the first one and save the attributes (phenomenonTime, feautureOfInterest,...)
//		 */
//		Measurement m = this.realisationCollection.get(0).getObservations().get(0);
//		
//		/*
//		 * create hashmap with identifier of observed property and values of the different realisations
//		 */
//		Map<String, List<Double>> observedPropertyValuesMap = new HashMap<String, List<Double>>(this.realisationCollection.get(0).getObservations().size());
//		Map<String, String> observedPropertyResultUOMMap = new HashMap<String, String>(this.realisationCollection.get(0).getObservations().size());
//		
//		for (MeasurementCollection obscoll : this.realisationCollection) {
//			
//			MeasurementCollection mcoll = obscoll;
//			
//			for (Measurement tmpm : mcoll.getObservations()) {
//				
//				if(!observedPropertyValuesMap.keySet().contains(tmpm.getObservedProperty().toString())){
//					
//					List<Double> valuesList = new ArrayList<Double>();
//					
//					valuesList.add(tmpm.getResult().getMeasureValue());
//					
//					observedPropertyValuesMap.put(tmpm.getObservedProperty().toString(), valuesList);
//					observedPropertyResultUOMMap.put(tmpm.getObservedProperty().toString(), tmpm.getResult().getUnitOfMeasurement());
//				}else{
//					observedPropertyValuesMap.get(tmpm.getObservedProperty().toString()).add(tmpm.getResult().getMeasureValue());
//				}
//				
//			}
//		}
//		
//		/*
//		 * iterate over observedproperties and create uncertaintyobservations
//		 * with attributes of Measurement "m" and the respective values
//		 */
//		
//		int counter = 0;
//		
//		for (String uriString : observedPropertyValuesMap.keySet()) {
//			
//			List<Double> values = observedPropertyValuesMap.get(uriString);
//			
//			URI uri = null;
//
//			URI procedure = null;
//			
//			URI observedProperty = null;
//			
//			try {
//				uri = new URI("http://uncertweb.org"); 
//				procedure = new URI(
//						"http://www.uncertweb.org/models/albatross");
//				observedProperty = new URI(
//						uriString);
//			} catch (URISyntaxException e) {
//					e.printStackTrace();
//			}
//			/*
//			 * TODO: if the bug in the api gets fixed remove weight and id
//			 */
//
//			ContinuousRealisation cr = new ContinuousRealisation(values);
//
//
//			UncertaintyResult uResult = new UncertaintyResult(cr, observedPropertyResultUOMMap.get(uriString));
//			
////			Identifier identifier = new Identifier(uri, "Albatross" + counter);	
//			
//			UncertaintyObservation uob = new UncertaintyObservation(m.getPhenomenonTime(), m.getResultTime(), procedure, observedProperty, m.getFeatureOfInterest(), uResult);
//			
//			uobscoll.addObservation(uob);
//			
//			counter++;
//		}
//		
//		return uobscoll;
	}
	
	
	/**
	 * Extracts the String value of a Property from a WPS execute response
	 * 
	 * @param response the WPS response document
	 * @param propertyID the ID of the property
	 * 
	 * @return the property value as string or <code>null</code> in case of errors
	 */
	private String extractProperty(ExecuteResponseDocument response, String propertyID) {
		try {
			OutputDataType[] outputs = response.getExecuteResponse().getProcessOutputs().getOutputArray();
			OutputDataType output = null;
			String outputID;
			
			//find output
			for (int i = 0; i < outputs.length; i++) {
				output = outputs[i];
				outputID = output.getIdentifier().getStringValue();
				if (outputID.equals(propertyID)) {
					break;
				}
			}
			
			if (output == null) {
				return null;
			}
			
			//get data content
			if (output.getData().isSetLiteralData()) {
				return output.getData().getLiteralData().getStringValue();
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
			this.errors.add(t.getMessage());
			return null;
		}
		
		return null;
	}


	/**
	 * Executes a WPS execute request.
	 * 
	 * @param execDoc the execute request document
	 * @param wpsURL the WPS service URL
	 * 
	 * @return the WPS response or <code>null</code> in case of errors
	 */
	private ExecuteResponseDocument executeRequest(ExecuteDocument execDoc, String wpsURL) {
		try {
			ExecuteResponseDocument response = (ExecuteResponseDocument) WPSClientSession.getInstance().execute(wpsURL, execDoc);
			
			return response;
		} catch (WPSClientException e) {
			e.printStackTrace();
			this.errors.add(e.getMessage());
			return null;
		}
	}
	
//	private void handleResponse(int runNumber) {
//		try {
//			 ExecuteResponseDocument response =
//			 (ExecuteResponseDocument)WPSClientSession.getInstance().execute(this.modelServiceURL,
//			 this.exDoc);
////			ExecuteResponseDocument response = ExecuteResponseDocument.Factory
////					.parse(new File(
////							"C:\\UncertWeb\\Albatross\\alb_resp_indicators.xml"));
//
//			OutputDataType oType = response.getExecuteResponse()
//					.getProcessOutputs().getOutputArray(0);
//			Node wpsComplexData = oType.getData().getComplexData().getDomNode();
//			// the complex data node
//			Node unRealisation = wpsComplexData.getChildNodes().item(0);
//			// the realisation node
//			IObservationCollection iobs = null;
//			try {
//				iobs = new XBObservationParser()
//						.parseObservationCollection(nodeToString(unRealisation));
//			} catch (Exception e) {
//				unRealisation = wpsComplexData.getChildNodes().item(1);
//				iobs = new XBObservationParser()
//						.parseObservationCollection(nodeToString(unRealisation));
//			}
//			/*
//			 * gather all responses
//			 */
//			this.realisationCollection.add((MeasurementCollection)iobs);
//
//			logger.debug(response);
//		} catch (Exception e) {
//			this.errors.add(e.getMessage());
//			logger.error(e);
//		}
//	}
	
//	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
//		StringWriter stringWriter = new StringWriter();
//		Transformer transformer = TransformerFactory.newInstance().newTransformer();
//		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
//		
//		return stringWriter.toString();
//	}
	
	@Override
	public List<String> getErrors() {
		return this.errors;
	}

	@Override
	public Class<? extends IData> getInputDataType(String id) {
		if (id.equals(INPUT_ID_SIMULATION_PROCESS_IDENTIFIER)) {
			return LiteralStringBinding.class;
		} 
		else if (id.equals(INPUT_ID_MODEL_SERVICE_URL)) {
			return LiteralStringBinding.class;
		} 
//		else if(getInputIDs().contains(id)) {
			return LiteralStringBinding.class;			
//		}
//		return null;
			//TODO implement
	}

	@Override
	public Class<? extends IData> getOutputDataType(String id) {
//		if (id.equals(OUTPUT_ID_UNCERT_PROCESS_OUTPUTS)) {
//			return UncertWebIODataBinding.class;
//		}
		//TODO implement
		return null;
	}
	
	
	/**
	 * This method creates an <c>ExecuteDocument</c>.
	 * 
	 * @param urlString the URL of the WPS that is executed
	 * @param processID Identifier of the WPS process
	 * @param inputs a map holding the identifiers of the inputs and the values
	 * 
	 * @return an execute document to send to the WPS or <code>null</code> in case of Exceptions
	 */
	public ExecuteDocument createExecuteDocument(String urlString, String processID, HashMap<String, Object> inputs) {		

		//get process description from WPS
		ProcessDescriptionType processDescription;
		try {
			processDescription = WPSClientSession.getInstance().getProcessDescription(urlString, processID);
		
			//create execute request builder
			org.n52.wps.client.ExecuteRequestBuilder executeBuilder = new org.n52.wps.client.ExecuteRequestBuilder(
					processDescription);
	
			//add all input data
			for (InputDescriptionType input : processDescription.getDataInputs().getInputArray()) {
				String inputName = input.getIdentifier().getStringValue();
				Object inputValue = inputs.get(inputName);
				executeBuilder.addLiteralData(inputName,inputValue.toString());
			}
			
			logger.debug(executeBuilder.getExecute());
			
			return executeBuilder.getExecute();
		} 
		catch (IOException e) {
			e.printStackTrace();
			this.errors.add(e.getMessage());
			return null;
		}
	}
	
	
//	private List<String> getInputIDs(){
//		
//		if(this.inputIDs == null){			
//			this.inputIDs = new ArrayList<String>();
//			this.inputIDs.add(INPUT_ID_EXPORT_FILE);
//			this.inputIDs.add(INPUT_ID_EXPORT_FILE_BIN);
//			this.inputIDs.add(INPUT_ID_GENPOP);
//			this.inputIDs.add(INPUT_ID_MUNICIPALITIES);
//			this.inputIDs.add(INPUT_ID_POSTCODE_AREAS);
//			this.inputIDs.add(INPUT_ID_RWDATA);
//			this.inputIDs.add(INPUT_ID_ZONES);
//		}		
//		return this.inputIDs;
//	}

}
