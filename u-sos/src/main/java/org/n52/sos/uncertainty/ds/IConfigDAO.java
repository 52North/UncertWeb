package org.n52.sos.uncertainty.ds;

import java.util.List;

import org.n52.sos.ogc.ows.OwsExceptionReport;

/** 
 * interface to add uncertainty related DAO methods 
 * @author Kiesow
 */
public interface IConfigDAO extends org.n52.sos.ds.IConfigDAO {

	/**
	 * queries the value units of uncertainties from the DB
	 * @return 
	 * 
	 * @throws OwsExceptionReport
	 *             if query of value units failed
	 */
	public List<String> queryValueUnits() throws OwsExceptionReport;
}
