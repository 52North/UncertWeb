package org.uncertweb.ups;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.opengis.om.x20.FoiPropertyType;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMTextObservationCollectionDocument;
import net.opengis.om.x20.UWMeasurementType;
import net.opengis.om.x20.UWTextObservationType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author t_ever02
 *
 */
public class UPSAlbatrossProcessSimple extends AbstractAlgorithm {


	/**
	 * identifier for model service URL (Albatross)
	 */
	private static final String INPUT_ID_MODEL_SERVICE_URL = "AlbatrossServiceURL";	
	
	
	/**
	 * identifier for syn pop service URL
	 */
	private static final String INPUT_ID_SYN_POP_MODEL_SERVICE_URL = "SynPopServiceURL";	
	

	/**
	 * identifier for process identifier of the simulated process/model (Albatross)
	 */
	private static final String INPUT_ID_SIMULATION_PROCESS_IDENTIFIER = "IdentifierAlbatrossProcess"; 
	
	
	/**
	 * identifier for process identifier of the simulated process/model
	 */
	private static final String INPUT_ID_SYN_POP_PROCESS_IDENTIFIER = "IdentifierSynPopProcess"; 
	
	
	/**
	 * identifier for static input: Number of houses created by genpop
	 */	
	private static final String INPUT_ID_GENPOP = "genpop-households"; 
	
	
	/**
	 * identifier for static input: Number of household activity sets created by rwdata
	 */	
	private static final String INPUT_ID_RWDATA = "rwdata-households"; 
	
	
	/**
	 * identifier for static input: Number of postal code areas (PC4)
	 */	
	private static final String INPUT_ID_POSTAL_CODE_AREAS = "postcode-areas"; 
	
	
	/**
	 * identifier for static input: Number of (larger) zones
	 */	
	private static final String INPUT_ID_ZONES = "zones"; 
	
	
	/**
	 * identifier for static input: Number of municipalities
	 */	
	private static final String INPUT_ID_MUNICIPALITIES = "municipalities"; 
	
	
	/**
	 * identifier for the path to the export file created by syn-pop
	 */	
	private static final String INPUT_ID_EXPORT_FILE= "export-file"; 
	
	
	/**
	 * identifier for the path to the binary export file created by syn-pop
	 */	
	private static final String INPUT_ID_EXPORT_FILE_BIN = "export-file-bin";    
	
	
	/**
	 * identifier for the bootstrapping flag
	 */	
	private static final String INPUT_ID_IS_BOOTSTRAPPING = "isBootstrapping";  
	
	
	/**
	 * identifier for the random number seed
	 */	
	private static final String INPUT_ID_RANDOM_NUMBER_SEED = "randomNumberSeed";  
	
	
	/**
	 * identifier for the first Albatross output
	 */
	private static final String OUTPUT_ID_OD_MATRIX = "ODmatrix";
	
	
	/**
	 * identifier for the second Albatross output
	 */
	private static final String OUTPUT_ID_INDICATORS = "indicators";
	
	
	/**
	 * List of errors that occurred during the execution of the process
	 */
	private List<String> errors;
	
	
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
		IData iData = inputData.get(INPUT_ID_SYN_POP_MODEL_SERVICE_URL).get(0);
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
		iData = inputData.get(INPUT_ID_POSTAL_CODE_AREAS).get(0);
		inputs.put(INPUT_ID_POSTAL_CODE_AREAS, iData.getPayload());
		
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
		
		if (exportBinPath == null || exportPath == null) {
			this.errors.add("could not extract results from syn-pop-WPS response");
			return new HashMap<String, IData>();
		}
		
		//adjust inputs map
		inputs.put(INPUT_ID_EXPORT_FILE, exportPath);
		inputs.put(INPUT_ID_EXPORT_FILE_BIN, exportBinPath);
		inputs.put(INPUT_ID_RANDOM_NUMBER_SEED, new Integer(1));
		inputs.remove(INPUT_ID_IS_BOOTSTRAPPING);
		
		
		/*
		 * 4: execute Albatross model and return results
		 */
		
		//build execute document
		execDoc = this.createExecuteDocument(modelServiceURL, modelProcessID, inputs);
		
		
		//execute request
		response = this.executeRequest(execDoc, modelServiceURL);
		
		//build result
		HashMap<String, IData> result = new HashMap<String, IData>();
		
		/*
		 * parse ODMatrix
		 * 
		 * make a TextObservationCollection object
		 * for each observation in the OD Matrix build the Observation object (e.g. TextObservation)
		 * add all observations to the collection
		 * make a new OMBinding object with collection as parameter
		 * put OMBinding in result map
		 * 
		 * 
		 */
		String odMatrixString = this.extractProperty(response, OUTPUT_ID_OD_MATRIX);
		
		try {
			//parse result collection
			OMTextObservationCollectionDocument ocDoc = OMTextObservationCollectionDocument.Factory.parse(odMatrixString);
			
			TextObservationCollection textObservationCollection = buildTextObservationCollection(ocDoc);
			
			//build IData object and add to result map
			OMBinding odMatrix = new OMBinding(textObservationCollection);
			result.put(OUTPUT_ID_OD_MATRIX, odMatrix);
		} 
		catch (XmlException e) {
			this.errors.add(e.getMessage());
		}
		catch (IndexOutOfBoundsException e) {
			this.errors.add(e.getMessage());
		} 
		catch (URISyntaxException e) {
			this.errors.add(e.getMessage());
		}
		catch (ParseException e) {
			this.errors.add(e.getMessage());
		}
		
		/*
		 * parse indicators
		 * 
		 * same procedure as above
		 */
		String indicatorString = this.extractProperty(response, OUTPUT_ID_INDICATORS);
		
		try {
			OMMeasurementCollectionDocument mcDoc = OMMeasurementCollectionDocument.Factory.parse(indicatorString);
			
			MeasurementCollection measurementCollection = buildMeasurementCollection(mcDoc);
			
			//build IData object and add to result map
			OMBinding indicators = new OMBinding(measurementCollection);

			result.put(OUTPUT_ID_INDICATORS, indicators);
		}
		catch (XmlException e) {
			this.errors.add(e.getMessage());
		}
		catch (URISyntaxException e) {
			this.errors.add(e.getMessage());
		}
		catch (ParseException e) {
			this.errors.add(e.getMessage());
		}

		//return process outputs
		return result;
	}


	/**
	 * Creates a {@link MeasurementCollection} object form an XML Beans
	 * {@link OMMeasurementCollectionDocument}.
	 * 
	 * @param mcDoc the XML Beans document
	 * 
	 * @return the om-api measurement collection object
	 * 
	 * @throws URISyntaxException in case of bad URIs
	 * @throws ParseException in case of WKT geometry parsing errors
	 */
	private MeasurementCollection buildMeasurementCollection(OMMeasurementCollectionDocument mcDoc) throws URISyntaxException, ParseException {
		//build measurement collection object
		MeasurementCollection measurementCollection = new MeasurementCollection();
		
		//measurement and content types
		Measurement measurementObj;
		UWMeasurementType measurement;
		TimeObject phenomenonTime;
		TimeObject resultTime;
		URI procedure;
		URI observedProperty;
		SpatialSamplingFeature featureOfInterest;
		MeasureResult measureResult;
		String s;
		
		//parse each measurement in the measurement collection
		UWMeasurementType[] measurements = mcDoc.getOMMeasurementCollection().getOMMeasurementArray();
		for (int i = 0; i < measurements.length; i++) {
			measurement = measurements[i];
			
			//phenomenon time
			if (measurement.getPhenomenonTime().isSetAbstractTimePrimitive()) {
				//parse phenomenon time
				s = measurement.getPhenomenonTime().getAbstractTimePrimitive().xmlText();
				s = s.split(">")[2];
				s = s.substring(0, s.indexOf("</"));
				phenomenonTime = new TimeObject(s);
			}
			else {
				//set href
				s = measurement.getPhenomenonTime().getHref();
				phenomenonTime = new TimeObject(new URI(s));
			}
			
			//result time
			if (measurement.getResultTime().isSetTimeInstant()) {
				//parse result time
				s = measurement.getResultTime().getTimeInstant().getTimePosition().getStringValue();
				resultTime = new TimeObject(s);
			}
			else {
				//set href
				s = measurement.getResultTime().getHref();
				resultTime = new TimeObject(new URI(s));
			}
			
			//procedure
			s = measurement.getProcedure().getHref();
			procedure = new URI(s);
			
			//observed property
			s = measurement.getObservedProperty().getHref();
			observedProperty = new URI(s);
			
			//feature of interest
			featureOfInterest = this.createSamplngFeature(measurement.getFeatureOfInterest());
			
			//result
			measureResult = new MeasureResult(measurement.getResult().getDoubleValue(), measurement.getResult().getUom());
			
			//build representation
			measurementObj = new Measurement(phenomenonTime, resultTime, procedure, observedProperty, featureOfInterest, measureResult);
			
			//add to collection implementation
			measurementCollection.addObservation(measurementObj);
		}
		return measurementCollection;
	}


	/**
	 * Parses an XML Beans text observation collection document to
	 * a {@link TextObservationCollection} object.
	 * 
	 * @param ocDoc the XML Beans document
	 * 
	 * @return the om-api object
	 * 
	 * @throws URISyntaxException in case of bad URIs in the document
	 * @throws ParseException in case of WKT to Geometry parsing errors
	 */
	private TextObservationCollection buildTextObservationCollection(OMTextObservationCollectionDocument ocDoc) throws URISyntaxException,
			ParseException {
		//build representations for each observation and add to result collection
		TextObservationCollection textObservationCollection = new TextObservationCollection();
		
		//observation and content types
		TextObservation textObservation;
		UWTextObservationType observation;
		String s;
		TimeObject phenomenonTime;
		TimeObject resultTime;
		URI procedure;
		URI observedProperty;
		SpatialSamplingFeature featureOfInterest;
		TextResult textResult;
		
		//parse each observation in result collection
		UWTextObservationType[] observations = ocDoc.getOMTextObservationCollection().getOMTextObservationArray();
		for (int i = 0; i < observations.length; i++) {
			observation = observations[i];
			
			//phenomenon time
			s = observation.getPhenomenonTime().getAbstractTimePrimitive().xmlText();
			s = s.split(">")[2];
			s = s.substring(0, s.indexOf("</"));
			phenomenonTime = new TimeObject(s);
			
			//result time
			s = observation.getResultTime().getTimeInstant().getTimePosition().getStringValue();
			resultTime = new TimeObject(s);
			
			//procedure
			s = observation.getProcedure().getHref();
			procedure = new URI(s);
			
			//observed property
			s = observation.getObservedProperty().getHref();
			observedProperty = new URI(s);
			
			//feature of interest
			featureOfInterest = createSamplngFeature(observation.getFeatureOfInterest());
			
			//text result
			s = observation.getResult();
			textResult = new TextResult(s);
			
			//build representation
			textObservation = new TextObservation(phenomenonTime , resultTime, procedure, observedProperty, featureOfInterest, textResult);
			
			//add to collection representation
			textObservationCollection.addObservation(textObservation);
		}
		return textObservationCollection;
	}
	
	
	/**
	 * Creates a {@link SpatialSamplingFeature} from an O&M Observation
	 * 
	 * @param observation the observation as {@link UWTextObservationType}
	 * 
	 * @return the sampling feature object
	 * @throws URISyntaxException in case of malformed href URIs
	 * @throws ParseException in case of WKT geometry parsing problems
	 */
	private SpatialSamplingFeature createSamplngFeature(FoiPropertyType foi) throws URISyntaxException, ParseException {
		
		if (foi.isSetHref()) {
			//just href
			return new SpatialSamplingFeature(new URI(foi.getHref()));
		}
		
		SFSpatialSamplingFeatureType spatialSamplingFeatureType = foi.getSFSpatialSamplingFeature();
		
		String sampledFeature;
		if (spatialSamplingFeatureType.getSampledFeature().isNil()) {
			sampledFeature = null;
		}
		else {
			sampledFeature = spatialSamplingFeatureType.getSampledFeature().xmlText();
		}
		
		Geometry shape = parseGeometry(spatialSamplingFeatureType);
		
		if (spatialSamplingFeatureType.isSetIdentifier()) {
			//add identifier
			URI codeSpace = new URI(spatialSamplingFeatureType.getIdentifier().getCodeSpace());
			Identifier identifier = new Identifier(codeSpace, spatialSamplingFeatureType.getIdentifier().getStringValue());
			
			
			if (spatialSamplingFeatureType.isSetBoundedBy()) {
				//also add bounded by
				//TODO implement, bounded by is optional and ignored by now
//				return new SpatialSamplingFeature(identifier, boundedBy, sampledFeature, shape);
			}
			
			return new SpatialSamplingFeature(identifier, sampledFeature, shape);
		}
		
		return new SpatialSamplingFeature(sampledFeature, shape);
	}


	/**
	 * parses a {@link Geometry} from the shape information of a spatial sampling feature
	 * 
	 * @param spatialSamplingFeatureType the sampling feature
	 * 
	 * @return a {@link Polygon} of the feature shape
	 * @throws ParseException in case the WKT text could not be parsed
	 */
	private Geometry parseGeometry(SFSpatialSamplingFeatureType spatialSamplingFeatureType) throws ParseException {
		ShapeType shape = spatialSamplingFeatureType.getShape();
		
		String s = shape.getAbstractGeometry().xmlText();
		
		//cut by posList
		String[] sArray = s.split("posList");
		s = sArray[1];
		s = s.substring(1, s.indexOf("</"));
		
		//add ',' between each coordinate pair
		sArray = s.split(" ");
		s = "";
		for (int i = 0; i < sArray.length - 2; i += 2) {
			s += sArray[i] + " " + sArray[i + 1] + ", ";
		}
		s += sArray[sArray.length - 2] + " " + sArray[sArray.length - 1];
		
		//build WKT polygon
		s = "Polygon ((" + s + "))";
		
		WKTReader reader = new WKTReader();
		return reader.read(s);
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
//					LOGGER.info("## property found");
					break;
				}
			}
			
			if (output == null) {
				return null;
			}
			if (!output.isSetData()) {
				return null;
			}
			
			DataType data = output.getData();
			
			//get data content
			if (data.isSetLiteralData()) {
				return data.getLiteralData().getStringValue();
			}
			else if (data.isSetComplexData()) {
				//get complex data
				String complexData = data.getComplexData().getDomNode().getFirstChild().getNodeValue();
				
				//extract from CDATA caption
				complexData = this.removeCDATA(complexData);
				
				return complexData;
			}
		}
		catch (Throwable t) {
			this.errors.add(t.getMessage());
			return null;
		}
		
		return null;
	}


	/**
	 * Removes a CDATA caption from a XML text and 
	 * replaces &lt; and &gt; by '<' and '>'
	 * 
	 * @param xmlText the XML ass text string
	 * 
	 * @return the updated text
	 */
	private String removeCDATA(String xmlText) {
		String text = xmlText;
		//remove CDATA caption
		Pattern pattern = Pattern.compile("<![CDATA[", Pattern.LITERAL);
		Matcher matcher = pattern.matcher(text);
		text = matcher.replaceAll("");
		text = text.replaceAll("]]", "");
		
		//replace encoded < and >
		text = text.replaceAll("&lt;", "<");
		text = text.replaceAll("&gt;", ">");
		
		return text;
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
			this.errors.add(e.getMessage());
			return null;
		}
	}
	
	
	@Override
	public List<String> getErrors() {
		return this.errors;
	}

	@Override
	public Class<? extends IData> getInputDataType(String id) {
		if (id.equals(INPUT_ID_MODEL_SERVICE_URL)) {
			return LiteralStringBinding.class;
		} 
		else if (id.equals(INPUT_ID_SYN_POP_MODEL_SERVICE_URL)) {
			return LiteralStringBinding.class;
		} 
		else if (id.equals(INPUT_ID_SIMULATION_PROCESS_IDENTIFIER)) {
			return LiteralStringBinding.class;
		}
		else if (id.equals(INPUT_ID_SYN_POP_PROCESS_IDENTIFIER)) {
			return LiteralStringBinding.class;
		}
		else if (id.equals(INPUT_ID_GENPOP)) {
			return LiteralStringBinding.class;
		}
		else if (id.equals(INPUT_ID_RWDATA)) {
			return LiteralStringBinding.class;
		}
		else if (id.equals(INPUT_ID_POSTAL_CODE_AREAS)) {
			return LiteralStringBinding.class;
		}
		else if (id.equals(INPUT_ID_ZONES)) {
			return LiteralStringBinding.class;
		}
		else if (id.equals(INPUT_ID_MUNICIPALITIES)) {
			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public Class<? extends IData> getOutputDataType(String id) {
		if (id.equals(OUTPUT_ID_OD_MATRIX)) {
			return UncertWebIODataBinding.class;
		}
		else if (id.equals(OUTPUT_ID_INDICATORS)) {
			return UncertWebIODataBinding.class;
		}
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
			InputDescriptionType input;
			InputDescriptionType[] inputArray = processDescription.getDataInputs().getInputArray();
			String inputName;
			Object inputValue;
			for (int i = 0; i < inputArray.length; i++) {
				input = inputArray[i];
				inputName = input.getIdentifier().getStringValue();
				if (!inputs.containsKey(inputName)) {
					//input name not available, OK if optional
					continue;
				}
				inputValue = inputs.get(inputName);
				executeBuilder.addLiteralData(inputName,inputValue.toString());
			}
			
			return executeBuilder.getExecute();
		} 
		catch (IOException e) {
			this.errors.add(e.getMessage());
			return null;
		}
	}
}
