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
package org.uncertweb.viss.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_CREATE_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE_LIST_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER_LIST_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.OM_2_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.STYLED_LAYER_DESCRIPTOR_TYPE;
import static org.uncertweb.viss.core.web.RESTServlet.DATASETS;
import static org.uncertweb.viss.core.web.RESTServlet.DATASET_PARAM_P;
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCE;
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCES;
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCE_PARAM_P;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZATION_PARAM_P;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZATION_SLD;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZERS_FOR_DATASET;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZER_FOR_DATASET;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZER_PARAM_P;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uncertweb.viss.core.util.JSONConstants;
import org.uncertweb.viss.core.vis.IVisualizer;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.Mean;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.Probability;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.ProbabilityForInterval;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.StandardDeviation;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.Variance;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

public class VissTest extends JerseyTest {
//	private static final Logger log = LoggerFactory.getLogger(JerseyTest.class);
	
	private static final String OM_DATE_TIME = "2011-08-02T16:39:17.820+02:00";
	
	
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

	public VissTest() throws Exception {
		super("org.uncertweb.viss");
	}

	private InputStream getNetCDFStream() {
		return getClass().getResourceAsStream("/biotemp.nc");
	}
	
	private InputStream getOsloMetStream() {
		return getClass().getResourceAsStream("/oslo_met_20110102.nc");
	}

	private InputStream getSLDStream() {
		return getClass().getResourceAsStream("/raster.xml");
	}

	private InputStream getOMStream() {
		return getClass().getResourceAsStream("/reference-observation.xml");
	}

	@Test
	public void testIS() {
		InputStream is = null;
		is = getNetCDFStream();
		assertNotNull(is);
		IOUtils.closeQuietly(is);
		is = getSLDStream();
		assertNotNull(is);
		IOUtils.closeQuietly(is);
		is = getOMStream();
		assertNotNull(is);
		IOUtils.closeQuietly(is);
	}

	
	@Test
	public void testTime() throws JSONException {
		ObjectId oid = addResource(NETCDF_TYPE, getOsloMetStream());
		String[] datasets = getDataSetsForResource(oid);
		for (String s : datasets)
			System.out.print(s+", ");
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	WebResource getWebResource() {
		return this.client().resource(getBaseURI());
	}

	@Test
	public void testEmpty() throws UniformInterfaceException, JSONException {
		getWebResource().path(RESOURCES).accept(JSON_RESOURCE_LIST_TYPE)
				.get(JSONObject.class).getJSONArray(JSONConstants.RESOURCES_KEY);
	}

	@Test
	public void testNotExistingResource() {
		try {
			getWebResource()
					.path(RESOURCE.replace(RESOURCE_PARAM_P,
							new ObjectId().toString()))
					.accept(JSON_RESOURCE_TYPE).get(Response.class);
		} catch (UniformInterfaceException e) {
			assertEquals(404, e.getResponse().getStatus());
			return;
		}
		fail();
	}

	private ObjectId addResource(MediaType mt, InputStream is)
			throws JSONException {
		JSONObject j = getWebResource().path(
				getWebResource().path(RESOURCES).accept(JSON_RESOURCE_TYPE)
						.entity(is, mt).post(ClientResponse.class)
						.getLocation().getPath()).get(JSONObject.class);

		return new ObjectId(j.getString(JSONConstants.ID_KEY));
	}

	private String createVisualization(ObjectId resource, String dataset,
			String visualizer, JSONObject params) throws JSONException {
		
		String path = VISUALIZER_FOR_DATASET
				.replace(RESOURCE_PARAM_P, resource.toString())
				.replace(DATASET_PARAM_P, dataset)
				.replace(VISUALIZER_PARAM_P, visualizer);
		System.err.println(path);
		ClientResponse cr = getWebResource()
				.path(path)
				.entity(params, JSON_CREATE_TYPE)
				.post(ClientResponse.class);
		System.err.println(cr);
		System.err.println(cr.getLocation());
		String s = cr.getLocation().getPath();
		
		JSONObject j = getWebResource().path(s).get(JSONObject.class);
		
		return j.getString(JSONConstants.ID_KEY);
	}

	@Test
	public void sameVisualizationWithParameters() throws JSONException {
		ObjectId oid = addResource(OM_2_TYPE, getOMStream());
		String[] datasets = getDataSetsForResource(oid);
		String cdfVisId1 = createVisualization(oid, datasets[0],
				getNameForVisualizer(Probability.class),
				new JSONObject().put("max", 0.5D).put("time", OM_DATE_TIME));
		String cdfVisId2 = createVisualization(oid, datasets[0],
				getNameForVisualizer(Probability.class),
				new JSONObject().put("max", 0.5D).put("time", OM_DATE_TIME));
		assertEquals(cdfVisId1, cdfVisId2);
	}

	public String[] getDataSetsForResource(ObjectId resource) {
		try {
			JSONObject j = getWebResource().path(
					DATASETS.replace(RESOURCE_PARAM_P, resource.toString())).get(
					JSONObject.class);
			JSONArray a = j.getJSONArray(JSONConstants.DATASETS_KEY);
			String[] result = new String[a.length()];
			for (int i = 0; i < result.length; ++i) {
				result[i] = a.getJSONObject(i).getString(JSONConstants.ID_KEY);
			}
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	public void addResourceAndCreateVisualizations() throws JSONException,
			UniformInterfaceException, XmlException, IOException {
		deleteAll();
		ObjectId oid = addResource(NETCDF_TYPE, getNetCDFStream());

		String[] datasets = getDataSetsForResource(oid);

		String meanVisId = createVisualization(oid, datasets[0],
				getNameForVisualizer(Mean.class), new JSONObject());
		createVisualization(oid, datasets[0],
				getNameForVisualizer(Variance.class), new JSONObject());
		createVisualization(oid, datasets[0],
				getNameForVisualizer(StandardDeviation.class), new JSONObject());
		createVisualization(oid, datasets[0],
				getNameForVisualizer(Probability.class),
				new JSONObject().put(Probability.MAX_PARAMETER, 0.5D));
		createVisualization(oid, datasets[0],
				getNameForVisualizer(ProbabilityForInterval.class),
				new JSONObject().put(ProbabilityForInterval.MIN_PARAMETER, 0.3D)
								.put(ProbabilityForInterval.MAX_PARAMETER, 0.6D));

		String url = VISUALIZATION_SLD
				.replace(RESOURCE_PARAM_P, oid.toString())
				.replace(DATASET_PARAM_P, datasets[0])
				.replace(VISUALIZATION_PARAM_P, meanVisId);

		ClientResponse cr = getWebResource()
				.path(url)
				.entity(getSLDStream(), STYLED_LAYER_DESCRIPTOR_TYPE)
				.post(ClientResponse.class);
		
		String sld = cr.getLocation().getPath(); 
		String xml = getWebResource().path(sld).get(String.class);
		StyledLayerDescriptorDocument.Factory.parse(xml);
		
	}

	@Test
	public void visualizersForResource() throws JSONException {
		ObjectId oid = addResource(NETCDF_TYPE, getNetCDFStream());
		String[] datasets = getDataSetsForResource(oid);
		JSONObject j = getWebResource()
				.path(VISUALIZERS_FOR_DATASET
						.replace(RESOURCE_PARAM_P, oid.toString())
						.replace(DATASET_PARAM_P, datasets[0]))
				.accept(JSON_VISUALIZER_LIST_TYPE).get(JSONObject.class);

		System.out.println(j.toString(4));
	}

	@Test
	public void testSameResource() throws JSONException {
		ObjectId oid1 = addResource(NETCDF_TYPE, getNetCDFStream());
		ObjectId oid2 = addResource(NETCDF_TYPE, getNetCDFStream());
		ObjectId oid3 = addResource(OM_2_TYPE, getOMStream());
		assertEquals(oid1, oid2);
		assertTrue(!oid1.equals(oid3));
	}

	@Test
	public void testSldFile() throws XmlException, IOException {
		StyledLayerDescriptorDocument.Factory.parse(getSLDStream());
	}

	@Test
	public void testOMResource() throws JSONException {
		deleteAll();
		ObjectId oid = addResource(OM_2_TYPE, getOMStream());
		String[] datasets = getDataSetsForResource(oid);
		createVisualization(oid, datasets[0], getNameForVisualizer(Mean.class),
				new JSONObject());
	}

	@Test
	public void testSameVisualization() throws JSONException {
		ObjectId oid = addResource(NETCDF_TYPE, getNetCDFStream());
		String[] datasets = getDataSetsForResource(oid);
		String visId1 = createVisualization(oid, datasets[0],
				getNameForVisualizer(Mean.class), new JSONObject());
		String visId2 = createVisualization(oid, datasets[0],
				getNameForVisualizer(Mean.class), new JSONObject());
		assertEquals(visId1, visId2);
		String visId3 = createVisualization(oid, datasets[0],
				getNameForVisualizer(Variance.class), new JSONObject());
		assertTrue(!visId2.equals(visId3));
	}

	public void deleteAll() throws JSONException {
		JSONObject j = getWebResource().path(RESOURCES)
				.accept(JSON_RESOURCE_LIST_TYPE).get(JSONObject.class);
		JSONArray a = j.optJSONArray(JSONConstants.RESOURCES_KEY);
		if (a != null) {
			for (int i = 0; i < a.length(); i++) {
				String id = a.getJSONObject(i).getString(JSONConstants.ID_KEY);
				System.out.println("Deleting resource " + id);
				deleteResource(id);
			}
		}
	}

	private String getNameForVisualizer(Class<? extends IVisualizer> vc) {
		try {
			return vc.newInstance().getShortName();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void deleteResource(String id) {
		getWebResource().path(RESOURCE.replace(RESOURCE_PARAM_P, id))
				.delete();
	}
}
