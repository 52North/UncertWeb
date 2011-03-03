package org.uncertweb.sta.wps.xml.io.enc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractSurfaceType;
import net.opengis.gml.AbstractTimeObjectType;
import net.opengis.gml.CompositeSurfaceType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.MeasureType;
import net.opengis.gml.MetaDataPropertyType;
import net.opengis.gml.PolygonType;
import net.opengis.gml.SurfacePropertyType;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePeriodType;
import net.opengis.om.x10.MeasurementType;
import net.opengis.om.x10.ObservationDocument;
import net.opengis.sampling.x10.SamplingPointType;
import net.opengis.sampling.x10.SamplingSurfaceType;

import org.apache.xmlbeans.XmlCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.intamap.om.SamplingPoint;
import org.uncertweb.intamap.om.SamplingSurface;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.om.OriginAwareObservation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ObservationGenerator {

	protected static final Logger log = LoggerFactory.getLogger(ObservationGenerator.class);
	
	public ObservationDocument generateXML(Observation o) {
		ObservationDocument doc = ObservationDocument.Factory.newInstance();
		
		MeasurementType m = (MeasurementType) doc.addNewObservation()
				.substitute(new QName(Namespace.OM.URI, "Measurement"),
						MeasurementType.type);
		if (o.getId() != null)
			m.setId(o.getId());
		generateProcedure(o, m);
		generateMetaData(o, m);
		generateObservedProperty(o, m);
		generateResult(o, m);
		generateResultTime(o, m);
		generateFeatureOfInterest(o, m);
		return doc;
	}
	

	protected void generateMetaData(Observation o, MeasurementType m) {
		if (!(o instanceof OriginAwareObservation)) return;
		OriginAwareObservation oao = (OriginAwareObservation) o;
		LinkedList<String> obsIds = new LinkedList<String>();
		for (Observation so : oao.getSourceObservations()) {
			obsIds.add(so.getId());
		}
		MetaDataPropertyType md = m.addNewMetaDataProperty();
		md.setTitle("Provenance");
		md.setRole("Provenance");
		md.setHref(getObservationByIdUrl(oao.getSourceUrl(), obsIds));
	}
	
	public static String getObservationByIdUrl(String url, List<String> observationIds) {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put(Constants.Sos.Parameter.REQUEST, Constants.Sos.Operation.GET_OBSERVATION_BY_ID);
		props.put(Constants.Sos.Parameter.SERVICE, Constants.Sos.SERVICE_NAME);
		props.put(Constants.Sos.Parameter.VERSION, Constants.Sos.SERVICE_VERSION);
		props.put(Constants.Sos.Parameter.OUTPUT_FORMAT, Constants.Sos.SENSOR_OUTPUT_FORMAT);
		StringBuilder sb = new StringBuilder();
		int size = observationIds.size(), i = 1;
		for (String s : observationIds) {
			sb.append(s.trim());
			if (size != i) {
				sb.append(",");
			}
			i++;
		}
		props.put(Constants.Sos.Parameter.OBSERVATION_ID, sb.toString());
		return Utils.buildGetRequest(url, props);
	}

	protected static void generateFeatureOfInterest(Observation o,
			MeasurementType m) {
		ISamplingFeature f = o.getFeatureOfInterest();
		if (f != null) {
			FeaturePropertyType fpt = m.addNewFeatureOfInterest();
			if (f instanceof SamplingPoint) {
				SamplingPointType spt = SamplingPointType.Factory.newInstance(); 
				SamplingPoint sp = (SamplingPoint) f;

				if (sp.getName() != null) { 
					spt.addNewName().setStringValue(sp.getName());
				} 
				if (sp.getId() != null) { 
					spt.setId(sp.getId());
				}
				
				if (sp.getSampledFeature() != null) {
					spt.addNewSampledFeature().setHref(sp.getSampledFeature());
				} else {
					spt.addNewSampledFeature().setHref(Constants.NULL_URN);
				}

				DirectPositionType dpt = spt.addNewPosition().addNewPoint()
						.addNewPos();
				dpt.setSrsName(Constants.URN_EPSG_SRS_PREFIX + sp.getLocation().getSRID());
				
				if (Utils.switchCoordinates(sp.getLocation().getSRID())) {
					dpt.setStringValue(sp.getLocation().getCoordinate().x + " " + sp.getLocation().getCoordinate().y);
				} else {
					dpt.setStringValue(sp.getLocation().getCoordinate().y + " " + sp.getLocation().getCoordinate().x);
					
				}

				fpt.addNewFeature().set(spt);
				XmlCursor c = fpt.newCursor();
				c.toChild(new QName(Namespace.GML.URI, "_Feature"));
				c.setName(new QName(Namespace.SA.URI, "SamplingPoint"));
				c.dispose();

			} else if (f instanceof SamplingSurface) {
				SamplingSurfaceType sst = (SamplingSurfaceType) fpt
						.addNewFeature().changeType(SamplingSurfaceType.type);
				SamplingSurface ss = (SamplingSurface) f;
				if (ss.getName() != null) {
					sst.addNewName().setStringValue(ss.getName());
				}
				if (ss.getId() != null) {
					sst.setId(ss.getId());
				}
				if (ss.getSampledFeature() != null) {
					sst.addNewSampledFeature().setHref(ss.getSampledFeature());
				} else {
					sst.addNewSampledFeature().setHref(Constants.NULL_URN);
				}
				generateShape(ss.getLocation(), sst);

				XmlCursor c = fpt.newCursor();
				c.toChild(new QName(Namespace.GML.URI, "_Feature"));
				c.setName(new QName(Namespace.SA.URI, "SamplingSurface"));
				c.dispose();

			}
		}
	}

	protected static void generateShape(Geometry geom, SamplingSurfaceType sst) {
		
		if (geom instanceof Polygon) {
			
			generatePolygon((Polygon) geom, sst.addNewShape().addNewSurface());
	
		} else if (geom instanceof MultiPolygon) {
			
			if (geom.getNumGeometries() == 1) {
				generateShape(geom.getGeometryN(0), sst);
				return;
			}
			
			/* TODO currently not supported by 52N SOS */
			log.warn("SOS does not support CompositeSurfaces; creating only the biggest Polygon.");
			double area = -1;
			Geometry biggest = null;
			for (int i = 0; i < geom.getNumGeometries(); i++) {
				double a = geom.getGeometryN(i).getArea();
				if (a > area || biggest == null) {
					area = a;
					biggest = geom.getGeometryN(i);
					log.info("Area: {}", a);
				}
			}
			generateShape(biggest, sst);

//			generateMultiPolygon((MultiPolygon) geom, sst.addNewShape());

		} else {
			throw new RuntimeException("Not yet implemented: " + geom.getClass());
		}
	}
	
	protected static void generateMultiPolygon(MultiPolygon mp, SurfacePropertyType spt) {
		CompositeSurfaceType cst = (CompositeSurfaceType) spt.addNewSurface()
				.changeType(CompositeSurfaceType.type);
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			SurfacePropertyType spm = cst.addNewSurfaceMember();
			generatePolygon((Polygon) mp.getGeometryN(i), spm.addNewSurface());
		}
		XmlCursor c = spt.newCursor();
		c.toChild(new QName(Namespace.GML.URI, "_Surface"));
		c.setName(new QName(Namespace.GML.URI, "CompositeSurface"));
		c.dispose();
	}

	protected static void generatePolygon(Polygon p, AbstractSurfaceType ast) {
		
		PolygonType pt = (PolygonType) ast.substitute(new QName(Namespace.GML.URI, "Polygon"), PolygonType.type);
		
		int srs = p.getSRID();
		if (srs == 0) {
			//TODO GeoTools parser does not set SRID
			log.warn("SRID not set on Polygon. Defaulting to '4326'");
			srs = 4326;
		}
		pt.setSrsName(Constants.URN_EPSG_SRS_PREFIX	+ srs);
		
		LinearRingType outer = (LinearRingType) pt.addNewExterior().addNewRing()
				.substitute(new QName(Namespace.GML.URI, "LinearRing"), LinearRingType.type);
		outer.addNewCoordinates().setStringValue(generateCoordinates(srs, p.getExteriorRing().getCoordinates()));
		
		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			LinearRingType inner = (LinearRingType) pt.addNewInterior().addNewRing()
					.substitute(new QName(Namespace.GML.URI, "LinearRing"), LinearRingType.type);
			inner.addNewCoordinates().setStringValue(generateCoordinates(srs, p.getInteriorRingN(i).getCoordinates()));
		}
	}
	
	protected static String generateCoordinates(int epsg, Coordinate[] c) {
		StringBuilder buf = new StringBuilder();
		boolean swap = Utils.switchCoordinates(epsg);
		for (int i = 0; i < c.length; i++) {
			if (swap) {
				buf.append(Utils.NUMBER_FORMAT.format(c[i].x)).append(" ").append(Utils.NUMBER_FORMAT.format(c[i].y));
			} else {
				buf.append(Utils.NUMBER_FORMAT.format(c[i].y)).append(" ").append(Utils.NUMBER_FORMAT.format(c[i].x));
			}
			if (i < c.length-1) buf.append(",");
		}
		return buf.toString();
	}

	protected static void generateProcedure(Observation o, MeasurementType m) {
		if (o.getSensorModel() != null) {
			m.addNewProcedure().setHref(o.getSensorModel());
		}
	}

	protected static void generateObservedProperty(Observation o,
			MeasurementType m) {
		if (o.getObservedProperty() != null) {
			m.addNewObservedProperty().setHref(o.getObservedProperty());
		}
	}

	protected static void generateResult(Observation o, MeasurementType m) {
		if (!new Double(o.getResult()).equals(Double.NaN)) {
			MeasureType xbresult = MeasureType.Factory.newInstance();
			xbresult.setStringValue(Utils.NUMBER_FORMAT.format(o.getResult()));
			if (o.getUom() != null) {
				xbresult.setUom(o.getUom());
			}
			m.addNewResult().set(xbresult);
		} else {
			m.addNewResult().setNil();
		}
	}

	protected static void generateResultTime(Observation o, MeasurementType m) {
		if (o.getObservationTime() != null) {
//			log.debug("Got ObservationTime: {}: {}",o.getObservationTime().getClass().getName(), o.getObservationTime());
			AbstractTimeObjectType atot = m.addNewSamplingTime().addNewTimeObject();
			if (o.getObservationTime() instanceof ObservationTimeInterval) {
				TimePeriodType tpt = (TimePeriodType) atot.substitute(
						new QName(Namespace.GML.URI, "TimePeriod"),
						TimePeriodType.type);
				ObservationTimeInterval time = (ObservationTimeInterval) o.getObservationTime();
				tpt.addNewBeginPosition().setStringValue(TimeUtils.format(time.getStart()));
				tpt.addNewEndPosition().setStringValue(TimeUtils.format(time.getEnd()));
			} if (o.getObservationTime() instanceof ObservationTimeInstant) {
				TimeInstantType tit = (TimeInstantType) atot.substitute(
						new QName(Namespace.GML.URI, "TimeInstant"),
						TimeInstantType.type);
				tit.addNewTimePosition().setStringValue(TimeUtils.format(
						((ObservationTimeInstant) o.getObservationTime()).getDateTime()));
			}
		} else {
			throw new NullPointerException("ObservationTime is null.");
		}
	}

}
