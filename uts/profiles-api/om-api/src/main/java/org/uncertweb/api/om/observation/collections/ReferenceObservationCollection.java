package org.uncertweb.api.om.observation.collections;

import java.util.List;

import org.uncertweb.api.om.observation.ReferenceObservation;

/**
 * represents collection with ReferenceObservations
 * 
 * @author staschc
 *
 */
public class ReferenceObservationCollection implements IObservationCollection {
	
	/**gml ID of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<ReferenceObservation> members;
	
	/**
	 * constructor
	 * 
	 * @param members
	 * 			members of collection
	 */
	public ReferenceObservationCollection(List<ReferenceObservation> members){
		this.members = members;
	}
	
	/**
	 * @return the members
	 */
	public List<ReferenceObservation> getMembers() {
		return members;
	}
	
	@Override
	public String getGmlId() {
		return gmlId;
	}
}
