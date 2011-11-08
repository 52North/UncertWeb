package org.n52.sos.ds.pgsql.uncertainty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ds.uncertainty.IConfigDAO;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Config DAO wrapper to add uncertainty enablement
 * @author Kiesow
 *
 */
public class PGSQLConfigDAO extends org.n52.sos.ds.pgsql.PGSQLConfigDAO implements IConfigDAO {

	/**
	 * Constructor 
	 * @param conPool
	 */
	public PGSQLConfigDAO(PGConnectionPool conPool) {
		super(conPool);
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
			con = cpool.getConnection();
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
				cpool.returnConnection(con);
			}
		}
		return valueUnits;
	}
}
