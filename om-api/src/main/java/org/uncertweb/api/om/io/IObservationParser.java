package org.uncertweb.api.om.io;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.xmlbeans.XmlException;
import org.json.JSONException;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

/**
 * Interface for parsing observations 
 * 
 * @author staschc
 *
 */
public interface IObservationParser {
	
	/**
	 * generic method for parsing a single observation or an observation collection; in case of a
	 * single observation, a collection containing one element is returned.
	 * 
	 * @param xmlString
	 * 			
	 * @return returns internal representation of observation or observation collection
	 * @throws URISyntaxException
	 * 			if xlinks could not be resolved or are malformed 
	 * @throws XmlException
	 * 			if parsing of geometries fails 
	 * @throws MalformedURLException 
	 * 			if xlinks could not be resolved or are malformed
	 * @throws IllegalArgumentException 
	 * 			If parsing of observation fails
	 * @throws UncertaintyParserException 
				if parsing of uncertainty fails
	 * @throws JSONException
	 * 			if parsing fails
	 */
	public IObservationCollection parse(String xmlString)
	throws OMParsingException;

	
	/**
	 * 
	 * parses an Observation Collection from String and resturns a Java representation
	 * 
	 * @param xmlObsCol
	 * 			String containing the XML representation of the observation collection
	 * @return returns internal representation of observation collection
	 * @throws OMParsingException
	 * 			if parsing fails
	 */
	public IObservationCollection parseObservationCollection(String xmlObsCol)
			throws OMParsingException;
	
	/**
	 * parses an Observation and it's SpatialSamplingFeature
	 * 
	 * @param xmlObs xml observation document's string
	 * @return internal JAVA representation of observation
	 * @throws URISyntaxException
	 * 			if xlinks could not be resolved or are malformed 
	 * @throws OMParsingException
	 * 			if parsing fails	
	 */
	public AbstractObservation parseObservation(String xmlObs) throws OMParsingException;

}
