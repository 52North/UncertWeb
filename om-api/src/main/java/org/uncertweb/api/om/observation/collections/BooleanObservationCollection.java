package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.LinkedList;
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
	
	/**type name of observation collection*/
	public static final String NAME = "OM_BooleanObservationCollection";
	
	/**
	 * contructor; initializes members list
	 * 
	 */
	public BooleanObservationCollection(){
		this.members = new LinkedList<BooleanObservation>();
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

	@Override
	public String getTypeName() {
		return "OM_BooleanObservationCollection";
	}

	@Override
	public void addObservationCollection(IObservationCollection obsCol) {
		if (obsCol.getObservations().get(0) instanceof BooleanObservation){
			for (AbstractObservation ao : obsCol.getObservations()) {
				this.members.add((BooleanObservation) ao);
			}
		}
		else {
			throw new RuntimeException("ObservationCollection with type"+obsCol.getObservations().get(0).getName()+ " cannot be added to BooleanObservationCollection!!");
		}
	}

}
