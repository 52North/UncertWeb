package org.uncertweb.sta.wps.algorithms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebIOData;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertweb.sta.wps.AggregationInputs;


/**
 * abstract super class for all aggregation processes; provides helper method for extracting the common parameters
 * of every aggregation process
 * 
 * @author staschc
 *
 */
public abstract class AbstractAggregationProcess extends AbstractAlgorithm{
	
	
	////////////////////////////////////
	//Identifiers of common aggregation inputs
	protected final static String INPUT_IDENTIFIER_VARIABLE = "Variable";
	protected final static String INPUT_IDENTIFIER_INPUT_DATA = "InputData";
	protected final static String INPUT_IDENTIFIER_SPATIAL_FIRST = "SpatialFirst";
	protected final static String INPUT_IDENTIFIER_TARGET_SERVER = "TargetServer";
	protected final static String INPUT_IDENTIFIER_TARGET_SERVER_TYPE = "TargetServerType";
		
	
	////////////////////////////////////
	//Identifiers of common aggregation outputs
	protected final static String OUTPUT_IDENTIFIER_AGGREGATED_DATA = "AggregatedData";
	
	/**
	 * returns the standard parameters of every aggregation process
	 * 
	 * @param inputData
	 * 			map containing the inputs as served by the 52N WPS framework
	 * @return Returns {@link AggregationInputs} containing standard inputs of every aggregation process
	 */
	protected AggregationInputs getAggregationInputs4Inputs(Map<String, List<IData>> inputData){
		
		AggregationInputs result = null;
		
		//extract Variable
		List<IData> variableList = inputData.get(INPUT_IDENTIFIER_VARIABLE);
		ArrayList<String> variables = new ArrayList<String>(variableList.size());
		Iterator<IData> variableIter = variableList.iterator();
		while (variableIter.hasNext()){
			String variable = ((LiteralStringBinding)variableIter.next()).getPayload();
			variables.add(variable);
		}
		
		//extract InputData
		UncertWebIOData uwDataInput = null;
		Object dataInput = ((UncertWebIODataBinding)inputData.get(INPUT_IDENTIFIER_INPUT_DATA).get(0)).getPayload();
		if (dataInput instanceof UncertWebIOData){
			uwDataInput = (UncertWebIOData) dataInput;
		}
		//create Aggregation Inputs object
		result = new AggregationInputs(variables,uwDataInput);
		
		//extract SpatialFirst
		List<IData> spatialFirstList = inputData.get(INPUT_IDENTIFIER_SPATIAL_FIRST);
		if (spatialFirstList!=null&&spatialFirstList.size()==1){
			LiteralBooleanBinding spfBb = (LiteralBooleanBinding)spatialFirstList.get(0);
			result.setSpatialFirst(spfBb.getPayload());
		}
		
		//extract TargetServer
		List<IData> targetServerList = inputData.get(INPUT_IDENTIFIER_TARGET_SERVER);
		if (targetServerList!=null&&targetServerList.size()==1){
			LiteralStringBinding tsSb = (LiteralStringBinding)targetServerList.get(0);
			try {
				result.setTargetServer(new URL(tsSb.getPayload()));
			} catch (MalformedURLException e) {
				throw new RuntimeException("Error while retrieving target server URL in aggregation process:"+e.getLocalizedMessage());
			}
		}
		
		//extract TargetServerType
		List<IData> targetServerType = inputData.get(INPUT_IDENTIFIER_TARGET_SERVER_TYPE);
		if (targetServerType!=null&&targetServerType.size()==1){
			LiteralStringBinding spfBb = (LiteralStringBinding)targetServerType.get(0);
			result.setTargetServerType(spfBb.getPayload());
		}
		
		return result;
	}

	/**
	 * returns type of data binding class for passed input identifier
	 * 
	 * @param identifier
	 * 			identifier of input
	 * @return data binding class 
	 */
	protected Class<?> getCommonInputType(String identifier){
		if (identifier.equals(INPUT_IDENTIFIER_INPUT_DATA)){
			return UncertWebIODataBinding.class;
		}
		else if (identifier.equals(INPUT_IDENTIFIER_VARIABLE)){
			return LiteralStringBinding.class;
		}
		else if (identifier.equals(INPUT_IDENTIFIER_SPATIAL_FIRST)){
			return LiteralStringBinding.class;
		}
		else if (identifier.equals(INPUT_IDENTIFIER_SPATIAL_FIRST)){
			return LiteralBooleanBinding.class;
		}
		else if (identifier.equals(INPUT_IDENTIFIER_TARGET_SERVER)){
			return LiteralStringBinding.class;
		}
		else if (identifier.equals(INPUT_IDENTIFIER_TARGET_SERVER_TYPE)){
			return LiteralStringBinding.class;
		}
		else return null;
	}
	
	/**
	 * returns type of data binding class for passed output identifier
	 * 
	 * @param identifier
	 * 			identifier of output
	 * @return data binding class 
	 */
	protected Class<?> getCommonOutputType(String identifier){
		if (identifier.equals(OUTPUT_IDENTIFIER_AGGREGATED_DATA )){
			return UncertWebIODataBinding.class;
		}
		else return null;
	}

}
