package org.uncertweb.wps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.uncertml.IUncertainty;

public class UncertMLTestProcess extends AbstractAlgorithm {

	@Override
	public Class<?> getInputDataType(String arg0) {		
		return UncertWebIODataBinding.class;
	}

	@Override
	public Class<?> getOutputDataType(String arg0) {		
		return UncertWebIODataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> arg0) {

		List<IData> inputDataList = arg0.get("input");
		
		IData firstData = inputDataList.get(0);
		
		if(!(firstData instanceof UncertMLBinding)){
			return null;			
		}
		
		IUncertainty uncertaintyType = ((UncertMLBinding)firstData).getPayload();
		
		HashMap<String, IData> result = new HashMap<String, IData>();
		
		result.put("output", (UncertMLBinding)firstData);
		
		return result;
	}

//	@Override
//	public List<String> getInputIdentifiers() {
//		List<String> identifiers = new ArrayList<String>();
//		identifiers.add("input");
//		return identifiers;
//	}
//
//	@Override
//	public List<String> getOutputIdentifiers() {
//		List<String> identifiers = new ArrayList<String>();
//		identifiers.add("output");
//		return identifiers;
//	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

}
