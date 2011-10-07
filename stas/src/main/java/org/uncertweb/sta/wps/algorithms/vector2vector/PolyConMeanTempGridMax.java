package org.uncertweb.sta.wps.algorithms.vector2vector;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.uncertweb.sta.wps.algorithms.AbstractAggregationProcess;

public class PolyConMeanTempGridMax extends AbstractAggregationProcess{
	
	private static Logger LOGGER = Logger.getLogger(PolyConMeanTempGridMax.class);

	// //////////////////////////////////////////////////////
	// constants for input/output identifiers
	private final static String WFS_URL = "distribution";
	private final static String INPUT_IDENTIFIER_NUMB_REAL = "numbReal";
	private final static String OUTPUT_IDENTIFIER_SAMPLES = "samples";
	private final static String LAT_VAR_NAME = "lat";
	private final static String LON_VAR_NAME = "lon";
	private final static String UNITS_ATTR_NAME = "units";
	private final static String MV_ATTR_NAME = "missing_value";
	private final static String REF_ATTR_NAME = "ref";
	
	
	
	
	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getInputDataType(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getOutputDataType(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		// TODO Auto-generated method stub
		return null;
	}

}
