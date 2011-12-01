package org.uncertweb.wps;

import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

/**
 * class that implements a process for taking samples from a spatial distribution
 * 
 * @author staschc, Ben Graeler
 *
 */
public class SpatialDistribution2Samples extends AbstractSelfDescribingAlgorithm{
	
	private final String INPUT_ID_DATA = "inputData";
	private final String INPUT_ID_VARIOGRAM = "variogram";
	private final String OUTPUT_ID_SAMPLES = "samples";

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getInputDataType(String id) {
		if (id.equalsIgnoreCase(INPUT_ID_DATA)){
			return NetCDFBinding.class;
		}
		else if (id.equals(INPUT_ID_VARIOGRAM)){
			return UncertMLBinding.class;
		}
		else return null;
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

	@Override
	public List<String> getInputIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
}
