package org.uncertweb.sta.wps.method.grouping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.ProcessInput;

/**
 * @author Christian Autermann
 */
public abstract class GroupingMethod<T> implements Iterable<ObservationMapping<T>>{
	private List<Observation> observations;
	private String description = Utils.getMethodDescription(this);
	private Map<ProcessInput, List<IData>> additionalInputs;
	
	public void setInputs(List<Observation> observations, Map<ProcessInput, List<IData>> additionalInputs) {
		this.observations = observations;
		this.additionalInputs = additionalInputs;
	}

	protected List<Observation> getObservations() {
		return observations;
	}

	protected Object getAdditionalInput(ProcessInput input) {
		return (additionalInputs.get(input) == null) ? null : additionalInputs.get(input).get(0).getPayload();
	}
	
	protected Map<ProcessInput, List<IData>> getAdditionalInputs() {
		return additionalInputs;
	}
	
	protected List<IData> getAdditionalInputList(ProcessInput input) {
		return (additionalInputs == null) ? null : additionalInputs.get(input);
	}
	
	public String getDescription() {
		return description;
	}
	
	public abstract Set<ProcessInput> getAdditionalInputDeclarations();
	
	
}