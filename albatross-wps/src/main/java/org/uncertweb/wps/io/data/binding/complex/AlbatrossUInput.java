package org.uncertweb.wps.io.data.binding.complex;

import java.util.List;
import java.util.Map;

import org.uncertml.statistic.StandardDeviation;

public class AlbatrossUInput {

	private List<String> albatrossIDs;
	private Map<String, String> parameters;
	private StandardDeviation standardDeviation;
	
	public AlbatrossUInput(List<String> albatrossIDs, Map<String, String> parameters, StandardDeviation standardDeviation){
		this.albatrossIDs = albatrossIDs;
		this.parameters = parameters;
		this.standardDeviation = standardDeviation;
	}

	public List<String> getAlbatrossIDs() {
		return albatrossIDs;
	}

	public void setAlbatrossIDs(List<String> albatrossIDs) {
		this.albatrossIDs = albatrossIDs;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public StandardDeviation getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(StandardDeviation standardDeviation) {
		this.standardDeviation = standardDeviation;
	}
	
}
