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
package org.uncertweb.sta.wps;

import java.util.List;
import java.util.Map;

import net.opengis.sos.x10.GetObservationDocument;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.om.OMParser;
import org.n52.wps.server.AlgorithmParameterException;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.api.ProcessInputHandler;
import org.uncertweb.sta.wps.api.SingleProcessInput;

/**
 * Class that handles a {@code String}-URL and a {@code GetObservationDocument}
 * input to create a {@link ObservationCollection}.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationCollectionInputHandler extends
		ProcessInputHandler<IObservationCollection> {

	/**
	 * Cache for GetObservation requests.
	 */
	private static final RequestCache<GetObservationDocument, IObservationCollection> CACHE 
		= new RequestCache<GetObservationDocument, IObservationCollection>(new OMParser(),
			Constants.MAX_CACHED_REQUESTS);

	private SingleProcessInput<String> urlInput;
	private SingleProcessInput<GetObservationDocument> requestInput;
	private SingleProcessInput<String> responseMimeTypeInput;
	private SingleProcessInput<String> responseSchemaInput;

	public ObservationCollectionInputHandler(
			SingleProcessInput<String> url,
			SingleProcessInput<GetObservationDocument> request, 
			SingleProcessInput<String> responseMimeType,
			SingleProcessInput<String> responseSchema) {
		super(url, request, responseMimeType, responseSchema);
		this.urlInput = url;
		this.requestInput = request;
		this.responseMimeTypeInput = responseMimeType;
		this.responseSchemaInput = responseSchema;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IObservationCollection processInputs(
			Map<String, List<IData>> inputs) {
		String sosUrl = urlInput.handle(inputs);
		GetObservationDocument sosReq = requestInput.handle(inputs);
		String mimeType = responseMimeTypeInput.handle(inputs);
		String schema = responseSchemaInput.handle(inputs);
		if (sosUrl == null) {
			throw new AlgorithmParameterException("No Source SOS Url.");
		}
		return CACHE.getResponse(sosUrl, sosReq, mimeType, schema, false);
	}

}
