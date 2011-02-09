package org.uncertweb.api.om.observation.collections;

import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;

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

	@Override
	public void addObservation(AbstractObservation obs) throws Exception {
		if (!(obs instanceof Measurement)){
			throw new Exception("Only Measurement could be added to MeasurementCollection!");
		}
		this.members.add((Measurement)obs);
	}

	@Override
	public List<Measurement> getObservations() {
		return members;
	}
	
	
}
