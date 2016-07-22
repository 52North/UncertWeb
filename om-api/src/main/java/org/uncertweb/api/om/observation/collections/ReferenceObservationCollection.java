package org.uncertweb.api.om.observation.collections;

import java.util.ArrayList;
import java.util.LinkedList;
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
		this.members = new LinkedList<ReferenceObservation>();
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

	@Override
	public void addObservationCollection(IObservationCollection obsCol) {
		if (obsCol.getObservations().get(0) instanceof ReferenceObservation){
			for (AbstractObservation ao : obsCol.getObservations()) {
				this.members.add((ReferenceObservation) ao);
			}
		}
		else {
			throw new RuntimeException("ObservationCollection with type"+obsCol.getObservations().get(0).getName()+ " cannot be added to ReferenceObservationCollection!!");
		}
	}
}
