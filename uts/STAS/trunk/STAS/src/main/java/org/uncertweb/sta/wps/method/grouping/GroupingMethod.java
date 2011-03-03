package org.uncertweb.sta.wps.method.grouping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.sta.wps.IProcessInput;

/**
 * @author Christian Autermann
 */
public abstract class GroupingMethod<T> implements Iterable<ObservationMapping<T>>{
	private List<Observation> observations;
	private Map<IProcessInput<?>, Object> inputs;
	protected static final Logger log = LoggerFactory.getLogger(GroupingMethod.class);
	
	public void setInputs(List<Observation> observations, Map<IProcessInput<?>, Object> inputs) {
		this.observations = observations;
		this.inputs = inputs;
	}

	protected List<Observation> getObservations() {
		return this.observations;
	}

	protected Map<IProcessInput<?>, Object> getInputs() {
		return this.inputs;
	}
	
	public abstract Set<IProcessInput<?>> getAdditionalInputDeclarations();
	
}