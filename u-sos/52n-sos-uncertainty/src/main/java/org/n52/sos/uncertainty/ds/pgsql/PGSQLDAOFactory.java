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
	
//    /** the logger, used to log exceptions and additionally information */
//    private static Logger LOGGER = Logger.getLogger(PGSQLDAOFactory.class);
//
//    /** ConnectionPool, which contains connections to the DB */
//    private PGConnectionPool cpool;

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
    
//    /**
//     * constructor
//     * 
//     * @param connection
//     *            String containing the connection URL
//     * @param user
//     *            String username for the DB
//     * @param password
//     *            String password for the DB
//     * @param driver
//     *            String classname of the DB driver (with packages in front)
//     * @param initcon
//     *            int number of initial connections contained in the
//     *            PGConnectionPool
//     * @param maxcon
//     *            int max number of connections contained in the
//     *            PGConnectionPool
//     */
//    public PGSQLDAOFactory(Properties daoProps) throws OwsExceptionReport {
//
//        initializeDAOConstants(daoProps);
//        String connection = PGDAOConstants.connectionString;
//        String user = PGDAOConstants.user;
//        String password = PGDAOConstants.password;
//        String driver = PGDAOConstants.driver;
//        int initcon = PGDAOConstants.initcon;
//        int maxcon = PGDAOConstants.maxcon;
//        // initialize PGConnectionPool
//        this.cpool = new PGConnectionPool(connection, user, password, driver, initcon, maxcon);
//
//    }

//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     * @throws OwsExceptionReport
//     */
//    public PGSQLGetCapabilitiesDAO getCapabilitiesDAO() throws OwsExceptionReport {
//        PGSQLGetCapabilitiesDAO capsDao = null;
//        capsDao = new PGSQLGetCapabilitiesDAO(cpool);
//        return capsDao;
//    }
//
//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     */
//    public PGSQLDescribeObservationTypeDAO getDescribeObservationTypeDAO() {
//        // TODO not yet implemented
//        return null;
//    }
//
//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     */
//    public PGSQLDescribeSensorDAO getDescribeSensorDAO() {
//        return new PGSQLDescribeSensorDAO(cpool);
//    }
//
//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     */
//    public PGSQLDescribeFeatureOfInterestDAO getDescribeFeatureOfInterestDAO() {
//        // TODO not yet implemented
//        return null;
//    }

    /**
     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
     * 
     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
     *         operation
     */
    public PGSQLGetObservationDAO getObservationDAO() {
        return new PGSQLGetObservationDAO(cpool);
    }

//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     */
//    public PGSQLGetResultDAO getResultDAO() {
//        return new PGSQLGetResultDAO(cpool, new PGSQLGetObservationDAO(cpool));
//    }
//
//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     */
//    public PGSQLGetFeatureOfInterestDAO getFeatureOfInterestDAO() {
//        return new PGSQLGetFeatureOfInterestDAO(cpool);
//    }
//
//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     */
//    public IGetDomainFeatureDAO getDomainFeatureDAO() {
//        return new PGSQLGetDomainFeatureDAO(cpool);
//    }
//
//    /**
//     * method intitializes and returns a PostgreSQLGetTargetFeatureDAO
//     * 
//     * @return PostgreSQLGetTargetFeatureDAO DAO for the GetTargetFeature
//     *         operation
//     */
//    public IGetFeatureOfInterestTimeDAO getFeatureOfInterestTimeDAO() {
//        return new PGSQLGetFeatureOfInterestTimeDAO(cpool);
//    }

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
        return new PGSQLConfigDAO(cpool);
    }

//    /**
//     * 
//     * @return Returns the GetObservationByIdDAO
//     */
//    public IGetObservationByIdDAO getObservationByIdDAO() {
//        return new PGSQLGetObservationByIdDAO(cpool);
//    }
//
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

        return new PGSQLRegisterSensorDAO(cpool);
    }

//    /**
//     * returns the updateSensor DAO
//     * 
//     * @return returns the updateSensor DAO
//     * 
//     */
//    public IUpdateSensorDAO getUpdateSensorDAO() {
//        return new PGSQLUpdateSensorDAO(cpool);
//    }

    /**
     * 
     * @return Returns the insertObservation DAO
     */
    public IInsertObservationOperationDAO getInsertObservationOperationDAO() {
        return new PGSQLInsertObservationOperationDAO(cpool);
    }

//    /**
//     * method returns connection pool used by this DAO Factory
//     * 
//     * @return returns Connection Pool implementation used by this DAO factory
//     */
//    public AbstractConnectionPool getConnectionPool() {
//        return this.cpool;
//    }
//
//    public void cleanup() {
//        try {
//            cpool.cleanup();
//        } catch (Exception e) {
//            LOGGER.error("Error while shutting down Database connections: " + e.getMessage());
//        }
//
//    }
//
//    /**
//     * method intitializes and returns a DescribeFeatureTypeDAO
//     * 
//     * @return DescribeFeatureTypeDAO DAO for the DescribeFeatureType operation
//     */
//    public IDescribeFeatureTypeDAO getDescribeFeatureTypeDAO() {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
