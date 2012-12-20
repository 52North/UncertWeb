package org.n52.sos.uncertainty.ds.insert.pgsql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.uncertainty.cache.CapabilitiesCacheController;
import org.n52.sos.uncertainty.ds.pgsql.PGDAOUncertaintyConstants;
import org.n52.sos.uncertainty.ogc.IUncertainObservation;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.AbstractRealisation;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.CategoricalRealisation;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.SystematicSample;
import org.uncertml.sample.UnknownSample;
import org.uncertml.statistic.Mean;
import org.uncertweb.api.om.DQ_UncertaintyResult;

/**
 * class provides methods for inserting uncertainties into the SOSD
 * 
 * @author Martin Kiesow
 */
public class InsertUncertaintyDAO {

	/** connection pool */
	private PGConnectionPool cpool;

	/** logger */
	private static final Logger LOGGER = Logger
			.getLogger(InsertUncertaintyDAO.class);

	/**
	 * constructor
	 * 
	 * @param cpoolp
	 *            PGConnectionPool which offers a pool of open connections to
	 *            the db
	 */
	public InsertUncertaintyDAO(PGConnectionPool cpool) {
		this.cpool = cpool;
	}

	/**
	 * inserts a single uncertainty into database
	 * 
	 * @param obs
	 *            associated observation
	 * @param unc
	 *            uncertainty to be inserted
	 * @param valueUnit
	 *            unit of measurement
	 * @param trCon
	 *            database connection
	 * @return inserted uncertainty's ID
	 * @throws OwsExceptionReport
	 */
	public int insertUncertainty(IUncertainObservation obs, IUncertainty unc,
			String valueUnit, Connection trCon) throws SQLException,
			OwsExceptionReport {

		return this.insertUncertainty(obs, unc, null, valueUnit, trCon);
	}

	/**
	 * inserts a single uncertainty into database
	 * 
	 * @param obs
	 *            associated observation
	 * @param unc
	 *            uncertainty to be inserted
	 * @param valueUnits
	 *            list of recently added value units (optional)
	 * @param valueUnit
	 *            unit of measurement
	 * @param trCon
	 *            database connection
	 * @return inserted uncertainty's ID
	 * @throws OwsExceptionReport
	 */
	public int insertUncertainty(IUncertainObservation obs, IUncertainty unc,
			List<String> valueUnits, String valueUnit, Connection trCon)
			throws SQLException, OwsExceptionReport {

		int valUnitID = Integer.MIN_VALUE;
		int uncertaintyID = Integer.MIN_VALUE;
		int uncValID = Integer.MIN_VALUE;

		String insertStmt;
		Statement stmt;
		String query;
		ResultSet rs;

		Connection con = null;
		try {

			if (trCon == null) {
				con = cpool.getConnection();
			} else {
				con = trCon;
			}

			// insert value unit table
			CapabilitiesCacheController capsCache;
			if (SosConfigurator.getInstance().getCapsCacheController() instanceof CapabilitiesCacheController) {

				capsCache = (CapabilitiesCacheController) SosConfigurator
						.getInstance().getCapsCacheController();
			} else {
				String message = "Uncertainty cannot be inserted into database: You have to name a uncertainty enabled CapabilitiesCacheController in build.properties file.";
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.PlainExceptions);
				LOGGER.error(message);
				se.addCodedException(ExceptionCode.OperationNotSupported, null,
						message);
				throw se;
			}

			if (!obs.getUnitsOfMeasurement().equalsIgnoreCase(
					SosConstants.PARAMETER_NOT_SET)) {

				if (valueUnits == null) {
					valueUnits = new ArrayList<String>();
				}

				if (!capsCache.getValueUnits().contains(
						obs.getUnitsOfMeasurement())
						&& !valueUnits.contains(valueUnit)) {
					insertStmt = new String(" INSERT INTO "
							+ PGDAOUncertaintyConstants.uValUnitTn + " ("
							+ PGDAOUncertaintyConstants.uVUValUnitCn
							+ ") VALUES ('" + obs.getUnitsOfMeasurement()
							+ "');");
					stmt = con.createStatement();
					stmt.execute(insertStmt);

					valueUnits.add(valueUnit);

					// get value_unit_id to insert uncertainty table
					query = "SELECT currval(pg_get_serial_sequence('"
							+ PGDAOUncertaintyConstants.uValUnitTn + "', '"
							+ PGDAOUncertaintyConstants.uVUValUnitIdCn
							+ "')) AS valUnitID;";

					rs = stmt.executeQuery(query);
					while (rs.next()) {
						valUnitID = rs.getInt("valUnitID");
					}
				} else {

					// get value_unit_id to insert uncertainty table
					query = "SELECT "
							+ PGDAOUncertaintyConstants.uVUValUnitIdCn
							+ " AS valUnitID, "
							+ PGDAOUncertaintyConstants.uVUValUnitCn + " FROM "
							+ PGDAOUncertaintyConstants.uValUnitTn + " WHERE "
							+ PGDAOUncertaintyConstants.uVUValUnitCn + " = '"
							+ obs.getUnitsOfMeasurement() + "';";
					stmt = con.createStatement();
					rs = stmt.executeQuery(query);
					while (rs.next()) {
						valUnitID = rs.getInt("valUnitID");
					}
				}
			} else {
				String message = "Uncertainty cannot be inserted into database: Missing uncertainty value unit.";
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.PlainExceptions);
				LOGGER.error(message);
				se.addCodedException(ExceptionCode.MissingParameterValue, null,
						message);
				throw se;
			}

			// insert uncertainty table
			insertStmt = new String(" INSERT INTO "
					+ PGDAOUncertaintyConstants.uUncertTn + " ("
					+ PGDAOUncertaintyConstants.uUUncTypeCn + ", "
					+ PGDAOUncertaintyConstants.uVUValUnitIdCn + ") VALUES ('"
					+ getUncertaintyType(unc) + "', '" + valUnitID + "');");
			stmt = con.createStatement();
			stmt.execute(insertStmt);

			// get uncertainty_id and uncertainty_values_id to insert
			// uncertainty values table and obs_unc table
			query = "SELECT currval(pg_get_serial_sequence('"
					+ PGDAOUncertaintyConstants.uUncertTn + "','"
					+ PGDAOUncertaintyConstants.uUUncIdCn + "')) AS uncID;";

			rs = stmt.executeQuery(query);
			while (rs.next()) {
				uncertaintyID = rs.getInt("uncID");
			}

			query = "SELECT currval(pg_get_serial_sequence('"
					+ PGDAOUncertaintyConstants.uUncertTn + "','"
					+ PGDAOUncertaintyConstants.uUUncValIdCn
					+ "')) AS uncValID;";

			rs = stmt.executeQuery(query);
			while (rs.next()) {
				uncValID = rs.getInt("uncValID");
			}

			// insert uncertainty values
			if (unc instanceof NormalDistribution) {

				// insert normal distribution table
				insertNormalDistribution((NormalDistribution) unc, uncValID,
						con);
			} else if (unc instanceof Mean) {

				// insert mean and mean values table
				insertMean((Mean) unc, uncValID, con);

			} else if (unc instanceof AbstractRealisation) {

				// insert realisation and realisation values table
				insertRealisation((AbstractRealisation) unc, uncValID, con);

			} else if (unc instanceof AbstractSample) {

				// insert realisation and realisation values table
				insertSample((AbstractSample) unc, uncValID, con);

				// TODO add further uncertainty types here
				// } else if (unc instanceof ???) {
				// }

			} else {

				String message = "Uncertainty cannot be inserted into database: Uncertainty type might not be supported, yet.";
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.PlainExceptions);
				LOGGER.error(message);
				se.addCodedException(ExceptionCode.OperationNotSupported, null,
						message);
				throw se;
			}

		} finally {
			if (con != null && trCon == null) {
				cpool.returnConnection(con);
			}
		}
		return uncertaintyID;
	}

	/**
	 * inserts uncertainties from result quality into database; temporarily only
	 * single uncertainties of single result qualities are supported
	 * 
	 * @param obs
	 *            associated observation
	 * @param trCon
	 *            database connection
	 * @return inserted uncertainties' IDs
	 * @throws OwsExceptionReport
	 */
	public List<Integer> insertUncertaintiesFromRQ(IUncertainObservation obs,
			Connection trCon) throws SQLException, OwsExceptionReport {

		List<Integer> uncIDs = new ArrayList<Integer>();
		List<String> valueUnits = new ArrayList<String>();

		DQ_UncertaintyResult[] quality = obs.getUncQuality();
		if (quality != null && quality.length == 1) {

			// // collect all uncertainties
			// for (DQ_UncertaintyResult uncRes : quality) {
			//
			// String valueUnit = uncRes.getUom();
			// IUncertainty[] uncs = uncRes.getValues();
			//
			// for (IUncertainty unc : uncs) {
			//
			// uncIDs.add(insertUncertainty(obs, unc, valueUnits,
			// valueUnit, trCon));
			// }
			// }
			IUncertainty[] uncs = quality[0].getValues();

			if (uncs != null && uncs.length == 1) {

				// get and insert single uncertainty
				uncIDs.add(insertUncertainty(obs, uncs[0], valueUnits,
						quality[0].getUom(), trCon));

			} else if (uncs.length > 1) {

				String message = "Error while executing insertUncertainty operation. Only a single uncertainty per result quality is supported.";
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.OptionNotSupported, null,
						message);
				LOGGER.error(message);
				throw se;
			}

		} else if (quality.length > 1) {

			String message = "Error while executing insertUncertainty operation. Only single result qualities are supported.";
			OwsExceptionReport se = new OwsExceptionReport(
					ExceptionLevel.DetailedExceptions);
			se.addCodedException(ExceptionCode.OptionNotSupported, null,
					message);
			LOGGER.error(message);
			throw se;
		}

		return uncIDs;
	}

	/**
	 * insert an observation-uncertainty relationship
	 * 
	 * @param obsID
	 *            observation ID
	 * @param uncID
	 *            uncertainty ID
	 * @param identifier
	 *            gml Identifier
	 * @param trCon
	 *            database connection
	 * @throws OwsExceptionReport
	 * @throws SQLException
	 */
	public void insertObsUncRelationship(int obsID, int uncID,
			String identifier, Connection trCon) throws OwsExceptionReport,
			SQLException {

		Connection con = null;
		try {

			if (trCon == null) {
				con = cpool.getConnection();
			} else {
				con = trCon;
			}

			// create statement
			String insertStmt = new String(" INSERT INTO "
					+ PGDAOUncertaintyConstants.uObsUncTn + " ("
					+ PGDAOUncertaintyConstants.uOUObsIdCn + ", "
					+ PGDAOUncertaintyConstants.uUUncIdCn + ", "
					+ PGDAOUncertaintyConstants.uOUGmlIdCn + ") VALUES ("
					+ obsID + ", " + uncID + ", '" + identifier + "');");

			// insert
			Statement stmt = con.createStatement();
			stmt.execute(insertStmt);

		} finally {
			if (con != null && trCon == null) {
				cpool.returnConnection(con);
			}
		}
	}

	/**
	 * insert a list of observation-uncertainty relationships; this method
	 * provides the same functionallity as insertObsUncRelationship(...) but
	 * creates only a single insert statement
	 * 
	 * @param obsID
	 *            a single observation ID
	 * @param uncIDs
	 *            corresponding uncertainty IDs
	 * @param identifier
	 *            gml Identifier
	 * @param trCon
	 *            database connection
	 * @throws OwsExceptionReport
	 * @throws SQLException
	 */
	public void insertObsUncRelationships(int obsID, List<Integer> uncIDs,
			String identifier, Connection trCon) throws OwsExceptionReport,
			SQLException {

		Connection con = null;
		try {

			if (trCon == null) {
				con = cpool.getConnection();
			} else {
				con = trCon;
			}

			StringBuilder insertStmt = new StringBuilder();
			for (int uncID : uncIDs) {

				// create statement
				insertStmt.append(" INSERT INTO "
						+ PGDAOUncertaintyConstants.uObsUncTn + " ("
						+ PGDAOUncertaintyConstants.uOUObsIdCn + ", "
						+ PGDAOUncertaintyConstants.uUUncIdCn + ", "
						+ PGDAOUncertaintyConstants.uOUGmlIdCn + ") VALUES ("
						+ obsID + ", " + uncID + ", '" + identifier + "');");
			}

			// insert
			Statement stmt = con.createStatement();
			stmt.execute(insertStmt.toString());

		} finally {
			if (con != null && trCon == null) {
				cpool.returnConnection(con);
			}
		}
	}

	private void insertNormalDistribution(NormalDistribution unc, int uncValID,
			Connection con) throws SQLException {

		StringBuilder insertStmt = new StringBuilder();
		List<Double> means = unc.getMean();
		List<Double> variances = unc.getVariance();

		// insert pairs of values
		// NormalDistribution implementation ensures equal size of means and
		// variances
		for (int i = 0; i < means.size(); i++) {
			insertStmt.append(" INSERT INTO "
					+ PGDAOUncertaintyConstants.uNormTn + " ("
					+ PGDAOUncertaintyConstants.uNNormIdCn + ", "
					+ PGDAOUncertaintyConstants.uNMeanCn + ", "
					+ PGDAOUncertaintyConstants.uNVarCn + ") VALUES ("
					+ uncValID + ", " + means.get(i) + ", " + variances.get(i)
					+ ");");
		}

		Statement stmt = con.createStatement();
		stmt.execute(insertStmt.toString());
	}

	private void insertMean(Mean unc, int uncValID, Connection con)
			throws SQLException {

		StringBuilder insertStmt = new StringBuilder();
		List<Double> meanVals;
		StringBuilder arrayInput;

		// create array of values as a String
		meanVals = unc.getValues();
		arrayInput = new StringBuilder("'{");

		if (meanVals.size() > 0) {

			arrayInput.append(meanVals.get(0));
		}
		if (meanVals.size() > 1) {

			for (int i = 1; i < meanVals.size(); i++) {
				arrayInput.append(", " + meanVals.get(i) + "");
			}
		}
		arrayInput.append("}'");

		// insert realisation
		insertStmt.append(" INSERT INTO " + PGDAOUncertaintyConstants.uMeanTn
				+ " (" + PGDAOUncertaintyConstants.uMMeanIdCn + ", "
				+ PGDAOUncertaintyConstants.uMMeanValsCn + ") VALUES ("
				+ uncValID + ", " + arrayInput + ");");

		Statement stmt = con.createStatement();
		stmt.execute(insertStmt.toString());
	}

	private void insertRealisation(AbstractRealisation unc, int uncValID,
			Connection con) throws SQLException {

		StringBuilder insertStmt = new StringBuilder();
		Double weight = unc.getWeight();
		List<Double> conVals;
		List<String> catVals;
		StringBuilder arrayInput;

		if (unc instanceof ContinuousRealisation) {

			// create array of continuous values as a String
			conVals = ((ContinuousRealisation) unc).getValues();
			arrayInput = new StringBuilder("'{");

			if (conVals.size() > 0) {

				arrayInput.append(conVals.get(0));
			}
			if (conVals.size() > 1) {

				for (int i = 1; i < conVals.size(); i++) {
					arrayInput.append(", " + conVals.get(i) + "");
				}
			}
			arrayInput.append("}'");

			// insert realisation
			insertStmt.append(" INSERT INTO "
					+ PGDAOUncertaintyConstants.uRealTn + " ("
					+ PGDAOUncertaintyConstants.uRRealIdCn + ", "
					+ PGDAOUncertaintyConstants.uRWeightCn + ", "
					+ PGDAOUncertaintyConstants.uRConValsCn + ") VALUES ("
					+ uncValID + ", " + weight + ", " + arrayInput + ");");

		} else if (unc instanceof CategoricalRealisation) {

			// create array of categories as a String
			catVals = ((CategoricalRealisation) unc).getCategories();
			arrayInput = new StringBuilder("'{");

			if (catVals.size() > 0) {

				arrayInput.append("\"" + catVals.get(0) + "\"");
			}
			if (catVals.size() > 1) {

				for (int i = 1; i < catVals.size(); i++) {
					arrayInput.append(", \"" + catVals.get(i) + "\"");
				}
			}
			arrayInput.append("}'");

			// insert realisation
			insertStmt.append(" INSERT INTO "
					+ PGDAOUncertaintyConstants.uRealTn + " ("
					+ PGDAOUncertaintyConstants.uRRealIdCn + ", "
					+ PGDAOUncertaintyConstants.uRWeightCn + ", "
					+ PGDAOUncertaintyConstants.uRCatValsCn + ") VALUES ("
					+ uncValID + ", " + weight + ", " + arrayInput + ");");
		}

		Statement stmt = con.createStatement();
		stmt.execute(insertStmt.toString());
	}

	private void insertSample(AbstractSample unc, int uncValID, Connection con)
			throws SQLException {

		StringBuilder insertStmt = new StringBuilder();
		
		String samMethDesc = unc.getSamplingMethodDescription();		
		if (samMethDesc == null) {
			samMethDesc = SosConstants.PARAMETER_NOT_SET;
		}
		
		String id;
		Double weight;
		List<Double> conVals;
		List<String> catVals;
		StringBuilder arrayInput;

		for (AbstractRealisation real : unc.getRealisations()) {
			
			// every sample realisation should have an (unique) ID
			id = real.getId();
			
			weight = real.getWeight();

			if (real instanceof ContinuousRealisation) {

				// create array of continuous values as a String
				conVals = ((ContinuousRealisation) real).getValues();
				arrayInput = new StringBuilder("'{");

				if (conVals.size() > 0) {

					arrayInput.append(conVals.get(0));
				}
				if (conVals.size() > 1) {

					for (int i = 1; i < conVals.size(); i++) {
						arrayInput.append(", " + conVals.get(i) + "");
					}
				}
				arrayInput.append("}'");

				// insert realisation
				insertStmt.append(" INSERT INTO "
						+ PGDAOUncertaintyConstants.uRealTn + " ("
						+ PGDAOUncertaintyConstants.uRRealIdCn + ", "
						+ PGDAOUncertaintyConstants.uRWeightCn + ", "
						+ PGDAOUncertaintyConstants.uRConValsCn + ", "
						+ PGDAOUncertaintyConstants.uRIdCn + ", "
						+ PGDAOUncertaintyConstants.uRSamMethDescCn + ") VALUES ("
						+ uncValID + ", " + weight + ", " + arrayInput + ", '"
						+ id + "', '" + samMethDesc 
						+ "');");

			} else if (real instanceof CategoricalRealisation) {

				// create array of categories as a String
				catVals = ((CategoricalRealisation) real).getCategories();
				arrayInput = new StringBuilder("'{");

				if (catVals.size() > 0) {

					arrayInput.append("\"" + catVals.get(0) + "\"");
				}
				if (catVals.size() > 1) {

					for (int i = 1; i < catVals.size(); i++) {
						arrayInput.append(", \"" + catVals.get(i) + "\"");
					}
				}
				arrayInput.append("}'");

				// insert realisation
				insertStmt.append(" INSERT INTO "
						+ PGDAOUncertaintyConstants.uRealTn + " ("
						+ PGDAOUncertaintyConstants.uRRealIdCn + ", "
						+ PGDAOUncertaintyConstants.uRWeightCn + ", "
						+ PGDAOUncertaintyConstants.uRCatValsCn
						+ PGDAOUncertaintyConstants.uRIdCn + ", "
						+ PGDAOUncertaintyConstants.uRSamMethDescCn + ") VALUES ("
						+ uncValID + ", " + weight + ", " + arrayInput + ", '"
						+ id + "', '" + samMethDesc + "');");
			}
		}

		Statement stmt = con.createStatement();
		stmt.execute(insertStmt.toString());

	}

	/**
	 * returns a constant key word for every uncertainty type
	 * 
	 * @param unc
	 *            given uncertainty
	 * @return string constant
	 */
	private String getUncertaintyType(IUncertainty unc) {
		String uncType = null;

		if (unc instanceof NormalDistribution) {
			uncType = PGDAOUncertaintyConstants.u_normalDistType;
		} else if (unc instanceof Mean) {
			uncType = PGDAOUncertaintyConstants.u_meanType;
		} else if (unc instanceof AbstractRealisation) {
			uncType = PGDAOUncertaintyConstants.u_realType;
		} else if (unc instanceof RandomSample) {
			uncType = PGDAOUncertaintyConstants.u_randomSType;
		} else if (unc instanceof SystematicSample) {
			uncType = PGDAOUncertaintyConstants.u_systematicSType;
		} else if (unc instanceof UnknownSample) {
			uncType = PGDAOUncertaintyConstants.u_unknownSType;
		}
		// TODO add further uncertainty types here
		// } else if (unc instanceof ???) {
		// uncType = PGDAOUncertaintyConstants.???;
		// }

		return uncType;
	}
}
