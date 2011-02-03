package org.uncertweb.api.om.observation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Collection containing instances of
 * {@link org.uncertweb.api.om.observation.AbstractObservation}
 * 
 * @author Kiesow
 * 
 */
public class ObservationCollection {

	/** list containing the members of this observation collection */
	private Collection<AbstractObservation> members;

	/**
	 * Constructor, ensuring an empty but existing collection
	 */
	public ObservationCollection() {
		this.members = new ArrayList<AbstractObservation>();
	}

	/**
	 * Construcor, defining the collections data structure
	 * 
	 * @param members
	 */
	public ObservationCollection(Collection<AbstractObservation> members) {
		this.members = members;
	}

	// getter & setter

	public Collection<AbstractObservation> getMembers() {
		return members;
	}

	public void setMembers(Collection<AbstractObservation> members) {
		this.members = members;
	}
}
