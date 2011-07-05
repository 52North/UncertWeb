package org.n52.wps.io.datahandler.xml;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.om.x20.OMBooleanObservationCollectionDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument;
import net.opengis.om.x20.OMTextObservationCollectionDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

/**
 * Observation parser, handling observations encoded in XML according the
 * UncertWeb O&M profile; wraps {@link XBObservationParser}
 * 
 * @author Kiesow, staschc
 * 
 */
public class OMXmlParser extends AbstractXMLParser {

	private static Logger LOGGER = Logger.getLogger(OMXmlParser.class);
	private XBObservationParser parser = new XBObservationParser();

	@Override
	public IData parseXML(String xmlString) {
		OMData omData = null;
		OMDataBinding omDataBinding = null;

		
		XmlObject xbDoc;
		try {
			xbDoc = XmlObject.Factory.parse(xmlString);
		} catch (XmlException e) {
			String message = "Error while parsing Observation input: "+e.getMessage();
			LOGGER.info(message);
			throw new RuntimeException(message,e);
		}

		omData = createOMData4XmlObject(xbDoc);
		omDataBinding = new OMDataBinding(omData);
		return omDataBinding;
	}

	@Override
	public IData parseXML(InputStream primaryFile) {
		OMData omData = null;
		OMDataBinding omDataBinding = null;

		
		XmlObject xbDoc;
		try {
			xbDoc = XmlObject.Factory.parse(primaryFile);
		} catch (XmlException e) {
			String message = "Error while parsing Observation input: "+e.getMessage();
			LOGGER.info(message);
			throw new RuntimeException(message,e);
		} catch (IOException e) {
			String message = "Error while parsing Observation input: "+e.getMessage();
			LOGGER.info(message);
			throw new RuntimeException(message,e);
		}

		omData = createOMData4XmlObject(xbDoc);
		omDataBinding = new OMDataBinding(omData);
		return omDataBinding;
	}

	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		if (!mimeType.equalsIgnoreCase(UncertWebDataConstants.MIME_TYPE_OMX)){
			throw new RuntimeException("MimeType "+ mimeType + " is not supported by OMXmlParser!");
		}
		return parseXML(primaryFile);
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = { OMDataBinding.class };
		return supportedClasses;
	}
	
	/**
	 * helper method for creating OMData from input
	 * 
	 * @param xbDoc
	 * 			XMLBeans representation of observation input
	 * @return Returns OMData representation of observation
	 */
	private OMData createOMData4XmlObject(XmlObject xbDoc){
		OMData omData = null;
		try{
		if (xbDoc instanceof OMObservationDocument) {
			AbstractObservation obs;
			obs = parser.parseObservation(xbDoc.toString());
			omData = new OMData(obs, UncertWebDataConstants.MIME_TYPE_OMX);
		} else if (xbDoc instanceof OMBooleanObservationCollectionDocument
				|| xbDoc instanceof OMDiscreteNumericObservationCollectionDocument
				|| xbDoc instanceof OMMeasurementCollectionDocument
				|| xbDoc instanceof OMReferenceObservationCollectionDocument
				|| xbDoc instanceof OMTextObservationCollectionDocument
				|| xbDoc instanceof OMUncertaintyObservationCollectionDocument) {

			// TODO missing: OMCategoryObservationCollectionDocument

			IObservationCollection obsCol = parser
					.parseObservationCollection(xbDoc.toString());
			omData = new OMData(obsCol, UncertWebDataConstants.MIME_TYPE_OMX);
		} else {
			throw new RuntimeException(
					"The data is neither an observation nor an observation collection.");
		}
		} catch (OMParsingException e) {
			LOGGER.info("Error while parsing Observation input: "+e.getMessage());
		}
		return omData;
	}
}

