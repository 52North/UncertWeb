package org.uncertweb.api.om.io;

import net.opengis.om.x20.OMObservationCollectionDocument;
import net.opengis.om.x20.OMObservationDocument;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.ObservationCollection;

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
	 * @throws Exception
	 */
	public String encodeObservationCollection(ObservationCollection obsCol)
			throws Exception;

	/**
	 * encodes an {@link OMObservationCollectionDocument}
	 * 
	 * @param obsCol
	 *            observation collection
	 * @return observation collections's xml document
	 * @throws Exception
	 */
	public OMObservationCollectionDocument encodeObservationCollectionDocument(
			ObservationCollection obsCol) throws Exception;

	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document as formatted String
	 * @throws Exception
	 */
	public String encodeObservation(AbstractObservation obs) throws Exception;

	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document
	 * @throws Exception
	 */
	public OMObservationDocument encodeObservationDocument(
			AbstractObservation obs) throws Exception;
}
