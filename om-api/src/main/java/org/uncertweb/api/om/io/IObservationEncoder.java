package org.uncertweb.api.om.io;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

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
	 * encodes an {@link IObservationCollection}
	 *
	 * @param obsCol
	 *            observation collection
	 * @return observation collections's xml document as formatted String
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public String encodeObservationCollection(IObservationCollection obsCol) throws OMEncodingException;

	/**
	 * encodes an {@link IObservationCollection}
	 *
	 * @param obsCol
	 *            observation collection
	 * @param f
	 * 			file to which observation collection should be written
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public void encodeObservationCollection(IObservationCollection obsCol, File f) throws OMEncodingException;

	/**
	 *
	 * encodes an {@link IObservationCollection}
	 *
	 * @param obsCol
	 * 			obsbervation collection
	 * @param out
	 * 			output stream to which the observation collection should be written
	 * @throws OMEncodingException
	 */
	public void encodeObservationCollection(IObservationCollection obsCol, OutputStream out) throws OMEncodingException;

	/**
	 *
	 * encodes an {@link IObservationCollection}
	 *
	 * @param obsCol
	 * 			observation collection
	 * @param writer
	 * 			writer to which the observation collection should be written
	 * @throws OMEncodingException
	 */
	public void encodeObservationCollection(IObservationCollection obsCol, Writer writer) throws OMEncodingException;

	/**
	 * encodes an {@link AbstractObservation}
	 *
	 * @param obs
	 *            observation
	 * @return observation's xml document as formatted String
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public String encodeObservation(AbstractObservation obs) throws OMEncodingException;

	/**
	 *
	 * encodes subtypes of an {@link AbstractObservation}
	 *
	 * @param obs
	 * 			observation that should be encoded
	 * @param f
	 * 			file to which the observation should be written
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public void encodeObservation(AbstractObservation obs, File f) throws OMEncodingException;

	/**
	 * encodes subtypes of an {@link AbstractObservation}
	 *
	 * @param obs
	 * 			observation that should be encoded
	 * @param out
	 * 			output stream to which the observation should be written
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public void encodeObservation(AbstractObservation obs, OutputStream out) throws OMEncodingException;

	/**
	 * encodes subtypes of an {@link AbstractObservation}
	 *
	 * @param obs
	 * 			observation that should be encoded
	 * @param writer
	 * 			writer to which the observation should be written
	 * @throws OMEncodingException
	 * 			if encoding fails
	 */
	public void encodeObservation(AbstractObservation obs, Writer writer) throws OMEncodingException;


}
