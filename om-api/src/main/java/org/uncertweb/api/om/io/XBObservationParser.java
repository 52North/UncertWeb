package org.uncertweb.api.om.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractTimePrimitiveType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodPropertyType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePrimitivePropertyType;
import net.opengis.gml.x32.UncertaintyPropertyType;
import net.opengis.om.x20.FoiPropertyType;
import net.opengis.om.x20.OMAbstractObservationType;
import net.opengis.om.x20.OMBooleanObservationCollectionDocument;
import net.opengis.om.x20.OMBooleanObservationCollectionDocument.OMBooleanObservationCollection;
import net.opengis.om.x20.OMBooleanObservationDocument;
import net.opengis.om.x20.OMCategoryObservationDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument;
import net.opengis.om.x20.OMDiscreteNumericObservationCollectionDocument.OMDiscreteNumericObservationCollection;
import net.opengis.om.x20.OMDiscreteNumericObservationDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument;
import net.opengis.om.x20.OMMeasurementCollectionDocument.OMMeasurementCollection;
import net.opengis.om.x20.OMMeasurementDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument;
import net.opengis.om.x20.OMReferenceObservationCollectionDocument.OMReferenceObservationCollection;
import net.opengis.om.x20.OMReferenceObservationDocument;
import net.opengis.om.x20.OMTextObservationCollectionDocument;
import net.opengis.om.x20.OMTextObservationCollectionDocument.OMTextObservationCollection;
import net.opengis.om.x20.OMTextObservationDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument;
import net.opengis.om.x20.OMUncertaintyObservationCollectionDocument.OMUncertaintyObservationCollection;
import net.opengis.om.x20.OMUncertaintyObservationDocument;
import net.opengis.om.x20.UWBooleanObservationType;
import net.opengis.om.x20.UWDiscreteNumericObservationType;
import net.opengis.om.x20.UWMeasurementType;
import net.opengis.om.x20.UWReferenceObservationType;
import net.opengis.om.x20.UWTextObservationType;
import net.opengis.om.x20.UWUncertaintyObservationType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.isotc211.x2005.gco.UnitOfMeasurePropertyType;
import org.isotc211.x2005.gmd.DQElementPropertyType;
import org.isotc211.x2005.gmd.DQResultPropertyType;
import org.isotc211.x2005.gmd.DQUncertaintyResultType;
import org.isotc211.x2005.gmd.DQUncertaintyResultType.Value;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.XMLParser;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.io.XmlBeansGeometryParser;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.w3.x1999.xlink.ActuateAttribute.Actuate;
import org.w3.x1999.xlink.ShowAttribute.Show;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * implementation of Observation parser with XmlBeans; can be used to encode and parse observations
 * encoded in XML according the UncertWeb O&M profile
 * 
 * @author Kiesow, staschc
 * 
 */
public class XBObservationParser implements IObservationParser {
	
	private HashMap<String, SpatialSamplingFeature> featureCache;
	private HashMap<String, TimeObject> timeCache;
	

	/**
	 * constructor initializes the caches for time and featureOfInterest elements
	 * 
	 */
	public XBObservationParser(){
		featureCache = new HashMap<String, SpatialSamplingFeature>();
		timeCache = new HashMap<String, TimeObject>();
	}
	
	/**
	 * parses an observation collection
	 * 
	 * @param xmlObsCol
	 *            String containing an XML encoded observation collection
	 * @return POJO observation collection
	 * @throws OMParsingException 
	 */
	@Override
	public synchronized IObservationCollection parseObservationCollection(String xmlObsCol) throws OMParsingException {

		featureCache = new HashMap<String, SpatialSamplingFeature>();
		timeCache = new HashMap<String, TimeObject>();
		IObservationCollection oc = null;
		XmlObject xb_obsColDoc;
		try {
			xb_obsColDoc = XmlObject.Factory.parse(xmlObsCol);
		

		//Measurement collection
		if (xb_obsColDoc instanceof OMMeasurementCollectionDocument) {
			OMMeasurementCollection xb_ocType = ((OMMeasurementCollectionDocument)xb_obsColDoc).getOMMeasurementCollection();
			UWMeasurementType[] xb_obsArray = xb_ocType.getOMMeasurementArray();
			List<Measurement> obsList = new ArrayList<Measurement>(xb_obsArray.length);
			for (UWMeasurementType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				Measurement obs = (Measurement)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new MeasurementCollection(obsList);
			return oc;
		}
		//BooleanObservation collection
		else if (xb_obsColDoc instanceof OMBooleanObservationCollectionDocument){
			OMBooleanObservationCollection xb_ocType = ((OMBooleanObservationCollectionDocument)xb_obsColDoc).getOMBooleanObservationCollection();
			UWBooleanObservationType[] xb_obsArray = xb_ocType.getOMBooleanObservationArray();
			List<BooleanObservation> obsList = new ArrayList<BooleanObservation>(xb_obsArray.length);
			for (UWBooleanObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				BooleanObservation obs = (BooleanObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new BooleanObservationCollection(obsList);
			return oc;
		}
		//DiscreteNumericObservation collection
		else if (xb_obsColDoc instanceof OMDiscreteNumericObservationCollectionDocument){
			OMDiscreteNumericObservationCollection xb_ocType = ((OMDiscreteNumericObservationCollectionDocument)xb_obsColDoc).getOMDiscreteNumericObservationCollection();
			UWDiscreteNumericObservationType[] xb_obsArray = xb_ocType.getOMDiscreteNumericObservationArray();
			List<DiscreteNumericObservation> obsList = new ArrayList<DiscreteNumericObservation>(xb_obsArray.length);
			for (UWDiscreteNumericObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				DiscreteNumericObservation obs = (DiscreteNumericObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new DiscreteNumericObservationCollection(obsList);
			return oc;
		}
		//UncertaintyObservation collection
		else if (xb_obsColDoc instanceof OMUncertaintyObservationCollectionDocument){
			OMUncertaintyObservationCollection xb_ocType = ((OMUncertaintyObservationCollectionDocument)xb_obsColDoc).getOMUncertaintyObservationCollection();
			UWUncertaintyObservationType[] xb_obsArray = xb_ocType.getOMUncertaintyObservationArray();
			List<UncertaintyObservation> obsList = new ArrayList<UncertaintyObservation>(xb_obsArray.length);
			for (UWUncertaintyObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				UncertaintyObservation obs = (UncertaintyObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new UncertaintyObservationCollection(obsList);
			return oc;
		}
		//ReferenceObservation collection
		else if (xb_obsColDoc instanceof OMReferenceObservationCollectionDocument){
			OMReferenceObservationCollection xb_ocType = ((OMReferenceObservationCollectionDocument)xb_obsColDoc).getOMReferenceObservationCollection();
			UWReferenceObservationType[] xb_obsArray = xb_ocType.getOMReferenceObservationArray();
			List<ReferenceObservation> obsList = new ArrayList<ReferenceObservation>(xb_obsArray.length);
			for (UWReferenceObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				ReferenceObservation obs = (ReferenceObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new ReferenceObservationCollection(obsList);
			return oc;
		}
		//TextObservation collection
		else if (xb_obsColDoc instanceof OMTextObservationCollectionDocument){
			OMTextObservationCollection xb_ocType = ((OMTextObservationCollectionDocument)xb_obsColDoc).getOMTextObservationCollection();
			UWTextObservationType[] xb_obsArray = xb_ocType.getOMTextObservationArray();
			List<TextObservation> obsList = new ArrayList<TextObservation>(xb_obsArray.length);
			for (UWTextObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				TextObservation obs = (TextObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new TextObservationCollection(obsList);
			return oc;
		}
		
		
		else {
			throw new OMParsingException("ObservationCollection type" + xb_obsColDoc.getClass() + "is not supported by this parser!");
		}
		
		} catch (XmlException e) {
			throw new OMParsingException(e);
		}

	}
	
	
	/**
	 * parses an observation collection
	 * 
	 * @param xmlObsCol
	 *            XmlBeans XmlObject containing an XML encoded observation collection
	 * @return POJO observation collection
	 * @throws OMParsingException 
	 */
	public synchronized IObservationCollection parseObservationCollection(XmlObject xb_obsColDoc) throws OMParsingException {

		IObservationCollection oc = null;
		//Measurement collection
		if (xb_obsColDoc instanceof OMMeasurementCollectionDocument) {
			OMMeasurementCollection xb_ocType = ((OMMeasurementCollectionDocument)xb_obsColDoc).getOMMeasurementCollection();
			UWMeasurementType[] xb_obsArray = xb_ocType.getOMMeasurementArray();
			List<Measurement> obsList = new ArrayList<Measurement>(xb_obsArray.length);
			for (UWMeasurementType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				Measurement obs = (Measurement)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new MeasurementCollection(obsList);
			return oc;
		}
		//BooleanObservation collection
		else if (xb_obsColDoc instanceof OMBooleanObservationCollectionDocument){
			OMBooleanObservationCollection xb_ocType = ((OMBooleanObservationCollectionDocument)xb_obsColDoc).getOMBooleanObservationCollection();
			UWBooleanObservationType[] xb_obsArray = xb_ocType.getOMBooleanObservationArray();
			List<BooleanObservation> obsList = new ArrayList<BooleanObservation>(xb_obsArray.length);
			for (UWBooleanObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				BooleanObservation obs = (BooleanObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new BooleanObservationCollection(obsList);
			return oc;
		}
		//DiscreteNumericObservation collection
		else if (xb_obsColDoc instanceof OMDiscreteNumericObservationCollectionDocument){
			OMDiscreteNumericObservationCollection xb_ocType = ((OMDiscreteNumericObservationCollectionDocument)xb_obsColDoc).getOMDiscreteNumericObservationCollection();
			UWDiscreteNumericObservationType[] xb_obsArray = xb_ocType.getOMDiscreteNumericObservationArray();
			List<DiscreteNumericObservation> obsList = new ArrayList<DiscreteNumericObservation>(xb_obsArray.length);
			for (UWDiscreteNumericObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				DiscreteNumericObservation obs = (DiscreteNumericObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new DiscreteNumericObservationCollection(obsList);
			return oc;
		}
		//UncertaintyObservation collection
		else if (xb_obsColDoc instanceof OMUncertaintyObservationCollectionDocument){
			OMUncertaintyObservationCollection xb_ocType = ((OMUncertaintyObservationCollectionDocument)xb_obsColDoc).getOMUncertaintyObservationCollection();
			UWUncertaintyObservationType[] xb_obsArray = xb_ocType.getOMUncertaintyObservationArray();
			List<UncertaintyObservation> obsList = new ArrayList<UncertaintyObservation>(xb_obsArray.length);
			for (UWUncertaintyObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				UncertaintyObservation obs = (UncertaintyObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new UncertaintyObservationCollection(obsList);
			return oc;
		}
		//ReferenceObservation collection
		else if (xb_obsColDoc instanceof OMReferenceObservationCollectionDocument){
			OMReferenceObservationCollection xb_ocType = ((OMReferenceObservationCollectionDocument)xb_obsColDoc).getOMReferenceObservationCollection();
			UWReferenceObservationType[] xb_obsArray = xb_ocType.getOMReferenceObservationArray();
			List<ReferenceObservation> obsList = new ArrayList<ReferenceObservation>(xb_obsArray.length);
			for (UWReferenceObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				ReferenceObservation obs = (ReferenceObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new ReferenceObservationCollection(obsList);
			return oc;
		}
		//TextObservation collection
		else if (xb_obsColDoc instanceof OMTextObservationCollectionDocument){
			OMTextObservationCollection xb_ocType = ((OMTextObservationCollectionDocument)xb_obsColDoc).getOMTextObservationCollection();
			UWTextObservationType[] xb_obsArray = xb_ocType.getOMTextObservationArray();
			List<TextObservation> obsList = new ArrayList<TextObservation>(xb_obsArray.length);
			for (UWTextObservationType xb_obs:xb_obsArray){
				OMObservationDocument xb_omDoc = OMObservationDocument.Factory.newInstance();
				xb_omDoc.setOMObservation(xb_obs);
				
				TextObservation obs = (TextObservation)parseObservationDocument(xb_omDoc);
				obsList.add(obs);
			}
			oc = new TextObservationCollection(obsList);
			return oc;
		}
		
		
		else {
			throw new OMParsingException("ObservationCollection type" + xb_obsColDoc.getClass() + "is not supported by this parser!");
		}
		
		

	}

	/**
	 * parses an Observation and it's SpatialSamplingFeature
	 * 
	 * @param xmlObs
	 *            xml observation document's string
	 * @return POJO observation
	 * @throws OMParsingException 
	 * 			if parsing failed
	 */
	@Override
	public synchronized AbstractObservation parseObservation(String xmlObs) throws OMParsingException {

		featureCache = new HashMap<String, SpatialSamplingFeature>();
		timeCache = new HashMap<String, TimeObject>();
		
		XmlObject xb_obsDoc;
		try {
			xb_obsDoc = XmlObject.Factory.parse(xmlObs);
		} catch (XmlException e) {
			throw new OMParsingException(e);
		}

		return parseObservationDocument((OMObservationDocument) xb_obsDoc);
	}

	/**
	 * parses an Observation and it's SpatialSamplingFeature
	 * 
	 * @param xb_obsDoc
	 *            XMLBeans observation document
	 * @return POJO observation
	 * @throws OMParsingException
	 * 			if parsing of uncertainty fails
	 */
	public synchronized AbstractObservation parseObservationDocument(
			OMObservationDocument xb_obsDoc) throws OMParsingException {

		AbstractObservation obs = null;

		try{
		if (xb_obsDoc instanceof OMObservationDocument) {

			OMAbstractObservationType xb_obsType = null;
			
			// observation is instance of a specific uncertweb observation type 
			if (xb_obsDoc instanceof OMBooleanObservationDocument) {
				xb_obsType = ((OMBooleanObservationDocument) xb_obsDoc).getOMBooleanObservation();
			} else if (xb_obsDoc instanceof OMCategoryObservationDocument) {
				xb_obsType = ((OMCategoryObservationDocument) xb_obsDoc).getOMCategoryObservation();
			} else if (xb_obsDoc instanceof OMDiscreteNumericObservationDocument) {
				xb_obsType = ((OMDiscreteNumericObservationDocument) xb_obsDoc).getOMDiscreteNumericObservation();
			} else if (xb_obsDoc instanceof OMMeasurementDocument) {
				xb_obsType = ((OMMeasurementDocument) xb_obsDoc).getOMMeasurement();
			} else if (xb_obsDoc instanceof OMReferenceObservationDocument) {
				xb_obsType = ((OMReferenceObservationDocument) xb_obsDoc).getOMReferenceObservation();
			} else if (xb_obsDoc instanceof OMTextObservationDocument) {
				xb_obsType = ((OMTextObservationDocument) xb_obsDoc).getOMTextObservation();
			} else if (xb_obsDoc instanceof OMUncertaintyObservationDocument) {
				xb_obsType = ((OMUncertaintyObservationDocument) xb_obsDoc).getOMUncertaintyObservation();

			} else if (xb_obsDoc.getOMObservation() instanceof OMAbstractObservationType) {
				// observation is instance of general O&M 2 observation type
				xb_obsType = ((OMObservationDocument) xb_obsDoc).getOMObservation();
			}
			
			CodeWithAuthorityType xb_identifier = xb_obsType.getIdentifier();
			Identifier identifier = null;
			if (xb_identifier!=null){
				String id = xb_identifier.getStringValue();
				URI codeSpace;
				codeSpace = new URI(xb_identifier.getCodeSpace());
				identifier = new Identifier(codeSpace, id);
			}

			// parse boundedBy (optional parameter)
			Envelope boundedBy = null;

			if (!(xb_obsType.getBoundedBy() == null)) {
				boundedBy = parseEnvelope(xb_obsType.getBoundedBy()
						.getEnvelope());
			}

			// parse phenomenonTime
			TimeObject phenomenonTime = parseTimeProperty(xb_obsType
					.getPhenomenonTime());

			// parse resultTime
			TimeObject resultTime = parseTimeProperty(xb_obsType
					.getResultTime());

			// parse validTime (optional parameter)
			TimeObject validTime = null;

			if (!(xb_obsType.getValidTime() == null)) {
				validTime = parseTimeProperty(xb_obsType.getValidTime());
			}

			// parse procedure
			URI procedure = parseProcedure(xb_obsType.getProcedure());

			// parse observedProperty
			URI observedProperty = parseObservedProperty(xb_obsType
					.getObservedProperty());

			// parse featureOfInterest
			SpatialSamplingFeature featureOfInterest = parseSamplingFeature(xb_obsType
					.getFeatureOfInterest());

			// parse resultQuality (optional parameter)
			DQ_UncertaintyResult[] resultQuality = null;
			if (xb_obsType.getResultQualityArray() != null
					&& xb_obsType.getResultQualityArray().length > 0) {

				resultQuality = parseResultQuality(xb_obsType
						.getResultQualityArray());

			}

			// parse result and build observation
			if (xb_obsType instanceof UWMeasurementType) {
				MeasureType xb_measureType = ((UWMeasurementType)xb_obsType)
						.getResult();
				double value = xb_measureType.getDoubleValue();
				String uom = xb_measureType.getUom();

				MeasureResult result = new MeasureResult(value, uom);
				obs = new Measurement(identifier, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality, result);

			} else if (xb_obsType instanceof UWTextObservationType) {

				String value = ((UWTextObservationType)xb_obsType).getResult().trim();

				TextResult result = new TextResult(value);
				obs = new TextObservation(identifier, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality, result);

			} else if (xb_obsType instanceof UWUncertaintyObservationType) {

				UncertaintyPropertyType xb_uncPropType = ((UWUncertaintyObservationType) xb_obsType).getResult();

				//Workaround for parsing uncertainties
				//TODO test and maybe re-implement
				String uncertaintyText = xb_uncPropType.toString();
				String uom = null;
				if (uncertaintyText.startsWith("<xml-fragment")){
					
					//preserve uom
					if (uncertaintyText.contains("uom=") || uncertaintyText.contains("uom =")) {
						uom = XmlObject.Factory.parse(uncertaintyText).selectAttribute(new QName("uom")).getDomNode().getNodeValue();
					}
					
					//removing xml fragments
					int start = uncertaintyText.indexOf("<un:");
					int end = uncertaintyText.indexOf("</xml-fragment");
					uncertaintyText = uncertaintyText.substring(start,end);
					
					//add namespace uncertml
					if (!uncertaintyText.contains("xmlns:un=\"http://www.uncertml.org/2.0")){
						uncertaintyText = uncertaintyText.replaceFirst(">", " xmlns:un=\"http://www.uncertml.org/2.0\">");
					}
				}
				XMLParser uncertaintyParser = new XMLParser();
				IUncertainty uncertainty = uncertaintyParser
						.parse(uncertaintyText);
				UncertaintyResult result = new UncertaintyResult(uncertainty, uom);
				obs = new UncertaintyObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						result);

			} else if (xb_obsType instanceof UWDiscreteNumericObservationType) {

				BigInteger value = ((UWDiscreteNumericObservationType) xb_obsType).getResult();

				IntegerResult result = new IntegerResult(value);
				obs = new DiscreteNumericObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						result);

			} else if (xb_obsType instanceof UWBooleanObservationType) {

				boolean value = ((UWBooleanObservationType)xb_obsType).getResult();

				BooleanResult result = new BooleanResult(value);
				obs = new BooleanObservation(identifier, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality, result);

			} else if (xb_obsType instanceof UWReferenceObservationType) {

				ReferenceType xb_referenceType = ((UWReferenceObservationType)xb_obsType).getResult();
				String type = xb_referenceType.getType();
				String href = xb_referenceType.getHref();
				String role = xb_referenceType.getRole();
				String arcrole = xb_referenceType.getArcrole();
				String title = xb_referenceType.getTitle();
				Show.Enum show = xb_referenceType.getShow();
				Actuate.Enum actuate = xb_referenceType.getActuate();

				ReferenceResult result = new ReferenceResult(type, href, role,
						arcrole, title, show, actuate);
				obs = new ReferenceObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						result);
			}
		}
		} catch (Exception e) {
			throw new OMParsingException(e);		
		} 
		return obs;
	}

	/**
	 * helper method for parsing a resultQuality array; ATTENTION: currently,
	 * only uncertainty results as defined in the UncertWeb profile are parsed;
	 * has to be extended, if further data quality types as defined in ISO 19139
	 * have to be supported.
	 * 
	 * @param xb_resultQualityArray
	 *            XmlBeans representation of resultQualities
	 * @return Array with POJO objects representing uncertainty results
	 * @throws UncertaintyParserException
	 */
	private DQ_UncertaintyResult[] parseResultQuality(
			DQElementPropertyType[] xb_resultQualityArray)
			throws UncertaintyParserException {

		DQ_UncertaintyResult[] resultQuality = new DQ_UncertaintyResult[0];

		ArrayList<DQ_UncertaintyResult> rqList = new ArrayList<DQ_UncertaintyResult>();

		XMLParser uncertaintyParser = new XMLParser();
		for (DQElementPropertyType xb_rq : xb_resultQualityArray) {

			DQResultPropertyType[] xb_resultArray = xb_rq
					.getAbstractDQElement().getResultArray();

			for (DQResultPropertyType xb_r : xb_resultArray) {
				
				if (!xb_r.isNil() && xb_r.getAbstractDQResult() != null
						&& !xb_r.getAbstractDQResult().isNil()
						&& xb_r.getAbstractDQResult() instanceof DQUncertaintyResultType) {

					DQUncertaintyResultType uncResult = (DQUncertaintyResultType) xb_r
							.getAbstractDQResult().changeType(
									DQUncertaintyResultType.type);

					
					UnitOfMeasurePropertyType valueUnit = uncResult
							.getValueUnit();
					String identifier = valueUnit.getUnitDefinition()
							.getIdentifier().getStringValue();
					
					Value[] value = uncResult.getValueArray();
					IUncertainty[] values = new IUncertainty[value.length];
					for (int i = 0; i < value.length; ++i) {
						IUncertainty uncertainty = uncertaintyParser
								.parse(value[i].toString());
						values[i] = uncertainty;
					}

					rqList.add(new DQ_UncertaintyResult(values,
							identifier));
				}
			}
		}
		return rqList.toArray(resultQuality);
	}

	/**
	 * helper method for parsing observedProperty
	 * 
	 * @param xb_observedProperty
	 *            XmlBeans representation of observed property
	 * @return the property's URI
	 * @throws URISyntaxException
	 *             If creation of URI from observed property fails
	 */
	private URI parseObservedProperty(ReferenceType xb_observedProperty)
			throws URISyntaxException {

		return new URI(xb_observedProperty.getHref());
	}

	/**
	 * helper method for parsing procedure
	 * 
	 * @param xb_procedure
	 *            XmlBeans representation of procedure
	 * @return the procedure's URI
	 * @throws URISyntaxException
	 *             if creation of the URI out of the procedure property fails
	 */
	private URI parseProcedure(OMProcessPropertyType xb_procedure)
			throws URISyntaxException {

		return new URI(xb_procedure.getHref());
	}

	/**
	 * helper method for parsing time properteis
	 * 
	 * @param xb_absTimeObj
	 *            XmlBeans XmlObject representing the time property
	 * @return Returns Java representation of temporal property
	 * @throws URISyntaxException 
	 */
	private TimeObject parseTimeProperty(XmlObject xb_absTimeObj) throws URISyntaxException {

		TimeObject timeObject = null;
		String href = null;

		if (xb_absTimeObj instanceof TimePrimitivePropertyType) {
			TimePrimitivePropertyType xb_tppt = (TimePrimitivePropertyType) xb_absTimeObj;
			href = xb_tppt.getHref();
			if (href == null || href.equals("")) {
				AbstractTimePrimitiveType xb_absTp = xb_tppt
						.getAbstractTimePrimitive();
				if (xb_absTp instanceof TimeInstantType) {
					timeObject = new TimeObject(((TimeInstantType) xb_absTp)
							.getTimePosition().getStringValue());
					this.timeCache.put(xb_absTp.getId(),timeObject);

				} else if (xb_absTp instanceof TimePeriodType) {
					timeObject = new TimeObject(((TimePeriodType) xb_absTp).getBegin().getTimeInstant().getTimePosition().getStringValue(),((TimePeriodType) xb_absTp).getEnd().getTimeInstant().getTimePosition().getStringValue());
					this.timeCache.put(xb_absTp.getId(), timeObject);
				}

			} else {
				timeObject = getTimeObject4Href(href);
				timeObject.setHref(new URI(href));
			}

		} else if (xb_absTimeObj instanceof TimeInstantPropertyType) {
			TimeInstantPropertyType xb_tipt = (TimeInstantPropertyType) xb_absTimeObj;
			href = xb_tipt.getHref();
			if (href == null || href.equals("")) {

				timeObject = new TimeObject(xb_tipt.getTimeInstant()
						.getTimePosition().getStringValue());
				this.timeCache.put(xb_tipt.getTimeInstant().getId(), timeObject);

			} else {
				timeObject = getTimeObject4Href(href);
				timeObject.setHref(new URI(href));
			}
		} else if (xb_absTimeObj instanceof TimePeriodPropertyType) {

			TimePeriodPropertyType xb_tppt = (TimePeriodPropertyType) xb_absTimeObj;
			href = xb_tppt.getHref();
			if (href == null || href.equals("")) {
				timeObject = new TimeObject(xb_tppt.getTimePeriod()
						.getBegin().getTimeInstant().getTimePosition().getStringValue(),xb_tppt.getTimePeriod()
						.getEnd().getTimeInstant().getTimePosition().getStringValue());
				this.timeCache.put(xb_tppt.getTimePeriod().getId(), timeObject);
			} else {
				timeObject = getTimeObject4Href(href);
				timeObject.setHref(new URI(href));
			}
		}

		return timeObject;
	}

	/**
	 * helper method for parsing timePosition to DateTime
	 * 
	 * @param timePosition
	 *            time as a string e.g. 1970-01-01T00:00:00Z
	 * @return time as an Object
	 */
	public DateTime parseTimePosition(String timePosition) {
		DateTime dateTime = null;

		DateTimeFormatter dtf = ISODateTimeFormat.dateTimeParser();
		dateTime = dtf.withOffsetParsed().parseDateTime(timePosition);

		return dateTime;
	}

	/**
	 * helper method for parsing a gml Envelope XmlBeans object to a jts
	 * envelope
	 * 
	 * @param env
	 *            XmlBeans representation of Envelope
	 * @return Returns JTS envelope
	 * @throws Exception
	 *             if parsing of envelope fails
	 */
	private Envelope parseEnvelope(EnvelopeType env){

		// get shape geometry
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();

		DirectPositionType xb_upperCorner = env.getUpperCorner();
		Coordinate upperCoord = parser.parsePositionString(xb_upperCorner
				.getStringValue());

		DirectPositionType xb_lowerCorner = env.getLowerCorner();
		Coordinate lowerCoord = parser.parsePositionString(xb_lowerCorner
				.getStringValue());

		return new Envelope(upperCoord, lowerCoord);
	}

	/**
	 * parses a SpatialSamlingFeature from a feature of interest
	 * 
	 * @param xb_featureOfInterest
	 *            XmlBeans representation of SamplingFeature
	 * @return Returns POJO representation of sampling feature
	 * @throws URISyntaxException
	 * 			if xlinks cannot be resolved or are malformed 
	 * @throws MalformedURLException 
	 * 			if xlinks cannot be resolved or are malformed
	 * @throws IllegalArgumentException 
	 *             if parsing of the feature fails
	 * @throws XmlException 
	 * 			if parsing of the geometry fails
	 */
	public synchronized SpatialSamplingFeature parseSamplingFeature(
			FoiPropertyType xb_featureOfInterest) throws IllegalArgumentException, MalformedURLException, URISyntaxException, XmlException {

		SpatialSamplingFeature ssf = null;

		// if reference is set, create SamplingFeature and set reference
		if (xb_featureOfInterest.isSetHref()) {
			return getSamplingFeature4Href(xb_featureOfInterest.getHref());
			//return new SpatialSamplingFeature(xb_featureOfInterest.getHref());
		}

		// FOI is encoded inline, so parse the feature
		else {
			
			String gmlId = xb_featureOfInterest.getSFSpatialSamplingFeature().getId();
			// get identifier
			//TODO add parsing of code space
			Identifier identifier = null;
			if (xb_featureOfInterest.getSFSpatialSamplingFeature()
					.getIdentifier()!=null){
				CodeWithAuthorityType xb_identifier = xb_featureOfInterest.getSFSpatialSamplingFeature().getIdentifier();
				String idString = xb_identifier.getStringValue();
				URI codeSpace = new URI(xb_identifier.getCodeSpace());
				identifier = new Identifier(codeSpace,idString);
			}

			// TODO add boundedBy, location

			// get reference to the sampled feature
			String sampledFeature = null;

			
			if (!(xb_featureOfInterest.getSFSpatialSamplingFeature().getSampledFeature().getHref() == null)) {

				sampledFeature = xb_featureOfInterest.getSFSpatialSamplingFeature().getSampledFeature().getHref();
			}

			// get shape geometry
			XmlBeansGeometryParser parser = new XmlBeansGeometryParser();

			ShapeType geomString = xb_featureOfInterest.getSFSpatialSamplingFeature().getShape();
			Geometry shape = parser.parseUwGeometry(geomString.toString());

			ssf = new SpatialSamplingFeature(identifier, sampledFeature, shape);
			this.featureCache.put(gmlId, ssf);
			return ssf;
		}
	}
	
	/**
	 * helper method which returns a spatialSamplingFeature for a reference contained in the xlink:href attribute of the featureOfInterest
	 *
	 * 
	 * @param href
	 * 			value of the xlink:href attribute
	 * @return Returns spatial sampling feature which is referenced
	 * @throws URISyntaxException 
	 * 			if xlinks cannot be resolved or are malformed
	 * @throws MalformedURLException
	 * 			if xlinks cannot be resolved or are malformed 
	 */
	private SpatialSamplingFeature getSamplingFeature4Href(String href) throws IllegalArgumentException, URISyntaxException, MalformedURLException {
		SpatialSamplingFeature ssf = null;
		if	(!href.startsWith("#")){
			URL hrefUrl = new URL(href);
			
			if (this.featureCache.containsKey(href)){
				ssf = this.featureCache.get(href);
			}
			else {
				String xmlString = null;
				try {
					xmlString = readTextFromStream(hrefUrl.openStream());
				} catch (IOException e) {
					throw new IllegalArgumentException("Error while resolving URI to featureOfInterest element" + e.getMessage());
				}
				if (xmlString!=null&&!(xmlString.equals(""))){
					try {
						XmlObject object = XmlObject.Factory.parse(xmlString);
						if (object instanceof SFSpatialSamplingFeatureDocument){
							ssf = parseSamplingFeatureDocument(((SFSpatialSamplingFeatureDocument)object).getSFSpatialSamplingFeature());
							ssf.setHref(new URI(href));
							this.featureCache.put(href.toString(), ssf);						}
					} catch (XmlException e) {
						throw new IllegalArgumentException("Error while resolving URI to featureOfInterest element" + e.getMessage());
					}
				}
			}
		}
		else {
			String gmlId = href.replace("#", "");
			ssf = this.featureCache.get(gmlId);
			if (ssf==null){
				throw new IllegalArgumentException("Feature of interest with reference " + gmlId + " could not be resolved in document!");
			}
			
		}
		return ssf;
	}

	/**
	 * helper method for parsing XmlBeans representation of SamplingFeature to internal representation
	 * 
	 * @param xb_sfType
	 * 			XmlBeans representation of SamplingFeature		
	 * @return Returns internal representation of SamplingFeature
	 * @throws XmlException 
	 * 			if geometry of feature cannot be parsed
	 * @throws IllegalArgumentException 
	 * 			if geometry of feature cannot be parsed
	 * @throws URISyntaxException 
	 */
	private SpatialSamplingFeature parseSamplingFeatureDocument(
			SFSpatialSamplingFeatureType xb_sfType) throws IllegalArgumentException, XmlException, URISyntaxException  {
		// get id
		Identifier identifier = null;
		if (xb_sfType.getIdentifier()!=null){
			CodeWithAuthorityType xb_identifier = xb_sfType.getIdentifier();
			String idString = xb_identifier.getStringValue();
			URI codeSpace = new URI(xb_identifier.getCodeSpace());
			identifier = new Identifier(codeSpace,idString);
		}

		// TODO add boundedBy, location

		// get id of the sampled feature
		String sampledFeature = null;

		if (!(xb_sfType.getSampledFeature().getAbstractFeature() == null)) {

			sampledFeature = xb_sfType.getSampledFeature()
					.getAbstractFeature().getId();
		}

		// get shape geometry
		XmlBeansGeometryParser parser = new XmlBeansGeometryParser();

		ShapeType geomString = xb_sfType.getShape();
		Geometry shape = parser.parseUwGeometry(geomString.toString());

		return new SpatialSamplingFeature(identifier, sampledFeature, shape);
		
	}

	/**
	 * helper method which returns a TimeObject from the cache
	 * 
	 * @param href
	 * 			value of xlink:href attribute carrying the reference to the time object
	 * @return Returns the timeObject
	 * @throws IllegalArgumentException
	 * 			if the reference could not be resolved in the document or if the reference is pointing to an external
	 * 			document which is currently not supported 
	 */
	private TimeObject getTimeObject4Href(String href) throws IllegalArgumentException{
		if	(!href.startsWith("#")){
			throw new IllegalArgumentException("External references for time objects are currently not supported!");
		}
		String gmlId = href.replace("#", "");
		TimeObject timeObject = this.timeCache.get(gmlId);
		if (timeObject==null){
			throw new IllegalArgumentException("Time object with reference " + gmlId + " could not be resolved in document!");
		}
		return timeObject;
	}
	
	/**
	 * helper method for reading strings from inputstream
	 * 
	 * @param in
	 * 			input stream from which text should be read
	 * @return Returns string that is read from stream
	 * @throws IOException
	 * 			if string could not be read
	 */
	 private String readTextFromStream(InputStream in) throws IOException {
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        String line;
	        StringBuffer sb = new StringBuffer();
	        for (int i=0; (line = br.readLine()) != null; i++) {
	            
	            // if not first line --> append "\n"
	            if (i > 0) {
	                sb.append("\n");
	            }
	            
	            sb.append(line);
	        }
	        br.close();

	        return sb.toString();
	    }

	 /**
		 * generic method for parsing a single observation or an observation collection; in case of a
		 * single observation, a collection containing one element is returned.
		 * 
		 * @param xmlString
		 *			string containing the O&M encoded as XML
		 * @return returns internal representation of observation or observation collection
		 * @throws OMParsingException 
		 * 			if parsing fails
		 */
	public IObservationCollection parse(String xmlString) throws OMParsingException {
		XmlObject xb_object;
		try {
			xb_object = XmlObject.Factory.parse(xmlString);
		} catch (XmlException e) {
			throw new OMParsingException(e);
		}
		IObservationCollection col = null;
		if (xb_object instanceof OMObservationDocument){
			
			AbstractObservation obs = parseObservation(xmlString);
			if (obs instanceof Measurement){
				col = new MeasurementCollection();
				col.addObservation(obs);
			}
			else if (obs instanceof CategoryObservation){
				col = new CategoryObservationCollection();
				col.addObservation(obs);
			}
			else if (obs instanceof UncertaintyObservation){
				col = new UncertaintyObservationCollection();
				col.addObservation(obs);
			}
			else if (obs instanceof TextObservation){
				col = new TextObservationCollection();
				col.addObservation(obs);
			}
			else if (obs instanceof DiscreteNumericObservation){
				col = new DiscreteNumericObservationCollection();
				col.addObservation(obs);
			}
			else if (obs instanceof ReferenceObservation){
				col = new ReferenceObservationCollection();
				col.addObservation(obs);
			}
			return col;
		}
		else {
			return parseObservationCollection(xmlString);
		}
	}
	
	public HashMap<String, SpatialSamplingFeature> getFeatureCache() {
		return featureCache;
	}
}
