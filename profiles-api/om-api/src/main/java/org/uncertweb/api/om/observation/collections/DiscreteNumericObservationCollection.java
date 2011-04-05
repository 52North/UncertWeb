package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;

/**
 * Collection for DiscreteNumericObservation
 * 
 * @author staschc
 *
 */
public class DiscreteNumericObservationCollection implements
		IObservationCollection {
	
	/** gml Id of collection */
	private String gmlId;

	/** members of collection */
	private List<DiscreteNumericObservation> members;
	
	/**type name of observation collection*/
	public static final String NAME = "OM_DiscreteNumericObservationCollection";
	
	/**
	 * contructor; initializes members list
	 * 
	 */
	public DiscreteNumericObservationCollection(){
		this.members = new ArrayList<DiscreteNumericObservation>();
	}

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
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
		if (!(obs instanceof DiscreteNumericObservation)){
			throw new IllegalArgumentException("Only DiscreteNumericObservation could be added to DiscreteNumericObservationCollection!");
		}	
		this.members.add((DiscreteNumericObservation)obs);
	}

	@Override
	public List<? extends AbstractObservation> getObservations() {
		return members;
	}

	@Override
	public String getTypeName() {
		return NAME;
	}
}
