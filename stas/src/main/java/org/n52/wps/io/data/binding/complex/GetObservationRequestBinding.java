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
package org.n52.wps.io.data.binding.complex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;

import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.GetObservationRequestGenerator;
import org.n52.wps.io.datahandler.parser.GetObservationRequestParser;

/**
 * {@link IData} binding for {@link GetObservationDocument}s.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class GetObservationRequestBinding implements IComplexData {

	private static final long serialVersionUID = 2249930191625226883L;
	private transient GetObservationDocument getObs;

	public GetObservationRequestBinding(GetObservationDocument getObs) {
		this.getObs = getObs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GetObservationDocument getPayload() {
		return getObs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getSupportedClass() {
		return GetObservationDocument.class;
	}

	/**
	 * Serializes this {@code IData}.
	 *
	 * @param oos the {@code ObjectOutputStream} to write to
	 * @throws IOException if an IO error occurs
	 */
	private synchronized void writeObject(ObjectOutputStream oos)
			throws IOException {
		StringWriter buffer = new StringWriter();
		GetObservationRequestGenerator g = new GetObservationRequestGenerator();
		g.write(this, buffer);
		oos.writeObject(buffer.toString());
	}

	/**
	 * De-serializes this {@code IData}.
	 *
	 * @param oos the {@code ObjectInputStream} to read from
	 * @throws IOException if an IO error occurs
	 * @throws ClassNotFoundException if the class of a serialized object cannot
	 *             be found
	 */
	private synchronized void readObject(ObjectInputStream oos)
			throws IOException, ClassNotFoundException {
		this.getObs = new GetObservationRequestParser()
				.parseXML((String) oos.readObject()).getPayload();
	}

}
