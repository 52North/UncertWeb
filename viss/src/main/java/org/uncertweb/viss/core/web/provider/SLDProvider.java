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
package org.uncertweb.viss.core.web.provider;

import static org.uncertweb.viss.core.util.MediaTypes.STYLED_LAYER_DESCRIPTOR_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.uncertweb.viss.core.VissConfig;
import org.uncertweb.viss.core.VissError;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

@Provider
public class SLDProvider extends
		AbstractMessageReaderWriterProvider<StyledLayerDescriptorDocument> {

	
	private boolean isProcessable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return mt.isCompatible(STYLED_LAYER_DESCRIPTOR_TYPE)
				&& StyledLayerDescriptorDocument.class.isAssignableFrom(t);
	}
	
	@Override
	public boolean isReadable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return isProcessable(t, gt, a, mt);
	}
	
	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return isProcessable(t, gt, a, mt);
	}

	@Override
	public StyledLayerDescriptorDocument readFrom(
			Class<StyledLayerDescriptorDocument> t, Type gt, Annotation[] a,
			MediaType mt, MultivaluedMap<String, String> hh, InputStream es)
			throws IOException, WebApplicationException {
		try {
			String encoding = mt.getParameters().get("encoding");
			String sld = (encoding != null) ? IOUtils.toString(es, encoding) : IOUtils.toString(es);
			return StyledLayerDescriptorDocument.Factory.parse(sld, 
					VissConfig.getInstance().getDefaultXmlOptions());
		} catch (XmlException e) {
			throw VissError.internal(e);
		}
	}

	
	@Override
	public void writeTo(StyledLayerDescriptorDocument x, Class<?> t, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> hh,
			OutputStream es) throws IOException {
		x.save(es, VissConfig.getInstance().getDefaultXmlOptions());
	}

}
