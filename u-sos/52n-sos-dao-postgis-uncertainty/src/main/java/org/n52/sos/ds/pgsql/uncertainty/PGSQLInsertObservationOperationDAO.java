package org.n52.sos.ds.pgsql.uncertainty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ds.insert.pgsql.uncertainty.InsertUncertaintyDAO;
import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.om.uncertainty.UNCUncertaintyObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sos.ogc.uncertainty.IUncertainObservation;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.DQ_UncertaintyResult;

/**
 * data access object for InsertObservation operation
 * 
 * @author Kiesow
 * 
 */
public class PGSQLInsertObservationOperationDAO extends
		org.n52.sos.ds.pgsql.PGSQLInsertObservationOperationDAO {

	private InsertUncertaintyDAO insertUncDAO;

	/**
	 * constructor
	 * 
	 * @param cpool
	 *            PGConnectionPool which contains the connections to the DB
	 */
	public PGSQLInsertObservationOperationDAO(PGConnectionPool cpool) {
		super(cpool);
		this.insertUncDAO = new InsertUncertaintyDAO(cpool);
	}

	/**
	 * method for inserting observation into database; should insert also new
	 * feature of interests into db; wrapper adding uncertainty support to super
	 * class method
	 * 
	 * @param observation
	 *            should be inserted into db
	 * @return Returns id for observation, which is generated by the 52 North
	 *         SOS
	 * @throws OwsExceptionReport
	 *             if insert of Observation fails
	 */
	public int insertObservation(AbstractSosObservation observation,
			boolean mobileEnabled) throws OwsExceptionReport {

		List<Integer> uncIDs = new ArrayList<Integer>();
		int obsID = Integer.MIN_VALUE;
		String identifier = null;

		Connection trCon = null;

		// //////////////////////////////////////////////////
		// insert uncertainty
		if ((observation instanceof SosObservationCollection)
				&& ((SosObservationCollection) observation)
						.getObservationMembers().size() == 1) {

			AbstractSosObservation obs = (AbstractSosObservation) ((SosObservationCollection) observation)
					.getObservationMembers().toArray()[0];

			if (obs instanceof IUncertainObservation) {
				
				// set identifier to insert with obs_unc relationship
				identifier = ((IUncertainObservation) obs).getIdentifier();

				// connect to data base
				try {
					trCon = cpool.getConnection();
					trCon.setAutoCommit(false);

					// //////////////////////////////////////////////////
					// add uncertainty result from UncertaintyObservation
					if (obs instanceof UNCUncertaintyObservation) {

						UNCUncertaintyObservation uncObs = (UNCUncertaintyObservation) obs;
						
						// insert uncertainty
						uncIDs.add(this.insertUncDAO.insertUncertainty(uncObs,
								uncObs.getUncertainty(), uncObs.getUnitsOfMeasurement(), trCon));

					} else {

						// //////////////////////////////////////////////////
						// add uncertainties from resultQuality
						uncIDs.addAll(this.insertUncDAO.insertUncertaintiesFromRQ((IUncertainObservation) obs, trCon));
					}

					trCon.commit();
					trCon.setAutoCommit(true);

					/*
					 * if uncertainty is inserted AFTER the corresponding
					 * observation, capabilities cache has to be refreshed
					 */

				} catch (SQLException e) {
					String message = "Error while executing insertUncertainty operation. Values could not be stored in database: "
							+ e.getMessage();
					OwsExceptionReport se = new OwsExceptionReport(
							ExceptionLevel.DetailedExceptions);
					se.addCodedException(ExceptionCode.NoApplicableCode, null,
							message);
					LOGGER.error(message);
					throw se;
				} finally {
					if (trCon != null) {
						cpool.returnConnection(trCon);
					}
				}
			}
		}

		// //////////////////////////////////////////////////
		// insert observation to get observation ID
		obsID = super.insertObservation(observation, mobileEnabled);

		// //////////////////////////////////////////////////
		// insert obs_unc relationsship
		if (!uncIDs.isEmpty()) {

			// connect to data base

			try {
				trCon = cpool.getConnection();
				trCon.setAutoCommit(false);

				// insert relationship
				this.insertUncDAO.insertObsUncRelationships(obsID, uncIDs,
						identifier, trCon);

				// commit
				trCon.commit();
				trCon.setAutoCommit(true);

			} catch (SQLException e) {
				String message = "Error while executing insertObsUncRelationship operation. Values could not be stored in database: "
						+ e.getMessage();
				OwsExceptionReport se = new OwsExceptionReport(
						ExceptionLevel.DetailedExceptions);
				se.addCodedException(ExceptionCode.NoApplicableCode, null,
						message);
				LOGGER.error(message);
				throw se;
			} finally {
				if (trCon != null) {
					cpool.returnConnection(trCon);
				}
			}
		}
		return obsID;
	}

//	/**
//	 * method for inserting observation into database; should insert also new
//	 * feature of interests into db; wrapper adding uncertainty support to super
//	 * class method
//	 * 
//	 * @param observation
//	 *            should be inserted into db
//	 * @return Returns id for observation, which is generated by the 52 North
//	 *         SOS
//	 * @throws OwsExceptionReport
//	 *             if insert of Observation fails
//	 */
//	public int insertObservation(AbstractSosObservation observation,
//			boolean mobileEnabled) throws OwsExceptionReport {
//
//		// //////////////////////////////////////////////////
//		// insert observation to get observation ID
//		int obsID = super.insertObservation(observation, mobileEnabled);
//
//		// //////////////////////////////////////////////////
//		// insert uncertainty
//		if ((observation instanceof SosObservationCollection)
//				&& ((SosObservationCollection) observation)
//						.getObservationMembers().size() == 1) {
//
//			AbstractSosObservation obs = (AbstractSosObservation) ((SosObservationCollection) observation)
//					.getObservationMembers().toArray()[0];
//
//			if (obs instanceof IUncertainObservation) {
//
//				// connect to data base
//				Connection trCon = null;
//				try {
//					trCon = cpool.getConnection();
//					trCon.setAutoCommit(false);
//
//					// //////////////////////////////////////////////////
//					// add uncertainty result from UncertaintyObservation
//					if (obs instanceof UNCUncertaintyObservation) {
//
//						UNCUncertaintyObservation uncObs = (UNCUncertaintyObservation) obs;
//
//						IUncertainty unc = uncObs.getUncertainty();
//
//						// insert uncertainty
//						int uncID = this.insertUncDAO.insertUncertainty(uncObs,
//								unc, trCon);
//
//						// insert obs_unc relationsship
//						this.insertUncDAO.insertObsUncRelationship(obsID,
//								uncID, uncObs.getIdentifier(), trCon);
//
//					} else {
//
//						// //////////////////////////////////////////////////
//						// add uncertainties from resultQuality
//						IUncertainObservation uncObs = (IUncertainObservation) obs;
//						DQ_UncertaintyResult[] quality = uncObs.getUncQuality();
//						if (quality != null && quality.length > 0) {
//
//							for (DQ_UncertaintyResult uncRes : quality) {
//
//								// TODO handle UOM
//								String uom = uncRes.getUom();
//								IUncertainty[] uncs = uncRes.getValues();
//
//								for (IUncertainty unc : uncs) {
//
//									// insert uncertainty
//									int uncID = this.insertUncDAO
//											.insertUncertainty(uncObs, unc,
//													trCon);
//
//									// insert obs_unc relationsship
//									this.insertUncDAO.insertObsUncRelationship(
//											obsID, uncID,
//											uncObs.getIdentifier(), trCon);
//								}
//							}
//						}
//					}
//
//					trCon.commit();
//					trCon.setAutoCommit(true);
//
//					/*
//					 * if uncertainty is inserted AFTER the corresponding
//					 * observation, capabilities cache has to be refreshed
//					 */
//
//				} catch (SQLException e) {
//					String message = "Error while executing insertUncertainty operation. Values could not be stored in database: "
//							+ e.getMessage();
//					OwsExceptionReport se = new OwsExceptionReport(
//							ExceptionLevel.DetailedExceptions);
//					se.addCodedException(ExceptionCode.NoApplicableCode, null,
//							message);
//					LOGGER.error(message);
//					throw se;
//				} finally {
//					if (trCon != null) {
//						cpool.returnConnection(trCon);
//					}
//				}
//			}
//		}
//		return obsID;
//	}

}
