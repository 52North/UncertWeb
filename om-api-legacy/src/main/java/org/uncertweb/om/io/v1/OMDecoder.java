package org.uncertweb.om.io.v1;

import static org.uncertweb.utils.UwCollectionUtils.collection;
import static org.uncertweb.utils.UwCollectionUtils.in;
import static org.uncertweb.utils.UwCollectionUtils.list;
import static org.uncertweb.utils.UwCollectionUtils.map;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractFeatureCollectionType;
import net.opengis.gml.AbstractFeatureType;
import net.opengis.gml.AbstractGeometryType;
import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.AbstractSurfaceType;
import net.opengis.gml.AbstractTimeObjectType;
import net.opengis.gml.CodeType;
import net.opengis.gml.CompositeSurfaceType;
import net.opengis.gml.CoordinatesType;
import net.opengis.gml.DirectPositionListType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.FeatureCollectionDocument;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.LineStringType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.MeasureType;
import net.opengis.gml.ObservationDocument;
import net.opengis.gml.PointType;
import net.opengis.gml.PolygonType;
import net.opengis.gml.SurfacePropertyType;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePeriodType;
import net.opengis.gml.TimePositionType;
import net.opengis.gml.impl.LineStringTypeImpl;
import net.opengis.gml.impl.PointTypeImpl;
import net.opengis.gml.impl.PolygonTypeImpl;
import net.opengis.om.x10.CategoryObservationType;
import net.opengis.om.x10.MeasurementType;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationPropertyType;
import net.opengis.om.x10.ObservationType;
import net.opengis.om.x10.ProcessPropertyType;
import net.opengis.sampling.x10.SamplingFeatureType;
import net.opengis.sampling.x10.SamplingPointType;
import net.opengis.sampling.x10.SamplingSurfaceType;
import net.opengis.swe.x101.DataArrayDocument;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.PhenomenonPropertyType;
import net.opengis.swe.x101.ScopedNameType;
import net.opengis.swe.x101.TimeObjectPropertyType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.CategoryResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.utils.UwGeometryUtils;
import org.uncertweb.utils.UwUrlConstants;
import org.uncertweb.utils.UwUrnConstants;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

public class OMDecoder {
	protected enum ValueType {
		TEXT, NUMERIC, BOOL, COUNT, CATEGORIAL, TIME;
	}

	protected static final String NO_DATA_VALUE = "noData";

	protected static final String[] FEATURE_OF_INTEREST_DEFINITION = { 
		UwUrlConstants.FEATURE_OF_INTEREST,
		UwUrnConstants.FEATURE_DEFINITION
	};
	
	protected static final String[] SAMPLING_TIME_DEFINITION = { 
		UwUrlConstants.SAMPLING_TIME, 
		UwUrnConstants.ISO8601_DEFINITION
	};
	
	protected static final String[] EPSG_PREFIXES = {
		"urn:ogc:def:crs:EPSG::",
		"urn:ogc:def:crs:EPSG:",
		"http://www.opengis.net/def/crs/EPSG/0/",
		"EPSG:"
	};
	
	private static final String DECIMAL = ".", TS = " ", CS = ",";
	
	protected static final URI DEFAULT_CODE_SPACE = URI.create(UwUrlConstants.INAPPLICABLE);
	
	protected static final Logger log = LoggerFactory.getLogger(OMDecoder.class);

	public IObservationCollection parse(InputStream is) throws OMParsingException {
		IObservationCollection col = new MeasurementCollection();
		try {
			XmlObject object = XmlObject.Factory.parse(is);
			
			if (object instanceof ObservationCollectionDocument) {
				
				ObservationCollectionDocument doc = (ObservationCollectionDocument) object;
				for (ObservationPropertyType opt : doc.getObservationCollection().getMemberArray()) {
					opt.getObservation();
				}
				
			} else  if (object instanceof ObservationDocument) {
				((ObservationDocument) object).getObservation();
			}
			
			ObservationType ot = null;
			
			return null;
			
		} catch (XmlException e) {
			throw new OMParsingException(e);
		} catch (IOException e) {
			throw new OMParsingException(e);
		}
	}
	
	public static Collection<AbstractObservation> parseMeasurement(
			MeasurementType xb_measType) throws OMParsingException {

		URI procID = parseProcedure(xb_measType.getProcedure());
		URI phenID = parseObservedProperty(xb_measType.getObservedProperty());
		TimeObject samplingTime = parseSamplingTime(xb_measType
				.getSamplingTime());
		SpatialSamplingFeature foi = parseFeature(xb_measType
				.getFeatureOfInterest());
		Identifier identifier = parseIdentifier(xb_measType);

		// result
		MeasureResult result = null;
		try {
			MeasureType xb_result = (MeasureType) xb_measType.getResult();
			String valueString = xb_result.getStringValue();
			String uom = xb_result.getUom();
			double value = Double.parseDouble(valueString);
			result = new MeasureResult(value, uom);
		} catch (Exception e) {
			throw new OMParsingException(
					"Error while parsing result value of Measurement in insertObservation request: "
							+ e.getMessage());
		}
		final AbstractObservation ao = new Measurement(identifier, null,
				samplingTime, samplingTime, null, procID, phenID, foi, null,
				result);
		return list(ao);
	}

	protected static URI parseObservedProperty(PhenomenonPropertyType xb_phen)
			throws OMParsingException {
		try {
			return new URI(xb_phen.getHref());
		} catch (URISyntaxException e) {
			throw new OMParsingException("ObservedProperty is not a valid URI",
					e);
		}
	}

	protected static URI parseProcedure(ProcessPropertyType xb_proc)
			throws OMParsingException {
		try {
			if (xb_proc == null || !xb_proc.isSetHref()
					|| xb_proc.getHref().trim().isEmpty()) {
				throw new OMParsingException("Procedure is not specified.");
			}
			return new URI(xb_proc.getHref());
		} catch (URISyntaxException e) {
			throw new OMParsingException("Procedure is not a valid URI", e);
		}
	}

	protected static TimeObject parseSamplingTime(
			TimeObjectPropertyType xb_samplingTime) throws OMParsingException {
		AbstractTimeObjectType xb_absTime = xb_samplingTime.getTimeObject();
		if (xb_absTime instanceof TimeInstantType) {
			return parseTimeInstant((TimeInstantType) xb_absTime);
		} else if (xb_absTime instanceof TimePeriodType) {
			return parseTimePeriod((TimePeriodType) xb_absTime);
		} else {
			throw new OMParsingException("Unknown SampingTime format: "
					+ xb_absTime.getClass().getName());
		}
	}

	protected static Identifier parseIdentifier(ObservationType xb_observation) {
		String id = xb_observation.getId();
		if (id != null && !id.trim().isEmpty()) {
			return new Identifier(DEFAULT_CODE_SPACE, id);
		}
		return null;
	}

	public static Collection<AbstractObservation> parseCategoryObservation(
			CategoryObservationType xb_catObsType) throws OMParsingException {

		URI procID = parseProcedure(xb_catObsType.getProcedure());
		URI phenID = parseObservedProperty(xb_catObsType.getObservedProperty());
		TimeObject samplingTime = parseSamplingTime(xb_catObsType
				.getSamplingTime());
		SpatialSamplingFeature foi = parseFeature(xb_catObsType
				.getFeatureOfInterest());
		Identifier identifier = parseIdentifier(xb_catObsType);

		ScopedNameType xb_result = (ScopedNameType) xb_catObsType.getResult();

		CategoryResult result = new CategoryResult(xb_result.getStringValue(),
				xb_result.getCodeSpace());

		final AbstractObservation ao = new CategoryObservation(identifier,
				null, samplingTime, samplingTime, null, procID, phenID, foi,
				null, result);

		return list(ao);
	}

	public static Collection<AbstractObservation> parseGenericObservation(
			ObservationType xb_obsType) throws OMParsingException {

		Collection<AbstractObservation> obs = collection();

		URI procId = parseProcedure(xb_obsType.getProcedure());

		// features of interest
		Map<String, SpatialSamplingFeature> foiMap = parseFeaturesOfInterest(xb_obsType);

		// result
		if (xb_obsType.getResult() == null) {
			throw new OMParsingException("Result is not specified.");
		}

		// so we have to do this in that stupid way...
		XmlCursor cResult = xb_obsType.getResult().newCursor();
		if (!cResult.toChild(new QName("http://www.opengis.net/swe/1.0.1",
				"DataArray"))) {
			throw new OMParsingException(
					"DataArray in Result is not specified.");
		}
		DataArrayDocument xb_dataArrayDoc = DataArrayDocument.Factory
				.newInstance();

		try {
			xb_dataArrayDoc = DataArrayDocument.Factory.parse(cResult
					.getDomNode());
		} catch (XmlException e) {
			throw new OMParsingException(e.getCause());
		}
		cResult.dispose();

		DataComponentPropertyType[] fieldArray = ((DataRecordType) xb_dataArrayDoc
				.getDataArray1().getElementType().getAbstractDataRecord())
				.getFieldArray();

		// evaluate the components
		int tstIdx = -1;
		int foiIdx = -1;

		int i = 0;
		Map<Integer, URI> phens = map();
		Map<URI, String> units4Phens = map();
		Map<URI, ValueType> valueTypes4Phens = map();

		for (DataComponentPropertyType s : fieldArray) {
			if (s.isSetBoolean()) {
				URI def;
				try {
					def = new URI(s.getBoolean().getDefinition());
				} catch (URISyntaxException e) {
					throw new OMParsingException(e);
				}
				phens.put(i, def);
				valueTypes4Phens.put(def, ValueType.BOOL);
			} else if (s.isSetCount()) {
				URI def;
				try {
					def = new URI(s.getCount().getDefinition());
				} catch (URISyntaxException e) {
					throw new OMParsingException(e);
				}
				phens.put(i, def);
				valueTypes4Phens.put(def, ValueType.COUNT);
			} else if (s.isSetQuantity()) {
				URI def;
				try {
					def = new URI(s.getQuantity().getDefinition());
				} catch (URISyntaxException e) {
					throw new OMParsingException(e);
				}
				phens.put(i, def);
				valueTypes4Phens.put(def, ValueType.NUMERIC);
				units4Phens.put(def, s.getQuantity().getUom().getCode());
			} else if (s.isSetTime()) {
				if (s.getName().equalsIgnoreCase("time")
						|| in(s.getTime().getDefinition(), SAMPLING_TIME_DEFINITION)) {
					tstIdx = i;
				} else {
					URI def;
					try {
						def = new URI(s.getTime().getDefinition());
					} catch (URISyntaxException e) {
						throw new OMParsingException(e);
					}
					valueTypes4Phens.put(def, ValueType.TIME);
					units4Phens.put(def, s.getTime().getUom().getCode());
				}
			} else if (s.isSetCategory()) {
				URI def;
				try {
					def = new URI(s.getCategory().getDefinition());
				} catch (URISyntaxException e) {
					throw new OMParsingException(e);
				}
				phens.put(i, def);
				valueTypes4Phens.put(def, ValueType.CATEGORIAL);
				units4Phens.put(def, s.getCategory().getCodeSpace().getHref());
			} else if (s.isSetText()) {
				if (s.getName().equalsIgnoreCase("feature")
						|| in(s.getText().getDefinition(),
								FEATURE_OF_INTEREST_DEFINITION)) {
					foiIdx = i;
				}
			}
			i++;
		}
		if (tstIdx < 0)
			throw new OMParsingException(
					"Time is not specified in SimpleDataRecord.");
		if (foiIdx < 0)
			throw new OMParsingException(
					"Feature is not specified in SimpleDataRecord.");

		// evaluate the separators of the SOS instance
		String tokenSepLocal = xb_dataArrayDoc.getDataArray1().getEncoding().getTextBlock().getTokenSeparator();
		String blockSepLocal = xb_dataArrayDoc.getDataArray1().getEncoding().getTextBlock().getBlockSeparator();
		
		String resultText = xb_dataArrayDoc.getDataArray1().getValues()
				.getDomNode().getFirstChild().getNodeValue();

		String[] tupels = resultText.trim().split(blockSepLocal);

		if (tupels.length == 0) {
			throw new OMParsingException("No values specified in DataRecord.");
		}

		for (String t : tupels) {
			String[] v = t.split(tokenSepLocal);

			if (v.length != phens.size() + 2) {
				throw new OMParsingException(
						"Invalid number of data values in tuple: " + t);
			}

			TimeObject to;
			try {
				to = new TimeObject(new DateTime(v[tstIdx].trim()));
			} catch (Exception e) {
				throw new OMParsingException("Invalid date format: "
						+ v[tstIdx].trim());
			}

			for (int j : phens.keySet()) {
				URI phenId = phens.get(j);
				SpatialSamplingFeature sap = foiMap.get(v[foiIdx]);
				if (sap == null) {
					throw new OMParsingException(
							"The Feature Of Interest id is not valid: "
									+ v[foiIdx]);
				}
				String phenValueType = valueTypes4Phens.get(phens.get(j))
						.name();
				if (phenValueType.equalsIgnoreCase(ValueType.NUMERIC.name())
						|| phenValueType.equalsIgnoreCase(ValueType.COUNT
								.name())) {
					double val = Double.NaN;
					if (v[j] != null && !v[j].equalsIgnoreCase(NO_DATA_VALUE)) {
						val = Double.parseDouble(v[j]);
					}
					String uom = units4Phens.get(phens.get(j));
					MeasureResult result = new MeasureResult(val, uom);
					obs.add(new Measurement(null, null, to, to, null, procId,
							phenId, sap, null, result));
				} else if (phenValueType.equalsIgnoreCase(ValueType.CATEGORIAL
						.name())) {
					String uom = units4Phens.get(phens.get(j));
					CategoryResult result = new CategoryResult(v[j], uom);
					obs.add(new CategoryObservation(null, null, to, to, null,
							procId, phenId, sap, null, result));
				} else if (phenValueType
						.equalsIgnoreCase(ValueType.BOOL.name())) {
					BooleanResult result = new BooleanResult(
							Boolean.valueOf(v[j]));
					obs.add(new BooleanObservation(null, null, to, to, null,
							procId, phenId, sap, null, result));
				}
			}
		}

		return obs;
	}

	protected static Map<String, SpatialSamplingFeature> parseFeaturesOfInterest(
			ObservationType xb_obsType) throws OMParsingException {
		if (xb_obsType.getFeatureOfInterest() == null
				|| xb_obsType.getFeatureOfInterest().getFeature() == null) {
			throw new OMParsingException("FeatureOfInterest is not specified.");
		}
		return parseFeatureCollection(xb_obsType.getFeatureOfInterest());
	}

	protected static Map<String, SpatialSamplingFeature> parseFeatureCollection(
			FeaturePropertyType xb_fpt) throws OMParsingException {
		AbstractFeatureType xb_aft = xb_fpt.getFeature();
		Map<String, SpatialSamplingFeature> foi_map = map();
		if (xb_aft instanceof AbstractFeatureCollectionType) {
			AbstractFeatureCollectionType xb_afct = (AbstractFeatureCollectionType) xb_aft;
			for (FeaturePropertyType xb_m : xb_afct.getFeatureMemberArray()) {
				SpatialSamplingFeature saf = parseFeature(xb_m);
				if (saf != null)
					foi_map.put(saf.getIdentifier().getIdentifier(), saf);
			}
		} else {
			SpatialSamplingFeature saf = parseFeature(xb_fpt);
			if (saf != null)
				foi_map.put(saf.getIdentifier().getIdentifier(), saf);
		}
		return foi_map;
	}

	protected static SpatialSamplingFeature parseFeature(
			FeaturePropertyType xb_fpt) throws OMParsingException {
		XmlObject object = null;

		try {
			object = XmlObject.Factory.parse(xb_fpt.toString());
		} catch (XmlException xmle) {
			throw new OMParsingException(
					"Error while parsing feature of interest: "
							+ xmle.getMessage());
		}
		if (object instanceof FeatureCollectionDocument) {
			Collection<SpatialSamplingFeature> features = parseFeatureCollection(
					xb_fpt).values();
			List<Geometry> geoms = list();
			for (SpatialSamplingFeature ssf : features) {
				geoms.add(ssf.getShape());
			}
			GeometryFactory factory = new GeometryFactory();
			Geometry geom = factory.createGeometryCollection(geoms
					.toArray(new Geometry[geoms.size()]));
			return new SpatialSamplingFeature(null, geom);
		} else {
			return parseSamplingFeature((SamplingFeatureType) object);
		}
	}

	protected static SpatialSamplingFeature parseSamplingFeature(
			SamplingFeatureType xb_sfType) throws OMParsingException {
		String id = xb_sfType.getId();
		try {

			if (id == null || id.trim().isEmpty()) {
				// get name
				CodeType[] xb_nameArray = xb_sfType.getNameArray();
				if (xb_nameArray.length >= 1) {
					id = xb_nameArray[0].getStringValue();
				}
			}

			// get sampled feature
			FeaturePropertyType[] xb_sampledFeatures = xb_sfType
					.getSampledFeatureArray();
			String sampledFeature = null;
			if (xb_sampledFeatures != null) {
				sampledFeature = xb_sampledFeatures[0].getHref();
			}

			Geometry geom = null;
			if (xb_sfType instanceof SamplingPointType) {
				geom = getGeometry4XmlGeometry(((SamplingPointType) xb_sfType)
						.getPosition().getPoint());
			} else if (xb_sfType instanceof SamplingSurfaceType) {
				SamplingSurfaceType xb_spType = (SamplingSurfaceType) xb_sfType;
				if (xb_spType.getShape() != null) {
					SurfacePropertyType xb_surfPropType = xb_spType.getShape();
					AbstractSurfaceType xb_agt = xb_surfPropType.getSurface();
					geom = getGeometry4XmlGeometry(xb_agt);
				}
			} else {
				throw new OMParsingException("Unknown SamplingFeatureType: "
						+ xb_sfType.getClass().getName());
			}
			return new SpatialSamplingFeature(new Identifier(
					DEFAULT_CODE_SPACE, id), sampledFeature, geom);
		} catch (OMParsingException se) {
			throw se;
		} catch (Exception e) {
			throw new OMParsingException(
					"Missing or wrong elements for Feature of Interest: "
							+ e.getMessage());
		}
	}

	protected static TimeObject parseTimeInstant(TimeInstantType tp) {
		TimeObject to = null;
		TimePositionType tpt = tp.getTimePosition();
		String timeString = tpt.getStringValue();
		if (timeString != null && !timeString.equals("")) {
			to = new TimeObject(timeString);
		}
		return to;
	}

	protected static TimeObject parseTimePeriod(TimePeriodType xb_timePeriod)
			throws OMParsingException {
		TimePositionType xb_beginPos = xb_timePeriod.getBeginPosition();
		TimePositionType xb_endPos = xb_timePeriod.getEndPosition();
		if (xb_beginPos == null) {
			throw new OMParsingException(
					"gml:TimePeriod! must contain beginPos Element with valid ISO:8601 String!!");
		}
		if (xb_endPos == null) {
			throw new OMParsingException(
					"gml:TimePeriod! must contain endPos Element with valid ISO:8601 String!!");
		}
		String beginString = xb_beginPos.getStringValue();
		String endString = xb_endPos.getStringValue();
		return new TimeObject(beginString, endString);
	}

	protected static Geometry getGeometry4XmlGeometry(
			AbstractGeometryType xb_geometry) throws OMParsingException {

		Geometry geometry = null;
		// parse srid; if not set, throw exception!

		// point
		if (xb_geometry instanceof PointTypeImpl) {
			PointType xb_point = (PointType) xb_geometry;
			geometry = getGeometry4Point(xb_point);
		} // LineString
		else if (xb_geometry instanceof LineStringTypeImpl) {
			LineStringType xb_lineString = (LineStringType) xb_geometry;
			geometry = getGeometry4LineString(xb_lineString);
		} // polygon
		else if (xb_geometry instanceof PolygonTypeImpl) {
			PolygonType xb_polygon = (PolygonType) xb_geometry;
			geometry = getGeometry4Polygon(xb_polygon);
		} // multi surface
		else if (xb_geometry instanceof CompositeSurfaceType) {
			CompositeSurfaceType xb_compositeSurface = (CompositeSurfaceType) xb_geometry;
			geometry = getGeometry4CompositeSurface(xb_compositeSurface);
		} else {
			throw new OMParsingException("The FeatureType: " + xb_geometry
					+ " is not supportted! Only PointType and PolygonType");
		}

		if (UwGeometryUtils.switchCoordinatesForEPSG(geometry.getSRID())) {
			geometry = UwGeometryUtils.switchCoordinate4Geometry(geometry);
		}

		return geometry;
	}// end getGeometry4XmlGeometry

	protected static Geometry getGeometry4CompositeSurface(
			CompositeSurfaceType xb_compositeSurface) throws OMParsingException {
		SurfacePropertyType[] xb_surfaceProperties = xb_compositeSurface
				.getSurfaceMemberArray();
		int srid = -1;
		List<Polygon> polygons = list();
		if (xb_compositeSurface.getSrsName() != null) {
			srid = parseSrsName(xb_compositeSurface.getSrsName());
		}
		for (SurfacePropertyType xb_surfaceProperty : xb_surfaceProperties) {
			AbstractSurfaceType xb_abstractSurface = xb_surfaceProperty
					.getSurface();
			if (srid == -1 && xb_abstractSurface.getSrsName() != null) {
				srid = parseSrsName(xb_abstractSurface.getSrsName());
			}
			if (xb_abstractSurface instanceof PolygonType) {
				polygons.add((Polygon) getGeometry4Polygon((PolygonType) xb_abstractSurface));
			} else {
				throw new OMParsingException("The FeatureType: "
						+ xb_abstractSurface
						+ " is not supportted! Only PolygonType");

			}
		}
		if (polygons.isEmpty()) {
			throw new OMParsingException("The FeatureType: "
					+ xb_compositeSurface + " does not contain any member!");
		}
		if (srid == -1) {
			throw new OMParsingException("No SrsName ist specified!");
		}
		GeometryFactory factory = new GeometryFactory();
		Geometry geom = factory.createMultiPolygon(polygons
				.toArray(new Polygon[polygons.size()]));
		geom.setSRID(srid);
		return geom;
	}

	protected static Geometry getGeometry4Point(PointType xb_pointType)
			throws OMParsingException {

		Geometry geom = null;
		String geomWKT = null;
		int srid = -1;
		if (xb_pointType.getSrsName() != null) {
			srid = parseSrsName(xb_pointType.getSrsName());
		}

		if (xb_pointType.getPos() != null) {
			DirectPositionType xb_pos = xb_pointType.getPos();
			if (srid == -1 && xb_pos.getSrsName() != null) {
				srid = parseSrsName(xb_pos.getSrsName());
			}
			String directPosition = getString4Pos(xb_pos);
			geomWKT = "POINT(" + directPosition + ")";
		} else if (xb_pointType.getCoordinates() != null) {
			CoordinatesType xb_coords = xb_pointType.getCoordinates();
			String directPosition = getString4Coordinates(xb_coords);
			geomWKT = "POINT" + directPosition;
		} else {
			throw new OMParsingException(
					"For geometry type 'gml:Point' only element 'gml:pos' and 'gml:coordinates' are allowed "
							+ "in the feature of interest parameter!");

		}

		if (srid == -1) {
			throw new OMParsingException("No SrsName ist specified!");

		}
		geom = createGeometryFromWKT(geomWKT);
		geom.setSRID(srid);

		return geom;
	}

	protected static Geometry getGeometry4LineString(
			LineStringType xb_lineSringType) throws OMParsingException {

		Geometry geom = null;
		String geomWKT = null;
		int srid = -1;
		if (xb_lineSringType.getSrsName() != null) {
			srid = parseSrsName(xb_lineSringType.getSrsName());
		}

		DirectPositionType[] xb_positions = xb_lineSringType.getPosArray();

		StringBuilder positions = new StringBuilder();
		if (xb_positions != null && xb_positions.length > 0) {
			if (srid == -1 && xb_positions[0].getSrsName() != null
					&& !(xb_positions[0].getSrsName().equals(""))) {
				srid = parseSrsName(xb_positions[0].getSrsName());
			}
			positions
					.append(getString4PosArray(xb_lineSringType.getPosArray()));
		}
		geomWKT = "LINESTRING" + positions.toString() + "";

		if (srid == -1) {
			throw new OMParsingException("No SrsName ist specified!");
		}

		geom = createGeometryFromWKT(geomWKT);
		geom.setSRID(srid);

		return geom;
	}

	protected static Geometry getGeometry4Polygon(PolygonType xb_polygonType)
			throws OMParsingException {
		Geometry geom = null;
		int srid = -1;
		if (xb_polygonType.getSrsName() != null) {
			srid = parseSrsName(xb_polygonType.getSrsName());
		}
		String exteriorCoordString = null;
		StringBuilder geomWKT = new StringBuilder();
		StringBuilder interiorCoordString = new StringBuilder();

		AbstractRingPropertyType xb_exterior = xb_polygonType.getExterior();

		if (xb_exterior != null) {
			AbstractRingType xb_exteriorRing = xb_exterior.getRing();
			if (xb_exteriorRing instanceof LinearRingType) {
				LinearRingType xb_linearRing = (LinearRingType) xb_exteriorRing;
				if (srid == -1 && xb_linearRing.getSrsName() != null) {
					srid = parseSrsName(xb_linearRing.getSrsName());
				}
				exteriorCoordString = getCoordString4LinearRing(xb_linearRing);
			} else {
				throw new OMParsingException(
						"The Polygon must contain the following elements <gml:exterior><gml:LinearRing><gml:posList>!");
			}
		}

		AbstractRingPropertyType[] xb_interior = xb_polygonType
				.getInteriorArray();
		AbstractRingPropertyType xb_interiorRing;
		if (xb_interior != null && xb_interior.length != 0) {
			for (int i = 0; i < xb_interior.length; i++) {
				xb_interiorRing = xb_interior[i];
				if (xb_interiorRing instanceof LinearRingType) {
					interiorCoordString
							.append(", "
									+ getCoordString4LinearRing((LinearRingType) xb_interiorRing));
				}
			}
		}

		geomWKT.append("POLYGON(");
		geomWKT.append(exteriorCoordString);
		if (interiorCoordString != null) {
			geomWKT.append(interiorCoordString);
		}
		geomWKT.append(")");

		if (srid == 0) {
			throw new OMParsingException("No SrsName ist specified!");
		}
		geom = createGeometryFromWKT(geomWKT.toString());
		geom.setSRID(srid);

		return geom;
	}

	protected static int parseSrsName(String srsName) throws OMParsingException {
		int srid = Integer.MIN_VALUE;
		if (!(srsName == null || srsName.equals(""))) {
			try {
				for (String pre : EPSG_PREFIXES) {
					if (srsName.startsWith(pre)) {
						srsName.replace(pre, "");
						break;
					}
				}
				srid = Integer.valueOf(srsName).intValue();
			} catch (Exception e) {
				throw new OMParsingException(
						"For geometry of the feature of interest parameter has to have a srsName attribute, which contains the Srs Name as EPSGcode following the following schema:"
								+ EPSG_PREFIXES + "number!");
			}
		} else {
			throw new OMParsingException(
					"For geometry of the feature of interest parameter has to have a srsName attribute, which contains the Srs Name as EPSGcode following the following schema:"
							+ EPSG_PREFIXES + "number!");

		}
		return srid;
	}

	protected static String getCoordString4LinearRing(
			LinearRingType xb_linearRing) throws OMParsingException {

		String result = "";
		DirectPositionListType xb_posList = xb_linearRing.getPosList();
		CoordinatesType xb_coordinates = xb_linearRing.getCoordinates();
		DirectPositionType[] xb_posArray = xb_linearRing.getPosArray();
		if (xb_posList != null && !(xb_posList.getStringValue().equals(""))) {
			result = getString4PosList(xb_posList);
		} else if (xb_coordinates != null
				&& !(xb_coordinates.getStringValue().equals(""))) {
			result = getString4Coordinates(xb_coordinates);
		} else if (xb_posArray != null && xb_posArray.length > 0) {
			result = getString4PosArray(xb_posArray);
		} else {
			throw new OMParsingException(
					"The Polygon must contain the following elements <gml:exterior><gml:LinearRing><gml:posList>, <gml:exterior><gml:LinearRing><gml:coordinates> or <gml:exterior><gml:LinearRing><gml:pos>{<gml:pos>}!");
		}

		return result;
	}

	protected static String getString4Pos(DirectPositionType xb_pos) {
		StringBuilder coordinateString = new StringBuilder();

		coordinateString.append(xb_pos.getStringValue());

		return coordinateString.toString();
	}

	protected static String getString4PosArray(DirectPositionType[] xb_posArray) {
		StringBuilder coordinateString = new StringBuilder();
		coordinateString.append("(");
		for (DirectPositionType directPositionType : xb_posArray) {
			coordinateString.append(directPositionType.getStringValue());
			coordinateString.append(", ");
		}
		coordinateString.append(xb_posArray[0].getStringValue());
		coordinateString.append(")");

		return coordinateString.toString();
	}

	protected static String getString4PosList(DirectPositionListType xb_posList)
			throws OMParsingException {
		StringBuilder coordinateString = new StringBuilder("(");
		List<?> values = xb_posList.getListValue();
		if ((values.size() % 2) != 0) {
			throw new OMParsingException(
					"The Polygons posList must contain pairs of coordinates!");
		} else {
			for (int i = 0; i < values.size(); i++) {
				coordinateString.append(values.get(i));
				if ((i % 2) != 0) {
					coordinateString.append(", ");
				} else {
					coordinateString.append(" ");
				}
			}
		}
		int length = coordinateString.length();
		coordinateString.delete(length - 2, length);
		coordinateString.append(")");

		return coordinateString.toString();
	}

	/**
	 * parses XmlBeans Coordinates to a String with coordinates for WKT.
	 * Replaces cs, decimal and ts if different from default.
	 * 
	 * @param xb_coordinates
	 *            XmlBeans generated Coordinates.
	 * @return Returns String with coordinates for WKT.
	 */
	protected static String getString4Coordinates(CoordinatesType xb_coordinates) {
		String coordinateString = "";

		coordinateString = "(" + xb_coordinates.getStringValue() + ")";

		// replace cs, decimal and ts if different from default.
		if (!xb_coordinates.getCs().equals(CS)) {
			coordinateString = coordinateString.replace(xb_coordinates.getCs(),
					CS);
		}
		if (!xb_coordinates.getDecimal().equals(DECIMAL)) {
			coordinateString = coordinateString.replace(
					xb_coordinates.getDecimal(), DECIMAL);
		}
		if (!xb_coordinates.getTs().equals(TS)) {
			coordinateString = coordinateString.replace(xb_coordinates.getTs(),
					TS);
		}

		return coordinateString;
	}

	protected static Geometry createGeometryFromWKT(String wktString)
			throws OMParsingException {
		WKTReader wktReader = new WKTReader();
		Geometry geom = null;
		try {
			log.debug("FOI Geometry: {}", wktString);
			geom = wktReader.read(wktString);
		} catch (com.vividsolutions.jts.io.ParseException pe) {
			throw new OMParsingException(
					"An error occurred, while parsing the foi parameter: "
							+ pe.getMessage());
		}

		return geom;
	}



}
