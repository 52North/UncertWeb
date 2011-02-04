package org.uncertweb.sta.wps.method.grouping;

import java.util.List;

import org.uncertweb.intamap.om.Observation;

public class ObservationMapping<T> {
	private List<Observation> observations;
	private T t;

	public ObservationMapping(T t, List<Observation> observations) {
		this.observations = observations;
		this.t = t;
	}

	public List<Observation> getObservations() {
		return observations;
	}

	public void setObservations(List<Observation> observations) {
		this.observations = observations;
	}

	public T getKey() {
		return t;
	}

	public void setKey(T t) {
		this.t = t;
	}
}