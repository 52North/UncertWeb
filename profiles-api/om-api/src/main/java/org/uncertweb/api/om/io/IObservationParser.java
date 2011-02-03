package org.uncertweb.api.om.io;

import java.net.URISyntaxException;

import net.opengis.om.x20.FoiPropertyType;
import net.opengis.om.x20.OMObservationCollectionDocument;
import net.opengis.om.x20.OMObservationDocument;

import org.apache.xmlbeans.XmlException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.ObservationCollection;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

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
	public ObservationCollection parseObservationCollection(String xmlObsCol)
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

	/**
	 * parses an Observation and it's SpatialSamplingFeature
	 * 
	 * @param xb_obsDoc xml observation document
	 * @return
	 * @throws Exception
	 * @throws XmlException
	 * @throws URISyntaxException
	 */
	public AbstractObservation parseObservationDocument(OMObservationDocument xb_obsDoc)
			throws Exception, XmlException, URISyntaxException;
	
	/**
	 * parses a SpatialSamlingFeature from a feature of interest
	 * 
	 * @param xb_featureOfInterest
	 * @return
	 * @throws Exception
	 */
	public SpatialSamplingFeature parseSamplingFeature(
			FoiPropertyType xb_featureOfInterest) throws Exception;
}
