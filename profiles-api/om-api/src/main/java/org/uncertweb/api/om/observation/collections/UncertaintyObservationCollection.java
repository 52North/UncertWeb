package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;

/**
 * represents collection with ReferenceObservations
 * 
 * @author staschc
 *
 */
public class UncertaintyObservationCollection implements IObservationCollection {
	
	/**gml ID of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<UncertaintyObservation> members;
	
	/**
	 * constructor
	 * 
	 * @param members
	 * 		members of collection
	 */
	public UncertaintyObservationCollection(List<UncertaintyObservation> members){
		this.members = members;
	}
	
	/**
	 * constructor; initializes collection and members array; members have to be added
	 * using the add(UncertaintyObs) method
	 * 
	 */
	public UncertaintyObservationCollection() {
		this.members = new ArrayList<UncertaintyObservation>();
	}

	/**
	 * @return the members
	 */
	public List<UncertaintyObservation> getMembers() {
		return members;
	}
	
	@Override
	public String getGmlId() {
		return gmlId;
	}

	@Override
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
		if (!(obs instanceof UncertaintyObservation)){
			throw new IllegalArgumentException("Only UncertaintyObservation could be added to UncertaintyObservationCollection!");
		}
		this.members.add((UncertaintyObservation)obs);
	}

	@Override
	public List<? extends AbstractObservation> getObservations() {
		return members;
	}
}
