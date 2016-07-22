package org.n52.sos.uncertainty.ds.pgsql;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.domainFeatures.SosDomainArea;
import org.n52.sos.ogc.om.features.domainFeatures.SosGenericDomainFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosGenericSamplingFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingPoint;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingSurface;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class for DAOs. Parse and Create.
 * 
 * @author Carsten Hollmann, Martin Kiesow
 * 
 */
public class ResultSetUtilities extends org.n52.sos.ds.pgsql.ResultSetUtilities {

	/** logger */
	private static final Logger LOGGER = Logger
			.getLogger(PGSQLGetObservationDAO.class);

	/**
	 * Hide utility constructor
	 */
	protected ResultSetUtilities() {
		super();
	}

	/**
	 * Creates a SosAbstractFeature (SosSamplingPoint, SosSamplingSurface,
	 * SosDomainArea, SosGenericDomainFeature) from parameters.
	 * 
	 * @param id
	 * @param desc
	 * @param name
	 * @param geomWKT
	 * @param srid
	 * @param featureType
	 * @param schemaLink
	 * @return SosAbstractFeature
	 * @throws OwsExceptionReport
	 */
	public static SosAbstractFeature getAbstractFeatureFromValues(String id,
			String desc, String name, String geomWKT, int srid,
			String featureType, String schemaLink,
			Collection<SosAbstractFeature> domainFeaturesp)
			throws OwsExceptionReport {

		SosAbstractFeature absFeat = null;
		Geometry geometry = createJTSGeom(geomWKT, srid);

		// add new AbstractFeature to Collection
		if (featureType
				.equalsIgnoreCase(org.uncertweb.api.om.OMConstants.NS_SFT
						+ org.uncertweb.api.om.OMConstants.EN_SAMPLINGPOINT)) {
			if (geometry instanceof Point) {
				absFeat = new SosSamplingPoint(id, name, desc,
						(Point) geometry, featureType, schemaLink,
						domainFeaturesp);
			} else {
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						"The geometry of feature type '" + featureType
								+ "' has to be Point!!");
				LOGGER.error("The feature type '" + featureType
						+ "' is not supported!", se);
				throw se;
			}
		} else if (featureType
				.equalsIgnoreCase(org.uncertweb.api.om.OMConstants.NS_SFT
						+ org.uncertweb.api.om.OMConstants.EN_SAMPLINGCURVE)) {
			if (geometry instanceof LineString) {
				absFeat = new SosGenericSamplingFeature(id, name, desc,
						geometry, schemaLink, domainFeaturesp);
			} else {
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						"The geometry of feature type '" + featureType
								+ "' has to be Sampling Curve!!");
				LOGGER.error("The feature type '" + featureType
						+ "' is not supported!", se);
				throw se;
			}
		} else if (featureType
				.equalsIgnoreCase(org.uncertweb.api.om.OMConstants.NS_SFT
						+ org.uncertweb.api.om.OMConstants.EN_SAMPLINGSURFACE)) {
			if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
				absFeat = new SosSamplingSurface(id, name, desc, geometry,
						featureType, schemaLink, domainFeaturesp);
			} else {
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						"The geometry of feature type '" + featureType
								+ "' has to be Polygon!!");
				LOGGER.error("The feature type '" + featureType
						+ "' is not supported!", se);
				throw se;
			}
		} else if (featureType.equalsIgnoreCase(OMConstants.NS_SA_PREFIX + ":"
				+ OMConstants.EN_SAMPLINGPOINT)) {
			if (geometry instanceof Point) {
				absFeat = new SosSamplingPoint(id, name, desc,
						(Point) geometry, featureType, schemaLink,
						domainFeaturesp);
			} else {
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						"The geometry of feature type '" + featureType
								+ "' has to be Point!!");
				LOGGER.error("The feature type '" + featureType
						+ "' is not supported!", se);
				throw se;
			}
		} else if (featureType.equalsIgnoreCase(OMConstants.NS_SA_PREFIX + ":"
				+ OMConstants.EN_SAMPLINGSURFACE)) {
			if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
				absFeat = new SosSamplingSurface(id, name, desc, geometry,
						featureType, schemaLink, domainFeaturesp);
			} else {
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						"The geometry of feature type '" + featureType
								+ "' has to be Polygon!!");
				LOGGER.error("The feature type '" + featureType
						+ "' is not supported!", se);
				throw se;
			}
		} else if (featureType.equalsIgnoreCase(OMConstants.NS_SA_PREFIX + ":"
				+ OMConstants.EN_DOMAINAREA)) {
			if (geometry instanceof Polygon) {
				absFeat = new SosDomainArea(id, name, desc, geometry,
						featureType, schemaLink);
			} else {
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						"The geometry of feature type '" + featureType
								+ "' has to be Polygon!!");
				LOGGER.error("The feature type '" + featureType
						+ "' is not supported!", se);
				throw se;
			}

		} else if (featureType.equalsIgnoreCase(OMConstants.NS_SOS_PREFIX + ":"
				+ OMConstants.EN_GENERICDOMAINFEATURE)) {
			if (geometry instanceof Polygon) {
				absFeat = new SosGenericDomainFeature(id, name, desc, geometry,
						featureType, schemaLink);
			} else {
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						"The geometry of feature type '" + featureType
								+ "' has to be Polygon!!");
				LOGGER.error("The feature type '" + featureType
						+ "' is not supported!", se);
				throw se;
			}
		} else {
			OwsExceptionReport se = new OwsExceptionReport(
					ExceptionLevel.DetailedExceptions);
			se.addCodedException(ExceptionCode.NoApplicableCode, null,
					"The geometry or the Prefix of feature type '"
							+ featureType + "' is not supported!!");
			LOGGER.error("The feature type '" + featureType
					+ "' is not supported!", se);
			throw se;
		}
		return absFeat;
	}
}
