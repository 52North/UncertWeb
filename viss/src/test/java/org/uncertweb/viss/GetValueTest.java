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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uncertweb.viss;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.viss.core.util.MediaTypes;


public class GetValueTest extends AbstractVissTest {
	@Test
	public void testGetValue() throws JSONException, OMEncodingException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP_T);
		ObjectId d = getDataSetsForResource(r)[0];

		getVisualizerForDataset(r,d, getVisualizersForDataset(r, d)[0]);
		System.err.println(getDataSetForResource(r, d).toString(4));
		JSONObject req = new JSONObject()
			.put("time", new JSONObject()
				.put("TimeInstant", new JSONObject()
					.put("timePosition", "2012-04-01T10:00:00.000Z")))
			.put("location", new JSONObject()
				.put("type", "Point")
				.put("coordinates", new JSONArray()
					.put(7).put(52))
				.put("crs", new JSONObject()
					.put("type","name")
					.put("properties", new JSONObject()
							.put("name", "http://www.opengis.net/def/crs/EPSG/0/4326"))));

		System.out.println(req.toString(4));
		IObservationCollection col = getValue(r, d, req);
		assertNotNull(col);
		assertTrue(col.getObservations().size() > 0);
		XBObservationEncoder enc = new XBObservationEncoder();
		enc.encodeObservationCollection(col, System.err);
	}

	@Test
	public void testGetValueWithDifferentSrs() throws JSONException, OMEncodingException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP_T);
		ObjectId d = getDataSetsForResource(r)[0];

		getVisualizerForDataset(r,d, getVisualizersForDataset(r, d)[0]);
		System.err.println(getDataSetForResource(r, d).toString(4));
		JSONObject req = new JSONObject()
			.put("location", new JSONObject()
				.put("type", "Point")
				.put("coordinates", new JSONArray()
					.put(2568717.63923).put(5763380.95098))
				.put("crs", new JSONObject()
					.put("type","name")
					.put("properties", new JSONObject()
							.put("name", "http://www.opengis.net/def/crs/EPSG/0/31466"))));

		System.out.println(req.toString(4));
		IObservationCollection col = getValue(r, d, req);
		assertNotNull(col);
		assertTrue(col.getObservations().size() > 0);
		XBObservationEncoder enc = new XBObservationEncoder();
		enc.encodeObservationCollection(col, System.err);
	}

	@Test
	public void testGetValueWithDifferentSrs2() throws JSONException, OMEncodingException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP_T);
		ObjectId d = getDataSetsForResource(r)[0];

		getVisualizerForDataset(r,d, getVisualizersForDataset(r, d)[0]);
		System.err.println(getDataSetForResource(r, d).toString(4));
		JSONObject req = new JSONObject()
			.put("location", new JSONObject()
				.put("type", "Point")
				.put("coordinates", new JSONArray()
					.put(1022139.0431734).put(6292867.3477923))
				.put("crs", new JSONObject()
					.put("type","name")
					.put("properties", new JSONObject()
							.put("name", "http://www.opengis.net/def/crs/EPSG/0/3857"))));
		System.out.println(req.toString(4));
		IObservationCollection col = getValue(r, d, req);
		assertNotNull(col);
		assertTrue(col.getObservations().size() > 0);
		XBObservationEncoder enc = new XBObservationEncoder();
		enc.encodeObservationCollection(col, System.err);
	}

}
