package org.uncertweb.u_wps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.UncertWebIOData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.multivariate.IMultivariateDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateStudentTDistribution;
import org.uncertml.sample.Realisation;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.io.StaxObservationEncoder;
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
import org.uncertweb.ups.austal.AustalObservationInput;
import org.uncertweb.ups.austal.DayDistribution;
import org.uncertweb.ups.austal.DayEmissions;
import org.uncertweb.ups.austal.HourDistribution;
import org.uncertweb.ups.austal.MeteoObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Envelope;

public class MonteCarloAustal2000 extends AbstractAlgorithm {

	private static Logger logger = Logger.getLogger(MonteCarloAustal2000.class);

	// Path to resources
	//private String localPath = "C:\\UncertWeb\\workspace\\uWPS4";
	private String localPath = "D:\\JavaProjects\\ups";
	private String resPath = localPath
			+ "\\src\\main\\resources\\austalResources";
	private String utsAddress = "http://localhost:8080/uts/WebProcessingService";
//	private String utsAddress = "http://giv-uw2.uni-muenster.de:8080/uts/WebProcessingService";
	
	// Attributes
	// Store PM10 distributions and emissions:
	private LinkedList<DayDistribution> daydist = new LinkedList<DayDistribution>(); // day
																						// sum
																						// distributions
																						// for
																						// each
																						// source
																						// /
																						// each
																						// day
	private LinkedList<HourDistribution> hourdist = new LinkedList<HourDistribution>(); // hour
																						// fraction
																						// distributions
																						// for
																						// each
																						// source
																						// /
																						// each
																						// day
	private LinkedList<DayEmissions> emissions = new LinkedList<DayEmissions>(); // emission
																					// values
																					// for
																					// each
																					// source
																					// /
																					// each
																					// day
	private ArrayList<String> emissionsFiles;
	private ArrayList<String> meteoFiles;
	
	// Store meteorology
	private LinkedList<MeteoObject> meteoObservations = new LinkedList<MeteoObject>();

	// Booleans that tell whether these inputs exist:
	private boolean emissionsExist = false;
	private boolean meteoExist = false;
	private boolean staticInputsExist = false;
	// Inputs for austal
	private List<IData> staticInputsList = null;
	private int numberOfRealisations = -999; // how often to call the Austal
												// WPS?
	private String serviceURL = ""; // URL of austal wps
	private String identifierAustal = "";
	// Input & output IDs
	private String inputIDNumberOfRealisations = "NumberOfRealisations";
	private String inputIDServiceURL = "ServiceURL"; // austal service URL
	private String inputIDIdentifierSimulatedProcess = "IdentifierSimulatedProcess"; // austal
																						// process
																						// identifier
	// private String inputIDsUncertainProcessInputs = "UncertainProcessInputs";
	// // uncertain stuff, treated via UTS
	private String inputIDStaticProcessInputs = "StaticProcessInputs"; // certain
																		// stuff,
																		// only
																		// durchgereicht
	private String inputIDProcessExecuteRequest = "ProcessExecuteRequest";
	private String inputIDOutputUncertaintyType = "OutputUncertaintyType";
	private String outputIDUncertainProcessOutputs = "UncertainProcessOutputs";

	@Override
	public List<String> getErrors() {
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if (id.equals(inputIDIdentifierSimulatedProcess)) {
			return LiteralStringBinding.class;
		} else if (id.startsWith("u_")) {
			return UncertWebIODataBinding.class;
		} else if (id.equals(inputIDStaticProcessInputs)) {
			return LiteralStringBinding.class;
		} else if (id.equals(inputIDProcessExecuteRequest)) {
			// return GenericFileDataBinding.class;
			return PlainStringBinding.class;
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
	public Class<UncertWebIODataBinding> getOutputDataType(String id) {
		if (id.equals(outputIDUncertainProcessOutputs)) {
			return UncertWebIODataBinding.class;
		}
		return null;
	}

	/**
	 * RUN Method
	 */
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputMap) {
		logger.debug("Start of process"); // for debugging

		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
		for(Property property : propertyArray){
			// check the name and active state
			if(property.getName().equalsIgnoreCase("localPath") && property.getActive()){
				localPath = property.getStringValue();
				resPath = localPath
				+ "\\src\\main\\resources\\austalResources";
			}else if(property.getName().equalsIgnoreCase("FullUTSAddress") && property.getActive()){
				utsAddress = property.getStringValue();
			}
		}
		
		emissionsFiles = new ArrayList<String>();
		meteoFiles = new ArrayList<String>();
		/* Retrieve some inputs */

		// Retrieve Austal service URL from input. Must exist!
		List<IData> list = inputMap.get(inputIDServiceURL);
		IData serviceURL_IData = list.get(0);
		serviceURL = (String) serviceURL_IData.getPayload();

		// Retrieve austal service URL from input. Must exist!
		list = inputMap.get(inputIDIdentifierSimulatedProcess);
		IData austalIdentifier_IData = list.get(0);
		identifierAustal = (String) austalIdentifier_IData.getPayload();

		// Retrieve number of Realisations from input. Must exist!
		list = inputMap.get(inputIDNumberOfRealisations);
		IData numberOfReal = list.get(0);
		numberOfRealisations = (Integer) numberOfReal.getPayload();

		/* Uncertain Process Inputs */

		// needed to recognize different kinds of input:
		List<List<IData>> observationInputList = new ArrayList<List<IData>>(6);
		observationInputList.add(inputMap.get("u_input1"));
		observationInputList.add(inputMap.get("u_input2"));
		observationInputList.add(inputMap.get("u_input3"));
		observationInputList.add(inputMap.get("u_input4"));
		observationInputList.add(inputMap.get("u_input5"));
		observationInputList.add(inputMap.get("u_input6"));

		AustalObservationInput obsInputs = null;

		try {

			obsInputs = getObservations4Inputs(observationInputList);
		} catch (Exception e) {
			logger
					.error("Error while retrieving observation collections input: "
							+ e.getMessage());
		}

		/* Treat PM10 data */

		if (obsInputs.hasEmissionObs()) {
			this.emissionsExist=true;

			/*
			 * for each observation, a new object is created - for the Daily
			 * Emission observations, a DayDistribution object, for the Hourly
			 * Fractions of Daily Emissions, a HourDistributions object is
			 * created. Differentiation by unit of measurement, once that is
			 * implemented.
			 */
			for (UncertaintyObservation uncObs : obsInputs.getEmissionObs()
					.getMembers()) {

				Identifier source = uncObs.getFeatureOfInterest()
						.getIdentifier();

				/*
				 * So far, unit of measurement in O&M is not supported by out
				 * profile yet, i.e. the whole O&M won't be read if there is a
				 * unit in it. Christoph was/is working on this. So right now, I
				 * differentiate by distribution type and then write the unit of
				 * measurement into the observation. This bit below can simply
				 * be deleted (TODO) without replacement once the units can be
				 * read from the O&Ms.
				 */
				// <delete>
				IUncertainty iUn = uncObs.getResult().getUncertaintyValue();
				if (iUn instanceof LogNormalDistribution) { // Daily Emissions
					uncObs.getResult().setUnitOfMeasurement("g[PM10]/s");
				} else if (iUn instanceof MultivariateNormalDistribution) { // Hourly
																			// Fractions
					uncObs.getResult().setUnitOfMeasurement("%");
				}
				// </delete>

				// Extract and store distribution
				/*
				 * In DayDistribution objects, the whole observation is stored,
				 * so that all necessary information can be retrieved from there
				 * later on (when writing O&M again). In HourDistribution, only
				 * the distribution is stored, otherwise it would be
				 * redundant/overhead.
				 */
				String uom = uncObs.getResult().getUnitOfMeasurement();
				if (uom.equals("g[PM10]/s")) { // Day distribution
					daydist.add(new DayDistribution(source, uncObs
							.getPhenomenonTime(), uncObs));
				} else if (uom.equals("%")) { // Hour distribution
					try {
						IMultivariateDistribution iMulti = (IMultivariateDistribution) uncObs
								.getResult().getUncertaintyValue();
						hourdist.add(new HourDistribution(source, uncObs
								.getPhenomenonTime(), iMulti));
					} catch (Exception e) {
						logger.error("Error while adding hourdistribution: "
										+ e.getMessage());
					}
				}
			}

			/* UTS sampling PM10 */

			// Sample daily distributions
			// and store the realisations in the DayDistribution objects
			for (DayDistribution dd : daydist) {
				IUncertainty distribution = dd.getDistribution();
				double[] daySamples = callUTSforDAY(distribution); // length of
																	// array:
																	// [numberOfRealisations]
				dd.putSamples(daySamples);
			}

			// Sample hourly fraction distributions
			// and store the realisations in the HourDistribution objects
			for (HourDistribution hd : hourdist) {
				IMultivariateDistribution distribution = hd.getDistribution();
				double[][] hourSamples = callUTSforHOURS(distribution, 24); // length
																			// of
																			// array:
																			// [numberOfRealisations][24]
				hd.putSamples(hourSamples);
			}

			// Compute hourly emissions out of sampled realisations
			computeEmissions();

			/* Make O&M Observations and ObservationCollections */

			// One ObservationCollection per Austal Run!
			LinkedList<IObservationCollection> pm10_OM_Collections = new LinkedList<IObservationCollection>();
			for (int austalrun = 0; austalrun < numberOfRealisations; austalrun++) {
				pm10_OM_Collections.add(makePM10_OMMeasurements(austalrun));
			}

			/*
			 * Write each O&M observation collection to one XML file. This is
			 * not necessary for running the process, it is only checking if
			 * they work allright.
			 */

		
			for (int i = 0; i < numberOfRealisations; i++) {

				// write this collection to file:
				IObservationCollection ioc = pm10_OM_Collections.get(i);

				// make file
				// String filepath =
				// "D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\output_om\\Streets";
				String filepath = resPath + "\\output_om";
				filepath = filepath.concat("\\streets"+(i + 1) + ".xml");
				File file = new File(filepath);
				try {
					emissionsFiles.add(file.toURL().toString());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}

				// encode, store (for using in austal request later)
				try {
					new StaxObservationEncoder().encodeObservationCollection(ioc,file);
				} catch (OMEncodingException e) {
					e.printStackTrace();
				}

			}
		} // end of treating PM10 source inputs

		/* Treat meteo data / UTS Sampling */

		if (obsInputs.hasMeteoObs()) {

			this.meteoExist=true;
			
			//obs.getMembers().get(0).getObservedProperty()
			// Store each observation as MeteoObject
			for (UncertaintyObservation uncObs : obsInputs.getMeteoObs()
					.getMembers()) {
				MeteoObject meteo = new MeteoObject(uncObs);
				// UTS sampling, store results
				double[] samples = callUTSforMeteo(meteo.getDistribution());
				meteo.putSamples(samples);
				meteoObservations.add(meteo);
			}

			/* Make O&M Observations and ObservationCollections */

			// One ObservationCollection per austal run!
			LinkedList<IObservationCollection> meteo_OM_Collections = new LinkedList<IObservationCollection>();
			for (int austalrun = 0; austalrun < numberOfRealisations; austalrun++) {
				meteo_OM_Collections.add(makeMeteo_OMMeasurements(austalrun));

			}

			/*
			 * Write each O&M observation collection to one XML file. This is
			 * not necessary for running the process, it is only checking if
			 * they work allright.
			 */

			for (int i = 0; i < numberOfRealisations; i++) {

				// write this collection
				IObservationCollection ioc = meteo_OM_Collections.get(i);

				// make file
				// String filepath =
				// "D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\output_om\\Meteo";
				String filepath = resPath + "\\output_om";
				filepath = filepath.concat("\\meteo"+(i + 1) + ".xml");
				File om_file = new File(filepath);
				try {
					meteoFiles.add(om_file.toURL().toString());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}

				// encode, store (for using in austal request later)
				try {
					new StaxObservationEncoder().encodeObservationCollection(ioc, om_file);
				} catch (OMEncodingException e) {
					e.printStackTrace();
				}
				
			} // end of treating meteo inputs
		} // end of treating uncertain process inputs
		
		/* Treat static inputs */
		staticInputsList = inputMap.get(inputIDStaticProcessInputs);
		if ((staticInputsList != null) && (staticInputsList.size() > 0)) {
			staticInputsExist = true;
		}
		
		ArrayList<IObservationCollection> observationList  = new ArrayList<IObservationCollection>(3);
		
		/* Make Austal requests and run Austal n times */
		for (int i = 0; i < numberOfRealisations; i++) {
			IObservationCollection tmpioc = makeRequestAndRunAustal(i);
			observationList.add(tmpioc);
		}

		/*
		 * Here, treat the austal results and make e.g. distribution out of
		 * them. Then make the OutputMap to return!
		 */
		HashMap<String, HashMap<DateTime, ArrayList<Double>>> mightyMap = new HashMap<String, HashMap<DateTime, ArrayList<Double>>>();
		
		HashMap<String, SpatialSamplingFeature> idSpSFMap = new HashMap<String, SpatialSamplingFeature>();
		
		for (IObservationCollection iObservationCollection : observationList) {
			
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

					Realisation r = new Realisation(values, -1.0d, "id");

					UncertaintyResult uResult = new UncertaintyResult(r);
					UncertaintyObservation uObs = new UncertaintyObservation(
							newT, newT, procedure, observedProperty, tmpSF,
							uResult);
					mcoll.addObservation(uObs);
				}

			}
			
			// write uncertainty observation as XML and JSON
			String filepath = resPath + "\\output_om";
			String xmlFilepath = filepath.concat("\\realisations.xml");
			File xmlFile = new File(xmlFilepath);
			String jsonFilepath = filepath.concat("\\realisations.json");
			File jsonFile = new File(jsonFilepath);
			// encode, store (for using in austal request later)
			try {
				new StaxObservationEncoder().encodeObservationCollection(mcoll,xmlFile);
				new JSONObservationEncoder().encodeObservationCollection(mcoll, jsonFile);
			} catch (OMEncodingException e) {
				e.printStackTrace();
			}

			
			// make UPS result
			HashMap<String, IData> resultMap = new HashMap<String, IData>();
			
			OMData omd = new OMData(mcoll, UncertWebDataConstants.MIME_TYPE_OMX);
			
			UncertWebIOData resultData = new UncertWebIOData(omd);
			
			resultMap.put(outputIDUncertainProcessOutputs, new UncertWebIODataBinding(resultData));
			

			logger.debug("End of process"); // for debugging
			return resultMap;
			
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(
					"An Exception occurred while creating the result collection.");
		}
	}

	/* UTS methods */

	/**
	 * callUTSforDAY()
	 * 
	 * 
	 * 
	 * This methods calls the UTS (LogNormalDistribution) and returns samples.
	 * 
	 * Functions well (17 August 2011). XML handling could be done more
	 * elegantly.
	 * 
	 * @param shape
	 *            The shape parameter of the log normal distribution, as a
	 *            string.
	 * @param lgoScale
	 *            The logScale parameter of the log normal distribution, as a
	 *            string.
	 */
	private double[] callUTSforDAY(IUncertainty distribution) {

		// to be returned
		double[] samples = new double[numberOfRealisations];

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		ExecuteDocument execDocUTSday = null;

		try {
			session.connect(utsAddress);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}

		// Get distribution specific parameters and add them to request
		// The following code is specific for LogNormalDistribution:
		if (distribution instanceof LogNormalDistribution) {

			LogNormalDistribution distrib = (LogNormalDistribution) distribution;

			// parameters
			String logScale = distrib.getLogScale().get(0).toString();
			String shape = distrib.getShape().get(0).toString();

			// Make execute request
			try {
				// execDocUTSday = ExecuteDocument.Factory.parse(new
				// File("D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\request_templates\\LogNormalRequest.xml"));
				execDocUTSday = ExecuteDocument.Factory.parse(new File(resPath
						+ "\\request_templates\\LogNormalRequest.xml"));
			} catch (XmlException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Inserting values into request
			InputType[] types = execDocUTSday.getExecute().getDataInputs()
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

		} else if (distribution instanceof NormalDistribution) {
			// the same for this distribution type (or others),
			// if ever this is needed.
		}

		// The following is the same for all distribution types:

		// Run UTS and get output (=Realisation object)
		ExecuteResponseDocument responseUTSday = null;
		try {
			responseUTSday = (ExecuteResponseDocument) session.execute(
					utsAddress,
					execDocUTSday);
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
		}
		return samples;

	} // end of method

	/**
	 * callUTSforHOURS()
	 * 
	 * 
	 * This methods calls the UTS (MultivariateNormalDistribution) and returns
	 * samples.
	 * 
	 * Functions well (17 August 2011). XML handling could be done more
	 * elegantly.
	 * 
	 * @param dimensions
	 *            The dimensions of the multivariate distribution. Should be 24.
	 * @param meanVector
	 *            The mean vector of the multivariate gaussian distribution, as
	 *            a atring, readily formatted to be given to the UTS as UncertML
	 *            input (all values separated by space)
	 * @param covMatrix
	 *            The covariance matrix of the the multivariate gaussian
	 *            distribution, as a atring, readily formatted to be given to
	 *            the UTS as UncertML input (all values separated by space)
	 * 
	 * @return Returns a 2d-double array, which contains n arrays (n being the
	 *         number of realisations, i.e. number of times that austal should
	 *         be run) with m values (m being the number of variables of the
	 *         distribution).
	 */
	private double[][] callUTSforHOURS(IMultivariateDistribution distribution,
			int dimensions) {

		// to be returned
		double[][] samples = new double[numberOfRealisations][24];

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		ExecuteDocument execDocUTShours = null;

		try {
			session.connect(utsAddress);
		} catch (WPSClientException e) {
			e.printStackTrace();
		}

		// Get distribution specific parameters and add them to request
		// The following code is specific for MultivariateNormalDistribution:
		if (distribution instanceof MultivariateNormalDistribution) {

			// mean vector as String
			MultivariateNormalDistribution distrib = (MultivariateNormalDistribution) distribution;
			String meanVector = distrib.getMean().toString(); // "[0.0046, 0.0029, 0.0023, 0.0028, ... 0.0105]"
			meanVector = meanVector.substring(1, meanVector.length() - 1); // to
																			// remove
																			// "[",
																			// "]"
			meanVector = meanVector.replace(",", ""); // to remove ","
			// cov matrix as String
			String covMatrix = distrib.getCovarianceMatrix().getValues()
					.toString(); // This should look like [x, y, z]
			covMatrix = covMatrix.substring(1, covMatrix.length() - 1); // Now
																		// like
																		// this:
																		// x, y,
																		// z
			covMatrix = covMatrix.replace(",", ""); // to remove ","

			// Make execute request
			try {
				// execDocUTShours = ExecuteDocument.Factory.parse(new
				// File("D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\request_templates\\MultivariateRequest.xml"));
				execDocUTShours = ExecuteDocument.Factory
						.parse(new File(resPath
								+ "\\request_templates\\MultivariateRequest.xml"));
			} catch (XmlException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			InputType[] types = execDocUTShours.getExecute().getDataInputs()
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
					Integer dim = dimensions; // should always be 24
					attrib.setNodeValue(dim.toString());
				}
			}

		} else if (distribution instanceof MultivariateStudentTDistribution) {
			// the same for this distribution type (or others),
			// if ever this is needed.
		}

		// the following applies to any distribution:
		// run UTS and get output (=Realisation object)
		ExecuteResponseDocument responseUTShours = null;
		try {
			responseUTShours = (ExecuteResponseDocument) session.execute(
					utsAddress,
					execDocUTShours);
		} catch (WPSClientException e) {// Auto-generated catch block
			e.printStackTrace();
		}

		// Get realisations out of response xml
		// TODO This could be done more elegantly, maybe?
		OutputDataType oType = responseUTShours.getExecuteResponse()
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

	/* Other methods */

	/**
	 * callUTSforMeteo()
	 * 
	 * 
	 * 
	 * This methods calls the UTS (NormalDistribution or potentially others) and
	 * returns samples.
	 * 
	 * XML handling could be done more elegantly.
	 * 
	 * @param distribution
	 *            This is any distribution. So far, only NormalDistribution is
	 *            implemented.
	 */
	private double[] callUTSforMeteo(IUncertainty distribution) {

		// to be returned
		double[] samples = new double[numberOfRealisations];

		// connect to UTS
		WPSClientSession session = WPSClientSession.getInstance();
		try {
			session.connect(utsAddress);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
		}

		// Make execute request
		ExecuteDocument execDocUTSmeteo = null;

		// Get distribution specific parameters and add them to request
		// The following code is specific for NormalDistribution:
		if (distribution instanceof NormalDistribution) {

			// extract parameters
			String meanString = ((NormalDistribution) distribution).getMean()
					.get(0).toString();
			String varString = ((NormalDistribution) distribution)
					.getVariance().get(0).toString();

			// Make execute request
			try {
				// execDocUTSmeteo = ExecuteDocument.Factory.parse(new
				// File("D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\request_templates\\NormalRequest.xml"));
				execDocUTSmeteo = ExecuteDocument.Factory.parse(new File(
						resPath + "\\request_templates\\NormalRequest.xml"));
			} catch (XmlException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Inserting value into request
			InputType[] types = execDocUTSmeteo.getExecute().getDataInputs()
					.getInputArray();
			for (InputType inputType : types) {
				String id = inputType.getIdentifier().getStringValue();
				if (id.equals("distribution")) {

					// Set new mean
					Node wpsComplexData = inputType.getData().getComplexData()
							.getDomNode(); // Element wps:ComplexData
					Node unNormalDistribution = wpsComplexData.getChildNodes()
							.item(1); // Element un:LogNormalDistribution
					Node unMean = unNormalDistribution.getChildNodes().item(1); // Element
																				// un:LogScale
					Node unMeanValueNode = unMean.getChildNodes().item(0); // Contents
																			// of
																			// it
					unMeanValueNode.setNodeValue(meanString);

					// Set new variance
					Node unVariance = unNormalDistribution.getChildNodes()
							.item(3); // Element un:LogScale
					Node varianceValueNode = unVariance.getChildNodes().item(0); // Contents
																					// of
																					// it
					varianceValueNode.setNodeValue(varString);

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

		} else if (distribution instanceof LogNormalDistribution) {
			// the same for this distribution type (or others),
			// if ever this is needed.
		}

		// Run UTS and get output (=Realisation object)
		ExecuteResponseDocument responseUTSmeteo = null;
		try {
			responseUTSmeteo = (ExecuteResponseDocument) session.execute(
					utsAddress,
					execDocUTSmeteo);
		} catch (WPSClientException e) {// Auto-generated catch block
			e.printStackTrace();
		}

		// Get output realisations
		// this could be done more elegantly, maybe?
		OutputDataType oType = responseUTSmeteo.getExecuteResponse()
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
		// put into double array:
		String[] temp = realisationsValue.split(" ");
		for (int i = 0; i < temp.length; i++) {
			// check for negative values
			double v = Double.parseDouble(temp[i]);
			if(v<0.1){
				v=0.1;
			}
			samples[i] = v;
		}

		return samples;
	}

	/* Other methods */

	/**
	 * computeEmissions()
	 * 
	 * This method computes the hourly emissions out of the UTS results (samples
	 * of daily emission sums and samples of hourly fractions).
	 * 
	 * This method works.
	 */
	private void computeEmissions() {

		// Combine daily emission (sum) and hour fractions to get hourly
		// emissions

		for (DayDistribution dd : daydist) {

			for (HourDistribution hd : hourdist) {

				// Get day distribution and hour distribution
				// of same source and same day!
				String ddSource = dd.getSourceID().toIdentifierString();
				String hdSource = hd.getSourceID().toIdentifierString();
				long ddTime = dd.getTime().getDateTime().getMillis();
				long hdTime = hd.getTime().getDateTime().getMillis();

				if ((ddSource.equals(hdSource)) && (ddTime == hdTime)) {

					// Combine them
					double[] dayEmissions = dd.getSamples();
					double[][] hourFractions = hd.getSamples();
					double[][] hourlyEmissions = new double[numberOfRealisations][24];

					for (int i = 0; i < numberOfRealisations; i++) {
						for (int j = 0; j < 24; j++) {
							hourlyEmissions[i][j] = dayEmissions[i]
									* hourFractions[i][j];
						}
					}
					DayEmissions dayEmis = new DayEmissions(hourlyEmissions, dd
							.getTime(), dd.getSourceID(), dd.getObservation());
					emissions.add(dayEmis);
				}
			}
		}
	} // end of method computeEmissions()

	/**
	 * makePM10_OMMeasurements()
	 * 
	 * 
	 * This method makes O&M Measurement objects out of all the computed
	 * emissions. For every source and every hour, one Measurement is created.
	 * The measurements are stored in a MeasurementCollection, which is
	 * returned.
	 * 
	 * @param austalrun
	 *            The number of the austal run. Austal is run n times, and for
	 *            each time, a different set of emissions is used. So for each
	 *            austal run, a different MeasurementCollection is made out of
	 *            different emissions.
	 */
	private IObservationCollection makePM10_OMMeasurements(int austalrun) {

		// Collection for PM10 emissions, to be returned
		IObservationCollection collection = new MeasurementCollection();

		// For making unique identifier
		String id = "Emission";
		int laufendeNummer = 0;

		// Loop through each Emission object, make 24 Measurements out of it
		for (DayEmissions dayEmis : emissions) {

			// correct sample for this austalrun
			double[][] allEmissionsOfThisDay = dayEmis.getEmissions();
			double[] currentEmissions = allEmissionsOfThisDay[austalrun];

			// Needed for making measurement
			// String unitOfMeasurement = dayEmis.getUnitOfMeasurement(); //
			// TODO Once unit works, enable this!
			String unitOfMeasurement = "platzhalter unit of measurement";
			DateTime dayDatetime = dayEmis.getTime().getDateTime();

			// Make 24 results with one hour emission in each
			for (int hour = 0; hour < 24; hour++) {

				UncertaintyObservation uo = dayEmis.getUncertaintyObservation();

				// make result
				MeasureResult result = new MeasureResult(
						currentEmissions[hour], unitOfMeasurement);

				// make identifier for this observation
				laufendeNummer++;
				String ident = id.concat(((Integer) laufendeNummer).toString());
				URI uri = null;
				try {
					uri = new URI("platzhalter_codespace");
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				Identifier identifier = new Identifier(uri, ident);

				// make time of that hour TODO
				DateTime dayDateTime = new DateTime(dayDatetime);
				DateTime hourDate = dayDateTime.plusHours(hour);
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

				// add to collection
				collection.addObservation(me);
			}
		}
		return collection;
	}

	/**
	 * makeMeteo_OMMeasurements()
	 * 
	 * @param j
	 * @return
	 */
	private IObservationCollection makeMeteo_OMMeasurements(int j) {

		// Collection for Meteo data, to be returned
		IObservationCollection collection = new MeasurementCollection();

		// For making unique identifier
		String id = "Meteo";
		int laufendeNummer = 0;

		// Loop through each Meteo object, make one Measurement out of it
		for (MeteoObject meteo : meteoObservations) {

			// correct sample for this austalrun
			double currentValue = meteo.getSample(j);

			// make identifier for this observation
			laufendeNummer++;
			String ident = id.concat(((Integer) laufendeNummer).toString());
			URI uri = null;
			try {
				uri = new URI("platzhalter_codespace");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Identifier identifier = new Identifier(uri, ident);

			// make result
			// String unitOfMeasurement = meteo.getUnitOfMeasurement();
			String unitOfMeasurement = "platzhalter unit of measurement"; // TODO
																			// Once
																			// unit
																			// works,
																			// enable
																			// this!
			MeasureResult result = new MeasureResult(currentValue,
					unitOfMeasurement);

			// make measurement
			UncertaintyObservation uo = meteo.getUncertaintyObservation();
			Measurement me = new Measurement(identifier, uo.getBoundedBy(), uo
					.getPhenomenonTime(), uo.getResultTime(),
					uo.getValidTime(), uo.getProcedure(), uo
							.getObservedProperty(), uo.getFeatureOfInterest(),
					uo.getResultQuality(), result);

			// add to collection
			collection.addObservation(me);

		}
		return collection;
	}

	/* Call Austal WPS */

	/**
	 * makeRequestAndRunAustal()
	 * 
	 * 
	 * This method creates an execute request for the Austal WPS and runs it. At
	 * the end, the outputs should be stored and/or processed.
	 * 
	 * The method is not finished! It uses locally stored request files, so the
	 * paths have to be adapted!
	 */
	private IObservationCollection makeRequestAndRunAustal(int runNumber) {

		// connect to austal WPS
		WPSClientSession session = WPSClientSession.getInstance();

		try {
			session.connect(serviceURL);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
		}

		// Make execute request
		ExecuteDocument execDoc = null;
		try {
			// execDoc = ExecuteDocument.Factory.parse(new
			// File("D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\request_templates\\AustalRequest.xml"));
			execDoc = ExecuteDocument.Factory.parse(new File(resPath
					+ "\\request_templates\\AustalRequest.xml"));
		} catch (XmlException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		// Set input data
		DataInputsType wpsDataInputs = execDoc.getExecute().getDataInputs();
		
		
		
		// Make a long string including all inputs
		String allInput = "";

		if (emissionsExist) { 
			
			InputType wpsInput = wpsDataInputs.addNewInput();
			wpsInput.addNewIdentifier().setStringValue("street-emissions");
			InputReferenceType wpsReference = wpsInput.addNewReference();
			wpsReference.setHref(emissionsFiles.get(runNumber));
			wpsReference.setSchema("http://v-mars.uni-muenster.de/uncertweb/schema/Profiles/OM/UncertWeb_OM.xsd");
			wpsReference.setEncoding("UTF-8");
			wpsReference.setMimeType("application/x-om-u");
		}
		if (meteoExist) { // if meteorology exist
			InputType wpsInput = wpsDataInputs.addNewInput();
			wpsInput.addNewIdentifier().setStringValue("meteorology");
			InputReferenceType wpsReference = wpsInput.addNewReference();
			wpsReference.setHref(meteoFiles.get(runNumber));
			wpsReference.setSchema("http://v-mars.uni-muenster.de/uncertweb/schema/Profiles/OM/UncertWeb_OM.xsd");
			wpsReference.setEncoding("UTF-8");
			wpsReference.setMimeType("application/x-om-u");
		}
		if (staticInputsExist){ 
			
			String url = (String) staticInputsList.get(0).getPayload();
			
			InputType wpsInput = wpsDataInputs.addNewInput();
			wpsInput.addNewIdentifier().setStringValue("receptor-points");
			InputReferenceType wpsReference = wpsInput.addNewReference();
			
			//TODO the v-mars reference does not work yet (not parsed by the Austal WPS)
			// <wps:Reference xlink:href="http://v-mars.uni-muenster.de/uncertweb/austalResources/inputs/staticInput.xml" schema="http://schemas.opengis.net/gml/2.1.2/feature.xsd" mimeType="text/xml"/>
			//wpsReference.setHref("http://v-mars.uni-muenster.de/uncertweb/austalResources/inputs/staticInput.xml");
			//wpsReference.setSchema("http://giv-uw.uni-muenster.de:8080/uts/schemas/StaticInputType.xsd");
	       
			//<wps:Reference schema="http://schemas.opengis.net/gml/2.1.2/feature.xsd" encoding="UTF-8" mimeType="text/xml" xlink:href="http://giv-wps.uni-muenster.de:8080/geoserver/wfs?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=cite2:schulweg"/>	    	
			wpsReference.setHref(url);
//			wpsReference.setHref("file:/C:/UncertWeb/workspace/uWPS4/src/main/resources/austalResources/inputs/staticInput.xml");
			wpsReference.setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
			wpsReference.setMimeType("text/xml");
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
		 
		//		
		// // Handle austal wps output
		// /* I have no idea whether this works, because I could not test it
		// * yet, because austal WPS does not function yet!
		// *
		// */
		// OutputDataType oType =
		// response1.getExecuteResponse().getProcessOutputs().getOutputArray(0);
		// // all output elements
		// Node wpsComplexData = oType.getData().getComplexData().getDomNode();
		// // the complex data node
		// Node unRealisation = wpsComplexData.getChildNodes().item(0); // the
		// realisation node
		// Node unValues = unRealisation.getChildNodes().item(3); // the values
		// node
		// Node realisationsValueNode = unValues.getChildNodes().item(0); // the
		// node with the contents of the value node
		// String realisationsValue = realisationsValueNode.getNodeValue(); //
		// the value of the contents
		//	
		// // Write output to file (one file per run)
		// /* TODO Instead, if might make more sense to store it in a
		// * list/array, so that UPS can process all austal results
		// * when all austal runs are finished.
		// *
		// */
		// String filepath =
		// "D:\\Eclipse_Workspace\\ups_wrapper\\src\\main\\resources\\AustalOutput";
		// filepath = filepath.concat((runNumber+1) + ".txt");
		// File austalfile = new File(filepath);
		// try {
		// BufferedWriter bw_austaloutput = new BufferedWriter(new
		// FileWriter(austalfile));
		// bw_austaloutput.write(realisationsValue);
		// bw_austaloutput.flush();
		// bw_austaloutput.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	private AustalObservationInput getObservations4Inputs(
			List<List<IData>> obsInputs) throws Exception {

		// needed to recognize different kinds of input:
		URI obsPropPM10 = null;
		URI obsPropWindDir = null;
		URI obsPropWindSpeed = null;
		try {
			obsPropPM10 = new URI(
					"http://www.uncertweb.org/phenomenon/pm10emissions");
			obsPropWindDir = new URI(
					"http://www.uncertweb.org/phenomenon/winddirection");
			obsPropWindSpeed = new URI(
					"http://www.uncertweb.org/phenomenon/windspeed");
		} catch (URISyntaxException e) { 
				logger.error(e);
		}

		// Retrieve source and meteo data from input map
		UncertaintyObservationCollection allPM10Observations = null;
		UncertaintyObservationCollection allMeteoObservations = null;

		for (List<IData> obsInput : obsInputs) {
			UncertaintyObservationCollection obsCol = getUObsCol4inputs(obsInput);

			// differentiate the input type:
			boolean emis = obsCol.getMembers().get(0).getObservedProperty()
					.equals(obsPropPM10);
			boolean winddir = obsCol.getMembers().get(0).getObservedProperty()
					.equals(obsPropWindDir);
			boolean windspeed = obsCol.getMembers().get(0)
					.getObservedProperty().equals(obsPropWindSpeed);

			// put all PM10 observations into one list
			if (emis) {
				if (allPM10Observations == null) {
					allPM10Observations = obsCol;
				} else {
					allPM10Observations.addObservationCollection(obsCol);
				}

				// put all wind observations into one list
			} else if (winddir || windspeed) {
				if (allMeteoObservations == null) {
					allMeteoObservations = obsCol;
				} else {
					allMeteoObservations.addObservationCollection(obsCol);
				}
			}
		}

		return new AustalObservationInput(allMeteoObservations,
				allPM10Observations);

	}

	private UncertaintyObservationCollection getUObsCol4inputs(
			List<IData> inputs) throws Exception {
		UncertWebIODataBinding uwDB = (UncertWebIODataBinding) inputs.get(0);
		Object payload = ((UncertWebIOData) uwDB.getPayload()).getData();
		if (!(payload instanceof OMData)) {
			throw new Exception(
					"Inputtype is not an UncertaintyObservationCollection!!");
		}
		OMData omData = (OMData) payload;
		return (UncertaintyObservationCollection) omData
				.getObservationCollection();
	}
	
	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		return stringWriter.toString();
	}
}
