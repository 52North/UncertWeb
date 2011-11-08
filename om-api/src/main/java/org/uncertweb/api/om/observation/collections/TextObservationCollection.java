package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.TextObservation;

/**
 * collection for TextObservations
 * 
 * @author staschc
 *
 */
public class TextObservationCollection implements IObservationCollection{
	
	/**gml Id of collection*/
	private String gmlId;
	
	/**members of collection*/
	private List<TextObservation> members;
	
	/**type name of observation collection*/
	public static final String NAME = "OM_TextObservationCollection";
	
	/**
	 * contructor; initializes members list
	 * 
	 */
	public TextObservationCollection(){
		this.members = new ArrayList<TextObservation>();
	}
	
	/**
	 * constructor
	 * 
	 * @param members
	 * 			members of collection
	 */
	public TextObservationCollection(List<TextObservation> members){
		this.members = members;
	}
	
	
	
	@Override
	public String getGmlId() {
		return gmlId;
	}



	/**
	 * @return the members
	 */
	public List<TextObservation> getMembers() {
		return members;
	}



	@Override
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
		if (!(obs instanceof TextObservation)){
			throw new IllegalArgumentException("Only BooleanObservation could be added to BooleanObservationCollection!");
		}
		this.members.add((TextObservation)obs);
	}



	@Override
	public List<? extends AbstractObservation> getObservations() {
		return members;
	}

	@Override
	public String getTypeName() {
		return NAME;
	}

	@Override
	public void addObservationCollection(IObservationCollection obsCol) {
		if (obsCol.getObservations().get(0) instanceof TextObservation){
			this.members.addAll((Collection<TextObservation>) obsCol.getObservations());
		}
		else {
			throw new RuntimeException("ObservationCollection with type"+obsCol.getObservations().get(0).getName()+ " cannot be added to TextObservationCollection!!");
		}
	}
	

}
