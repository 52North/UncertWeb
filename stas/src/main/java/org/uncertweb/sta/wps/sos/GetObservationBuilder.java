/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps.sos;

import java.net.URI;
import java.util.List;
import java.util.Set;

import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.ResponseModeType;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.utils.UwCollectionUtils;

/**
 * TODO JavaDoc
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class GetObservationBuilder {

	private static GetObservationBuilder singleton;

	public static GetObservationBuilder getInstance() {
		if (singleton == null) {
			singleton = new GetObservationBuilder();
		}
		return singleton;
	}

	private GetObservationBuilder() {}

	public GetObservationDocument build(URI process, List<? extends AbstractObservation> obs) {
		GetObservationDocument getObsDoc = GetObservationDocument.Factory.newInstance();
		GetObservation getObs = getObsDoc.addNewGetObservation();
		getObs.setService(Constants.Sos.SERVICE_NAME);
		getObs.setVersion(Constants.Sos.SERVICE_VERSION);
		getObs.setResultModel(Constants.Sos.MEASUREMENT_RESULT_MODEL);
		getObs.setOffering(Constants.Sos.AGGREGATION_OFFERING_ID);
		getObs.setResponseFormat(Constants.Sos.OBSERVATION_OUTPUT_FORMAT);
		getObs.setResponseMode(ResponseModeType.INLINE);
		getObs.addNewProcedure().setStringValue(process.toString());

		Set<String> obsProps = UwCollectionUtils.set();
		for (AbstractObservation o : obs) {
			obsProps.add(o.getObservedProperty().toString());
		}

		for (String s : obsProps) {
			getObs.addNewObservedProperty().setStringValue(s);
		}

		return getObsDoc;
	}
}
