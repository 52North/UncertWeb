package org.uncertweb.sta.wps;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.om.ObservationTime;
import org.uncertweb.intamap.uncertml.Uncertainty;


public class OriginAwareObservation extends Observation {

	private static final long serialVersionUID = -6557874377548562307L;

	public OriginAwareObservation(String id, double result,
			ISamplingFeature location, Uncertainty observationError,
			String observedProperty, String sensor, int srid,
			ObservationTime observationTime, String uom,Collection<Observation> obs, String sourceUrl) {
		super(id, result, location, observationError, observedProperty, sensor,
				srid, observationTime, uom);
		this.setSourceObservations(obs);
		this.setSourceUrl(sourceUrl);
	}

	private ObservationCollection sourceObservations;
	private String url;

	public void setSourceUrl(String url) {
		this.url = url;
	}

	public String getSourceUrl() {
		return this.url;
	}
	
	public Set<String> getSourceSensors() {
		HashSet<String> p = new HashSet<String>();
		for (Observation o : getSourceObservations()) {
			p.add(o.getSensorModel());
		}
		return p;
	}
	
	public ObservationCollection getSourceObservations() {
		return this.sourceObservations;
	}
	
	public void setSourceObservations(ObservationCollection sourceObservations) {
		this.sourceObservations = sourceObservations;
	}
	
	public void setSourceObservations(Collection<Observation> sourceObservations) {
		if (sourceObservations instanceof List) {
			this.sourceObservations = new ObservationCollection((List<Observation>)sourceObservations);
		} else {
			this.sourceObservations = new ObservationCollection(new LinkedList<Observation>(sourceObservations));
		}
	}

}
