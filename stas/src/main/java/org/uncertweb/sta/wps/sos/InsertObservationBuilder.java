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

import net.opengis.sos.x10.InsertObservationDocument;

import org.n52.wps.io.datahandler.generator.ObservationGenerator;
import org.uncertweb.api.om.observation.AbstractObservation;

/**
 * TODO JavaDoc
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class InsertObservationBuilder {

	private static InsertObservationBuilder singleton;

	public static InsertObservationBuilder getInstance() {
		if (singleton == null) {
			singleton = new InsertObservationBuilder();
		}
		return singleton;
	}

	private InsertObservationBuilder() {
	}

	private ObservationGenerator generator = new ObservationGenerator();

	public InsertObservationDocument build(AbstractObservation o) {
		InsertObservationDocument insObsDoc = InsertObservationDocument.Factory
				.newInstance();
		insObsDoc.addNewInsertObservation().set(generator.generateXML(o));
		insObsDoc.getInsertObservation().setAssignedSensorId(
				o.getProcedure().toString());
		return insObsDoc;
	}
}
