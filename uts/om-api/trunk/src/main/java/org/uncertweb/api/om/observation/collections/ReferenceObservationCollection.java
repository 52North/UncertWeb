package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;
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
	
	/**type name of observation collection*/
	public static final String NAME = "OM_ReferenceObservationCollection";
	
	/**
	 * constructor creates empty collection
	 * 
	 */
	public ReferenceObservationCollection(){
		this.members = new ArrayList<ReferenceObservation>();
	}
	
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

	@Override
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException {
		if (!(obs instanceof ReferenceObservation)){
			throw new IllegalArgumentException("Only ReferenceObservation could be added to ReferenceObservationCollection!");
		}
		this.members.add((ReferenceObservation)obs);
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
