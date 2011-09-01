package org.n52.wps.io.datahandler.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.opengis.om.x20.OMBooleanObservationCollectionDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument;
import net.opengis.om.x20.OMTextObservationCollectionDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.OMData;
import org.n52.wps.io.data.binding.complex.OMDataBinding;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

/**
 * Observation parser, handling observations encoded in XML according the
 * UncertWeb O&M profile; wraps {@link XBObservationParser}
 * 
 * @author Kiesow
 * 
 */
public class OMParser extends AbstractXMLParser {

	private static Logger log = Logger.getLogger(OMParser.class);
	private XBObservationParser parser = new XBObservationParser();

	@Override
	public IData parseXML(String xmlString) {
		try {
			OMData omData = null;

			// differ between obs and obsCol (as String)
			XmlObject xbDoc = XmlObject.Factory.parse(xmlString);

			if (xbDoc instanceof OMObservationDocument) {
				AbstractObservation obs = parser.parseObservation(xmlString);
				omData = new OMData(obs);

			} else if (xbDoc instanceof OMBooleanObservationCollectionDocument
					|| xbDoc instanceof OMDiscreteNumericObservationCollectionDocument
					|| xbDoc instanceof OMMeasurementCollectionDocument
					|| xbDoc instanceof OMReferenceObservationCollectionDocument
					|| xbDoc instanceof OMTextObservationCollectionDocument
					|| xbDoc instanceof OMUncertaintyObservationCollectionDocument) {

				// TODO missing: OMCategoryObservationCollectionDocument

				IObservationCollection obsCol = parser
						.parseObservationCollection(xmlString);
				omData = new OMData(obsCol);
			} else {
				throw new Exception(
						"The data is neither an observation nor an observation collection.");
			}

			OMDataBinding omDataBinding = new OMDataBinding(omData);
			return omDataBinding;

		} catch (Exception e) {
			log.debug("XML Data could not be parsed: " + e.getMessage());
		}
		return null;
	}

	@Override
	public IData parseXML(InputStream primaryFile) {
		try {
			OMData omData = null;
			
			BufferedReader bread = new BufferedReader(new InputStreamReader(primaryFile));
			
			String xmlString = "";
			
			String line = "";
			
			while((line = bread.readLine()) != null){
				xmlString = xmlString.concat(line);
			}
			// differ between obs and obsCol (as String)
			XmlObject xbDoc = XmlObject.Factory.parse(xmlString);

			if (xbDoc instanceof OMObservationDocument) {
				AbstractObservation obs = parser.parseObservation(xmlString);
				omData = new OMData(obs);

			} else if (xbDoc instanceof OMBooleanObservationCollectionDocument
					|| xbDoc instanceof OMDiscreteNumericObservationCollectionDocument
					|| xbDoc instanceof OMMeasurementCollectionDocument
					|| xbDoc instanceof OMReferenceObservationCollectionDocument
					|| xbDoc instanceof OMTextObservationCollectionDocument
					|| xbDoc instanceof OMUncertaintyObservationCollectionDocument) {

				// TODO missing: OMCategoryObservationCollectionDocument

				IObservationCollection obsCol = parser
						.parseObservationCollection(xmlString);
				omData = new OMData(obsCol);
			} else {
				throw new Exception(
						"The data is neither an observation nor an observation collection.");
			}

			OMDataBinding omDataBinding = new OMDataBinding(omData);
			return omDataBinding;

		} catch (Exception e) {
			log.debug("XML Data could not be parsed: " + e.getMessage());
		}
		return null;
	}

	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		try {
			OMData omData = null;

			BufferedReader bread = new BufferedReader(new InputStreamReader(primaryFile));
						
			String xmlString = "";
			
			String line = "";
			
			while((line = bread.readLine()) != null){
				xmlString = xmlString.concat(line);
			}
			
			// differ between obs and obsCol (as String)
			XmlObject xbDoc = XmlObject.Factory.parse(xmlString);

			if (xbDoc instanceof OMObservationDocument) {
				AbstractObservation obs = parser.parseObservation(xmlString);
				omData = new OMData(obs);

			} else if (xbDoc instanceof OMBooleanObservationCollectionDocument
					|| xbDoc instanceof OMDiscreteNumericObservationCollectionDocument
					|| xbDoc instanceof OMMeasurementCollectionDocument
					|| xbDoc instanceof OMReferenceObservationCollectionDocument
					|| xbDoc instanceof OMTextObservationCollectionDocument
					|| xbDoc instanceof OMUncertaintyObservationCollectionDocument) {

				// TODO missing: OMCategoryObservationCollectionDocument

				IObservationCollection obsCol = parser
						.parseObservationCollection(xmlString);
				omData = new OMData(obsCol);
			} else {
				throw new Exception(
						"The data is neither an observation nor an observation collection.");
			}

			OMDataBinding omDataBinding = new OMDataBinding(omData);
			return omDataBinding;

		} catch (Exception e) {
			log.debug("XML Data could not be parsed: " + e.getMessage());
		}
		return null;
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = { OMDataBinding.class };
		return supportedClasses;
	}
}
