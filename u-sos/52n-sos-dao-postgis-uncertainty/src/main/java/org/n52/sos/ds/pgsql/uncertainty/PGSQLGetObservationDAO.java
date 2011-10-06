package org.n52.sos.ds.pgsql.uncertainty;

import org.n52.sos.ds.IGetObservationDAO;
import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.SosGetObservationRequest;

public class PGSQLGetObservationDAO extends
		org.n52.sos.ds.pgsql.PGSQLGetObservationDAO implements IGetObservationDAO {

	public PGSQLGetObservationDAO(PGConnectionPool cpool) {
		super(cpool);
	}
	
	public SosObservationCollection getObservation(SosGetObservationRequest request) throws OwsExceptionReport {
		SosObservationCollection uncObsCol = null;
		SosObservationCollection obsCol = super.getObservation(request);
        
        
        return uncObsCol;
	}
}
