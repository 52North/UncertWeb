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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.opengis.wfs.GetFeatureDocument;

import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.LargeBufferStream;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GetFeatureRequestBinding;
import org.w3c.dom.Node;

/**
 * Generator for {@link GetFeatureDocument}s.
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class GetFeatureRequestGenerator extends AbstractGenerator {

	public GetFeatureRequestGenerator() {
		this.supportedIDataTypes.add(GetFeatureRequestBinding.class);
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
	public Class<?>[] getSupportedInternalInputDataType() {
		return new Class<?>[] { GetFeatureRequestBinding.class };
	}

	/**
	 * {@inheritDoc}
	 */
	public Node generateXML(IData data, String arg1) {
		return ((GetFeatureRequestBinding) data).getPayload().getDomNode();
	}

	/**
	 * Writes the given {@code IData} (which should be a
	 * {@link GetFeatureRequestBinding}) to a {@code Writer}.
	 *
	 * @param data the data
	 * @param writer the writer
	 */
	public void write(IData data, Writer writer) {
		GetFeatureDocument xml = ((GetFeatureRequestBinding) data).getPayload();
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			xml.save(bufferedWriter, new XmlOptions().setSavePrettyPrint());
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

	@Override
	public InputStream generateStream(IData arg0, String arg1, String arg2)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeToStream(arg0, out);
		return new ByteArrayInputStream(out.toByteArray());
	}

}