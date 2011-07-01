package org.uncertweb.api.om.io;

import net.opengis.om.x20.OMObservationDocument;

import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

/**
 * Interface for encoding observations
 * 
 * @author Kiesow
 *
 */
public interface IObservationEncoder {

	/**
	 * encodes an {@link OMObservationCollectionDocument}
	 * 
	 * @param obsCol
	 *            observation collection
	 * @return observation collections's xml document as formatted String
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public String encodeObservationCollection(IObservationCollection obsCol) throws OMEncodingException;


	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document as formatted String
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public String encodeObservation(AbstractObservation obs) throws OMEncodingException;

	
	
}
