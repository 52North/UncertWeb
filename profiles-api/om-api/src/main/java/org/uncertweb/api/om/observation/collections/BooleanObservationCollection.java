package org.uncertweb.api.om.observation.collections;

import java.util.List;

import org.uncertweb.api.om.observation.BooleanObservation;

public class BooleanObservationCollection implements IObservationCollection{
	
	/**gml Id of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<BooleanObservation> members;
	
	/**
	 * constructor
	 * 
	 * @param members
	 * 			members of collection
	 */
	public BooleanObservationCollection(List<BooleanObservation> members){
		this.members = members;
	}
	
	
	
	@Override
	public String getGmlId() {
		return gmlId;
	}



	/**
	 * @return the members
	 */
	public List<BooleanObservation> getMembers() {
		return members;
	}

	

}
