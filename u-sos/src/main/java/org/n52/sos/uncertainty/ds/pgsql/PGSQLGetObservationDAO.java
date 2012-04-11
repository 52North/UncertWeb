package org.n52.sos.uncertainty.ds.pgsql;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.n52.sos.Sos1Constants;
import org.n52.sos.Sos2Constants;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
import org.n52.sos.Sos1Constants.GetObservationParams;
import org.n52.sos.SosConstants.FirstLatest;
import org.n52.sos.SosConstants.ValueTypes;
import org.n52.sos.SosDateTimeUtilities;
import org.n52.sos.ds.IGetObservationDAO;
import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ds.pgsql.PGDAOConstants;
import org.n52.sos.ds.pgsql.ResultSetUtilities;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.SosCategoryObservation;
import org.n52.sos.ogc.om.SosGenericObservation;
import org.n52.sos.ogc.om.SosMeasurement;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.om.SosSpatialObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.domainFeatures.SosGenericDomainFeature;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.om.quality.SosQuality.QualityType;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.request.SosGetObservationRequest;
import org.n52.sos.uncertainty.SosUncConstants;
import org.n52.sos.uncertainty.decode.impl.CMean;
import org.n52.sos.uncertainty.decode.impl.ObservationConverter;
import org.n52.sos.uncertainty.ogc.IUncertainObservation;
import org.n52.sos.uncertainty.ogc.om.UNCMeasurementObservation;
import org.n52.sos.uncertainty.ogc.om.UNCUncertaintyObservation;
import org.n52.sos.utilities.JTSUtilities;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.statistic.Mean;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * DAO of PostgreSQL DB for GetObservation Operation. Central method is
 * getObservation() which creates the query and returns an ObservationCollection
 * XmlBean. The method is abstract, This class is abstract, because the
 * different PostGIS versions for the different PGSQL versions return the
 * geometries of the FOIs in different ways.
 * 
 * @author Martin Kiesow
 * 
 */
public class PGSQLGetObservationDAO extends
		org.n52.sos.ds.pgsql.PGSQLGetObservationDAO implements
		IGetObservationDAO {

	/** logger */
	private static Logger LOGGER = Logger
			.getLogger(PGSQLGetObservationDAO.class);

	/**
	 * constructor wrapping super class constructor
	 */
	public PGSQLGetObservationDAO(PGConnectionPool cpool) {
		super(cpool);
	}

	/**
	 * get uncertainties to a list of observation IDs
	 * 
	 * @param obsIDs
	 *            as a list
	 * @return array with a row for every uncertainty value; null, if obsIDs is
	 *         empty
	 * @throws OwsExceptionReport
	 */
	private Object[][] getUncertainty(List<String> obsIDs)
			throws OwsExceptionReport {

		if (obsIDs.isEmpty() || obsIDs.size() < 1) {
			return null;
		}

		ArrayList<Object[]> rsList = new ArrayList<Object[]>(obsIDs.size());

		Connection con = null;
		try {
			con = getCPool().getConnection();
			ResultSet rs = queryUncertainty(obsIDs, con);
			Object[] row = null;

			while (rs.next()) {

				row = new Object[8];

				Array.set(row, 0,
						rs.getString(PGDAOUncertaintyConstants.uOUObsIdCn));
				Array.set(row, 1,
						rs.getString(PGDAOUncertaintyConstants.uUUncIdCn));
				Array.set(row, 2,
						rs.getString(PGDAOUncertaintyConstants.uOUGmlIdCn));
				Array.set(row, 3,
						rs.getString(PGDAOUncertaintyConstants.uVUValUnitCn));
				Array.set(row, 4,
						rs.getString(PGDAOUncertaintyConstants.uUUncTypeCn));
				Array.set(row, 5,
						rs.getDouble(PGDAOUncertaintyConstants.uMVMeanValCn));
				Array.set(row, 6,
						rs.getDouble(PGDAOUncertaintyConstants.uNMeanCn));
				Array.set(row, 7,
						rs.getDouble(PGDAOUncertaintyConstants.uNVarCn));

				rsList.add(row);
			}

		} catch (SQLException sqle) {
			OwsExceptionReport se = new OwsExceptionReport(
					ExceptionLevel.DetailedExceptions);
			LOGGER.error(
					"An error occured while query data from the database!",
					sqle);
			se.addCodedException(ExceptionCode.NoApplicableCode, null, sqle);
			throw se;
		} finally {
			if (con != null) {
				getCPool().returnConnection(con);
			}
		}
		return rsList.toArray(new Object[0][0]);
	}

	/**
	 * querries uncertainties to a list of observation IDs
	 * 
	 * @param obsIDs
	 *            as a list
	 * @return corresponding uncertainties
	 * @throws OwsExceptionReport
	 * @throws SQLException
	 */
	private ResultSet queryUncertainty(List<String> obsIDs, Connection con)
			throws OwsExceptionReport, SQLException {

		ResultSet resultSet = null;

		Statement stmt = con.createStatement();
		StringBuilder query = new StringBuilder();

		// SELECT clause
		query.append("SELECT " + PGDAOUncertaintyConstants.uOUObsIdCn + ", "
				+ PGDAOUncertaintyConstants.uObsUncTn + "."
				+ PGDAOUncertaintyConstants.uUUncIdCn + ", "
				+ PGDAOUncertaintyConstants.uOUGmlIdCn + ", "
				+ PGDAOUncertaintyConstants.uVUValUnitCn + ", "
				+ PGDAOUncertaintyConstants.uUUncTypeCn);

		// append mean columns
		query.append(", " + PGDAOUncertaintyConstants.uMVMeanValCn);

		// append normal distribution colums
		query.append(", " + PGDAOUncertaintyConstants.uNMeanCn + ", "
				+ PGDAOUncertaintyConstants.uNVarCn);

		// FROM clause
		query.append(" FROM (" + PGDAOUncertaintyConstants.uObsUncTn
				+ " LEFT OUTER JOIN " + PGDAOUncertaintyConstants.uUncertTn
				+ " ON " + PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uUUncIdCn + " = "
				+ PGDAOUncertaintyConstants.uObsUncTn + "."
				+ PGDAOUncertaintyConstants.uUUncIdCn + " LEFT OUTER JOIN "
				+ PGDAOUncertaintyConstants.uValUnitTn + " ON "
				+ PGDAOUncertaintyConstants.uValUnitTn + "."
				+ PGDAOUncertaintyConstants.uVUValUnitIdCn + " = "
				+ PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uVUValUnitIdCn);

		// append mean tables
		query.append(" LEFT OUTER JOIN " + PGDAOUncertaintyConstants.uMeanTn
				+ " ON " + PGDAOUncertaintyConstants.uMeanTn + "."
				+ PGDAOUncertaintyConstants.uMMeanIdCn + " = "
				+ PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uUUncValIdCn + " LEFT OUTER JOIN "
				+ PGDAOUncertaintyConstants.uMeanValTn + " ON "
				+ PGDAOUncertaintyConstants.uMeanValTn + "."
				+ PGDAOUncertaintyConstants.uMMeanValIdCn + " = "
				+ PGDAOUncertaintyConstants.uMeanTn + "."
				+ PGDAOUncertaintyConstants.uMMeanValIdCn);

		// append normal distribution table
		query.append(" LEFT OUTER JOIN " + PGDAOUncertaintyConstants.uNormTn
				+ " ON " + PGDAOUncertaintyConstants.uNormTn + "."
				+ PGDAOUncertaintyConstants.uNNormIdCn + " = "
				+ PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uUUncValIdCn);

		// append WHERE clause
		query.append(") WHERE (" + PGDAOUncertaintyConstants.uObsUncTn + "."
				+ PGDAOUncertaintyConstants.uOUObsIdCn + " = "
				+ Integer.parseInt(obsIDs.get(0)));

		// append observation IDs
		for (int i = 1; i < obsIDs.size(); i++) {
			query.append(" OR " + PGDAOUncertaintyConstants.uObsUncTn + "."
					+ PGDAOUncertaintyConstants.uOUObsIdCn + " = "
					+ Integer.parseInt(obsIDs.get(i)));
		}
		query.append(")");

		resultSet = stmt.executeQuery(query.toString());

		return resultSet;
	}

	// /**
	// * returns an uncertainty to a given observation ID
	// * @param resultSet result set containing uncertainties
	// * @param obsID observation ID
	// * @return corresponding uncertainty or null
	// * @throws OwsExceptionReport
	// */
	// public IUncertainty getUncertaintyFromResultSet(
	// ResultSet resultSet, String obsID)
	// throws OwsExceptionReport {
	//
	// try {
	// while (resultSet.next()) {
	//
	// // check remaining heap size
	// // checkFreeMemory(); // used in
	// // getSingleObservationFromResultSet()
	//
	// if (obsID.equals(resultSet
	// .getString(PGDAOUncertaintyConstants.uOUObsIdCn))) {
	//
	// String uncID = resultSet
	// .getString(PGDAOUncertaintyConstants.uUUncIdCn);
	//
	// String gmlID = resultSet
	// .getString(PGDAOUncertaintyConstants.uOUGmlIdCn);
	// String valueUnit = resultSet
	// .getString(PGDAOUncertaintyConstants.uVUValUnitCn);
	// Double meanVal = resultSet
	// .getDouble(PGDAOUncertaintyConstants.uMVMeanValCn);
	// Double normalMean = resultSet
	// .getDouble(PGDAOUncertaintyConstants.uNMeanCn);
	// Double normalVar = resultSet
	// .getDouble(PGDAOUncertaintyConstants.uNVarCn);
	//
	// if (meanVal != null) {
	// // create mean
	// return new Mean(meanVal);
	//
	// } else if (normalMean != null && normalVar != null) {
	// // create normal distribution
	// return new NormalDistribution(normalMean, normalVar);
	//
	// } else {
	// OwsExceptionReport se = new OwsExceptionReport(
	// ExceptionLevel.DetailedExceptions);
	// se.addCodedException(
	// OwsExceptionReport.ExceptionCode.MissingParameterValue,
	// null, "Missing value(s) for uncertainty '"
	// + uncID + "' of observation '" + obsID
	// + "'.");
	// LOGGER.error(se.getMessage());
	// throw se;
	// }
	// }
	// }
	//
	// } catch (SQLException sqle) {
	// OwsExceptionReport se = new OwsExceptionReport(
	// ExceptionLevel.DetailedExceptions);
	// se.addCodedException(ExceptionCode.NoApplicableCode, null,
	// "Error while creating uncertainty from database query result set: "
	// + sqle.getMessage());
	// LOGGER.error(se.getMessage());
	// throw se;
	// }
	//
	// return null;
	// }

	/**
	 * method checks, whether the resultModel parameter is valid for the
	 * observed properties in the request. If f.e. the request contains
	 * resultModel=om:CategoryObservation and the request also contains the
	 * phenomenon waterCategorie, which is a categorical value, then a service
	 * exception is thrown!<br>
	 * if no fitting O&M2 resultModel is found the super class' method checks
	 * for O&M1 result Models
	 * 
	 * @param resultModel
	 *            resultModel parameter which should be checked
	 * @param observedProperties
	 *            string array containing the observed property parameters of
	 *            the request
	 * @throws OwsExceptionReport
	 *             if the resultModel parameter is incorrect in combination with
	 *             the observedProperty parameters
	 */
	protected void checkResultModel(QName resultModel,
			String[] observedProperties) throws OwsExceptionReport {
		Map<String, ValueTypes> valueTypes4ObsProps = SosConfigurator
				.getInstance().getCapsCacheController()
				.getValueTypes4ObsProps();
		ValueTypes valueType;

		// if measurement; check, if phenomenon is contained in request, which
		// values are not numeric
		if (resultModel != null) {
			if (resultModel.equals(SosUncConstants.RESULT_MODEL_MEASUREMENT)) {
				for (int i = 0; i < observedProperties.length; i++) {
					valueType = valueTypes4ObsProps.get(observedProperties[i]);
					if (valueType != ValueTypes.numericType) {
						OwsExceptionReport se = new OwsExceptionReport();
						se.addCodedException(
								OwsExceptionReport.ExceptionCode.InvalidParameterValue,
								GetObservationParams.resultModel.toString(),
								"The value ("
										+ resultModel
										+ ") of the parameter '"
										+ GetObservationParams.resultModel
												.toString()
										+ "' is invalid, because the request contains phenomena, which values are not numericValues!");
						LOGGER.error(
								"The resultModel="
										+ resultModel
										+ " parameter is incorrect, because request contains phenomena, which values are not numericValues!",
								se);
						throw se;
					}
				}
			} else if (resultModel
					.equals(SosUncConstants.RESULT_MODEL_UNCERTAINTY_OBSERVATION)) {

				// TODO FRAGE uncertaintyType? unc+meas+discNum?

				for (int i = 0; i < observedProperties.length; i++) {
					valueType = valueTypes4ObsProps.get(observedProperties[i]);
					if (valueType != ValueTypes.uncertaintyType) {
						OwsExceptionReport se = new OwsExceptionReport();
						se.addCodedException(
								OwsExceptionReport.ExceptionCode.InvalidParameterValue,
								GetObservationParams.resultModel.toString(),
								"The value ("
										+ resultModel
										+ ") of the parameter '"
										+ GetObservationParams.resultModel
												.toString()
										+ "' is invalid, because the request contains phenomena, which values are not uncertainty values!");
						LOGGER.error(
								"The resultModel="
										+ resultModel
										+ " parameter is incorrect, because request contains phenomena, which values are not uncertainty values!",
								se);
						throw se;
					}
				}

			} else {
				super.checkResultModel(resultModel, observedProperties);
			}
		}
	}

	/**
	 * returns an O&M 2 observation collection including uncertainties
	 * 
	 * @param obsCollection
	 *            O&M 1 observation collection
	 * @return O&M 2 observation collection
	 * @throws OwsExceptionReport
	 */
	public IObservationCollection getUncertainObservationCollection(
			SosObservationCollection om1ObsCol) throws OwsExceptionReport {

		IObservationCollection om2ObsCol = null;

		if (om1ObsCol != null && om1ObsCol.getObservationMembers() != null
				&& om1ObsCol.getObservationMembers().size() > 0) {

			// collect IDs of all observations
			List<String> obsIDs = new ArrayList<String>();
			Iterator<AbstractSosObservation> obsColIt = om1ObsCol
					.getObservationMembers().iterator();
			while (obsColIt.hasNext()) {
				AbstractSosObservation obs = obsColIt.next();
				obsIDs.add(obs.getObservationID());
			}

			SosObservationCollection uncOM1ObsCol = null;

			// query uncertainties for observations IDs
			Object[] unc = getUncertainty(obsIDs);

			// add uncertainties to observation collection
			uncOM1ObsCol = addUnc2OM1ObsCol(om1ObsCol, unc);

			// convert OM1 to OM2 observation collection
			om2ObsCol = ObservationConverter.getOM2ObsCol(uncOM1ObsCol);
		}

		return om2ObsCol;
	}

	/**
	 * converts multiple O&M 1 observations plus uncertainties into uncertainty
	 * enabled O&M 2 observations; observations without uncertainty are sorted
	 * out
	 * 
	 * @param om1ObsCol
	 *            O&M 1 observation collection
	 * @param uncertainties
	 *            ResultSet of corresponding uncertainties
	 * @return O&M 2 observation collection
	 * @throws OwsExceptionReport
	 */
	public static SosObservationCollection addUnc2OM1ObsCol(
			SosObservationCollection om1ObsCol, Object[] uncertainties)
			throws OwsExceptionReport {

		// return collection, if input is empty
		if (om1ObsCol.getObservationMembers().isEmpty()
				|| om1ObsCol.getObservationMembers().size() < 1
				|| uncertainties == null || uncertainties.length < 1) {
			return om1ObsCol;
		}

		// check whether all input observations are of the same type
		Iterator<AbstractSosObservation> om1ObsIter = om1ObsCol
				.getObservationMembers().iterator();
		Class<? extends AbstractSosObservation> om1ObsClass = null;

		while (om1ObsIter.hasNext()) {

			if (om1ObsClass == null) {
				om1ObsClass = om1ObsIter.next().getClass();
			} else {
				if (om1ObsIter.next().getClass() != om1ObsClass) {
					OwsExceptionReport se = new OwsExceptionReport();
					se.addCodedException(
							OwsExceptionReport.ExceptionCode.OperationNotSupported,
							null,
							"All observations have to be of the same type (e.g. Measurement).");
					throw se;
				}
			}
		}

		// ////////////////////////////////////////////////////////
		// create uncertainty enabled OM1 observations
		om1ObsIter = om1ObsCol.getObservationMembers().iterator();

		AbstractSosObservation om1Obs;
		ArrayList<DQ_UncertaintyResult> uncResult;
		ArrayList<Mean> uncResMeans;
		ArrayList<NormalDistribution> uncResNormals;
		IUncertainty resultUnc;

		String gmlID, valueUnit, uncType = null;

		while (om1ObsIter.hasNext()) {
			om1Obs = om1ObsIter.next();

			uncResult = null;
			uncResMeans = null;
			uncResNormals = null;
			resultUnc = null;

			gmlID = null;
			valueUnit = null;
			uncType = null;

			if (om1Obs instanceof IUncertainObservation) {

				if (om1Obs instanceof UNCMeasurementObservation) {
					uncResult = new ArrayList<DQ_UncertaintyResult>();
				}

				// ////////////////////////////////////////////////////////
				// get uncertainties from resultSet
				String obsID = om1Obs.getObservationID();

				String uncID = null;
				Double meanVal = null;
				Double normalMean = null;
				Double normalVar = null;

				IUncertainty unc = null;

				for (int i = 0; i < uncertainties.length; i++) {

					// check remaining heap size
					// checkFreeMemory(); // used in
					// getSingleObservationFromResultSet()

					// add new uncertainty, if it belongs to the current
					// observation
					Object[] row = (Object[]) uncertainties[i];

					if (obsID.equals(Array.get(row, 0))) {

						// get values from array
						uncID = (String) Array.get(row, 1);
						gmlID = (String) Array.get(row, 2);
						valueUnit = (String) Array.get(row, 3);
						uncType = (String) Array.get(row, 4);
						meanVal = (Double) Array.get(row, 5);
						normalMean = (Double) Array.get(row, 6);
						normalVar = (Double) Array.get(row, 7);

						if (uncType
								.equals(PGDAOUncertaintyConstants.u_meanType)) {

							// ////////////////////////////////////////////////////////
							// create mean (or add mean value)
							if (om1Obs instanceof UNCMeasurementObservation) {
								// uncertainty observation

								if (uncResMeans == null) {
									uncResMeans = new ArrayList<Mean>();
								}
								if (uncResMeans.isEmpty()) {
									uncResMeans.add(new CMean(uncID, meanVal));

								} else {

									// search for means of this uncID
									boolean contained = false;
									for (Mean mean : uncResMeans) {
										if (((CMean) mean).uncertaintyID
												.equals(uncID)) {

											// add to existing mean
											((CMean) mean).getValues().add(
													meanVal);
											contained = true;
											break;
										}
									}
									if (!contained) {
										// add new mean
										uncResMeans.add(new CMean(uncID,
												meanVal));
									}
								}

							} else if (om1Obs instanceof UNCUncertaintyObservation) {
								// uncertainty observation

								if (resultUnc == null) {
									resultUnc = new CMean(uncID, meanVal);

								} else if (resultUnc instanceof CMean) {
									CMean resultMean = (CMean) resultUnc;

									if (resultMean.uncertaintyID.equals(uncID)) {

										resultMean.getValues().add(meanVal);

									} else {
										OwsExceptionReport se = new OwsExceptionReport(
												ExceptionLevel.DetailedExceptions);
										se.addCodedException(
												OwsExceptionReport.ExceptionCode.InvalidParameterValue,
												null,
												"UncertaintyObservations may have only one uncertainty result.");
										LOGGER.error(se.getMessage());
										throw se;
									}

								} else {
									OwsExceptionReport se = new OwsExceptionReport(
											ExceptionLevel.DetailedExceptions);
									se.addCodedException(
											OwsExceptionReport.ExceptionCode.InvalidParameterValue,
											null,
											"UncertaintyObservations may have only one uncertainty result.");
									LOGGER.error(se.getMessage());
									throw se;
								}
							}

						} else if (uncType
								.equals(PGDAOUncertaintyConstants.u_normalDistType)) {

							// ////////////////////////////////////////////////////////
							// create normal distribution

							unc = new NormalDistribution(normalMean, normalVar);

							if (om1Obs instanceof UNCMeasurementObservation) {
								// measurement observation

								if (uncResNormals == null
										|| uncResNormals.isEmpty()) {
									uncResNormals = new ArrayList<NormalDistribution>();
								}
								uncResNormals.add((NormalDistribution) unc);

							} else if (om1Obs instanceof UNCUncertaintyObservation) {
								// uncertainty observation

								if (resultUnc == null) {
									resultUnc = unc;
								} else {
									OwsExceptionReport se = new OwsExceptionReport(
											ExceptionLevel.DetailedExceptions);
									se.addCodedException(
											OwsExceptionReport.ExceptionCode.InvalidParameterValue,
											null,
											"UncertaintyObservations may have only one uncertainty result.");
									LOGGER.error(se.getMessage());
									throw se;
								}
							}

							// TODO add further uncertainty types here
							// add further uncertainty types from database
							// (resultSet)
							// } else if (om1Obs instanceof
							// UNC...ObservationConverter) {

						} else {
							OwsExceptionReport se = new OwsExceptionReport(
									ExceptionLevel.DetailedExceptions);
							se.addCodedException(
									OwsExceptionReport.ExceptionCode.MissingParameterValue,
									null, "Missing value(s) for uncertainty '"
											+ uncID + "' of observation '"
											+ obsID + "'.");
							LOGGER.error(se.getMessage());
							throw se;
						}
					}
				}
			}

			// ////////////////////////////////////////////////////////
			// add uncertainties to current observation
			// create DQ_UncertaintyResult[]
			if (om1Obs instanceof UNCMeasurementObservation) {

				// add means to uncertainty result
				if (uncResMeans != null && !uncResMeans.isEmpty()) {
					CMean[] means = uncResMeans.toArray(new CMean[0]);
					uncResult.add(new DQ_UncertaintyResult(means, valueUnit));
				}
				// add normal distributions to uncertainty result
				if (uncResNormals != null && !uncResNormals.isEmpty()) {
					NormalDistribution[] normals = uncResNormals
							.toArray(new NormalDistribution[0]);
					uncResult.add(new DQ_UncertaintyResult(normals, valueUnit));
				}
				// add uncertainty result
				if (uncResult != null && !uncResult.isEmpty()) {
					DQ_UncertaintyResult[] quality = uncResult
							.toArray(new DQ_UncertaintyResult[0]);
					((UNCMeasurementObservation) om1Obs).setUncQuality(quality);
				}
			} else if (om1Obs instanceof UNCUncertaintyObservation) {
				((UNCUncertaintyObservation) om1Obs).setUncertainty(resultUnc);
			}

			// add gml identifier
			((IUncertainObservation) om1Obs).setIdentifier(gmlID);
		}

		// ////////////////////////////////////////////////////////
		// convert OM1 observation collection with uncertainties to
		// uncertainty enabled OM2 observation collection
		return om1ObsCol;
	}

	/**
	 * builds and executes the query to get the observations from the database;
	 * this method is also used from the GetResultDAO
	 * 
	 * @param request
	 *            getObservation request
	 * @return Returns ResultSet containing the results of the query
	 * @throws OwsExceptionReport
	 *             if query failed
	 */
	private ResultSet queryObservation(SosGetObservationRequest request,
			Connection con) throws OwsExceptionReport {

		ResultSet resultSet = null;

        // ////////////////////////////////////////////
        // get parameters from request
        String[] offering = request.getOffering();
        TemporalFilter[] temporalFilter = request.getEventTime();
        String[] procedures = request.getProcedure();
        String[] phenomena = request.getObservedProperty();
        boolean hasSpatialPhens = false;
        // int maxRecords = request.getMaxRecords();
        String srsName = request.getSrsName();
        if (!srsName.equals(SosConstants.PARAMETER_NOT_SET)) {
            setRequestSrid(ResultSetUtilities.parseSrsName(srsName));
        }
        if (filterSpatialPhenomena(phenomena).length > 0) {
            hasSpatialPhens = true;
        }

        // ///////////////////////////////////////////////
        // build query
        StringBuilder query = new StringBuilder();

        // select clause
        query.append(getSelectClause(isSupportsQuality(), request.isMobileEnabled()));

        // add geometry column to list, if srsName parameter is set, transform
        // coordinates into request system
        query.append(getGeometriesAsTextClause(srsName, request.isMobileEnabled()));

        // natural join of tables       
		if (request.getResponseFormat() != null
				&& request.getResponseFormat().equals(
						SosUncConstants.CONTENT_TYPE_OM2)) {

			query.append(this.getFromClause(isSupportsQuality(), request.isMobileEnabled(), true));
		} else {
			query.append(super.getFromClause(isSupportsQuality(), request.isMobileEnabled()));
		}

        List<String> whereClauses = new ArrayList<String>();

        // append mandatory observedProperty parameters
        if (phenomena != null && phenomena.length > 0) {
            whereClauses.add(getWhereClause4ObsProps(phenomena));
        }

        // append mandatory offering parameter
        if (offering != null && offering.length > 0) {
            Set<String> queryOfferings = new HashSet<String>();
            if (request.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                for (String offId : offering) {
                    if (offId.contains(SosConstants.SEPARATOR_4_OFFERINGS)) {
                        String[] offArray = offId.split(SosConstants.SEPARATOR_4_OFFERINGS);
                        StringBuilder offProcQuery = new StringBuilder();
                        offProcQuery.append("(");
                        offProcQuery.append(PGDAOConstants.getOfferingIDCn() + " = '" + offArray[0] + "'");
                        offProcQuery.append(" AND ");
                        offProcQuery.append(PGDAOConstants.getProcIDCn() + " = '" + offArray[1] + "'");
                        offProcQuery.append(")");
                        queryOfferings.add(offProcQuery.toString());
                    } else {
                        queryOfferings.add("(" + PGDAOConstants.getOfferingIDCn() + " = '" + offId + "')");
                    }
                }
            } else {
                for (String off : offering) {
                    queryOfferings.add("(" + PGDAOConstants.getOfferingIDCn() + " = '" + off + "')");
                }
//                queryOfferings = new HashSet<String>(Arrays.asList(offering));
            }
            whereClauses.add(getWhereClause4Offering(queryOfferings.toArray(new String[0])));
        }

        // append feature of interest parameter
        if (request.getFeatureOfInterest() != null && request.getFeatureOfInterest().length > 0) {
            whereClauses.add(getWhereClause4Foi(request.getFeatureOfInterest(), request.isMobileEnabled()));
        }

        // append domain feature parameter
        if ((request.getDomainFeature() != null && request.getDomainFeature().length > 0)
                || request.getDomainFeatureSpatialFilter() != null) {
            whereClauses.add(getWhereClause4DomainFeature(request.getDomainFeature(),
                    request.getDomainFeatureSpatialFilter()));
        }

        // append optional parameters
        if (procedures != null && procedures.length > 0) {
            whereClauses.add(getWhereClause4Procedures(procedures));
        }
        // append temporal filter parameter
        if (temporalFilter != null && temporalFilter.length > 0) {
            whereClauses.add(getWhereClause4Time(temporalFilter));
        }

        // append parameter for Result
        if (request.getResult() != null) {
            whereClauses.add(getWhereClause4Result(request.getResult(), offering, phenomena));
        }

        // append spatial filter parameter
        if (request.getResultSpatialFilter() != null) {
            whereClauses.add(getWhereClause4SpatialFilter(request.getResultSpatialFilter()));
        }

        if (whereClauses.size() > 0) {
            query.append(" WHERE ");
            int clauseCount = whereClauses.size();
            for (int i = 0; i < clauseCount; i++) {
                query.append("(");
                query.append(whereClauses.get(i));
                if (i != clauseCount - 1) {
                    query.append(") AND ");
                } else {
                    query.append(") ");
                }
            }
        }
        query.append(";");

        LOGGER.info("<<<QUERY>>>: " + query.toString());

        // //////////////////////////////////////////////////
        // execute query
        Statement stmt = null;
        try {
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = stmt.executeQuery(query.toString());
        } catch (SQLException sqle) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            LOGGER.error("An error occured while query the data from the database!", sqle);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, sqle);
            throw se;
        }

        return resultSet;
	} // end queryObservation

	/**
	 * creates a from and join clause 4 non spatial
	 * 
	 * @param uncertainties
	 *            get only observations with uncertainties
	 * @return String containing the from clause
	 */
	private String getFromClause(boolean quality, boolean isMobile, boolean uncertainties) {
		StringBuilder from = new StringBuilder();
		from.append(" FROM (" + PGDAOConstants.getObsTn() + " NATURAL INNER JOIN "
				+ PGDAOConstants.getPhenTn() + " NATURAL INNER JOIN "
				+ PGDAOConstants.getFoiTn());
		if (uncertainties) {
			from.append(" NATURAL INNER JOIN "
					+ PGDAOUncertaintyConstants.uObsUncTn);
		}
		if (isMobile) {
			from.append(" LEFT OUTER JOIN " + PGDAOConstants.getObsDfTn() + " ON "
					+ PGDAOConstants.getObsDfTn() + "." + PGDAOConstants.getObsIDCn() + " = "
					+ PGDAOConstants.getObsTn() + "." + PGDAOConstants.getObsIDCn()
					+ " LEFT OUTER JOIN " + PGDAOConstants.getDfTn() + " ON "
					+ PGDAOConstants.getObsDfTn() + "."
					+ PGDAOConstants.getDomainFeatureIDCn() + " = "
					+ PGDAOConstants.getDfTn() + "." + PGDAOConstants.getDomainFeatureIDCn());
		}
		if (quality) {
			from.append(" LEFT JOIN " + PGDAOConstants.getQualityTn() + " ON "
					+ PGDAOConstants.getQualityTn() + "." + PGDAOConstants.getObsIDCn()
					+ " = " + PGDAOConstants.getObsTn() + "."
					+ PGDAOConstants.getObsIDCn());
		}
		from.append(")");
		return from.toString();
	}// end getFromClause

	/**
	 * creates an ObservationCollection from the db ResultSet.
	 * 
	 * @param resultSet
	 *            ResultSet with the queried information
	 * @param resultSetSize
	 *            Size of the ResultSet
	 * @param request
	 *            getObservation request
	 * @param resultModel
	 *            QName of the result model
	 * @return SosObservationCollection
	 * @throws OwsExceptionReport
	 */
	private SosObservationCollection getSingleObservationsFromResultSet(
			ResultSet resultSet, SosGetObservationRequest request,
			QName resultModel) throws OwsExceptionReport {
		SosObservationCollection obsCol = new SosObservationCollection();
		HashMap<String, AbstractSosObservation> obs4obsIDs = new HashMap<String, AbstractSosObservation>();
		HashMap<String, AbstractSosObservation> obs4Procs = new HashMap<String, AbstractSosObservation>();

		WKTWriter wktWriter = new WKTWriter();
		int srid = 0;
		try {

			// now iterate over resultset and create Measurement for each row
			while (resultSet.next()) {

				// check remaining heap size
				checkFreeMemory();

				String obsID = resultSet.getString(PGDAOConstants.getObsIDCn());

				if (obs4obsIDs.containsKey(obsID)) {
					
					// mobile enabled
					if (request.isMobileEnabled()) {
					
						if (resultSet.getString(PGDAOConstants.getDomainFeatureIDCn()) != null
								&& !resultSet.getString(
										PGDAOConstants.getDomainFeatureIDCn())
										.equals("")) {
							AbstractSosObservation obs = obs4obsIDs.get(obsID);
							
							String dfID = resultSet.getString(PGDAOConstants.getDomainFeatureIDCn());
							obs.addDomainFeatureID(new SosGenericDomainFeature(dfID));
						}
					}
					// supports quality
					if (isSupportsQuality()) {
						String qualityTypeString = resultSet
								.getString(PGDAOConstants.getQualTypeCn());
						String qualityUnit = resultSet
								.getString(PGDAOConstants.getQualUnitCn());
						String qualityName = resultSet
								.getString(PGDAOConstants.getQualNameCn());
						String qualityValue = resultSet
								.getString(PGDAOConstants.getQualValueCn());
						QualityType qualityType = QualityType
								.valueOf(qualityTypeString);
						SosQuality quality = new SosQuality(qualityName,
								qualityUnit, qualityValue, qualityType);
						obs4obsIDs.get(obsID).addSingleQuality(quality);
					}

				} else {
					String offeringID = resultSet
							.getString(PGDAOConstants.getOfferingIDCn());
					String mimeType = SosConstants.PARAMETER_NOT_SET;

					// create time element
					String timeString = resultSet
							.getString(PGDAOConstants.getTimestampCn());
					DateTime timeDateTime = SosDateTimeUtilities
							.parseIsoString2DateTime(timeString);
					TimeInstant time = new TimeInstant(timeDateTime, "");

					String phenID = resultSet
							.getString(PGDAOConstants.getPhenIDCn());
					String valueType = resultSet
							.getString(PGDAOConstants.getValueTypeCn());
					String procID = resultSet
							.getString(PGDAOConstants.getProcIDCn());

					String unit = resultSet.getString(PGDAOConstants.getUnitCn());

					// domain feature
					String domainFeatID = null;
					ArrayList<SosAbstractFeature> domainFeatIDs = null;
					if (request.isMobileEnabled()) {
						if (resultSet.getString(PGDAOConstants.getDomainFeatureIDCn()) != null
								&& !resultSet.getString(
										PGDAOConstants.getDomainFeatureIDCn())
										.equals("")) {
							domainFeatID = resultSet
									.getString(PGDAOConstants.getDomainFeatureIDCn());
							domainFeatIDs = new ArrayList<SosAbstractFeature>();
							domainFeatIDs.add(new SosGenericDomainFeature(
									domainFeatID));
						}
					}

					// feature of interest
					String foiID = resultSet.getString(PGDAOConstants.getFoiIDCn());
					String foiName = resultSet
							.getString(PGDAOConstants.getFoiNameCn());
					String foiType = resultSet
							.getString(PGDAOConstants.getFeatureTypeCn());

					// foi geometry
					String foiGeomWKT = resultSet
							.getString(PGDAOConstants.getFoiGeometry());
					srid = checkRequestSridQuerySrid(resultSet
							.getInt(PGDAOConstants.getFoiSrid()));
					SosAbstractFeature foi = org.n52.sos.uncertainty.ds.pgsql.ResultSetUtilities
							.getAbstractFeatureFromValues(foiID,
									SosConstants.PARAMETER_NOT_SET, foiName,
									foiGeomWKT, srid, foiType,
									SosConstants.PARAMETER_NOT_SET,
									domainFeatIDs);
					setBoundedBy(checkEnvelope(getBoundedBy(), foi.getGeom()));

					// create quality
					ArrayList<SosQuality> qualityList = null;
					if (isSupportsQuality()) {
						String qualityTypeString = resultSet
								.getString(PGDAOConstants.getQualTypeCn());
						String qualityUnit = resultSet
								.getString(PGDAOConstants.getQualUnitCn());
						String qualityName = resultSet
								.getString(PGDAOConstants.getQualNameCn());
						String qualityValue = resultSet
								.getString(PGDAOConstants.getQualValueCn());
						qualityList = new ArrayList<SosQuality>();
						if (qualityValue != null) {
							QualityType qualityType = QualityType
									.valueOf(qualityTypeString);
							SosQuality quality = new SosQuality(qualityName,
									qualityUnit, qualityValue, qualityType);
							qualityList.add(quality);
						}
					}

					if (request.getResponseMode() != null
							&& !request.getResponseMode().equals(
									SosConstants.PARAMETER_NOT_SET)) {
						// if responseMode is resultTemplate, then create
						// observation template and return it
						if (request.getResponseMode() == SosConstants.RESPONSE_RESULT_TEMPLATE) {
							return getResultTemplate(resultSet, request);
						} else {
							checkResponseModeInline(request.getResponseMode());
						}
					}
					// if (Measurement, CategoryObs, Spatial, Observ)
					if (resultModel == null
							|| resultModel
									.equals(SosConstants.RESULT_MODEL_OBSERVATION)) {
						String value;
						if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.booleanType
										.name())) {
							value = resultSet
									.getString(PGDAOConstants.getTextValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.countType
										.name())) {
							value = Integer.toString((int) Math.round(resultSet
									.getDouble(PGDAOConstants.getNumericValueCn()))); // make
																					// sure
																					// we
																					// write
																					// an
																					// integer
																					// value
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.numericType
										.name())) {
							value = resultSet
									.getString(PGDAOConstants.getNumericValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.isoTimeType
										.name())) {
							value = new DateTime(
									resultSet
											.getLong(PGDAOConstants.getNumericValueCn()))
									.toString();
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.textType
										.name())) {
							value = resultSet
									.getString(PGDAOConstants.getTextValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.categoryType
										.name())) {
							value = resultSet
									.getString(PGDAOConstants.getTextValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.spatialType
										.name())) {
							Geometry value_geom = JTSUtilities.createGeometryFromWKT(resultSet.getString(PGDAOConstants
                                    .getValueGeometry()));
							srid = checkRequestSridQuerySrid(value_geom
									.getSRID());
							if (SosConfigurator.getInstance()
									.switchCoordinatesForEPSG(srid)) {
								value_geom = JTSUtilities.switchCoordinate4Geometry(value_geom);
							}
							value = wktWriter.write(value_geom) + "#" + srid;

						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.uncertaintyType
										.name())) {
							value = SosConstants.PARAMETER_NOT_SET;

						} else {
							OwsExceptionReport se = new OwsExceptionReport();
							se.addCodedException(
									OwsExceptionReport.ExceptionCode.InvalidParameterValue,
									valueType,
									"The valueType '"
											+ valueType
											+ "' is not supported for om:Observation or no resultModel!");
							LOGGER.error(
									"The valueType '"
											+ valueType
											+ "' is not supported for om:Observation or no resultModel!",
									se);
							throw se;
						}

						if (obs4Procs.containsKey(procID)) {
							SosGenericObservation sosGenObs = (SosGenericObservation) obs4Procs
									.get(procID);
							sosGenObs.addFeature(foi);
							sosGenObs.addValue(timeDateTime, foiID, phenID,
									value);
						} else {
							SosGenericObservation observation = new SosGenericObservation(
									new ArrayList<String>(), procID,
									offeringID, getTokenSeperator(), getTupleSeperator(),
									getNoDataValue());
							observation.addFeature(foi);
							observation.addValue(timeDateTime, foiID, phenID,
									value);
							observation.setObservationID(obsID);
							obs4Procs.put(procID, observation);
						}
					} else if (resultModel
							.equals(SosConstants.RESULT_MODEL_MEASUREMENT)
					// || valueType
					// .equalsIgnoreCase(SosConstants.ValueTypes.numericType
					// .toString())
					) {
						// if responseMode is resultTemplate, then create
						// observation template and return it
						if (request.getResponseMode() == SosConstants.RESPONSE_RESULT_TEMPLATE) {
							return getResultTemplate(resultSet, request);
						} else {
							checkResponseModeInline(request.getResponseMode());
						}

						double value = Double.NaN;
						if (resultSet.getString(PGDAOConstants.getNumericValueCn()) != null) {
							value = resultSet
									.getDouble(PGDAOConstants.getNumericValueCn());
						}

						SosMeasurement measurement = new SosMeasurement(time,
								obsID, procID, domainFeatIDs, phenID, foi,
								offeringID, mimeType, value, unit, qualityList);
						obs4obsIDs.put(obsID, measurement);
					} else if (resultModel
							.equals(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION)) {

						checkResponseModeInline(request.getResponseMode());

						String value = resultSet
								.getString(PGDAOConstants.getTextValueCn());
						if (value == null || (value != null && value.isEmpty())) {
							try {
								value = new DateTime(
										resultSet
												.getLong(PGDAOConstants.getNumericValueCn()))
										.toString();
							} catch (Exception e) {
								// nothing to throw,
								value = null;
							}
						}

						SosCategoryObservation categoryObservation = new SosCategoryObservation(
								time, obsID, procID, foi, domainFeatIDs,
								phenID, offeringID, mimeType, value, unit,
								qualityList);
						obs4obsIDs.put(obsID, categoryObservation);
					} else if (resultModel
							.equals(SosConstants.RESULT_MODEL_SPATIAL_OBSERVATION)) {
						String value_geomWKT = resultSet
								.getString(PGDAOConstants.getValueGeometry());
						srid = checkRequestSridQuerySrid(resultSet
								.getInt(PGDAOConstants.getValueSrid()));
						Geometry jts_value_geometry = ResultSetUtilities
								.createJTSGeom(value_geomWKT, srid);
						setBoundedBy(checkEnvelope(getBoundedBy(), jts_value_geometry));

						SosSpatialObservation spatialObs = new SosSpatialObservation(
								time, obsID, procID, domainFeatIDs, phenID,
								foi, offeringID, jts_value_geometry,
								qualityList);
						obs4obsIDs.put(obsID, spatialObs);
					} else if (resultModel
							.equals(SosUncConstants.RESULT_MODEL_MEASUREMENT)) {
						Double value = Double.NaN;
						if (resultSet.getString(PGDAOConstants.getNumericValueCn()) != null) {
							value = resultSet
									.getDouble(PGDAOConstants.getNumericValueCn());
						}

						UNCMeasurementObservation measurement = new UNCMeasurementObservation(
								time, obsID, procID, domainFeatIDs, phenID,
								foi, offeringID, mimeType, value, unit,
								qualityList);

						obs4obsIDs.put(obsID, measurement);
					} else if (resultModel
							.equals(SosUncConstants.RESULT_MODEL_UNCERTAINTY_OBSERVATION)) {

						UNCUncertaintyObservation uncertaintyObservation = new UNCUncertaintyObservation(
								time, obsID, procID, domainFeatIDs, phenID,
								foi, offeringID, mimeType, unit, qualityList);

						obs4obsIDs.put(obsID, uncertaintyObservation);
					}
				}
			}
		} catch (SQLException sqle) {
			OwsExceptionReport se = new OwsExceptionReport(
					ExceptionLevel.DetailedExceptions);
			se.addCodedException(ExceptionCode.NoApplicableCode, null,
					"Error while creating observations from database query result set: "
							+ sqle.getMessage());
			LOGGER.error(se.getMessage());
			throw se;
		}

		if (obs4obsIDs.size() == 0) {
			if (obs4Procs.size() == 0) {
				return obsCol;
			} else {
				obsCol.setObservationMembers(obs4Procs.values());
			}
		} else {
			obsCol.setObservationMembers(obs4obsIDs.values());
		}

		obsCol.setBoundedBy(getBoundedBy());
		obsCol.setSrid(srid);

		return obsCol;
	}// end getSingleObservationFromResultSet

	public SosObservationCollection getObservation(
			SosGetObservationRequest request) throws OwsExceptionReport {
		// setting a global "now" for this request
		setNow(new DateTime());

		// ObservationCollection object which will be returned
		SosObservationCollection response = new SosObservationCollection();
		Connection con = null;
		setRequestSrid(-1);
		setBoundedBy(null);

		try {
			if (request.getObservedProperty().length == 0 && request.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                        GetObservationParams.observedProperty.toString(),
                        "The request contains no observed Properties!");
                throw se;
            } else {
                boolean hasSpatialPhen = false;
                if (Arrays.asList(request.getObservedProperty()).contains(
                                SosConfigurator.getInstance().getSpatialObsProp4DynymicLocation())) {
                    hasSpatialPhen = true;
                }
				if (request.getObservedProperty().length > 0) {
					if (!(request.getSrsName() == null
							|| request.getSrsName().equals("") || !request
							.getSrsName().startsWith(
									SosConfigurator.getInstance()
											.getSrsNamePrefix()))) {
						setRequestSrid(ResultSetUtilities.parseSrsName(request
								.getSrsName()));
					}
					checkResultModel(request.getResultModel(),
							request.getObservedProperty());
	
					List<ResultSet> resultSetList = new ArrayList<ResultSet>();
	
					con = getCPool().getConnection();
	
					// if timeInstant contains "latest", return the last observation
					// for each phen/proc/foi/df
					if (request.getEventTime() != null && request.getEventTime().length > 0) {
	                    for (TemporalFilter tf : request.getEventTime()) {
	                        if (tf.getTime().getIndeterminateValue() != null) {
	                            if (tf.getTime().getIndeterminateValue().equals(FirstLatest.latest.name())) {
	                                resultSetList.add(queryLatestObservations(request, tf, con, hasSpatialPhen));
	                            } else if (tf.getTime().getIndeterminateValue()
	                                    .equalsIgnoreCase(FirstLatest.getFirst.name())) {
	                                resultSetList.add(queryGetFirstObservations(request, tf, con, hasSpatialPhen));
	                            } else {
	                                resultSetList.add(queryObservation(request, con, hasSpatialPhen));
	                            }
	                        } else {
	                            resultSetList.add(queryObservation(request, con, hasSpatialPhen));
	                        }
	                    }
	                } else {
	                    resultSetList.add(queryObservation(request, con, hasSpatialPhen));
	                }
					// end get ResultSets
					for (ResultSet resultSet : resultSetList) {
	
						// if resultModel parameter is set in the request, check,
						// whether it is correct and then return request
						// observations
						QName resultModel = request.getResultModel();
						// check ResponseMode
						if (request.getResponseMode() != null
								&& !request.getResponseMode().equalsIgnoreCase(
										SosConstants.PARAMETER_NOT_SET)) {
							if (request.getResponseMode() == SosConstants.RESPONSE_RESULT_TEMPLATE) {
								return getResultTemplate(resultSet, request);
							}
	
						}
						// check ResultModel
						if (resultModel == null
								|| resultModel
										.equals(SosConstants.RESULT_MODEL_MEASUREMENT)
								|| resultModel
										.equals(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION)
								|| resultModel
										.equals(SosConstants.RESULT_MODEL_OBSERVATION)
								|| resultModel
										.equals(SosConstants.RESULT_MODEL_SPATIAL_OBSERVATION)
								|| resultModel
										.equals(SosUncConstants.RESULT_MODEL_MEASUREMENT)
								|| resultModel
										.equals(SosUncConstants.RESULT_MODEL_UNCERTAINTY_OBSERVATION)) {
							response.addColllection(getSingleObservationsFromResultSet(
									resultSet, request, resultModel));
	
						} else {
							OwsExceptionReport se = new OwsExceptionReport();
							se.addCodedException(
									OwsExceptionReport.ExceptionCode.InvalidParameterValue,
									GetObservationParams.resultModel.toString(),
									"The value ("
											+ resultModel
											+ ") of the parameter '"
											+ GetObservationParams.resultModel
													.toString()
											+ "' is not supported by this SOS!");
							throw se;
						}
					}
				} else {
					OwsExceptionReport se = new OwsExceptionReport();
					se.addCodedException(
							OwsExceptionReport.ExceptionCode.InvalidParameterValue,
							GetObservationParams.observedProperty.toString(),
							"The request contains no observed Properties!");
					throw se;
				}
			}
			response.setBoundedBy(getBoundedBy());
			response.setSrid(getRequestSrid());
		} catch (OwsExceptionReport se) {
			throw se;
		} catch (Exception e) {
			OwsExceptionReport se = new OwsExceptionReport();
			se.addCodedException(
					OwsExceptionReport.ExceptionCode.InvalidParameterValue,
					"PGSQLGetObservationDAO", "Error during GetObservation: "
							+ e.getMessage());
			throw se;
		} finally {
			if (con != null) {
				getCPool().returnConnection(con);
			}
		}
		return response;
	}// end getObservation
}
