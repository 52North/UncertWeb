package org.uncertweb.api.om.io;

import net.opengis.om.x20.OMObservationDocument;

import org.apache.xmlbeans.XmlException;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
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
	 * @throws UncertaintyEncoderException 
	 * 			if encoding of uncertainty fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty is not supported 
	 * @throws XmlException 
	 * 			if encoding fails
	 * @throws IllegalArgumentException
	 *          if encoding fails
	 */
	public String encodeObservationCollection(IObservationCollection obsCol) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException;


	/**
	 * encodes an {@link OMObservationDocument}
	 * 
	 * @param obs
	 *            observation
	 * @return observation's xml document as formatted String
	 * @throws UncertaintyEncoderException 
	 * 			if encoding of uncertainty fails
	 * @throws UnsupportedUncertaintyTypeException
	 * 			if type of uncertainty is not supported 
	 * @throws XmlException 
	 * 			if encoding fails
	 * @throws IllegalArgumentException
	 *          if encoding fails
	 */
	public String encodeObservation(AbstractObservation obs) throws IllegalArgumentException, XmlException, UnsupportedUncertaintyTypeException, UncertaintyEncoderException;

}
