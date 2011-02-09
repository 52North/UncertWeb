package org.uncertweb.api.om.observation.collections;

import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.ReferenceObservation;

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

	@Override
	public void addObservation(AbstractObservation obs) throws Exception {
		if (!(obs instanceof DiscreteNumericObservation)){
			throw new Exception("Only DiscreteNumericObservation could be added to DiscreteNumericObservationCollection!");
		}	
		this.members.add((DiscreteNumericObservation)obs);
	}

	@Override
	public List<? extends AbstractObservation> getObservations() {
		return members;
	}
}
