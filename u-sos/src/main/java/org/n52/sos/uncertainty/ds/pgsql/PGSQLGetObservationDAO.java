package org.n52.sos.uncertainty.ds.pgsql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.n52.sos.Sos1Constants;
import org.n52.sos.Sos1Constants.GetObservationParams;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants;
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
import org.n52.sos.uncertainty.decode.impl.OM2Constants;
import org.n52.sos.uncertainty.decode.impl.ObservationConverter;
import org.n52.sos.uncertainty.ogc.om.UNCMeasurementObservation;
import org.n52.sos.uncertainty.ogc.om.UNCUncertaintyObservation;
import org.n52.sos.utilities.JTSUtilities;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.AbstractRealisation;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.CategoricalRealisation;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.SystematicSample;
import org.uncertml.sample.UnknownSample;
import org.uncertml.statistic.ConstraintType;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Probability;
import org.uncertml.statistic.ProbabilityConstraint;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;

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
	 * @param numberOfRealisations
	 *            maximum number of realisations per sample
	 * @return list of observation IDs and attached uncertainties; null, if
	 *         obsIDs is empty
	 * @throws OwsExceptionReport
	 */
	private List<WrappedUncertainty> getUncertaintyData(List<String> obsIDs,
			int numberOfRealisations) throws OwsExceptionReport {

		if (obsIDs.isEmpty() || obsIDs.size() < 1) {
			return null;
		}

		List<WrappedUncertainty> uncs = new ArrayList<WrappedUncertainty>();

		Connection con = null;
		try {
			con = getCPool().getConnection();
			ResultSet rs = queryUncertainty(obsIDs, con);

			String obsID;
			String gmlID;
			String valueUnit;
			String uncType;

			double[] meanValues;
			Double normalMean;
			Double normalVar;
			Double weight;
			BigDecimal[] realConDecs;
			double[] realConValues;
			String singleRealID;
			String samplingMethodDescription;
			boolean sampleType;
			ArrayList<AbstractRealisation> reals;
			double[] probValues;
			Double gt, lt, ge, le;

			IUncertainty unc;

			while (rs.next()) {

				// reset uncertainty
				unc = null;
				sampleType = false;

				// get general data
				obsID = rs.getString(PGDAOUncertaintyConstants.uOUObsIdCn);
				// rs.getString(PGDAOUncertaintyConstants.uUUncIdCn);
				gmlID = rs.getString(PGDAOUncertaintyConstants.uOUGmlIdCn);
				valueUnit = rs
						.getString(PGDAOUncertaintyConstants.uVUValUnitCn);

				uncType = rs.getString(PGDAOUncertaintyConstants.uUUncTypeCn);

				// get uncertainty specific data create uncertainties

				if (uncType.equals(PGDAOUncertaintyConstants.u_meanType)) {
					// mean

					// convert BigDecimal[] to double[]
					BigDecimal[] bigDecs = (BigDecimal[]) rs.getArray(
							PGDAOUncertaintyConstants.uMMeanValsCn).getArray();
					meanValues = new double[bigDecs.length];

					for (int i = 0; i < bigDecs.length; i++) {
						meanValues[i] = bigDecs[i].doubleValue();
					}

					unc = new Mean(meanValues);

				} else if (uncType
						.equals(PGDAOUncertaintyConstants.u_normalDistType)) {
					// normal distribution

					normalMean = rs
							.getDouble(PGDAOUncertaintyConstants.uNMeanCn);
					normalVar = rs.getDouble(PGDAOUncertaintyConstants.uNVarCn);

					unc = new NormalDistribution(normalMean, normalVar);

				} else if (uncType.equals(PGDAOUncertaintyConstants.u_realType)) {
					// realisation

					weight = rs.getDouble(PGDAOUncertaintyConstants.uRWeightCn);

					java.sql.Array conAr = rs
							.getArray(PGDAOUncertaintyConstants.uRConValsCn);
					java.sql.Array catAr = rs
							.getArray(PGDAOUncertaintyConstants.uRCatValsCn);

					if (conAr != null) {
						// continuous relisation

						// convert BigDecimal[] to double[]
						realConDecs = (BigDecimal[]) conAr.getArray();
						realConValues = new double[realConDecs.length];

						for (int i = 0; i < realConDecs.length; i++) {
							realConValues[i] = realConDecs[i].doubleValue();
						}

						unc = new ContinuousRealisation(realConValues, weight);

					} else if (catAr != null) {
						// categorical realisation

						unc = new CategoricalRealisation(
								(String[]) catAr.getArray(), weight);
					}
				} else if (uncType
						.equals(PGDAOUncertaintyConstants.u_randomSType)
						|| uncType
								.equals(PGDAOUncertaintyConstants.u_systematicSType)
						|| uncType
								.equals(PGDAOUncertaintyConstants.u_unknownSType)) {
					// sample (containing realisations)

					weight = rs.getDouble(PGDAOUncertaintyConstants.uRWeightCn);

					java.sql.Array conAr = rs
							.getArray(PGDAOUncertaintyConstants.uRConValsCn);
					java.sql.Array catAr = rs
							.getArray(PGDAOUncertaintyConstants.uRCatValsCn);

					singleRealID = rs
							.getString(PGDAOUncertaintyConstants.uRIdCn);
					samplingMethodDescription = rs
							.getString(PGDAOUncertaintyConstants.uRSamMethDescCn);
					sampleType = true;

					if (conAr != null) {
						// continuous relisation

						// convert BigDecimal[] to double[]
						realConDecs = (BigDecimal[]) conAr.getArray();
						realConValues = new double[realConDecs.length];

						for (int i = 0; i < realConDecs.length; i++) {
							realConValues[i] = realConDecs[i].doubleValue();
						}

						reals = new ArrayList<AbstractRealisation>(1);
						reals.add(new ContinuousRealisation(realConValues,
								weight, singleRealID));

						// create sample depending on uncertainty type
						if (uncType
								.equals(PGDAOUncertaintyConstants.u_randomSType)) {
							unc = new RandomSample(reals,
									samplingMethodDescription);
						} else if (uncType
								.equals(PGDAOUncertaintyConstants.u_systematicSType)) {
							unc = new SystematicSample(reals,
									samplingMethodDescription);
						} else if (uncType
								.equals(PGDAOUncertaintyConstants.u_unknownSType)) {
							unc = new UnknownSample(reals,
									samplingMethodDescription);
						}

					} else if (catAr != null) {
						// categorical realisation
						reals = new ArrayList<AbstractRealisation>(1);
						reals.add(new CategoricalRealisation((String[]) catAr
								.getArray(), weight, singleRealID));

						// create sample depending on uncertainty type
						if (uncType
								.equals(PGDAOUncertaintyConstants.u_randomSType)) {
							unc = new RandomSample(reals,
									samplingMethodDescription);
						} else if (uncType
								.equals(PGDAOUncertaintyConstants.u_systematicSType)) {
							unc = new SystematicSample(reals,
									samplingMethodDescription);
						} else if (uncType
								.equals(PGDAOUncertaintyConstants.u_unknownSType)) {
							unc = new UnknownSample(reals,
									samplingMethodDescription);
						}
					}
				} else if (uncType.equals(PGDAOUncertaintyConstants.u_probType)) {
					// probability
					
					// convert BigDecimal[] to double[]
					BigDecimal[] bigDecs = (BigDecimal[]) rs.getArray(
							PGDAOUncertaintyConstants.uPProbValsCn).getArray();
					probValues = new double[bigDecs.length];
				
					for (int i = 0; i < bigDecs.length; i++) {
						probValues[i] = bigDecs[i].doubleValue();
					}
					
					// create probability constraints
					List<ProbabilityConstraint> probConst = new ArrayList<ProbabilityConstraint>(2);
					
					gt = rs.getDouble(PGDAOUncertaintyConstants.uPGtCn);
					lt = rs.getDouble(PGDAOUncertaintyConstants.uPLtCn);
					ge = rs.getDouble(PGDAOUncertaintyConstants.uPGeCn);
					le = rs.getDouble(PGDAOUncertaintyConstants.uPLeCn);
					
					if (gt != 0) probConst.add(new ProbabilityConstraint(ConstraintType.GREATER_THAN, gt));
					if (lt != 0) probConst.add(new ProbabilityConstraint(ConstraintType.LESS_THAN, lt));
					if (ge != 0) probConst.add(new ProbabilityConstraint(ConstraintType.GREATER_OR_EQUAL, ge));
					if (le != 0) probConst.add(new ProbabilityConstraint(ConstraintType.GREATER_OR_EQUAL, le));
				
					unc = new Probability(probConst, Arrays.asList(ArrayUtils.toObject(probValues)));
				}
					
				// TODO add further uncertainty types here

				if (unc != null) {
					if (sampleType) {

						boolean newUnc = true;

						// add realisation to an already existing sample
						// check all listed observation's IDs
						for (WrappedUncertainty wu : uncs) {

							if (wu.getUncertainty() instanceof AbstractSample
									&& obsID.equals(wu.getObservationID())) {

								// add realisation only, if maximum number of
								// realisations for this sample has not been
								// reached
								if (numberOfRealisations == Integer.MIN_VALUE
										|| ((AbstractSample) wu
												.getUncertainty())
												.getRealisations().size() < numberOfRealisations) {
									((AbstractSample) wu.getUncertainty())
											.getRealisations().addAll(
													((AbstractSample) unc)
															.getRealisations());
								}
								newUnc = false;

								// realisation should only be assigned to one
								// observation (ID)
								break;
							}
						}
						// add realisation as new uncertainty
						if (newUnc) {
							uncs.add(new WrappedUncertainty(obsID, gmlID,
									valueUnit, unc));
						}

					} else {
						uncs.add(new WrappedUncertainty(obsID, gmlID,
								valueUnit, unc));
					}
				}
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
		return uncs;
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
		query.append(", " + PGDAOUncertaintyConstants.uMMeanValsCn);

		// append normal distribution colums
		query.append(", " + PGDAOUncertaintyConstants.uNMeanCn + ", "
				+ PGDAOUncertaintyConstants.uNVarCn);

		// append realisation colums
		query.append(", " + PGDAOUncertaintyConstants.uRWeightCn + ", "
				+ PGDAOUncertaintyConstants.uRConValsCn + ", "
				+ PGDAOUncertaintyConstants.uRCatValsCn + ", "
				+ PGDAOUncertaintyConstants.uRIdCn + ", "
				+ PGDAOUncertaintyConstants.uRSamMethDescCn);
		
		// append probability columns
		query.append(", " + PGDAOUncertaintyConstants.uPGtCn + ", " 
				+ PGDAOUncertaintyConstants.uPLtCn + ", " 
				+ PGDAOUncertaintyConstants.uPGeCn + ", " 
				+ PGDAOUncertaintyConstants.uPLeCn + ", " 
				+ PGDAOUncertaintyConstants.uPProbValsCn);

		// TODO add further uncertainty types' table colums here

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

		// append mean table
		query.append(" LEFT OUTER JOIN " + PGDAOUncertaintyConstants.uMeanTn
				+ " ON " + PGDAOUncertaintyConstants.uMeanTn + "."
				+ PGDAOUncertaintyConstants.uMMeanIdCn + " = "
				+ PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uUUncValIdCn);

		// append normal distribution table
		query.append(" LEFT OUTER JOIN " + PGDAOUncertaintyConstants.uNormTn
				+ " ON " + PGDAOUncertaintyConstants.uNormTn + "."
				+ PGDAOUncertaintyConstants.uNNormIdCn + " = "
				+ PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uUUncValIdCn);

		// append realisation table
		query.append(" LEFT OUTER JOIN " + PGDAOUncertaintyConstants.uRealTn
				+ " ON " + PGDAOUncertaintyConstants.uRealTn + "."
				+ PGDAOUncertaintyConstants.uRRealIdCn + " = "
				+ PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uUUncValIdCn);
		
		// append probability table
		query.append(" LEFT OUTER JOIN " + PGDAOUncertaintyConstants.uProbTn
				+ " ON " + PGDAOUncertaintyConstants.uProbTn + "."
				+ PGDAOUncertaintyConstants.uPProbIdCn + " = "
				+ PGDAOUncertaintyConstants.uUncertTn + "."
				+ PGDAOUncertaintyConstants.uUUncValIdCn);

		// TODO add further uncertainty types' tables here

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

		return getUncertainObservationCollection(om1ObsCol, null,
				Integer.MIN_VALUE);
	}

	/**
	 * returns an O&M 2 observation collection including uncertainties
	 * 
	 * @param obsCollection
	 *            O&M 1 observation collection
	 * @param resultModel
	 *            demanded observation type
	 * @param numberOfRealisations
	 *            maximum number of realisations per sample
	 * @return O&M 2 observation collection
	 * @throws OwsExceptionReport
	 */
	public IObservationCollection getUncertainObservationCollection(
			SosObservationCollection om1ObsCol, QName resultModel,
			int numberOfRealisations) throws OwsExceptionReport {

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

			// get and add uncertainties
			SosObservationCollection uncOM1ObsCol = null;
			List<WrappedUncertainty> uncList = getUncertaintyData(obsIDs,
					numberOfRealisations);
			uncOM1ObsCol = addUnc2OM1ObsCol(om1ObsCol, uncList);

			// convert OM1 to OM2 observation collection
			om2ObsCol = ObservationConverter.getOM2ObsCol(uncOM1ObsCol);
		} else {

			// empty obsCol (create an empty collection of the demanded type)
			if (resultModel.equals(new QName(OM2Constants.NS_OM2,
					OM2Constants.OBS_TYPE_BOOLEAN))) {
				return new BooleanObservationCollection();

			} else if (resultModel.equals(new QName(OM2Constants.NS_OM2,
					OM2Constants.OBS_TYPE_DISCNUM))) {
				return new DiscreteNumericObservationCollection();

			} else if (resultModel.equals(new QName(OM2Constants.NS_OM2,
					OM2Constants.OBS_TYPE_MEASUREMENT))) {
				return new MeasurementCollection();

			} else if (resultModel.equals(new QName(OM2Constants.NS_OM2,
					OM2Constants.OBS_TYPE_REFERENCE))) {
				return new ReferenceObservationCollection();

			} else if (resultModel.equals(new QName(OM2Constants.NS_OM2,
					OM2Constants.OBS_TYPE_TEXT))) {
				return new TextObservationCollection();

			} else if (resultModel.equals(new QName(OM2Constants.NS_OM2,
					OM2Constants.OBS_TYPE_UNCERTAINTY))) {
				return new UncertaintyObservationCollection();

			} else {
				OwsExceptionReport se = new OwsExceptionReport();
				se.addCodedException(
						OwsExceptionReport.ExceptionCode.NoDataAvailable,
						null,
						"No data available and resultModel '" + resultModel + "' not supported.");
				LOGGER.error(
						"No data available and resultModel '" + resultModel + "' not supported.",
						se);
				throw se;
			}
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
	 * @param uncList
	 *            List corresponding uncertainties
	 * @return O&M 2 observation collection
	 * @throws OwsExceptionReport
	 */
	public static SosObservationCollection addUnc2OM1ObsCol(
			SosObservationCollection om1ObsCol, List<WrappedUncertainty> uncList) {

		if (uncList == null || uncList.isEmpty()) {
			return null;
		}

		Iterator<AbstractSosObservation> obsColIt = om1ObsCol
				.getObservationMembers().iterator();
		while (obsColIt.hasNext()) {

			AbstractSosObservation obs = obsColIt.next();
			String obsID = obs.getObservationID();

			if (obs instanceof UNCMeasurementObservation) {

				UNCMeasurementObservation measObs = (UNCMeasurementObservation) obs;
				boolean withoutUnc = true;

				for (int i = 0; i < uncList.size(); i++) {

					if (obsID.equals(uncList.get(i).getObservationID())) {

						// add new uncertainty result
						// insert and decoder classes should prevent multiple
						// uncertainties for one observation

						IUncertainty[] uncArr = { uncList.get(i)
								.getUncertainty() };
						DQ_UncertaintyResult[] uncRes = { new DQ_UncertaintyResult(
								uncArr, uncList.get(i).getValueUnit()) };
						measObs.setUncQuality(uncRes);

						measObs.setIdentifier(uncList.get(i).getGmlID());
						withoutUnc = false;
					}
				}

				if (withoutUnc) {
					// remove observations without uncertainty from observation
					// collection
					om1ObsCol.getObservationMembers().remove(obs);
				}
			} else if (obs instanceof UNCUncertaintyObservation) {

				UNCUncertaintyObservation uncObs = (UNCUncertaintyObservation) obs;

				boolean withoutUnc = true;

				for (int i = 0; i < uncList.size(); i++) {

					// set (single) uncertainty to this observation
					if (obsID.equals(uncList.get(i).getObservationID())) {
						uncObs.setUncertainty(uncList.get(i).getUncertainty());
						uncObs.setIdentifier(uncList.get(i).getGmlID());

						withoutUnc = false;
						break;
					}
				}

				if (withoutUnc) {
					// remove observations without uncertainty from observation
					// collection
					om1ObsCol.getObservationMembers().remove(obs);
				}
			}
		}

		return om1ObsCol;
	}

	/**
	 * creates a from and join clause 4 non spatial
	 * 
	 * @param uncertainties
	 *            get only observations with uncertainties
	 * @return String containing the from clause
	 */
	private String getFromClause(boolean quality, boolean isMobile,
			boolean uncertainties) {
		StringBuilder from = new StringBuilder();
		from.append(" FROM (" + PGDAOConstants.getObsTn()
				+ " NATURAL INNER JOIN " + PGDAOConstants.getPhenTn()
				+ " NATURAL INNER JOIN " + PGDAOConstants.getFoiTn());
		if (uncertainties) {
			from.append(" NATURAL INNER JOIN "
					+ PGDAOUncertaintyConstants.uObsUncTn);
		}
		if (isMobile) {
			from.append(" LEFT OUTER JOIN " + PGDAOConstants.getObsDfTn()
					+ " ON " + PGDAOConstants.getObsDfTn() + "."
					+ PGDAOConstants.getObsIDCn() + " = "
					+ PGDAOConstants.getObsTn() + "."
					+ PGDAOConstants.getObsIDCn() + " LEFT OUTER JOIN "
					+ PGDAOConstants.getDfTn() + " ON "
					+ PGDAOConstants.getObsDfTn() + "."
					+ PGDAOConstants.getDomainFeatureIDCn() + " = "
					+ PGDAOConstants.getDfTn() + "."
					+ PGDAOConstants.getDomainFeatureIDCn());
		}
		if (quality) {
			from.append(" LEFT JOIN " + PGDAOConstants.getQualityTn() + " ON "
					+ PGDAOConstants.getQualityTn() + "."
					+ PGDAOConstants.getObsIDCn() + " = "
					+ PGDAOConstants.getObsTn() + "."
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

						if (resultSet.getString(PGDAOConstants
								.getDomainFeatureIDCn()) != null
								&& !resultSet.getString(
										PGDAOConstants.getDomainFeatureIDCn())
										.equals("")) {
							AbstractSosObservation obs = obs4obsIDs.get(obsID);

							String dfID = resultSet.getString(PGDAOConstants
									.getDomainFeatureIDCn());
							obs.addDomainFeatureID(new SosGenericDomainFeature(
									dfID));
						}
					}
					// supports quality
					if (isSupportsQuality()) {
						String qualityTypeString = resultSet
								.getString(PGDAOConstants.getQualTypeCn());
						String qualityUnit = resultSet.getString(PGDAOConstants
								.getQualUnitCn());
						String qualityName = resultSet.getString(PGDAOConstants
								.getQualNameCn());
						String qualityValue = resultSet
								.getString(PGDAOConstants.getQualValueCn());
						QualityType qualityType = QualityType
								.valueOf(qualityTypeString);
						SosQuality quality = new SosQuality(qualityName,
								qualityUnit, qualityValue, qualityType);
						obs4obsIDs.get(obsID).addSingleQuality(quality);
					}

				} else {
					String offeringID = resultSet.getString(PGDAOConstants
							.getOfferingIDCn());
					String mimeType = SosConstants.PARAMETER_NOT_SET;

					// create time element
					String timeString = resultSet.getString(PGDAOConstants
							.getTimestampCn());
					DateTime timeDateTime = SosDateTimeUtilities
							.parseIsoString2DateTime(timeString);
					TimeInstant time = new TimeInstant(timeDateTime, "");

					String phenID = resultSet.getString(PGDAOConstants
							.getPhenIDCn());
					String valueType = resultSet.getString(PGDAOConstants
							.getValueTypeCn());
					String procID = resultSet.getString(PGDAOConstants
							.getProcIDCn());

					String unit = resultSet.getString(PGDAOConstants
							.getUnitCn());

					// domain feature
					String domainFeatID = null;
					ArrayList<SosAbstractFeature> domainFeatIDs = null;
					if (request.isMobileEnabled()) {
						if (resultSet.getString(PGDAOConstants
								.getDomainFeatureIDCn()) != null
								&& !resultSet.getString(
										PGDAOConstants.getDomainFeatureIDCn())
										.equals("")) {
							domainFeatID = resultSet.getString(PGDAOConstants
									.getDomainFeatureIDCn());
							domainFeatIDs = new ArrayList<SosAbstractFeature>();
							domainFeatIDs.add(new SosGenericDomainFeature(
									domainFeatID));
						}
					}

					// feature of interest
					String foiID = resultSet.getString(PGDAOConstants
							.getFoiIDCn());
					String foiName = resultSet.getString(PGDAOConstants
							.getFoiNameCn());
					String foiType = resultSet.getString(PGDAOConstants
							.getFeatureTypeCn());

					// foi geometry
					String foiGeomWKT = resultSet.getString(PGDAOConstants
							.getFoiGeometry());
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
						String qualityUnit = resultSet.getString(PGDAOConstants
								.getQualUnitCn());
						String qualityName = resultSet.getString(PGDAOConstants
								.getQualNameCn());
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
							value = resultSet.getString(PGDAOConstants
									.getTextValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.countType
										.name())) {
							value = Integer.toString((int) Math.round(resultSet
									.getDouble(PGDAOConstants
											.getNumericValueCn()))); // make
																		// sure
																		// we
																		// write
																		// an
																		// integer
																		// value
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.numericType
										.name())) {
							value = resultSet.getString(PGDAOConstants
									.getNumericValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.isoTimeType
										.name())) {
							value = new DateTime(
									resultSet.getLong(PGDAOConstants
											.getNumericValueCn())).toString();
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.textType
										.name())) {
							value = resultSet.getString(PGDAOConstants
									.getTextValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.categoryType
										.name())) {
							value = resultSet.getString(PGDAOConstants
									.getTextValueCn());
						} else if (valueType
								.equalsIgnoreCase(SosConstants.ValueTypes.spatialType
										.name())) {
							Geometry value_geom = JTSUtilities
									.createGeometryFromWKT(resultSet
											.getString(PGDAOConstants
													.getValueGeometry()));
							srid = checkRequestSridQuerySrid(value_geom
									.getSRID());
							if (SosConfigurator.getInstance()
									.switchCoordinatesForEPSG(srid)) {
								value_geom = JTSUtilities
										.switchCoordinate4Geometry(value_geom);
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
									offeringID, getTokenSeperator(),
									getTupleSeperator(), getNoDataValue());
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
						if (resultSet.getString(PGDAOConstants
								.getNumericValueCn()) != null) {
							value = resultSet.getDouble(PGDAOConstants
									.getNumericValueCn());
						}

						SosMeasurement measurement = new SosMeasurement(time,
								obsID, procID, domainFeatIDs, phenID, foi,
								offeringID, mimeType, value, unit, qualityList);
						obs4obsIDs.put(obsID, measurement);
					} else if (resultModel
							.equals(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION)) {

						checkResponseModeInline(request.getResponseMode());

						String value = resultSet.getString(PGDAOConstants
								.getTextValueCn());
						if (value == null || (value != null && value.isEmpty())) {
							try {
								value = new DateTime(
										resultSet.getLong(PGDAOConstants
												.getNumericValueCn()))
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
						setBoundedBy(checkEnvelope(getBoundedBy(),
								jts_value_geometry));

						SosSpatialObservation spatialObs = new SosSpatialObservation(
								time, obsID, procID, domainFeatIDs, phenID,
								foi, offeringID, jts_value_geometry,
								qualityList);
						obs4obsIDs.put(obsID, spatialObs);
					} else if (resultModel
							.equals(SosUncConstants.RESULT_MODEL_MEASUREMENT)) {
						Double value = Double.NaN;
						if (resultSet.getString(PGDAOConstants
								.getNumericValueCn()) != null) {
							value = resultSet.getDouble(PGDAOConstants
									.getNumericValueCn());
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
			if (request.getObservedProperty().length == 0
					&& request.getVersion()
							.equals(Sos1Constants.SERVICEVERSION)) {
				OwsExceptionReport se = new OwsExceptionReport();
				se.addCodedException(
						OwsExceptionReport.ExceptionCode.InvalidParameterValue,
						GetObservationParams.observedProperty.toString(),
						"The request contains no observed Properties!");
				throw se;
			} else {
				boolean hasSpatialPhen = false;
				if (Arrays.asList(request.getObservedProperty()).contains(
						SosConfigurator.getInstance()
								.getSpatialObsProp4DynymicLocation())) {
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

					// if timeInstant contains "latest", return the last
					// observation
					// for each phen/proc/foi/df
					if (request.getEventTime() != null
							&& request.getEventTime().length > 0) {
						for (TemporalFilter tf : request.getEventTime()) {
							if (tf.getTime().getIndeterminateValue() != null) {
								if (tf.getTime().getIndeterminateValue()
										.equals(FirstLatest.latest.name())) {
									resultSetList.add(queryLatestObservations(
											request, tf, con, hasSpatialPhen));
								} else if (tf
										.getTime()
										.getIndeterminateValue()
										.equalsIgnoreCase(
												FirstLatest.getFirst.name())) {
									resultSetList
											.add(queryGetFirstObservations(
													request, tf, con,
													hasSpatialPhen));
								} else {
									resultSetList.add(queryObservation(request,
											con, hasSpatialPhen));
								}
							} else {
								resultSetList.add(queryObservation(request,
										con, hasSpatialPhen));
							}
						}
					} else {
						resultSetList.add(queryObservation(request, con,
								hasSpatialPhen));
					}
					// end get ResultSets
					for (ResultSet resultSet : resultSetList) {

						// if resultModel parameter is set in the request,
						// check,
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
