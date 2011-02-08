package org.uncertweb.api.om.observation.collections;

import java.util.List;

import org.uncertweb.api.om.observation.DiscreteNumericObservation;

public class DiscreteNumericObservationCollection implements
		IObservationCollection {
	
	/** gml Id of collection */
	private String gmlId;

	/** members of collection */
	private List<DiscreteNumericObservation> members;

	/**
	 * constructor
	 * 
	 * @param members
	 *            members of collection
	 */
	public DiscreteNumericObservationCollection(
			List<DiscreteNumericObservation> members) {
		this.members = members;
	}

	/**
	 * @return the members
	 */
	public List<DiscreteNumericObservation> getMembers() {
		return members;
	}

	@Override
	public String getGmlId() {
		return gmlId;
	}
}
