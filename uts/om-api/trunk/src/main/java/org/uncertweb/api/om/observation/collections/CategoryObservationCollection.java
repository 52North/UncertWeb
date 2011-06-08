package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.CategoryObservation;

/**
 * collection for CategoryObservations
 * 
 * @author staschc
 *
 */
public class CategoryObservationCollection implements IObservationCollection{
	
	/**gml Id of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<CategoryObservation> members;
	
	/**type name of observation collection*/
	public static final String NAME = "OM_CategoryObservationCollection";
	
	/**
	 * contructor; initializes members list
	 * 
	 */
	public CategoryObservationCollection(){
		this.members = new ArrayList<CategoryObservation>();
	}
	
	/**
	 * constructor
	 * 
	 * @param members
	 * 			members of collection
	 */
	public CategoryObservationCollection(List<CategoryObservation> members){
		this.members = members;
	}
	
	
	
	@Override
	public String getGmlId() {
		return gmlId;
	}



	/**
	 * @return the members
	 */
	public List<CategoryObservation> getMembers() {
		return members;
	}



	@Override
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
		if (!(obs instanceof CategoryObservation)){
			throw new IllegalArgumentException("Only CategoryObservation could be added to CategoryObservationCollection!");
		}
		this.members.add((CategoryObservation)obs);
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
