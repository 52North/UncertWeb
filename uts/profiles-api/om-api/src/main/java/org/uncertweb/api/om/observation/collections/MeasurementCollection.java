package org.uncertweb.api.om.observation.collections;

import java.util.List;

import org.uncertweb.api.om.observation.Measurement;

/**
 * collection of O&M measurements
 * 
 * @author staschc
 *
 */
public class MeasurementCollection implements IObservationCollection{
	
	/**gml Id of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<Measurement> members;
	
	/**
	 * constructor
	 * 
	 * @param members
	 * 			members of collection
	 */
	public MeasurementCollection(List<Measurement> members){
		this.members = members;
	}
	
	/**
	 * @return the members
	 */
	public List<Measurement> getMembers() {
		return members;
	}
	
	@Override
	public String getGmlId() {
		return gmlId;
	}
}
