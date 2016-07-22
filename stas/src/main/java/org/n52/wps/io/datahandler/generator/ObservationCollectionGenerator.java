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
package org.n52.wps.io.datahandler.generator;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_TEXT_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_OM_V1;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationDocument;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwXmlUtils;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;

/**
 * Generator for {@link ObservationCollectionDocument}s.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationCollectionGenerator extends AbstractUwGenerator {

	public ObservationCollectionGenerator() {
		super(set(SCHEMA_OM_V1), set(ENCODING_UTF_8), set(MIME_TYPE_TEXT_XML),
				UwCollectionUtils.<Class<?>> set(OMBinding.class));
	}

	public void writeToStream(IData coll, OutputStream os) {
		OutputStreamWriter w;
		try {
			w = new OutputStreamWriter(os, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		Collection<? extends AbstractObservation> oc = ((OMBinding) coll)
				.getObservationCollection().getObservations();
		ObservationCollectionDocument doc = ObservationCollectionDocument.Factory
				.newInstance();
		ObservationCollectionType xboc = doc.addNewObservationCollection();
		ObservationGenerator og = new ObservationGenerator();
		for (AbstractObservation o : oc) {
			ObservationDocument oDoc = og.generateXML(o);
			xboc.addNewMember().set(oDoc);
		}
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(w);
			bufferedWriter
					.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			doc.save(bufferedWriter, UwXmlUtils.defaultOptions());
			bufferedWriter.write("\n");
			bufferedWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}