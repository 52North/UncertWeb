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

import net.opengis.wfs.GetFeatureDocument;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.utils.Utils;
import org.uncertweb.sta.wps.api.ProcessInputHandler;
import org.uncertweb.sta.wps.api.SingleProcessInput;

/**
 * Class that handles a {@code String}-URL, a {@code GetFeatureDocument} and a
 * {@code FeatureCollection} input to create a {@code FeatureCollection}.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class FeatureCollectionInputHandler extends
		ProcessInputHandler<FeatureCollection<FeatureType, Feature>> {

	/**
	 * Cache for GetFeature requests.
	 */
	private static final RequestCache<GetFeatureDocument, FeatureCollection<FeatureType, Feature>> CACHE = new RequestCache<GetFeatureDocument, FeatureCollection<FeatureType, Feature>>(
			Namespace.GML.SCHEMA, GTVectorDataBinding.class, Constants.MAX_CACHED_REQUESTS);

	/**
	 * The input containing the WFS URL.
	 */
	private SingleProcessInput<String> urlInput;

	/**
	 * The input containing the {@link GetFeatureDocument} request.
	 */
	private SingleProcessInput<GetFeatureDocument> requestInput;

	/**
	 * The input containing the {@link FeatureCollection} input.
	 */
	private SingleProcessInput<FeatureCollection<FeatureType, Feature>> collectionInput;

	/**
	 * Creates a new {@code FeatureCollectionInputHandler} for the given inputs.
	 * 
	 * @param featureCollection the {@link FeatureCollection} input
	 * @param wfsUrl the URL input
	 * @param wfsRequest the {@link GetFeatureDocument} input
	 */
	public FeatureCollectionInputHandler(
			SingleProcessInput<FeatureCollection<FeatureType, Feature>> featureCollection,
			SingleProcessInput<String> wfsUrl,
			SingleProcessInput<GetFeatureDocument> wfsRequest) {
		super(wfsRequest, wfsUrl, featureCollection);
		this.urlInput = wfsUrl;
		this.requestInput = wfsRequest;
		this.collectionInput = featureCollection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FeatureCollection<FeatureType, Feature> processInputs(
			Map<String, List<IData>> inputs) {
		long start = System.currentTimeMillis();

		String wfsUrl = this.urlInput.handle(inputs);
		GetFeatureDocument wfsReq = this.requestInput.handle(inputs);
		FeatureCollection<FeatureType, Feature> paramPolColl = this.collectionInput
				.handle(inputs);

		FeatureCollection<FeatureType, Feature> requestPolColl = null;

		if (wfsUrl != null && wfsReq != null) {
			IParser p = ParserFactory
					.getInstance()
					.getParser(Namespace.GML.SCHEMA, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING, GTVectorDataBinding.class);
			if (p == null) {
				throw new NullPointerException(
						"No Parser found to parse FeatureCollection.");
			}
			requestPolColl = CACHE.getResponse(wfsUrl, wfsReq, false);
		}

		FeatureCollection<FeatureType, Feature> result = null;
		if (paramPolColl != null) {
			if (requestPolColl != null) {
				paramPolColl.addAll(requestPolColl);
			}
			result = paramPolColl;
		} else if (requestPolColl != null) {
			result = requestPolColl;
		}
		log.info("Fetching of FeatureCollection took {}", Utils
				.timeElapsed(start));

		return result;
	}

}
