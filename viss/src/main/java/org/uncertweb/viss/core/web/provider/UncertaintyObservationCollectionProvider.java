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

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
@Produces(MediaTypes.JSON_OBSERVATION_COLLECTION)
public class UncertaintyObservationCollectionProvider implements
		MessageBodyWriter<IObservationCollection> {

	
	
	@SuppressWarnings("unchecked")
	private JSONObservationEncoder enc = new JSONObservationEncoder(
			UwCollectionUtils.<EncoderHook<JSONObject>>collection(new EncoderHook<JSONObject>() {
		@Override
		public void encode(AbstractObservation ao, JSONObject encodedObject) throws OMEncodingException {
			if (ao instanceof NcUwObservation) {
				try {
					JSONArray coordinates = new JSONArray();
					for (int i : ((NcUwObservation) ao).getGridCoordinates().getCoordinateValues()) {
						coordinates.put(i);
					}
					encodedObject.put("gridCoordinate", coordinates);
				} catch (JSONException e) {
					throw new OMEncodingException(e);
				}
			}
		}
	}));
	
	@Override
	public boolean isWriteable(Class<?> t, Type gt, Annotation[] a, MediaType mt) {
		return IObservationCollection.class.isAssignableFrom(t)
				&& mt.isCompatible(MediaTypes.JSON_OBSERVATION_COLLECTION_TYPE);
	}

	@Override
	public long getSize(IObservationCollection t, Class<?> ty, Type gt,
			Annotation[] a, MediaType mt) {
		return -1;
	}

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

}
