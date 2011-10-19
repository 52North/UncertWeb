package org.n52.sos.ds.insert.pgsql.uncertainty;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.n52.sos.SosConstants;
import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ds.pgsql.PGDAOConstants;
import org.n52.sos.ds.pgsql.uncertainty.PGDAOUncertaintyConstants;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.SosCategoryObservation;
import org.n52.sos.ogc.om.SosMeasurement;
import org.n52.sos.ogc.om.SosSpatialObservation;
import org.n52.sos.ogc.om.uncertainty.UNCMeasurementObservation;
import org.n52.sos.ogc.om.uncertainty.UNCUncertaintyObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.ogc.uncertainty.IUncertainObservation;
import org.uncertml.IUncertainty;
import org.uncertml.x20.AbstractUncertaintyType;

public class InsertObservationDAO extends
		org.n52.sos.ds.insert.pgsql.InsertObservationDAO {

	public InsertObservationDAO(PGConnectionPool cpoolp) {
		super(cpoolp);
	}

	public int insertObservation(AbstractSosObservation obs, Connection trCon)
			throws SQLException, OwsExceptionReport {

		int observationID = Integer.MIN_VALUE;

		if (obs instanceof SosMeasurement) {
			SosMeasurement meas = (SosMeasurement) obs;
			observationID = insertObservation(obs.getSamplingTime(),
					obs.getProcedureID(), obs.getFeatureOfInterestID(),
					obs.getPhenomenonID(), obs.getOfferingID(),
					"" + meas.getValue(), obs.getMimeType(),
					PGDAOConstants.numericValueCn, obs.getQuality(), trCon);
		}

		else if (obs instanceof SosCategoryObservation) {
			SosCategoryObservation catObs = (SosCategoryObservation) obs;
			observationID = insertObservation(obs.getSamplingTime(),
					obs.getProcedureID(), obs.getFeatureOfInterestID(),
					obs.getPhenomenonID(), obs.getOfferingID(),
					catObs.getTextValue(), obs.getMimeType(),
					PGDAOConstants.textValueCn, obs.getQuality(), trCon);
		}

		else if (obs instanceof SosSpatialObservation) {
			SosSpatialObservation spaObs = (SosSpatialObservation) obs;
			observationID = insertObservation(obs.getSamplingTime(),
					obs.getProcedureID(), obs.getFeatureOfInterestID(),
					obs.getPhenomenonID(), obs.getOfferingID(),
					spaObs.getResult(), PGDAOConstants.spatialValueCn,
					obs.getQuality(), trCon);
		}

		else {
			OwsExceptionReport se = new OwsExceptionReport(
					ExceptionLevel.DetailedExceptions);
			se.addCodedException(
					ExceptionCode.InvalidParameterValue,
					SosConstants.RegisterSensorParams.SensorDescription.name(),
					"Only Measurements, SpatialObservations or CategoryObservations could be inserted!!");
			LOGGER.warn(se.getMessage());
			throw se;
		}

		if (observationID != Integer.MIN_VALUE) {

			if (obs instanceof UNCUncertaintyObservation) {
				insertUncertainty((UNCUncertaintyObservation) obs, observationID, trCon);
			} else if (obs instanceof UNCMeasurementObservation) {
				insertUncertainty((UNCMeasurementObservation) obs, observationID, trCon);
			}
			// additional O&M 2 observation types here
		}

		return observationID;
	} // end insertObservation

	private void insertUncertainty(IUncertainObservation obs, int obsID, Connection trCon)
			throws OwsExceptionReport, SQLException {

			// create insert statement
			StringBuilder insertStmt = new StringBuilder();

			// append single uncertainty from uncertain result
			if (obs instanceof UNCUncertaintyObservation) {
				IUncertainty unc = ((UNCUncertaintyObservation) obs)
						.getUncertainty();

				// TODO create INSERT statements for uncertainty and relationships
				
				String ins = " INSERT INTO " + PGDAOUncertaintyConstants.uValUnit
				+ " (" + PGDAOUncertaintyConstants.uUValUnitID + ", " + PGDAOUncertaintyConstants.uVUValUnit
				+ ") VALUES ('";

				insertStmt.append(ins);
			}
			// append uncertainties from resultQuality
			if (obs.getUncQuality() != null && obs.getUncQuality().length > 0) {

			}

			// insert uncertainties
			Connection con = null;
			
			try {
				if (trCon == null) {
					con = super.cpool.getConnection();
				} else {
					con = trCon;
				}
				Statement stmt = con.createStatement();
			
			stmt.execute(insertStmt.toString());
		} finally {
			if (con != null && trCon == null) {
				cpool.returnConnection(con);
			}
		}
	}
}
