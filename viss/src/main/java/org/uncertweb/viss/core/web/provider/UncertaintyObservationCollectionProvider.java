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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.coverage.grid.GridCoordinates;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.AbstractHookedObservationEncoder.EncoderHook;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.netcdf.NcUwObservation;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.MediaTypes;

@Provider
public class UncertaintyObservationCollectionProvider extends AbstractJsonSingleWriterProvider<IObservationCollection> {

	protected UncertaintyObservationCollectionProvider() {
		super(IObservationCollection.class, MediaTypes.JSON_OBSERVATION_COLLECTION_TYPE);
	}

	@SuppressWarnings("unchecked")
	private JSONObservationEncoder enc = new JSONObservationEncoder(UwCollectionUtils.<EncoderHook<JSONObject>> collection(new EncoderHook<JSONObject>() {
		public void encode(AbstractObservation ao, JSONObject eo) throws OMEncodingException {
			if (ao instanceof NcUwObservation) {
				try {
					JSONArray c = new JSONArray();
					GridCoordinates gc = ((NcUwObservation) ao)	.getGridCoordinates();
					if (gc != null) {
						for (int i : gc.getCoordinateValues()) { c.put(i); }
						eo.put("gridCoordinate", c);
					}
				} catch (JSONException e) { 
					throw new OMEncodingException(e); 
				}
			}
		}
	}));


	@Override
	public void writeTo(IObservationCollection t, Class<?> ty, Type gt,
			Annotation[] a, MediaType mt, MultivaluedMap<String, Object> h,
			OutputStream out) throws IOException, WebApplicationException {
		try {
			enc.encodeObservationCollection(t, out);
		} catch (OMEncodingException e) {
			throw VissError.internal(e);
		}
	}

	@Override
	protected org.codehaus.jettison.json.JSONObject encode(
			IObservationCollection t)
			throws org.codehaus.jettison.json.JSONException {
		try {
			return new org.codehaus.jettison.json.JSONObject(enc.encodeObservationCollection(t));
		} catch (OMEncodingException e) {
			throw VissError.internal(e);
		}
	}

}
