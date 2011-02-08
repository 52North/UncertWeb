package org.uncertweb.api.om.observation.collections;

import java.util.List;

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
	 * @return the members
	 */
	public List<UncertaintyObservation> getMembers() {
		return members;
	}
	
	@Override
	public String getGmlId() {
		return gmlId;
	}
}
