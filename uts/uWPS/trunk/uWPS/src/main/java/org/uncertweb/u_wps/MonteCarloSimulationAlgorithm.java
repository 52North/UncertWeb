package org.uncertweb.u_wps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;

import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.GaussianDistribution;


/**
 * process implements algorithm for executing monteCarlo simulation
 * 
 * @author staschc
 *
 */
public class MonteCarloSimulationAlgorithm extends AbstractAlgorithm{

	private String inputIDIdentifierSimulatedProcess = "IdentifierSimulatedProcess";
	private String inputIDUncertainProcessInputs = "UncertainProcessInputs";
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
			return UncertWebDataBinding.class;
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
		
		
		
		IData uncertainProcessInputsData = getIData(inputIDUncertainProcessInputs, inputData);
		
		UncertWebData uncertainProcessInputs = ((UncertWebDataBinding)uncertainProcessInputsData).getPayload();
		
		if(uncertainProcessInputs.getUncertaintyType() == null){
			return null;
		}
		
		IUncertainty uncertaintyType = uncertainProcessInputs.getUncertaintyType();
		
		String meanString = "";
		String stdDevString = "";		
		
		if(uncertaintyType instanceof GaussianDistribution){
			
			GaussianDistribution gD = (GaussianDistribution)uncertaintyType;
			
			meanString = String.valueOf(gD.getMean().get(0));
			stdDevString = String.valueOf(gD.getVariance().get(0));
			
		}		
		
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
		
			ArrayList<Double> realisations = new ArrayList<Double>();
			
			for (int i = 0; i < numberOfRealisations - 1; i++) {				
				
				inType.getData().getLiteralData().setStringValue("" + samples[i]);
				
				ExecuteResponseDocument response1 = (ExecuteResponseDocument) session.execute(serviceURL, execDoc);
				
				OutputDataType oType = response1.getExecuteResponse().getProcessOutputs().getOutputArray(0);
				
				double output = Double.parseDouble(oType.getData().getLiteralData().getStringValue());
				
				realisations.add(output);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error while creating ExecuteDocument.");
		} 
		
		/*
		 * TODO: transform realisations to distribution again using UTS
		 * 
		 */
		
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

}
