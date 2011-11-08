package org.n52.sos.cache.uncertainty;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.n52.sos.SosConstants;
import org.n52.sos.ds.uncertainty.IConfigDAO;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * CapabilitiesCacheController implements all methods to request all objects and
 * relationships from a standard datasource; this class adds uncertainty
 * extension specific request methods including their actual CapabilitiesCache
 * 
 * @author Kiesow
 * 
 */
public class CapabilitiesCacheController extends
		org.n52.sos.cache.CapabilitiesCacheController {

	/** contains the value units of uncertainties in the database */
	private List<String> valueUnits;

	/**
	 * constructor
	 */
	public CapabilitiesCacheController() {
		super();
	}

	/**
	 * returns the value units of uncertainties of this SOS
	 * 
	 * @return List<String> containing the value units
	 */
	public List<String> getValueUnits() {
		return this.valueUnits;
	}

	/**
	 * sets the value units of uncertainties of this SOS
	 * 
	 * @param valueUnits
	 *            List<String> containing the value units
	 */
	public void setValueUnits(List<String> valueUnits) {
		this.valueUnits = valueUnits;
	}

    /**
     * queries the value units of uncertainties from the DB
     * 
     * @throws OwsExceptionReport
     *             if query of value units of uncertainties failed
     */
    public void queryValueUnits() throws OwsExceptionReport {
        this.setValueUnits(((IConfigDAO) configDao).queryValueUnits());
    }

	/**
	 * queries the service offerings, the observedProperties for each offering,
	 * and the offering names from the DB and sets these values in this
	 * configurator; to remain threadsave this method was largely copied from
	 * the super class
	 * 
	 * @throws OwsExceptionReport
	 *             if the query of one of the values described upside failed
	 * 
	 */
	public boolean update(boolean checkLastUpdateTime)
			throws OwsExceptionReport {
		boolean timeNotElapsed = true;
		try {
			// thread safe updating of the cache map
			timeNotElapsed = getUpdateLock().tryLock(
					SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

			// has waiting for lock got a time out?
			if (!timeNotElapsed) {
				LOGGER.warn("\n******\nCapabilities caches not updated "
						+ "because of time out while waiting for update lock."
						+ "\nWaited " + SosConstants.UPDATE_TIMEOUT
						+ " milliseconds.\n******\n");
				return false;
			}

			while (!isUpdateIsFree()) {

				getUpdateFree().await();
			}
			setUpdateIsFree(false);

			queryOfferings();
			queryOffPhenomena();
			queryOffNames();
			queryProcedures();
			queryOffResultModels();
			queryOffProcedures();
			queryOffFois();
			queryFois();
			queryFoiProcedures();
			queryObsPropValueTypes();
			queryPhens4CompPhens();
			queryOffCompPhens();
			queryPhenProcs();
			queryProcPhens();
			queryTimes4Offerings();
			queryUnits4Phens();
			queryPhenOffs();
			querySRIDs();
			// always query since domain features can be used for static sensors
			// (e.g. sensor networks)
			queryOffDomainFeatures();
			queryDomainFeatures();
			queryDomainFeatureProcedures();
			queryDomainFeatureFois();

			queryValueUnits();

		} catch (InterruptedException e) {
			LOGGER.error("Problem while threadsafe capabilities cache update",
					e);
			return false;
		} finally {
			if (timeNotElapsed) {
				getUpdateLock().unlock();
				setUpdateIsFree(true);
			}
		}
		return true;
	}
	
    /**
     * methods for adding relationships in Cache for recently received new
     * observation; to remain threadsave this method was largely copied from
	 * the super class
     * 
     * @param observation
     *            recently received observation which has been inserted into SOS
     *            db and whose relationships have to be maintained in cache
     * @param mobileEnabled
     *            indicates whether request containing the passed observation
     *            has been mobile enabled
     * @throws OwsExceptionReport
     */
    public void updateMetadata4newObservation(AbstractSosObservation observation, boolean mobileEnabled)
            throws OwsExceptionReport {

        boolean timeNotElapsed = true;
        try {
            // thread safe updating of the cache map
            timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

            // has waiting for lock got a time out?
            if (!timeNotElapsed) {
                LOGGER.warn("\n******\nupdateMetadata4newObservation() not successful "
                        + "because of time out while waiting for update lock." + "\nWaited "
                        + SosConstants.UPDATE_TIMEOUT + " milliseconds.\n******\n");
                return;
            }
            while (!isUpdateIsFree()) {

                getUpdateFree().await();
            }
            setUpdateIsFree(false);

            queryOffResultModels();
            queryOffFois();
            queryFois();
            queryFoiProcedures();
            queryPhens4CompPhens();
            queryOffCompPhens();
            queryTimes4Offerings();
            querySRIDs();
            // always query since domain features can be used for static sensors
            // (e.g. sensor networks)
            queryOffDomainFeatures();
            queryDomainFeatures();
            queryDomainFeatureProcedures();
            queryDomainFeatureFois();
            
			queryValueUnits();

        } catch (InterruptedException e) {
            LOGGER.error("Problem while threadsafe capabilities cache update", e);
        } finally {
            if (timeNotElapsed) {
                getUpdateLock().unlock();
                setUpdateIsFree(true);
            }
        }
    }
}
