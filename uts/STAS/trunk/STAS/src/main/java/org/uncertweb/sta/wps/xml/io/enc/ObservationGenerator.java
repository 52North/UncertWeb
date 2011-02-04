package org.uncertweb.sta.wps.xml.io.enc;

import java.util.LinkedList;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractTimeObjectType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.MeasureType;
import net.opengis.gml.MetaDataPropertyType;
import net.opengis.gml.PolygonType;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePeriodType;
import net.opengis.om.x10.MeasurementType;
import net.opengis.om.x10.ObservationDocument;
import net.opengis.sampling.x10.SamplingPointType;
import net.opengis.sampling.x10.SamplingSurfaceType;

import org.apache.xmlbeans.XmlCursor;
import org.uncertweb.intamap.om.ISamplingFeature;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationTimeInstant;
import org.uncertweb.intamap.om.ObservationTimeInterval;
import org.uncertweb.sta.wps.OriginAwareObservation;
import org.uncertweb.intamap.om.SamplingPoint;
import org.uncertweb.intamap.om.SamplingSurface;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class ObservationGenerator {

	public ObservationDocument generateXML(Observation o) {
		ObservationDocument doc = ObservationDocument.Factory.newInstance();
		
		MeasurementType m = (MeasurementType) doc.addNewObservation()
				.substitute(new QName(Namespace.OM.URI, "Measurement"),
						MeasurementType.type);
		if (o.getId()!=null)
			m.setId(o.getId());
		generateProcedure(o, m);
		generateMetaData(o, m);
		generateObservedProperty(o, m);
		generateResult(o, m);
		generateResultTime(o, m);
		generateFeatureOfInterest(o, m);
		return doc;
	}
	

	private void generateMetaData(Observation o, MeasurementType m) {
		if (!(o instanceof OriginAwareObservation)) return;
		OriginAwareObservation oao = (OriginAwareObservation) o;
		LinkedList<String> obsIds = new LinkedList<String>();
		for (Observation so : oao.getSourceObservations()) {
			obsIds.add(so.getId());
		}
		MetaDataPropertyType md = m.addNewMetaDataProperty();
		md.setTitle("Provenance");
//		md.setRole("Provenance"); FIXME which one?
		md.setHref(Utils.getObservationByIdUrl(oao.getSourceUrl(), obsIds));
	}

	private static void generateFeatureOfInterest(Observation o,
			MeasurementType m) {
		ISamplingFeature f = o.getFeatureOfInterest();
		if (f != null) {
			FeaturePropertyType fpt = m.addNewFeatureOfInterest();
			if (f instanceof SamplingPoint) {
				SamplingPointType spt = (SamplingPointType) fpt.addNewFeature()
						.changeType(SamplingPointType.type);
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
				dpt.setSrsName(Constants.URN_EPSG_SRS_PREFIX
						+ sp.getLocation().getSRID());
				dpt.setStringValue(sp.getLocation().getCoordinate().x + " "
						+ sp.getLocation().getCoordinate().y);

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
					sst.addNewSampledFeature().setHref("");
				}
				generateShape(ss.getLocation(), sst);

				XmlCursor c = fpt.newCursor();
				c.toChild(new QName(Namespace.GML.URI, "_Feature"));
				c.setName(new QName(Namespace.SA.URI, "SamplingSurface"));
				c.dispose();

			}
		}
	}

	private static void generateShape(Geometry geom, SamplingSurfaceType sst) {
		if (geom instanceof Polygon) {
			PolygonType pt = (PolygonType) sst
					.addNewShape()
					.addNewSurface()
					.substitute(new QName(Namespace.GML.URI, "Polygon"),
							PolygonType.type);
			Polygon p = (Polygon) geom;
			pt.setSrsName(Constants.URN_EPSG_SRS_PREFIX
					+ String.valueOf(p.getSRID()));
			((LinearRingType) pt
					.addNewExterior()
					.addNewRing()
					.substitute(new QName(Namespace.GML.URI, "LinearRing"),
							LinearRingType.type)).addNewCoordinates()
					.setStringValue(
							generateCoordinates(p.getExteriorRing()
									.getCoordinates()));
			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				((LinearRingType) pt
						.addNewInterior()
						.addNewRing()
						.substitute(
								new QName(Namespace.GML.URI, "LinearRing"),
								LinearRingType.type)).addNewCoordinates()
						.setStringValue(
								generateCoordinates(p.getInteriorRingN(i)
										.getCoordinates()));
			}
		} else {
			// TODO Multipolygon
			throw new RuntimeException(
					"Surfaces other than a basic polygon are not yet implemented.");
		}
	}

	private static String generateCoordinates(Coordinate[] c) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < c.length; i++) {
			buf.append(Utils.NUMBER_FORMAT.format(c[i].x)).append(" ")
					.append(Utils.NUMBER_FORMAT.format(c[i].y)).append(" ");
		}
		return buf.toString();
	}

	private static void generateProcedure(Observation o, MeasurementType m) {
		if (o.getSensorModel() != null) {
			m.addNewProcedure().setHref(o.getSensorModel());
		}
	}

	private static void generateObservedProperty(Observation o,
			MeasurementType m) {
		if (o.getObservedProperty() != null) {
			m.addNewObservedProperty().setHref(o.getObservedProperty());
		}
	}

	private static void generateResult(Observation o, MeasurementType m) {
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

	private static void generateResultTime(Observation o, MeasurementType m) {
		
		
		if (o.getObservationTime() != null) {
			
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
		}
	}

}
