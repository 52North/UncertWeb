package org.n52.sos.uncertainty.ds.pgsql;

import java.util.Properties;

import org.n52.sos.ds.IConfigDAO;
import org.n52.sos.ds.IDAOFactory;
import org.n52.sos.ds.IInsertObservationOperationDAO;
import org.n52.sos.ds.IRegisterSensorDAO;
import org.n52.sos.ds.pgsql.PGDAOConstants;
import org.n52.sos.ds.pgsql.PGSQLRegisterSensorDAO;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * DAO factory for PostgreSQL 8.1
 * 
 * @author Christoph Stasch, Martin Kiesow
 * 
 */
public class PGSQLDAOFactory extends org.n52.sos.ds.pgsql.PGSQLDAOFactory implements IDAOFactory {

	//TODO comments
	
    /**
     * constructor
     * 
     * @param connection
     *            String containing the connection URL
     * @param user
     *            String username for the DB
     * @param password
     *            String password for the DB
     * @param driver
     *            String classname of the DB driver (with packages in front)
     * @param initcon
     *            int number of initial connections contained in the
     *            PGConnectionPool
     * @param maxcon
     *            int max number of connections contained in the
     *            PGConnectionPool
     */
    public PGSQLDAOFactory(Properties daoProps) throws OwsExceptionReport {
    	super(daoProps);

    }
    
    /**
     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
     * 
     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
     *         operation
     */
    public PGSQLGetObservationDAO getObservationDAO() {
        return new PGSQLGetObservationDAO(getCPool());
    }

	/**
     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
     * 
     * @param logLevel
     *            Level for logging, parameter is necessary cause the
     *            PGSQLConfigDAO is used in the SosConfigurator's constructor
     * @param handler
     *            MemoryHandler for logging, parameter is necessary cause the
     *            PGSQLConfigDAO is used in the SosConfigurator's constructor
     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
     *         operation
     */
    public IConfigDAO getConfigDAO() {
        return new PGSQLConfigDAO(getCPool());
    }

    /**
     * initializes the DAOConstants
     * 
     * @param daoProps
     *            Properties created from the dssos.config file
     * @throws OwsExceptionReport
     *             if initializing the DAOConstants failed
     */
    private void initializeDAOConstants(Properties daoProps) throws OwsExceptionReport {
        PGDAOConstants.getInstance(daoProps);
    }

    /**
     * returns the registerSensor DAO
     * 
     * @return returns the registerSensor DAO
     * 
     */
    public IRegisterSensorDAO getRegisterSensorDAO() {

        return new PGSQLRegisterSensorDAO(getCPool());
    }

    /**
     * 
     * @return Returns the insertObservation DAO
     */
    public IInsertObservationOperationDAO getInsertObservationOperationDAO() {
        return new PGSQLInsertObservationOperationDAO(getCPool());
    }
}
