package org.uncertweb.ups;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
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
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertml.IUncertainty;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.w3c.dom.Node;

public class UPSAlbatrossProcess extends AbstractAlgorithm {

	private static Logger logger = Logger.getLogger(UPSAlbatrossProcess.class);

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
	private String inputIDgenpop_households = "genpop-households";
	/*
	 * identifier for static inputs
	 */
	private String inputIDrwdata_householdss = "rwdata-households";
	/*
	 * identifier for static inputs
	 */
	private String inputIDpostcode_areas = "postcode-areas";
	/*
	 * identifier for static inputs
	 */
	private String inputIDzones = "zones";
	/*
	 * identifier for static inputs
	 */
	private String inputIDmunicipalities = "municipalities";
	/*
	 * identifier for static inputs
	 */
	private String inputIDexport_file= "export-file";
	/*
	 * identifier for static inputs
	 */
	private String inputIDexport_file_bin = "export-file-bin";
	/*
	 * identifier for requested output uncertainty type
	 */
	private String inputIDOutputUncertaintyType = "OutputUncertaintyType";
	/*
	 * identifier for the uncertain output
	 */
	private String outputIDUncertainProcessOutputs = "UncertainProcessOutputs";

	private ExecuteDocument exDoc = null;

	private List<String> errors;

	private List<String> inputIDs;

	private String modelIdentifier;

	List<MeasurementCollection> realisationCollection;

	public UPSAlbatrossProcess(){
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		// Retrieve model service URL from input. Must exist!
		List<IData> list = inputData.get(inputIDServiceURL);
		IData serviceURL_IData = list.get(0);
		serviceURL = (String) serviceURL_IData.getPayload();

		// Retrieve model process identifier URL from input. Must exist!
		list = inputData.get(inputIDIdentifierSimulatedProcess);
		IData modelIdentifier_IData = list.get(0);
		modelIdentifier = (String) modelIdentifier_IData.getPayload();

		// Retrieve number of realisations from input. Must exist!
		list = inputData.get(inputIDNumberOfRealisations);
		IData numberOfReal = list.get(0);
		numberOfRealisations = (Integer) numberOfReal.getPayload();

		realisationCollection = new ArrayList<MeasurementCollection>(numberOfRealisations);

		/*
		 * fill the map that is required to create the executedocument
		 */
		Map<String, Object> idsAndValues = new HashMap<String, Object>(getInputIDs().size());

		for (String id : getInputIDs()) {
			list = inputData.get(id);
			IData data = list.get(0);
			if(data instanceof LiteralStringBinding){
				idsAndValues.put(id,(String) data.getPayload());
			}
		}

		try {
			exDoc = createExecuteDocument(serviceURL, modelIdentifier, idsAndValues, "indicators");
		} catch (Exception e) {
			errors.add(e.getMessage());
			logger.error(e);
			throw new RuntimeException(e);
		}

		for (int i = 0; i < numberOfRealisations; i++) {
			handleResponse(i);
		}

		/*
		 *TODO: check output uncertainty type
		 */

		UncertaintyObservationCollection resultCollection = createResultCollection();

		UncertaintyObservationCollection uobscoll = new UncertaintyObservationCollection();

		Map<String, IData> result = new HashMap<String, IData>();

		result.put(outputIDUncertainProcessOutputs, new OMBinding(resultCollection));

		return result;
	}

	private UncertaintyObservationCollection createResultCollection(){

		UncertaintyObservationCollection uobscoll = new UncertaintyObservationCollection();

		/*
		 * its the same satial and temporal extent for all measurements
		 * so take the first one and save the attributes (phenomenonTime, feautureOfInterest,...)
		 */
		Measurement m = realisationCollection.get(0).getObservations().get(0);

		/*
		 * create hashmap with identifier of observed property and values of the different realisations
		 */
		Map<String, List<Double>> observedPropertyValuesMap = new HashMap<String, List<Double>>(realisationCollection.get(0).getObservations().size());
		Map<String, String> observedPropertyResultUOMMap = new HashMap<String, String>(realisationCollection.get(0).getObservations().size());

		for (MeasurementCollection obscoll : realisationCollection) {

			MeasurementCollection mcoll = (MeasurementCollection)obscoll;

			for (Measurement tmpm : mcoll.getObservations()) {

				if(!observedPropertyValuesMap.keySet().contains(tmpm.getObservedProperty().toString())){

					List<Double> valuesList = new ArrayList<Double>();

					valuesList.add(tmpm.getResult().getMeasureValue());

					observedPropertyValuesMap.put(tmpm.getObservedProperty().toString(), valuesList);
					observedPropertyResultUOMMap.put(tmpm.getObservedProperty().toString(), tmpm.getResult().getUnitOfMeasurement());
				}else{
					observedPropertyValuesMap.get(tmpm.getObservedProperty().toString()).add(tmpm.getResult().getMeasureValue());
				}

			}
		}

		/*
		 * iterate over observedproperties and create uncertaintyobservations
		 * with attributes of Measurement "m" and the respective values
		 */

		int counter = 0;

		for (String uriString : observedPropertyValuesMap.keySet()) {

			List<Double> values = observedPropertyValuesMap.get(uriString);

			URI uri = null;

			URI procedure = null;

			URI observedProperty = null;

			try {
				uri = new URI("http://uncertweb.org");
				procedure = new URI(
						"http://www.uncertweb.org/models/albatross");
				observedProperty = new URI(
						uriString);
			} catch (URISyntaxException e) {
					e.printStackTrace();
			}
			/*
			 * TODO: if the bug in the api gets fixed remove weight and id
			 */

			ContinuousRealisation cr = new ContinuousRealisation(values);


			UncertaintyResult uResult = new UncertaintyResult(cr, observedPropertyResultUOMMap.get(uriString));

//			Identifier identifier = new Identifier(uri, "Albatross" + counter);

			UncertaintyObservation uob = new UncertaintyObservation(m.getPhenomenonTime(), m.getResultTime(), procedure, observedProperty, m.getFeatureOfInterest(), uResult);

			uobscoll.addObservation(uob);

			counter++;
		}

		return uobscoll;
	}

	private void handleResponse(int runNumber) {
		try {
			 ExecuteResponseDocument response =
			 (ExecuteResponseDocument)WPSClientSession.getInstance().execute(serviceURL,
			 exDoc);
//			ExecuteResponseDocument response = ExecuteResponseDocument.Factory
//					.parse(new File(
//							"C:\\UncertWeb\\Albatross\\alb_resp_indicators.xml"));

			OutputDataType oType = response.getExecuteResponse()
					.getProcessOutputs().getOutputArray(0);
			Node wpsComplexData = oType.getData().getComplexData().getDomNode();
			// the complex data node
			Node unRealisation = wpsComplexData.getChildNodes().item(0);
			// the realisation node
			IObservationCollection iobs = null;
			try {
				iobs = new XBObservationParser()
						.parseObservationCollection(nodeToString(unRealisation));
			} catch (Exception e) {
				unRealisation = wpsComplexData.getChildNodes().item(1);
				iobs = new XBObservationParser()
						.parseObservationCollection(nodeToString(unRealisation));
			}
			/*
			 * gather all responses
			 */
			realisationCollection.add((MeasurementCollection)iobs);

			logger.debug(response);
		} catch (Exception e) {
			errors.add(e.getMessage());
			logger.error(e);
		}
	}

	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));

		return stringWriter.toString();
	}

	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
	public Class<? extends IData> getInputDataType(String id) {
		if (id.equals(inputIDIdentifierSimulatedProcess)) {
			return LiteralStringBinding.class;
		} else if (id.equals(inputIDServiceURL)) {
			return LiteralStringBinding.class;
		} else if (id.equals(inputIDOutputUncertaintyType)) {
			return LiteralStringBinding.class;
		} else if (id.equals(inputIDNumberOfRealisations)) {
			return LiteralIntBinding.class;
		}else if(getInputIDs().contains(id)) {
			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public Class<? extends IData> getOutputDataType(String id) {
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
		return createExecuteDocument(url, processID, inputs, null);
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
			Map<String, Object> inputs, String outputID) throws Exception {

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
		String outputIdentifier = "";
		OutputDescriptionType outType = null;
		if(outputID != null){

			for (OutputDescriptionType oType : processDescription.getProcessOutputs().getOutputArray()) {
				if(oType.getIdentifier().getStringValue().equals(outputID)){
					outType = oType;
					outputIdentifier = oType.getIdentifier().getStringValue();
				}
			}

		}else{

		outType = processDescription.getProcessOutputs().getOutputArray(0);

		outputIdentifier = outType.getIdentifier().getStringValue();
		}
		String outputSchema = outType.getComplexOutput().getDefault().getFormat().getSchema();

		String outputMimeType = outType.getComplexOutput().getDefault().getFormat().getMimeType();

		executeBuilder.setSchemaForOutput(
				outputSchema,
				outputIdentifier);
		executeBuilder.setMimeTypeForOutput(outputMimeType, outputIdentifier);

		logger.debug(executeBuilder.getExecute());

		return executeBuilder.getExecute();
	}


	private List<String> getInputIDs(){

		if(inputIDs == null){
			inputIDs = new ArrayList<String>();
			inputIDs.add(inputIDexport_file);
			inputIDs.add(inputIDexport_file_bin);
			inputIDs.add(inputIDgenpop_households);
			inputIDs.add(inputIDmunicipalities);
			inputIDs.add(inputIDpostcode_areas);
			inputIDs.add(inputIDrwdata_householdss);
			inputIDs.add(inputIDzones);
		}
		return inputIDs;
	}

}
