package org.n52.sos.uncertainty.ds.pgsql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.n52.sos.SosConstants;
import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ds.pgsql.PGDAOConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.uncertainty.SosUncConstants;
import org.n52.sos.uncertainty.ds.IConfigDAO;

/**
 * Config DAO wrapper to add uncertainty enablement
 * @author Kiesow
 *
 */
public class PGSQLConfigDAO extends org.n52.sos.ds.pgsql.PGSQLConfigDAO implements IConfigDAO {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static Logger LOGGER = Logger.getLogger(PGSQLConfigDAO.class);

	/**
	 * Constructor
	 * @param conPool
	 */
	public PGSQLConfigDAO(PGConnectionPool conPool) {
		super(conPool);
	}

    /**
     * queries the result models for each offerings and puts them into a HashMap
     *
     * @return Returns HashMap<String,String[]> containing the offerings as keys
     *         and the corresponding result models as values
     * @throws OwsExceptionReport
     *             if query of the result models failed
     */
    public Map<String, Collection<QName>> queryOfferingResultModels() throws OwsExceptionReport {

        Map<String, Collection<QName>> result = new HashMap<String, Collection<QName>>();

        Connection con = null;
        Statement stmt = null;
        String query =
                "SELECT DISTINCT " + PGDAOConstants.getOfferingIDCn() + ", " + PGDAOConstants.getValueTypeCn() + " FROM "
                        + PGDAOConstants.getPhenTn() + " NATURAL INNER JOIN " + PGDAOConstants.getPhenOffTn() + ";";
        String valueType;
        String offeringID;
        try {
            // execute query
            con = getCPool().getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                offeringID = rs.getString(PGDAOConstants.getOfferingIDCn());
                valueType = rs.getString(PGDAOConstants.getValueTypeCn());

                // if HashMap does not contain the offering, add new offering
                // and corresponding result models
                // to result hash map
                if (!result.containsKey(offeringID)) {
                    List<QName> resultModels = new ArrayList<QName>();

                    // if numerical value, add measurement type to result models
                    if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.numericType.name())) {
                        resultModels.add(SosConstants.RESULT_MODEL_MEASUREMENT);
                        result.put(offeringID, resultModels);

                    }

                    // if textual value add scopedNameType to result models
                    // (used in category observation)
                    else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.textType.name())
                            || valueType.equalsIgnoreCase(SosConstants.ValueTypes.categoryType.name())) {
                        resultModels.add(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION);
                        result.put(offeringID, resultModels);
                    }

                    // if spatial value add add scopedNameType to result models
                    // (used in spatial observation)
                    else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.spatialType.name())) {
                        resultModels.add(SosConstants.RESULT_MODEL_SPATIAL_OBSERVATION);
                        result.put(offeringID, resultModels);
                    }

                    // if uncertainty value add scopledNameType to result models
                    else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.uncertaintyType.name())) {
                        resultModels.add(SosUncConstants.RESULT_MODEL_UNCERTAINTY_OBSERVATION);
                        result.put(offeringID, resultModels);
                    }

                    // check, if offering ID was created successfully, than add
                    // common observation result
                    // model (swe:Data)
                    if (result.get(offeringID) != null) {
                        result.get(offeringID).add(SosConstants.RESULT_MODEL_OBSERVATION);
                    }

                    // if not, add to result models and put them as new pair
                    // into result hash map
                    else {
                        resultModels.add(SosConstants.RESULT_MODEL_OBSERVATION);
                        result.put(offeringID, resultModels);
                    }
                }

                // if offering already contained as key in hash map, add the new
                // resultModels to the models
                // already contained as values for the offering
                else {
                    if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.numericType.name())) {
                        if (!result.get(offeringID).contains(SosConstants.RESULT_MODEL_MEASUREMENT)) {
                            result.get(offeringID).add(SosConstants.RESULT_MODEL_MEASUREMENT);
                        }
                    } else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.textType.name())
                            || valueType.equalsIgnoreCase(SosConstants.ValueTypes.categoryType.name())) {
                        if (!result.get(offeringID).contains(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION)) {
                            result.get(offeringID).add(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION);
                        }
                    }

                    if (!result.get(offeringID).contains(SosConstants.RESULT_MODEL_OBSERVATION)) {
                        result.get(offeringID).add(SosConstants.RESULT_MODEL_OBSERVATION);
                    }
                }
            }

            // now repeat this for composite phenomena
            query =
                    "SELECT DISTINCT " + PGDAOConstants.getOfferingIDCn() + ", " + PGDAOConstants.getValueTypeCn() + " FROM "
                            + PGDAOConstants.getPhenTn() + " NATURAL INNER JOIN " + PGDAOConstants.getCompPhenTn()
                            + " NATURAL INNER JOIN " + PGDAOConstants.getCompPhenOffTn() + ";";
            stmt.close();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                offeringID = rs.getString(PGDAOConstants.getOfferingIDCn());
                valueType = rs.getString(PGDAOConstants.getValueTypeCn());

                // if HashMap does not contain the offering, add new offering
                // and corresponding result models
                // to result hash map
                if (!result.containsKey(offeringID)) {
                    List<QName> resultModels = new ArrayList<QName>();

                    // if numerical value, add measurement type to result models
                    if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.numericType.name())) {
                        resultModels.add(SosConstants.RESULT_MODEL_MEASUREMENT);
                        result.put(offeringID, resultModels);

                    }

                    // if textual value add add scopedNameType to result models
                    // (used in category observation)
                    else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.textType.name())
                            || valueType.equalsIgnoreCase(SosConstants.ValueTypes.categoryType.name())) {
                        resultModels.add(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION);
                        result.put(offeringID, resultModels);
                    }

                    // check, if offering ID was created successfully, than add
                    // common observation result
                    // model (swe:Data)
                    if (result.get(offeringID) != null) {
                        result.get(offeringID).add(SosConstants.RESULT_MODEL_OBSERVATION);
                    }

                    // if not, add to result models and put them as new pair
                    // into result hash map
                    else {
                        resultModels.add(SosConstants.RESULT_MODEL_OBSERVATION);
                        result.put(offeringID, resultModels);
                    }
                }

                // if offering already contained as key in hash map, add the new
                // resultModels to the models
                // already contained as values for the offering
                else {
                    if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.numericType.name())) {
                        if (!result.get(offeringID).contains(SosConstants.RESULT_MODEL_MEASUREMENT)) {
                            result.get(offeringID).add(SosConstants.RESULT_MODEL_MEASUREMENT);
                        }
                    } else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.textType.name())
                            || valueType.equalsIgnoreCase(SosConstants.ValueTypes.categoryType.name())) {
                        if (!result.get(offeringID).contains(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION)) {
                            result.get(offeringID).add(SosConstants.RESULT_MODEL_CATEGORY_OBSERVATION);
                        }
                    }

                    if (!result.get(offeringID).contains(SosConstants.RESULT_MODEL_OBSERVATION)) {
                        result.get(offeringID).add(SosConstants.RESULT_MODEL_OBSERVATION);
                    }
                }
            }
        } catch (SQLException sqle) {
            OwsExceptionReport se = new OwsExceptionReport(sqle);
            LOGGER.error("error while query result models from DB!", sqle);
            throw se;
        } catch (OwsExceptionReport se) {
            LOGGER.error("error while query result models from DB!", se);
            throw se;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqle) {
                    LOGGER.error("Error while closing database statement!", sqle);
                }
            }
            if (con != null) {
                getCPool().returnConnection(con);
            }
        }
        return result;
    }

	/**
	 * queries the value units of uncertainties from the DB
	 * @return
	 *
	 * @throws OwsExceptionReport
	 *             if query of value units failed
	 */
	@Override
	public List<String> queryValueUnits() throws OwsExceptionReport {

		String query = "SELECT " + PGDAOUncertaintyConstants.uVUValUnitCn
				+ " FROM " + PGDAOUncertaintyConstants.uValUnitTn;
		List<String> valueUnits = new ArrayList<String>();
		Connection con = null;
		Statement stmt = null;
		try {
			// execute query
			con = getCPool().getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			// get result as string and parse String to date
			while (rs.next()) {
				String valueUnit = rs
						.getString(PGDAOUncertaintyConstants.uVUValUnitCn);
				valueUnits.add(valueUnit);
			}
		} catch (SQLException sqle) {
			OwsExceptionReport se = new OwsExceptionReport(sqle);
			LOGGER.error("error while querying value units from DB!", sqle);
			throw se;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqle) {
					LOGGER.error("Error while closing database statement!",
							sqle);
				}
			}
			if (con != null) {
				getCPool().returnConnection(con);
			}
		}
		return valueUnits;
	}
}
