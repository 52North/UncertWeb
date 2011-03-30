package org.uncertweb.api.om.observation.collections;

import java.util.List;

import org.uncertweb.api.om.observation.AbstractObservation;


/**
 * Collection containing instances of
 * {@link org.uncertweb.api.om.observation.AbstractObservation}
 * 
 * @author Kiesow, staschc
 * 
 */
public interface IObservationCollection {

	/**
	 * 
	 * @return gmlID of collection
	 */
	public String getGmlId();
	
	/**
	 * method for adding observations
	 * 
	 * @param obs
	 * 			observation to add
	 * @throws IllegalArgumentException
	 * 			if type of observation does not match type of collection
	 */
	public void addObservation(AbstractObservation obs) throws IllegalArgumentException;
	
	
	/**
	 * getter for retrieving the list of observations contained in the collection
	 * 
	 * @return
	 * 		Returns list of observations contained in the collection
	 */
	public List<? extends AbstractObservation> getObservations();
}

