package org.n52.sos.uncertainty.ds.pgsql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosConstants.GetObservationParams;
import org.n52.sos.SosConstants.ValueTypes;
import org.n52.sos.decode.impl.uncertainty.ObservationConverter;
import org.n52.sos.ds.IGetObservationDAO;
import org.n52.sos.ds.pgsql.PGConnectionPool;
import org.n52.sos.ogc.om.AbstractSosObservation;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.SosGetObservationRequest;
import org.n52.sos.uncertainty.SosUncConstants;
import org.omg.CORBA.NameValuePair;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

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

	public Collection<NameValuePair> getUncCol(SosObservationCollection obsCol) {
		Collection<NameValuePair> uncCol = null;

		if (obsCol != null && obsCol.getObservationMembers() != null
				&& obsCol.getObservationMembers().size() > 0) {

			// collect IDs of all observations
			List<String> obsIDs = new ArrayList<String>();
			Iterator<AbstractSosObservation> obsColIt = obsCol
					.getObservationMembers().iterator();
			while (obsColIt.hasNext()) {
				AbstractSosObservation obs = obsColIt.next();
				obsIDs.add(obs.getObservationID());
			}
			
			// querry uncertainties for observations IDs
			ResultSet resultSet = queryUncertainty(obsIDs);
			
			
		}

		return uncCol;
	}
	
	private ResultSet queryUncertainty(List<String> obsIDs) {
		ResultSet resultSet = null;
		
		
		
		
		Connection con = null;
		
		
		
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

			} else {
				super.checkResultModel(resultModel, observedProperties);
			}
		}
	}
}
