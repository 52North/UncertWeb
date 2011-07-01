package org.uncertweb.u_wps;

import java.io.File;
import java.io.StringWriter;
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

import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ResponseFormType;
import net.opengis.wps.x100.ExecuteDocument.Execute;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.data.binding.complex.StaticInputDataBinding;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.data.binding.complex.UncertainInputDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertml.IUncertainty;
import org.uncertweb.StaticInputType;
import org.uncertweb.UncertainInputType;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.XMLEncoder;
import org.uncertml.io.XMLParser;
import org.uncertml.sample.Realisation;
import org.w3c.dom.Node;


/**
 * process implements algorithm for executing monteCarlo simulation
 * 
 * @author staschc
 *
 */
public class MonteCarloSimulationAlgorithm extends AbstractAlgorithm{

	private static Logger LOGGER = Logger.getLogger(MonteCarloSimulationAlgorithm.class);
	private String inputIDIdentifierSimulatedProcess = "IdentifierSimulatedProcess";
	private String inputIDUncertainProcessInputs = "UncertainProcessInputs";
	private String inputIDUncertainProcessInputs2 = "UncertainProcessInputs2";
	private String inputIDStaticProcessInputs = "StaticProcessInputs";
	private String inputIDProcessExecuteRequest = "ProcessExecuteRequest";
	private String inputIDServiceURL = "ServiceURL";
	private String inputIDOutputUncertaintyType = "OutputUncertaintyType";
	private String inputIDNumberOfRealisations = "NumberOfRealisations";
	private String outputIDUncertainProcessOutputs = "UncertainProcessOutputs";
	
	
	
	public List<String> getErrors() {
		return null;
	}

	public Class<?> getInputDataType(String id) {
		if(id.equals(inputIDIdentifierSimulatedProcess)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDUncertainProcessInputs)){
			return UncertainInputDataBinding.class;
		}else if(id.equals(inputIDStaticProcessInputs)){
			return StaticInputDataBinding.class;
		}else if(id.equals(inputIDUncertainProcessInputs2)){
			return PlainStringBinding.class;
		}else if(id.equals(inputIDProcessExecuteRequest)){
//			return GenericFileDataBinding.class;
			return PlainStringBinding.class;
		}else if(id.equals(inputIDServiceURL)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDOutputUncertaintyType)){
			return LiteralStringBinding.class;
		}else if(id.equals(inputIDNumberOfRealisations)){
			return LiteralIntBinding.class;
		}
		return null;
	}

	public Class<?> getOutputDataType(String id) {
		if(id.equals(outputIDUncertainProcessOutputs)){
			return UncertWebDataBinding.class;
		}
		return null;
	}

	public Map<String, IData> run(Map<String, List<IData>> inputData) {	

				
		IData outputUncertaintyTypeData = getIData(inputIDServiceURL, inputData);
		
		String outputUncertaintyType = ((LiteralStringBinding)outputUncertaintyTypeData).getPayload();

		
		
		IData identifierSimulatedProcessData = getIData(inputIDIdentifierSimulatedProcess, inputData);
		
		String identifierSimulatedProcess = ((LiteralStringBinding)identifierSimulatedProcessData).getPayload();
			
		
		
		IData numberOfRealisationsData = getIData(inputIDNumberOfRealisations, inputData);
		
		int numberOfRealisations = ((LiteralIntBinding)numberOfRealisationsData).getPayload();
		
		try {
			IData uncertainProcessInputsData = getIData(
					inputIDUncertainProcessInputs2, inputData);

			String uncertainProcessInputs = (String)((PlainStringBinding) uncertainProcessInputsData)
					.getPayload();

			UncertainInputType type = UncertainInputType.Factory.parse(uncertainProcessInputs);
			
			XMLParser parser = new XMLParser();
			
			String uType = type.getData().getComplexData().getDomNode().getFirstChild().getTextContent();
			
			XMLParser p = new XMLParser();
			
			IUncertainty uncertaintyType =  p.parse(uType);
			
			String meanString = "";
			String stdDevString = "";

			if (uncertaintyType instanceof NormalDistribution) {

				NormalDistribution gD = (NormalDistribution) uncertaintyType;

				meanString = String.valueOf(gD.getMean().get(0));
				stdDevString = String.valueOf(gD.getVariance().get(0));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		IData uncertainProcessInputsData = getIData(inputIDUncertainProcessInputs, inputData);
		
		UncertainInputType uncertainProcessInputs = ((UncertainInputDataBinding)uncertainProcessInputsData).getPayload();
		
		if(uncertainProcessInputs.getData().getComplexData() == null){
			return null;
		}
		Node n = uncertainProcessInputs.getData().getComplexData().getDomNode().getChildNodes().item(1);

		String uType = null;
		try {
			uType = nodeToString(n);
		} catch (TransformerFactoryConfigurationError e4) {
			e4.printStackTrace();
		} catch (TransformerException e4) {
			e4.printStackTrace();
		}
		
		XMLParser p = new XMLParser();
		
		IUncertainty uncertaintyType = null;
		try {
			uncertaintyType = p.parse(uType);
		} catch (UncertaintyParserException e3) {
			e3.printStackTrace();
		}
		
		String meanString = "";
		String stdDevString = "";

		if (uncertaintyType instanceof NormalDistribution) {

			NormalDistribution gD = (NormalDistribution) uncertaintyType;

			meanString = String.valueOf(gD.getMean().get(0));
			stdDevString = String.valueOf(gD.getVariance().get(0));

		}
		
//		IUncertainty uncertaintyType = uncertainProcessInputs.getUncertaintyType();

//		if(uncertaintyType instanceof GaussianDistribution){
//			
//			GaussianDistribution gD = (GaussianDistribution)uncertaintyType;
//			
//			meanString = String.valueOf(gD.getMean().get(0));
//			stdDevString = String.valueOf(gD.getVariance().get(0));
//			
//		}		
		
		IData staticProcessInputsData = getIData(inputIDStaticProcessInputs, inputData);
		
		StaticInputType staticProcessInputs = ((StaticInputDataBinding)staticProcessInputsData).getPayload();
		
//		if(staticProcessInputs.getData().getComplexData() == null){
//			return null;
//		}
		
		System.out.println(staticProcessInputs.getIdentifier().getStringValue());
		
		String staticInput = staticProcessInputs.getData().getLiteralData().getDomNode().getFirstChild().getNodeValue();

		
		
		/*
		 * transform distribution to samples using UTS
		 * 
		 */
		double[] samples = new double[1];
		
		WPSClientSession session = WPSClientSession.getInstance(); 
		
		try {
			session.connect("http://localhost:8080/uts/WebProcessingService");
			
			ExecuteDocument execDoc1 = ExecuteDocument.Factory.parse(new File("C:\\UncertWeb\\UTS\\Distribution2Samples.xml"));
			
			InputType[] types = execDoc1.getExecute().getDataInputs().getInputArray();
			
			for (InputType inputType : types) {
				
				String id = inputType.getIdentifier().getStringValue();
				
				if(id.equals("Mean")){
					inputType.getData().getLiteralData().setStringValue(meanString);
				}else if(id.equals("StandardDeviation")){
					inputType.getData().getLiteralData().setStringValue(stdDevString);
				}else if(id.equals("NumberOfRealisations")){
					inputType.getData().getLiteralData().setStringValue("" + numberOfRealisations);
				}
				
			}
			
			ExecuteResponseDocument response1 = (ExecuteResponseDocument) session.execute("http://localhost:8080/uts/WebProcessingService", execDoc1);
			
			OutputDataType oType = response1.getExecuteResponse().getProcessOutputs().getOutputArray(0);
			
			String sampleString = oType.getData().getLiteralData().getStringValue();

			String[] stringSamples = sampleString.split(";");

			samples = new double[stringSamples.length];
			
			for (int i = 0; i < stringSamples.length; i++) {
				samples[i] = Double.parseDouble(stringSamples[i]);
			}			
			
		} catch (Exception e2) {
			e2.printStackTrace();
		} 
		
		IData serviceURLData = getIData(inputIDServiceURL, inputData);
		
		String serviceURL = ((LiteralStringBinding)serviceURLData).getPayload();

		
		try {
			session.connect(serviceURL);
		} catch (WPSClientException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Error while connecting to specified WPS URL: " + serviceURL);			
		}
		
		IData processExecuteRequestData = getIData(inputIDProcessExecuteRequest, inputData);
		
//		GenericFileData processExecuteRequest = ((GenericFileDataBinding)processExecuteRequestData).getPayload();
		String processExecuteRequest = (String) ((PlainStringBinding)processExecuteRequestData).getPayload();
		
		ExecuteDocument execDoc = null;
		
		try {
			execDoc = ExecuteDocument.Factory.parse(processExecuteRequest);
			
			//execute Monte Carlo
			InputType inType = execDoc.getExecute().getDataInputs().getInputArray(0);
		
			ArrayList<Double> realisations = new ArrayList<Double>(numberOfRealisations);
			
			for (int i = 0; i < numberOfRealisations; i++) {				
				
				inType.getData().getLiteralData().setStringValue("" + samples[i]);
				
				ExecuteResponseDocument response1 = (ExecuteResponseDocument) session.execute(serviceURL, execDoc);
				
				OutputDataType oType = response1.getExecuteResponse().getProcessOutputs().getOutputArray(0);
				
				double output = Double.parseDouble(oType.getData().getLiteralData().getStringValue());
				
				realisations.add(output);
			}
			
			LOGGER.debug(realisations.size());		
			
			/*
			 * TODO: transform realisations to distribution again using UTS
			 * 
			 */
			
			Realisation rplus = new Realisation(realisations);
			
			/*
			 * Create execute document 
			 */
			
			ExecuteDocument utsExecDoc2 = ExecuteDocument.Factory.newInstance();
			
			Execute exc = utsExecDoc2.addNewExecute();
			
			exc.addNewIdentifier().setStringValue("org.uncertweb.wps.Realisations2Distribution");
			
			InputType inType1 = exc.addNewDataInputs().addNewInput();
			
			inType1.addNewIdentifier().setStringValue("realisations");
			
			ComplexDataType cData1 = inType1.addNewData().addNewComplexData();
						
			cData1.set(XmlObject.Factory.parse(new  XMLEncoder().encode(rplus)));
			
			cData1.setSchema("http://giv-uw.uni-muenster.de:8080/uts/schemas/uncertml2.0.0/Realisation.xsd");
			
			cData1.setEncoding("UTF-8");
			
			cData1.setMimeType("text/xml");			
			
			ResponseFormType rForm = exc.addNewResponseForm();
			
			DocumentOutputDefinitionType output1 = rForm.addNewResponseDocument().addNewOutput();
			
			output1.addNewIdentifier().setStringValue("output_distribution");
			
			output1.setMimeType("text/xml");
			
			output1.setSchema("http://giv-uw.uni-muenster.de:8080/uts/schemas/uncertml2.0.0/GaussianDistribution.xsd");
						
			ExecuteResponseDocument response1 = (ExecuteResponseDocument)session.execute("http://localhost:8080/uts/WebProcessingService", utsExecDoc2);
			
			OutputDataType outType1 = response1.getExecuteResponse().getProcessOutputs().getOutputArray(0);
			
			try {
				
				String s = nodeToString(outType1.getData().getComplexData().getDomNode().getFirstChild());
				
				IUncertainty uncertainty = new XMLParser().parse(s);
				
				UncertWebData uwd = new UncertWebData(uncertainty);
				
				Map<String, IData> result = new HashMap<String, IData>(1);
				
				result.put("UncertainProcessOutputs", new UncertWebDataBinding(uwd));
				
				return result;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error while creating ExecuteDocument.");
		} 
		
		return null;
	}
	
	private IData getIData(String id, Map<String, List<IData>> inputData){
		
		List<IData> dataList = inputData.get(id);
		if(dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = dataList.get(0);
		
		if(!firstInputData.getClass().equals(getInputDataType(id))){
			throw new RuntimeException("Error while allocating input parameters. Got: " +  firstInputData.getClass() + " expected: " + getInputDataType(id));
		}	
		
		return firstInputData;
	}
	
	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		return stringWriter.toString();
	}

}
