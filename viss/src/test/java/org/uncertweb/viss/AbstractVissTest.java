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
package org.uncertweb.viss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_CREATE_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE_LIST_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE_TYPE;
import static org.uncertweb.viss.core.web.RESTServlet.DATASET;
import static org.uncertweb.viss.core.web.RESTServlet.DATASETS;
import static org.uncertweb.viss.core.web.RESTServlet.DATASET_PARAM_P;
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCE;
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCES;
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCE_PARAM_P;
import static org.uncertweb.viss.core.web.RESTServlet.VALUE_OF_DATASET;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZERS_FOR_DATASET;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZER_FOR_DATASET;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZER_PARAM_P;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.viss.core.util.JSONConstants;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.core.vis.IVisualizer;
import org.uncertweb.viss.core.web.RESTServlet;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

public abstract class AbstractVissTest extends JerseyTest {

	protected static final Logger log = LoggerFactory.getLogger(JerseyTest.class);
	protected static final URI AGGREGATION_RESULT = URI.create("http://giv-uw.uni-muenster.de/data/netcdf/aggresults.nc");
	protected static final URI BIOTEMP_T = URI.create("http://giv-uw.uni-muenster.de/data/netcdf/biotemp-t.nc");
	protected static final URI BIOTEMP = URI.create("http://giv-uw.uni-muenster.de/data/netcdf/biotemp.nc");
	protected static final URI EU_JUNE = URI.create("http://giv-uw.uni-muenster.de/data/netcdf/EU_June_4.nc");
	protected static final String OM_DATE_TIME = "2011-08-02T16:39:17.820+02:00";

	@BeforeClass
	public static void initLogger() {
		java.util.logging.Logger rootLogger = java.util.logging.LogManager
				.getLogManager().getLogger("");
		java.util.logging.Handler[] handlers = rootLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			rootLogger.removeHandler(handlers[i]);
		}
		org.slf4j.bridge.SLF4JBridgeHandler.install();
	}

	public AbstractVissTest() {
		super("org.uncertweb.viss");
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Before
	public void deleteAll() throws JSONException {
		JSONObject j = getWebResource().path(RESOURCES)
				.accept(JSON_RESOURCE_LIST_TYPE).get(JSONObject.class);
		JSONArray a = j.optJSONArray(JSONConstants.RESOURCES_KEY);
		if (a != null) {
			for (int i = 0; i < a.length(); i++) {
				deleteResource(new ObjectId(a.getJSONObject(i).getString(JSONConstants.ID_KEY)));
			}
		}
	}

	protected InputStream getSLDStream() {
		return getClass().getResourceAsStream("/data/sld/raster.xml");
	}

	protected InputStream getOMStream() {
		return getClass().getResourceAsStream("/data/om/reference-observation.xml");
	}
	
	protected InputStream getUncertaintyCollectionStream() {
		return getClass().getResourceAsStream("/data/json/input.json");
	}

	protected JSONObject getVisualizerForDataset(ObjectId r, ObjectId ds, String s) throws JSONException {
		String path = VISUALIZER_FOR_DATASET
				.replace(RESOURCE_PARAM_P, r.toString())
				.replace(DATASET_PARAM_P, ds.toString())
				.replace(VISUALIZER_PARAM_P, s);
		JSONObject cr = getWebResource()
				.path(path)
				.get(JSONObject.class);
		return cr;
	}

	protected String[] getVisualizersForDataset(ObjectId r, ObjectId ds) {
		try {
			JSONObject j = getWebResource().path(
					VISUALIZERS_FOR_DATASET
					.replace(RESOURCE_PARAM_P, r.toString())
					.replace(DATASET_PARAM_P, ds.toString())).get(JSONObject.class);
			JSONArray a = j.getJSONArray(JSONConstants.VISUALIZERS_KEY);
			String[] result = new String[a.length()];
			for (int i = 0; i < result.length; ++i) {
				result[i] = a.getJSONObject(i).getString(JSONConstants.ID_KEY);
			}
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	protected JSONObject getVisualization(ObjectId resource, ObjectId dataset, String vis) {
		return getWebResource().path(RESTServlet.VISUALIZATION
				.replace(RESTServlet.RESOURCE_PARAM_P, resource.toString())
				.replace(RESTServlet.DATASET_PARAM_P, dataset.toString())
				.replace(RESTServlet.VISUALIZATION_PARAM_P, vis)).get(JSONObject.class);
	}

	protected IObservationCollection getValue(ObjectId r, ObjectId d, JSONObject req) {
		ClientResponse cr = getWebResource()
				.path(VALUE_OF_DATASET.replace(RESOURCE_PARAM_P, r.toString())
				.replace(DATASET_PARAM_P, d.toString()))
				.entity(req.toString()).type(MediaTypes.JSON_VALUE_REQUEST_TYPE)
				.post(ClientResponse.class);

		String s = cr.getEntity(String.class);
		JSONObservationParser p = new JSONObservationParser();
		IObservationCollection col = null;
		try {
			col = p.parse(s);
		} catch (OMParsingException e) {
			e.printStackTrace(System.err);
			fail();
		}
		return col;
	}

	protected JSONObject getDataSetForResource(ObjectId r, ObjectId d) {
		String path = DATASET.replace(RESOURCE_PARAM_P, r.toString()).replace(
				DATASET_PARAM_P, d.toString());
		JSONObject j = getWebResource().path(path).get(JSONObject.class);
		return j;
	}

	protected WebResource getWebResource() {
		return this.client().resource(getBaseURI());
	}

	protected ObjectId addResource(MediaType mt, InputStream is)
			throws JSONException {
		JSONObject j = getWebResource().path(
				getWebResource().path(RESOURCES).accept(JSON_RESOURCE_TYPE)
				.entity(is, mt).post(ClientResponse.class)
				.getLocation().getPath()).get(JSONObject.class);

		return new ObjectId(j.getString(JSONConstants.ID_KEY));
	}

	protected ObjectId addResource(MediaType mt, URI is)
			throws JSONException {
		JSONObject req = new JSONObject()
				.put(JSONConstants.URL_KEY, is.toString())
				.put(JSONConstants.METHOD_KEY, HttpMethod.GET)
				.put(JSONConstants.RESPONSE_MEDIA_TYPE_KEY, mt.toString());
		ClientResponse res = getWebResource().path(RESOURCES).accept(JSON_RESOURCE_TYPE)
				.entity(req, MediaTypes.JSON_REQUEST).post(ClientResponse.class);
		assertEquals(Status.CREATED.getStatusCode(), res.getStatus());
		JSONObject j = getWebResource().path(res
				.getLocation().getPath()).get(JSONObject.class);
		return new ObjectId(j.getString(JSONConstants.ID_KEY));
	}

	protected String createVisualization(ObjectId resource, ObjectId dataset,
			String visualizer, JSONObject params) throws JSONException {

		String path = VISUALIZER_FOR_DATASET
				.replace(RESOURCE_PARAM_P, resource.toString())
				.replace(DATASET_PARAM_P, dataset.toString())
				.replace(VISUALIZER_PARAM_P, visualizer);
		ClientResponse cr = getWebResource()
				.path(path)
				.entity(params, JSON_CREATE_TYPE)
				.post(ClientResponse.class);
		String s = cr.getLocation().getPath();

		return getWebResource().path(s).get(JSONObject.class).getString(JSONConstants.ID_KEY);
	}

	protected String createVisualization(ObjectId resource, ObjectId dataset, String visualizer) throws JSONException {
		return createVisualization(resource, dataset, visualizer, new JSONObject());
	}

	protected ObjectId[] getDataSetsForResource(ObjectId resource) {
		try {
			JSONObject j = getWebResource().path(
					DATASETS.replace(RESOURCE_PARAM_P, resource.toString())).get(
					JSONObject.class);
			JSONArray a = j.getJSONArray(JSONConstants.DATASETS_KEY);
			ObjectId[] result = new ObjectId[a.length()];
			for (int i = 0; i < result.length; ++i) {
				result[i] = new ObjectId(a.getJSONObject(i).getString(JSONConstants.ID_KEY));
			}
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	protected String getNameForVisualizer(Class<? extends IVisualizer> vc) {
		try {
			return vc.newInstance().getShortName();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void deleteResource(ObjectId id) {
		getWebResource().path(RESOURCE.replace(RESOURCE_PARAM_P, id.toString())).delete();
	}
}
