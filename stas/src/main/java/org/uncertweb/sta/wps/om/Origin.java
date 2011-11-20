package org.uncertweb.sta.wps.om;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class Origin {

	private String sourceUrl;
	private Collection<? extends AbstractObservation> observations;

	public Origin(String sourceUrl,
			Collection<? extends AbstractObservation> observations) {
		this.sourceUrl = sourceUrl;
		this.observations = observations;
	}

	public String getSourceUrl() {
		return this.sourceUrl;
	}

	public Set<URI> getSourceSensors() {
		Set<URI> sensors = UwCollectionUtils.set();
		for (AbstractObservation ao : getSourceObservations())
			sensors.add(ao.getProcedure());
		return sensors;
	}

	public Collection<? extends AbstractObservation> getSourceObservations() {
		return this.observations;
	}
}
