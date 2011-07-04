package org.uncertweb.wps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

public class SimpleRegressionModelProcess extends
		AbstractSelfDescribingAlgorithm {

	@Override
	public Class getInputDataType(String arg0) {
		return LiteralDoubleBinding.class;
	}

	@Override
	public Class getOutputDataType(String arg0) {
		return LiteralDoubleBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		IData doubleData = getIData("input", inputData);
		
		double d = ((LiteralDoubleBinding)doubleData).getPayload();
		
		double dOut = 2.1 + d * 1.3;
		
		LiteralDoubleBinding outputBinding = new LiteralDoubleBinding(dOut);
		
		Map<String, IData> result = new HashMap<String, IData>(1);
		
		result.put("output", outputBinding);
		
		return result;
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
	
	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifiers = new ArrayList<String>(1);
		identifiers.add("input");
		return identifiers;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifiers = new ArrayList<String>(1);
		identifiers.add("output");
		return identifiers;
	}

}
