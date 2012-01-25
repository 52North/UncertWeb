package org.n52.wps.io.datahandler.om;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.*;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V2;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationDocument;
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
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.AbstractUwParser;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.om.io.v1.XBv1ObservationParser;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * Observation parser, handling observations encoded in XML according the
 * UncertWeb O&M profile; wraps {@link XBObservationParser}
 * 
 * @author Kiesow, staschc
 * 
 */
public class OMXmlParser extends AbstractUwParser {

	private static Logger log = Logger.getLogger(OMXmlParser.class);
	private XBObservationParser parser = new XBObservationParser();

	public OMXmlParser() {
		super(
			set(SCHEMA_OM_V2, SCHEMA_OM_V1), 
			set(ENCODING_UTF_8),
			set(MIME_TYPE_OMX_XML, MIME_TYPE_TEXT_XML), 
			UwCollectionUtils.<Class<?>>set(OMBinding.class)
		);
	}

	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		XmlObject xbDoc;
		try {
			xbDoc = XmlObject.Factory.parse(primaryFile);
		} catch (XmlException e) {
			String message = "Error while parsing Observation input: "+e.getMessage();
			log.info(message);
			throw new RuntimeException(message,e);
		} catch (IOException e) {
			String message = "Error while parsing Observation input: "+e.getMessage();
			log.info(message);
			throw new RuntimeException(message,e);
		}
		
		try{
			if (xbDoc instanceof OMObservationDocument) {
				return new OMBinding(parser.parseObservation(xbDoc.toString()));
			} else if (xbDoc instanceof OMBooleanObservationCollectionDocument
					|| xbDoc instanceof OMDiscreteNumericObservationCollectionDocument
					|| xbDoc instanceof OMMeasurementCollectionDocument
					|| xbDoc instanceof OMReferenceObservationCollectionDocument
					|| xbDoc instanceof OMTextObservationCollectionDocument
					|| xbDoc instanceof OMUncertaintyObservationCollectionDocument) {

				// TODO missing: OMCategoryObservationCollectionDocument

				IObservationCollection obsCol = parser
						.parseObservationCollection(xbDoc);
				return new OMBinding(obsCol);
			} else if (xbDoc instanceof ObservationDocument
					|| xbDoc instanceof ObservationCollectionDocument) {
				IObservationCollection obsCol = new XBv1ObservationParser()
						.parse(xbDoc);
				return new OMBinding(obsCol);
			} else {
				throw new RuntimeException(
						"The data is neither an observation nor an observation collection.");
			}
		} catch (OMParsingException e) {
			log.info("Error while parsing Observation input: "+e.getMessage());
			throw new RuntimeException(e);
		}

	}

}
