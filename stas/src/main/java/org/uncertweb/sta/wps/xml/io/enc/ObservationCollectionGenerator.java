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
package org.uncertweb.sta.wps.xml.io.enc;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationDocument;

import org.n52.wps.io.LargeBufferStream;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.utils.UwXmlUtils;
import org.w3c.dom.Node;

/**
 * Generator for {@link ObservationCollectionDocument}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ObservationCollectionGenerator extends AbstractGenerator {

	
	public ObservationCollectionGenerator() {
		this.supportedIDataTypes.add(OMBinding.class);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public OutputStream generate(IData arg0) {
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(arg0, baos);
		return baos;
	}

	/**
	 * {@inheritDoc}
	 */
	public Node generateXML(IData arg0, String arg1) {
		return generateXML(arg0).getDomNode();
	}

	/**
	 * Writes the given {@code IData} (which should be a
	 * {@link ObservationCollectionBinding}) to a {@code Writer}.
	 * 
	 * @param data
	 *            the data
	 * @param writer
	 *            the writer
	 */
	public void write(IData coll, Writer writer) {
		ObservationCollectionDocument xml = generateXML(coll);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter
					.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			xml.save(bufferedWriter, UwXmlUtils.defaultOptions());
			bufferedWriter.write("\n");
			bufferedWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeToStream(IData coll, OutputStream os) {
		OutputStreamWriter w = new OutputStreamWriter(os);
		write(coll, w);
	}

	/**
	 * Generates an {@code ObservationCollectionDocument} out of the given
	 * {@code IData} (which should be a {@link ObservationCollectionBinding}).
	 * 
	 * @param om
	 *            the {@code IData}
	 * @return the generated XmlBean
	 */
	public ObservationCollectionDocument generateXML(IData om) {
		Collection<? extends AbstractObservation> oc = ((OMBinding) om)
				.getObservationCollection().getObservations();
		ObservationCollectionDocument doc = ObservationCollectionDocument.Factory
				.newInstance();
		ObservationCollectionType xboc = doc.addNewObservationCollection();
		ObservationGenerator og = new ObservationGenerator();
		for (AbstractObservation o : oc) {
			ObservationDocument oDoc = og.generateXML(o);
			xboc.addNewMember().set(oDoc);
		}
		return doc;
	}
	
	@Override
	public InputStream generateStream(IData arg0, String arg1, String arg2)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeToStream(arg0, out);
		return new ByteArrayInputStream(out.toByteArray());
	}

}