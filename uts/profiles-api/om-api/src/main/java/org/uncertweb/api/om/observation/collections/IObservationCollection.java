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
	 * 			
	 * @throws Exception
	 */
	public void addObservation(AbstractObservation obs) throws Exception;
	
	public List<? extends AbstractObservation> getObservations();
}

