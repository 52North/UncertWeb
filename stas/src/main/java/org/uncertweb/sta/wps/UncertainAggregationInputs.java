package org.uncertweb.sta.wps;

import java.util.List;

/**
 * class that carries the uncertain aggregation inputs
 * 
 * @author staschc
 *
 */
public class UncertainAggregationInputs {
	
	/**types of uncertainty for outputs*/
	private List<String> outputUncertaintyTypes;

	/**number of realisations*/
	private int numberOfRealisations;
	
	/**
	 * constructor 
	 * 
	 * @param outputUncertaintyTypes
	 * 		types of uncertainty for outputs
	 * @param numberOfRealisations
	 * 		number of realisations
	 */
	public UncertainAggregationInputs(List<String> outputUncertaintyTypes,
			int numberOfRealisations) {
		this.outputUncertaintyTypes = outputUncertaintyTypes;
		this.numberOfRealisations = numberOfRealisations;
	}
	

	public List<String> getOutputUncertaintyTypes() {
		return outputUncertaintyTypes;
	}

	public void setOutputUncertaintyTypes(List<String> outputUncertaintyTypes) {
		this.outputUncertaintyTypes = outputUncertaintyTypes;
	}

	public int getNumberOfRealisations() {
		return numberOfRealisations;
	}

	public void setNumberOfRealisations(int numberOfRealisations) {
		this.numberOfRealisations = numberOfRealisations;
	}
}
