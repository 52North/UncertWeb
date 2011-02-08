package org.uncertweb.api.om.io;

import java.net.URISyntaxException;

import org.apache.xmlbeans.XmlException;
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
	 * parses an {@link OMObservationCollectionDocument}
	 * 
	 * @param xmlObsCol
	 * @return
	 * @throws Exception
	 * @throws XmlException
	 * @throws URISyntaxException
	 */
	public IObservationCollection parseObservationCollection(String xmlObsCol)
			throws Exception, XmlException, URISyntaxException;
	
	/**
	 * parses an Observation and it's SpatialSamplingFeature
	 * 
	 * @param xmlObs xml observation document's string
	 * @return
	 * @throws Exception
	 * @throws XmlException
	 * @throws URISyntaxException
	 */
	public AbstractObservation parseObservation(String xmlObs)
			throws Exception, XmlException, URISyntaxException;

}
