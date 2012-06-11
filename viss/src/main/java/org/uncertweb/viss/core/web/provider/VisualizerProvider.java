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

import static org.uncertweb.viss.core.util.JSONConstants.DESCRIPTION_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.ID_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.OPTIONS_KEY;
import static org.uncertweb.viss.core.util.JSONConstants.SUPPORTED_UNCERTAINTIES_KEY;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER_TYPE;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.uncertweb.netcdf.NcUwUncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.vis.IVisualizer;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces(JSON_VISUALIZER)
public class VisualizerProvider implements MessageBodyWriter<IVisualizer> {

	@Override
	public boolean isWriteable(Class<?> type, Type gt, Annotation[] a,
	    MediaType mt) {
		return mt.equals(JSON_VISUALIZER_TYPE)
		    && IVisualizer.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(IVisualizer v, Class<?> t, Type gt, Annotation[] a,
	    MediaType mt, MultivaluedMap<String, Object> hh, OutputStream es)
	    throws IOException {
		try {

			JSONObject j = new JSONObject().putOpt(DESCRIPTION_KEY, v.getDescription())
			    .put(ID_KEY, v.getShortName());
			JSONArray ar = new JSONArray();
			for (NcUwUncertaintyType ut : v.getCompatibleUncertaintyTypes()) {
				ar.put(ut.getURI());
			}
			j.put(SUPPORTED_UNCERTAINTIES_KEY, ar);
			if (v.getDataSet() != null) {
				j.put(OPTIONS_KEY, v.getOptionsForDataSet(v.getDataSet()));
			} else {
				LoggerFactory.getLogger(getClass()).info("{}", v.getOptions());
				j.put(OPTIONS_KEY, v.getOptions());
			}
			ReaderWriter.writeToAsString(Utils.stringifyJson(j), es, mt);
		} catch (JSONException e) {
			VissError.internal(e);
		}
	}

	@Override
	public long getSize(IVisualizer v, Class<?> t, Type g, Annotation[] a,
	    MediaType m) {
		return -1;
	}

}
