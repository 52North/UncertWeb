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
package org.uncertweb.sta.wps.xml.io.dec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import net.opengis.om.x10.ObservationCollectionDocument;

import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.uncertweb.sta.wps.xml.binding.ObservationCollectionBinding;

/**
 * Parser for {@link ObservationCollectionDocument}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationCollectionParser extends AbstractXMLParser implements
		IStreamableParser {

	/**
	 * The supported output format.
	 */
	private static final Class<?>[] SUPPORTED_INTERNAT_OUTPUT_DATA_TYPE = new Class<?>[] { ObservationCollectionBinding.class };

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		return SUPPORTED_INTERNAT_OUTPUT_DATA_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservationCollectionBinding parse(InputStream is, String mime) {
		if (!isSupportedFormat(mime)) {
			throw new RuntimeException("Not a compatible mime type: " + mime);
		}
		return parseXML(is);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservationCollectionBinding parseXML(String xml) {
		return parseXML(new ByteArrayInputStream(xml.getBytes()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservationCollectionBinding parseXML(InputStream is) {
		try {
			return new ObservationCollectionBinding(
					new org.uncertweb.intamap.om.io.ObservationCollectionParser()
							.parse(is));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservationCollectionBinding parseXML(URI uri) {
		try {
			URLConnection connection = uri.toURL().openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			InputStream stream = connection.getInputStream();
			return parseXML(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
