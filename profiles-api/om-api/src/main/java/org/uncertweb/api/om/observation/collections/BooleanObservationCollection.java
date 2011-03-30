package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;


/**
 * collection for BooleanObservations
 * 
 * @author staschc
 *
 */
public class BooleanObservationCollection implements IObservationCollection{
	
	/**gml Id of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<BooleanObservation> members;
	
	/**
	 * contructor; initializes members list
	 * 
	 */
	public BooleanObservationCollection(){
		this.members = new ArrayList<BooleanObservation>();
	}
	
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



	@Override
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
		if (!(obs instanceof BooleanObservation)){
			throw new IllegalArgumentException("Only BooleanObservation could be added to BooleanObservationCollection!");
		}
		this.members.add((BooleanObservation)obs);
	}



	@Override
	public List<? extends AbstractObservation> getObservations() {
		return members;
	}

	

}
