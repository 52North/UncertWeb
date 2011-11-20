/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps.xml.io.enc;

import static org.uncertweb.utils.UwXmlUtils.Namespace.GML;
import static org.uncertweb.utils.UwXmlUtils.Namespace.OM;
import static org.uncertweb.utils.UwXmlUtils.Namespace.SA;
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
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.om.Origin;
import org.uncertweb.utils.UwConstants;
import org.uncertweb.utils.UwGeometryUtils;
import org.uncertweb.utils.UwTimeUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Generator for {@link ObservationDocument}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationGenerator {

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(ObservationGenerator.class);

	/**
	 * Generates a {@link ObservationDocument}.
	 * 
	 * @param o the {@code Observation}
	 */
	public ObservationDocument generateXML(AbstractObservation o) {
		ObservationDocument doc = ObservationDocument.Factory.newInstance();

		MeasurementType m = (MeasurementType) doc.addNewObservation()
				.substitute(OM.q("Measurement"), MeasurementType.type);
		if (o.getIdentifier().getIdentifier() != null)
			m.setId(o.getIdentifier().getIdentifier());
		generateProcedure(o, m);
		Origin origin = (Origin) o.getParameter(Constants.OBSERVATION_PARAMETER_AGGREGATED_OF);
		if (origin != null)
			generateMetaData(origin, m);
		generateObservedProperty(o, m);
		generateResult(o, m);
		generateResultTime(o, m);
		generateFeatureOfInterest(o, m);
		return doc;
	}

	/**
	 * Generates the Metadata.
	 * 
	 * @param o the {@code Observation}
	 * @param m the XmlBean within the Metadata is generated.
	 */
	protected void generateMetaData(Origin o, MeasurementType m) {
		MetaDataPropertyType md = m.addNewMetaDataProperty();
		md.setTitle("Provenance");
		md.setRole("Provenance");
		md.setHref(Utils.getObservationByIdUrl(o.getSourceUrl(), o.getSourceObservations()));
	}

	/**
	 * Generates the FeatureOfInterest.
	 * 
	 * @param o the {@code Observation}
	 * @param m the XmlBean within the FeatureOfInterest is generated.
	 */
	protected static void generateFeatureOfInterest(AbstractObservation o,
			MeasurementType m) {
		SpatialSamplingFeature f = o.getFeatureOfInterest();
		if (f != null) {
			FeaturePropertyType fpt = m.addNewFeatureOfInterest();
			if (f.getShape() instanceof Point) {
				SamplingPointType spt = SamplingPointType.Factory.newInstance();

				if (f.getIdentifier() != null) {
					spt.addNewName().setStringValue(f.getIdentifier().getIdentifier());
					spt.setId(f.getIdentifier().getIdentifier());
				}

				if (f.getSampledFeature() != null) {
					spt.addNewSampledFeature().setHref(f.getSampledFeature());
				} else {
					spt.addNewSampledFeature().setHref(UwConstants.URN.NULL.value);
				}

				DirectPositionType dpt = spt.addNewPosition().addNewPoint()
						.addNewPos();
				dpt.setSrsName(UwConstants.URN.EPSG_SRS_PREFIX.value
						+ f.getShape().getSRID());

				if (UwGeometryUtils.switchCoordinatesForEPSG(f.getShape().getSRID())) {
					dpt.setStringValue(f.getShape().getCoordinate().x + " "
							+ f.getShape().getCoordinate().y);
				} else {
					dpt.setStringValue(f.getShape().getCoordinate().y + " "
							+ f.getShape().getCoordinate().x);

				}

				fpt.addNewFeature().set(spt);
				XmlCursor c = fpt.newCursor();
				c.toChild(GML.q("_Feature"));
				c.setName(SA.q("SamplingPoint"));
				c.dispose();

			} else if (f.getShape() instanceof Polygon || f.getShape() instanceof MultiPolygon) {
				SamplingSurfaceType sst = (SamplingSurfaceType) fpt
						.addNewFeature().changeType(SamplingSurfaceType.type);
				
				
				if (f.getIdentifier() != null) {
					sst.addNewName().setStringValue(f.getIdentifier().getIdentifier());
					sst.setId(f.getIdentifier().getIdentifier());
				}
				if (f.getSampledFeature() != null) {
					sst.addNewSampledFeature().setHref(f.getSampledFeature());
				} else {
					sst.addNewSampledFeature().setHref(UwConstants.URN.NULL.value);
				}
				generateShape(f.getShape(), sst);

				XmlCursor c = fpt.newCursor();
				c.toChild(GML.q("_Feature"));
				c.setName(SA.q("SamplingSurface"));
				c.dispose();

			}
		}
	}

	/**
	 * Generates a sampling surface. Currently {@link Polygon} and
	 * {@link MultiPolygon} are supported.
	 * 
	 * @param geom the {@code Geometry}
	 * @param sst the XmlBean in which the surface will generated
	 */
	protected static void generateShape(Geometry geom, SamplingSurfaceType sst) {
		if (geom instanceof Polygon) {
			generatePolygon((Polygon) geom, sst.addNewShape().addNewSurface());
		} else if (geom instanceof MultiPolygon) {
			if (geom.getNumGeometries() == 1) {
				generateShape(geom.getGeometryN(0), sst);
			}
			generateMultiPolygon((MultiPolygon) geom, sst.addNewShape());
		} else {
			throw new RuntimeException("Not yet implemented: "
					+ geom.getClass());
		}
	}

	/**
	 * Generates the {@code MultiPolygon} (encoded as an
	 * {@code CompositeSurfaceType}).
	 * 
	 * @param p the {@code MultiPolygon}
	 * @param spt the XmlBean which will be substituted
	 */
	protected static void generateMultiPolygon(MultiPolygon mp,
			SurfacePropertyType spt) {
		CompositeSurfaceType cst = (CompositeSurfaceType) spt.addNewSurface()
				.changeType(CompositeSurfaceType.type);
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			SurfacePropertyType spm = cst.addNewSurfaceMember();
			generatePolygon((Polygon) mp.getGeometryN(i), spm.addNewSurface());
		}
		XmlCursor c = spt.newCursor();
		c.toChild(GML.q("_Surface"));
		c.setName(GML.q("CompositeSurface"));
		c.dispose();
	}

	/**
	 * Generates the Polygon.
	 * 
	 * @param p the {@code Polygon}
	 * @param ast the XmlBean which will be substituted
	 */
	protected static void generatePolygon(Polygon p, AbstractSurfaceType ast) {

		PolygonType pt = (PolygonType) ast
				.substitute(GML.q("Polygon"), PolygonType.type);

		int srs = p.getSRID();
		if (srs == 0) {
			// TODO GeoTools parser does not set SRID
			log.warn("SRID not set on Polygon. Defaulting to '4326'");
			srs = 4326;
		}
		pt.setSrsName(UwConstants.URN.EPSG_SRS_PREFIX.value + srs);

		LinearRingType outer = (LinearRingType) pt.addNewExterior()
				.addNewRing()
				.substitute(GML.q("LinearRing"), LinearRingType.type);
		outer.addNewCoordinates().setStringValue(generateCoordinates(srs, p
				.getExteriorRing().getCoordinates()));

		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			LinearRingType inner = (LinearRingType) pt.addNewInterior()
					.addNewRing()
					.substitute(GML.q("LinearRing"), LinearRingType.type);
			inner.addNewCoordinates().setStringValue(generateCoordinates(srs, p
					.getInteriorRingN(i).getCoordinates()));
		}
	}

	/**
	 * Generates a coordinate string.
	 * 
	 * @param epsg the EPSG id of the SRS
	 * @param c the coordinate array
	 * @return the coordinate string
	 * @see Utils#switchCoordinates(int)
	 */
	protected static String generateCoordinates(int epsg, Coordinate[] c) {
		StringBuilder buf = new StringBuilder();
		boolean swap = UwGeometryUtils.switchCoordinatesForEPSG(epsg);
		for (int i = 0; i < c.length; i++) {
			if (swap) {
				buf.append(Utils.NUMBER_FORMAT.format(c[i].x)).append(" ")
						.append(Utils.NUMBER_FORMAT.format(c[i].y));
			} else {
				buf.append(Utils.NUMBER_FORMAT.format(c[i].y)).append(" ")
						.append(Utils.NUMBER_FORMAT.format(c[i].x));
			}
			if (i < c.length - 1)
				buf.append(",");
		}
		return buf.toString();
	}

	/**
	 * Generates the Procedure.
	 * 
	 * @param o the {@code Observation}
	 * @param m the XmlBean within the Procedure is generated.
	 */
	protected static void generateProcedure(AbstractObservation o, MeasurementType m) {
		if (o.getProcedure() != null) {
			m.addNewProcedure().setHref(o.getProcedure().toString());
		}
	}

	/**
	 * Generates the ObservedProperty.
	 * 
	 * @param o the {@code Observation}
	 * @param m the XmlBean within the ObservedProperty is generated.
	 */
	protected static void generateObservedProperty(AbstractObservation o,
			MeasurementType m) {
		if (o.getObservedProperty() != null) {
			m.addNewObservedProperty().setHref(o.getObservedProperty().toString());
		}
	}

	/**
	 * Generates the Result.
	 * 
	 * @param o the {@code Observation}
	 * @param m the XmlBean within the Result is generated.
	 */
	protected static void generateResult(AbstractObservation o, MeasurementType m) {
		
		MeasureResult r = ((Measurement) o).getResult();
		
		if (!new Double(r.getMeasureValue()).equals(Double.NaN)) {
			MeasureType xbresult = MeasureType.Factory.newInstance();
			xbresult.setStringValue(Utils.NUMBER_FORMAT.format(o.getResult()));
			if (r.getUnitOfMeasurement() != null) {
				xbresult.setUom(r.getUnitOfMeasurement());
			}
			m.addNewResult().set(xbresult);
		} else {
			m.addNewResult().setNil();
		}
	}

	/**
	 * Generates the SamplingTime.
	 * 
	 * @param o the {@code Observation}
	 * @param m the XmlBean within the SamplingTime is generated.
	 */
	protected static void generateResultTime(AbstractObservation o, MeasurementType m) {
		AbstractTimeObjectType atot = m.addNewSamplingTime().addNewTimeObject();
		if (o.getPhenomenonTime().isInstant()) {
			TimeInstantType tit = (TimeInstantType) atot.substitute(GML.q("TimeInstant"), TimeInstantType.type);
			tit.addNewTimePosition().setStringValue(UwTimeUtils.format(o.getPhenomenonTime().getDateTime()));
		} else if (o.getPhenomenonTime().isInterval()) {
			TimePeriodType tpt = (TimePeriodType) atot.substitute(GML.q("TimePeriod"), TimePeriodType.type);
			tpt.addNewBeginPosition().setStringValue(UwTimeUtils.format(o.getPhenomenonTime().getInterval().getStart()));
			tpt.addNewEndPosition().setStringValue(UwTimeUtils.format(o.getPhenomenonTime().getInterval().getEnd()));
		}
	}

}
