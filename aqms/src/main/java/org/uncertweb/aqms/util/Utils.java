package org.uncertweb.aqms.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x20.impl.OMMeasurementCollectionDocumentImpl;
import net.opengis.om.x20.impl.OMUncertaintyObservationCollectionDocumentImpl;
import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ExecuteDocument.Execute;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.datahandler.generator.SimpleGMLGenerator;
import org.uncertml.IUncertainty;
import org.uncertml.io.XMLEncoder;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;

public class Utils {

	private static String resultsPath = "C:/Temp/AQMS";
	private static Logger logger = Logger.getLogger(Utils.class);

	public static String date4SOS(Date date){
		String sosDate = "";
		//"2009-03-08T11:00:00+01"
		SimpleDateFormat sosFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss+01");
		sosFormat.setTimeZone(TimeZone.getTimeZone("GMT+01"));

		return sosDate;
	}

	public static void writeObsColl(IObservationCollection obs, String filepath){
        // save result locally
		File file = new File(filepath);

		// encode
		try {
			new StaxObservationEncoder().encodeObservationCollection(obs,file);
		} catch (OMEncodingException e) {
			e.printStackTrace();
		}
	}

	public static IObservationCollection readObsColl(String filepath){
		File file = new File(filepath);
        IObservationCollection obs = null;
		try {
			InputStream in = new FileInputStream(file);
			XmlObject xml = XmlObject.Factory.parse(in);
			XBObservationParser omParser = new XBObservationParser();
			  if (xml instanceof OMUncertaintyObservationCollectionDocumentImpl){
				 obs = (IObservationCollection) omParser.parse(xml.xmlText());
			  }	else if(xml instanceof OMMeasurementCollectionDocumentImpl){
				  obs = (IObservationCollection) omParser.parse(xml.xmlText());
			  }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OMParsingException e) {
			e.printStackTrace();
		}

		return obs;
	}


	public static void writeCSV(HashMap<String, double[]> data, String filename){
		try {
			String filepath = resultsPath + "\\"+filename+".csv";
			 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filepath)));
			Iterator<String> keys = data.keySet().iterator();
			String date = keys.next();
			// write header
			out.write("Date");
			for(int i=0; i<data.get(date).length; i++){
				out.write(", "+i);
			}
			out.newLine();

			// write each observation as one line
			while(keys.hasNext()){
				date = keys.next();
				out.write(date);
				double[] r = data.get(date);
				for(int i=0; i<r.length; i++){
					out.write(", "+r[i]);
				}
				out.newLine();
			}
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public static ExecuteDocument createExecuteDocument(String url, String processID,
			Map<String, Object> inputs, String outputMimeType) throws Exception {
		// get process description to create execute document
		ProcessDescriptionType processDescription = WPSClientSession.getInstance().getProcessDescription(url, processID);
		ExecuteRequestBuilder executeBuilder = new ExecuteRequestBuilder(
				processDescription);

		// loop through inputs in the process description and add inputs from the input map
		for (InputDescriptionType input : processDescription.getDataInputs()
				.getInputArray()) {
			// get respective input from map
			String inputName = input.getIdentifier().getStringValue();
			Object inputValue = inputs.get(inputName);

			// if input is Literal data
			if (input.getLiteralData() != null) {
				if (inputValue instanceof String) {
					executeBuilder.addLiteralData(inputName,
							(String) inputValue);
				}
			}
			// if input is Complex data
			else if (input.getComplexData() != null) {
				// get supported mime types
				ComplexDataDescriptionType[] supportedTypes = input.getComplexData().getSupported().getFormatArray();
				List<String> inputMimeTypes = new ArrayList<String>();
				inputMimeTypes.add(input.getComplexData().getDefault().getFormat().getMimeType());
				for(ComplexDataDescriptionType type: supportedTypes){
					inputMimeTypes.add(type.getMimeType());
				}

				// Complexdata UncertML
				if (inputValue instanceof IUncertainty) {
					if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_UNCERTML_XML)){
						UncertMLBinding d = new UncertMLBinding((IUncertainty) inputValue);
						executeBuilder.
								addComplexData(inputName, d,
									UncertWebDataConstants.SCHEMA_UNCERTML,
									UncertWebDataConstants.ENCODING_UTF_8,
									UncertWebDataConstants.MIME_TYPE_UNCERTML_XML);
					}
					else{
						throw new IOException("Given Input mime type is not supported: "
								+ inputName + ", "+ UncertWebDataConstants.MIME_TYPE_UNCERTML_XML);
					}
				}
				// Complexdata Reference
				else if (inputValue instanceof String) {
					if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_OMX_XML)){
						executeBuilder
								.addComplexDataReference(
										inputName,
										(String) inputValue,
										UncertWebDataConstants.SCHEMA_OM_V2,
										UncertWebDataConstants.ENCODING_UTF_8,
										UncertWebDataConstants.MIME_TYPE_OMX_XML);
					}
					else{
						throw new IOException("Given Input mime type is not supported: "
								+ inputName + ", "+ UncertWebDataConstants.MIME_TYPE_OMX_XML);
					}
				}
				// Complexdata OM
				else if (inputValue instanceof IObservationCollection) {
					if(inputMimeTypes.contains(UncertWebDataConstants.MIME_TYPE_OMX_XML)){
						OMBinding d = new OMBinding((IObservationCollection) inputValue);
						executeBuilder
								.addComplexData(
										inputName, d,
										UncertWebDataConstants.SCHEMA_OM_V2,
										UncertWebDataConstants.ENCODING_UTF_8,
										UncertWebDataConstants.MIME_TYPE_OMX_XML);
					}
					else{
						throw new IOException("Given Input mime type is not supported: "
								+ inputName + ", "+ UncertWebDataConstants.MIME_TYPE_OMX_XML);
					}
				}
				// if an input is missing
				if (inputValue == null && input.getMinOccurs().intValue() > 0) {
					throw new IOException("Property not set, but mandatory: "
							+ inputName);
				}
			}
		}

		// set output type
		OutputDescriptionType outType = processDescription.getProcessOutputs().getOutputArray(0);
		String outputIdentifier = outType.getIdentifier().getStringValue();

		// if not the default type is used, find the correct mime type and schema
		String defaultOutputMimeType = outType.getComplexOutput().getDefault().getFormat().getMimeType();
		String outputSchema = outType.getComplexOutput().getDefault().getFormat().getSchema();
		boolean mimeTypeExists = false;
		if(!defaultOutputMimeType.equals(outputMimeType)){
			ComplexDataDescriptionType[] supportedTypes = outType.getComplexOutput().getSupported().getFormatArray();
			for(ComplexDataDescriptionType type : supportedTypes){
				if(type.getMimeType().equals(outputMimeType)){
					outputSchema = type.getSchema();
					mimeTypeExists = true;
				}
			}
		}
		if(mimeTypeExists){
			logger.debug(outputSchema);
			logger.debug(outputMimeType);
			logger.debug(outputIdentifier);

			executeBuilder.setSchemaForOutput(
					outputSchema,
					outputIdentifier);
			executeBuilder.setMimeTypeForOutput(outputMimeType, outputIdentifier);

			logger.debug(executeBuilder.getExecute());
		}else{
			throw new IOException("Given Output mime type is not supported: "
					+ outputMimeType);
		}

	return executeBuilder.getExecute();
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
}
