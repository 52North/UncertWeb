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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.uncertweb.sta.wps.parameter.ParametersDocument;
import org.uncertweb.sta.wps.parameter.ParametersDocument.Parameters;
import org.uncertweb.sta.wps.parameter.ParametersDocument.Parameters.Parameter;
import org.uncertweb.sta.wps.xml.binding.ParameterBinding;
import org.w3c.dom.Node;

/**
 * Generator for {@link ParametersDocument}s.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class ParameterGenerator extends AbstractXMLGenerator implements
		IStreamableGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream generate(IData arg0) {
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(arg0, baos);
		return baos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		return new Class<?>[] { ParameterBinding.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node generateXML(IData data, String arg1) {
		return generateXML(data).getDomNode();
	}

	/**
	 * Writes the given {@code IData} (which should be a {@link ParamterBinding}
	 * ) to a {@code Writer}.
	 * 
	 * @param data
	 *            the data
	 * @param writer
	 *            the writer
	 */
	public void write(IData data, Writer writer) {
		ParametersDocument xml = generateXML(data);
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
	@Override
	public void writeToStream(IData coll, OutputStream os) {
		OutputStreamWriter w = new OutputStreamWriter(os);
		write(coll, w);
	}

	/**
	 * Generates an {@code ParameterDocument} out of the given {@code IData}
	 * (which should be a {@link ParameterBinding}).
	 * 
	 * @param data
	 *            the {@code IData}
	 * @return the generated XmlBean
	 */
	protected ParametersDocument generateXML(IData data) {
		ParametersDocument doc = ParametersDocument.Factory.newInstance();
		Parameters xbParams = doc.addNewParameters();
		for (Map.Entry<String, String> entry : (((ParameterBinding) data)
				.getPayload()).entrySet()) {
			Parameter p = xbParams.addNewParameter();
			p.setKey(entry.getKey());
			p.setValue(entry.getValue());
		}
		return doc;
	}

}