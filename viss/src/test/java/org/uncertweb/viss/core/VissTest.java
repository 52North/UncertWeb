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
import static org.uncertweb.viss.core.web.RESTServlet.*;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZER_PARAM_P;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.uncertweb.utils.UwTimeUtils;
import org.uncertweb.viss.core.util.JSONConstants;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.core.vis.IVisualizer;
import org.uncertweb.viss.core.web.RESTServlet;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.Mean;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.Probability;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.ProbabilityForInterval;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.StandardDeviation;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer.Variance;
import org.uncertweb.viss.vis.sample.RealisationVisualizer;
import org.uncertweb.viss.vis.statistic.SimpleStatisticVisualizer.MeanStatistic;

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
	
	public VissTest() throws Exception {
		super("org.uncertweb.viss");
	}

	private InputStream getNetCDFStream() {
		return getClass().getResourceAsStream("/data/netcdf/biotemp.nc");
	}
	
	private InputStream getBiotempWithTimeStream() {
		return getClass().getResourceAsStream("/data/netcdf/biotemp-t.nc");
	}
	
	private InputStream getOsloMetStream() {
		return getClass().getResourceAsStream("/data/netcdf/oslo_met_20110102.nc");
	}

	private InputStream getOsloCbgStream() {
		return getClass().getResourceAsStream("/data/netcdf/oslo_cbg_20110101.nc");
	}
	
	private InputStream getSLDStream() {
		return getClass().getResourceAsStream("/data/sld/raster.xml");
	}

	private InputStream getOMStream() {
		return getClass().getResourceAsStream("/data/om/reference-observation.xml");
	}
	
	private InputStream getMahalanobianStream() {
		return getClass().getResourceAsStream("/data/netcdf/mahalanobian_stats.nc");
	}
	
	private InputStream getAggregationResultStream() {
		return getClass().getResourceAsStream("/data/netcdf/aggresults.nc");
	}
	
	private InputStream getUncertaintyCollectionStream() {
		return getClass().getResourceAsStream("/data/json/input.json");
	}

	@Test
	public void testIS() {
		testStream(getNetCDFStream());
		testStream(getSLDStream());
		testStream(getOMStream());
		testStream(getMahalanobianStream());
		testStream(getOsloMetStream());
		testStream(getOsloCbgStream());
		testStream(getAggregationResultStream());
		testStream(getUncertaintyCollectionStream());
		testStream(getBiotempWithTimeStream());
	}
	
	private void testStream(InputStream is) {
		assertNotNull(is);
		IOUtils.closeQuietly(is);
	}

	@Test
	public void testUncertaintyCollection() throws JSONException {
		testUncertaintyCollection_();
		testUncertaintyCollection_();
	}
	private void testUncertaintyCollection_() throws JSONException {
		ObjectId r = addResource(MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE, getUncertaintyCollectionStream());
		for (ObjectId ds : getDataSetsForResource(r)) {
			for (String s : getVisualizersForDataset(r, ds))  {
				if (s.equals("MeanStatistic")
						|| s.equals("StandardDeviationStatistic")
						|| s.equals("ProbabilityStatistic")) {
					getVisualizerForDataset(r,ds, s);
					createVisualization(r, ds, s);
				}
				if (s.equals("Realisation")) {
					getVisualizerForDataset(r, ds, s);
					createVisualization(r, ds, s, new JSONObject().put(
							RealisationVisualizer.REALISATION_PARAMETER, 0));
				}
			}
		}
	}
	
	private JSONObject getVisualizerForDataset(ObjectId r, ObjectId ds, String s) throws JSONException {
		String path = VISUALIZER_FOR_DATASET
			.replace(RESOURCE_PARAM_P, r.toString())
			.replace(DATASET_PARAM_P, ds.toString())
			.replace(VISUALIZER_PARAM_P, s);
		JSONObject cr = getWebResource()
				.path(path)
				.get(JSONObject.class);
		return cr;
	}

	private String[] getVisualizersForDataset(ObjectId r, ObjectId ds) {
		try {
			JSONObject j = getWebResource().path(
					VISUALIZERS_FOR_DATASET
						.replace(RESOURCE_PARAM_P, r.toString())
						.replace(DATASET_PARAM_P, ds.toString())
					).get(JSONObject.class);
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

	@Test 
	@Ignore
	public void testMahalanobian() throws JSONException {
		ObjectId r = addResource(NETCDF_TYPE, getMahalanobianStream());
		ObjectId ds = getDataSetsForResource(r)[0];
		
		String vis = createVisualization(r, ds, MeanStatistic.class.getSimpleName());
		
		JSONObject res = getVisualization(r, ds, vis);
		System.err.println(res.toString(4));
	}

	private JSONObject getVisualization(ObjectId resource, ObjectId dataset, String vis) {
		return getWebResource().path(RESTServlet.VISUALIZATION
				.replace(RESTServlet.RESOURCE_PARAM_P, resource.toString())
				.replace(RESTServlet.DATASET_PARAM_P, dataset.toString())
				.replace(RESTServlet.VISUALIZATION_PARAM_P, vis)).get(JSONObject.class);
	}
	
	@Test 
	public void testAggrgationResults() throws JSONException {
		ObjectId r = addResource(NETCDF_TYPE, getAggregationResultStream());
		ObjectId ds = getDataSetsForResource(r)[0];
		String vis = createVisualization(r, ds, MeanStatistic.class.getSimpleName());
		JSONObject res = getVisualization(r, ds, vis);
		System.err.println(res.toString(4));
	}
	
	@Test 
	public void testMimeTypeParameterForNetCDF() throws JSONException {
		ObjectId r = addResource(MediaType.valueOf("application/netcdf; encoding=\"utf-8\""), getAggregationResultStream());
		ObjectId ds = getDataSetsForResource(r)[0];
		String vis = createVisualization(r, ds, MeanStatistic.class.getSimpleName());
		JSONObject res = getVisualization(r, ds, vis);
		System.err.println(res.toString(4));
	}
	
	@Test
	public void testMimeTypeParameterForOM() throws JSONException {
		ObjectId oid = addResource(MediaType.valueOf("application/vnd.ogc.om+xml; encoding=\"utf-8\""), getOMStream());
		ObjectId[] datasets = getDataSetsForResource(oid);
		createVisualization(oid, datasets[0], getNameForVisualizer(Mean.class));
	}
	
	@Test
	public void testMimeTypeParameterForUC() throws JSONException {
		ObjectId r = addResource(MediaType.valueOf("application/vnd.org.uncertweb.viss.uncertainty-collection+json; encoding=\"utf-8\""), getUncertaintyCollectionStream());
		int created = 0;
		for (ObjectId ds : getDataSetsForResource(r)) {
			for (String s : getVisualizersForDataset(r, ds))  {
				if (s.equals("MeanStatistic")
						|| s.equals("StandardDeviationStatistic")
						|| s.equals("ProbabilityStatistic")) {
					getVisualizerForDataset(r,ds, s);
					createVisualization(r, ds, s);
					++created;
				}
				if (s.equals("Realisation")) {
					getVisualizerForDataset(r, ds, s);
					createVisualization(r, ds, s, new JSONObject().put(
							RealisationVisualizer.REALISATION_PARAMETER, 0));
					++created;
				}
			}
		}
		Assert.assertEquals(5, created);
	}	
	
	@Test
	public void testTime() throws JSONException {
		ObjectId r = addResource(NETCDF_TYPE, getOsloMetStream());
		ObjectId d = getDataSetsForResource(r)[0];
		String v = getVisualizersForDataset(r, d)[0];
		createVisualization(r, d, v, new JSONObject().put("time", "2011-01-02T15:00:00.000Z").put("realisation", 0).put("sample", 15));
	}
	
	
	@Test
	public void testBiotempTime() throws JSONException {
		ObjectId r = addResource(NETCDF_TYPE, getBiotempWithTimeStream());
		ObjectId[] datasets = getDataSetsForResource(r);
		assertEquals(1, datasets.length);
		ObjectId d = datasets[0];
		
		String[] visualizers = getVisualizersForDataset(r, d);
		assertEquals(7, visualizers.length);
		
		DateTime begin = UwTimeUtils.parseDateTime("2012-04-01T09:00:00.000Z");
		for (int i = 0; i < 6; ++i) {
			String time = UwTimeUtils.format(begin.plusHours(i));
			JSONObject timep = new JSONObject().put("time", time);
			createVisualization(r, d, getNameForVisualizer(Mean.class), timep);
			createVisualization(r, d, getNameForVisualizer(Variance.class), timep);
			createVisualization(r, d, getNameForVisualizer(StandardDeviation.class), timep);
			createVisualization(r, d, getNameForVisualizer(Probability.class), new JSONObject()
					.put(Probability.MAX_PARAMETER, 0.5D).put("time", time));
			createVisualization(r, d, getNameForVisualizer(ProbabilityForInterval.class), new JSONObject()
					.put(ProbabilityForInterval.MIN_PARAMETER, 0.3D)
					.put(ProbabilityForInterval.MAX_PARAMETER, 0.6D)
					.put("time", time));
		}
	}
	
	public JSONObject getDataSetForResource(ObjectId r, ObjectId d) {
		String path = DATASET.replace(RESOURCE_PARAM_P, r.toString()).replace(
				DATASET_PARAM_P, d.toString());
		JSONObject j = getWebResource().path(path).get(JSONObject.class);
		return j;
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

	private String createVisualization(ObjectId resource, ObjectId dataset,
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

	private String createVisualization(ObjectId resource, ObjectId dataset, String visualizer) throws JSONException {
		return createVisualization(resource, dataset, visualizer, new JSONObject());
	}

	@Test
	public void sameVisualizationWithParameters() throws JSONException {
		ObjectId oid = addResource(OM_2_TYPE, getOMStream());
		ObjectId[] datasets = getDataSetsForResource(oid);
		String cdfVisId1 = createVisualization(oid, datasets[0],
				getNameForVisualizer(Probability.class),
				new JSONObject().put("max", 0.5D).put("time", OM_DATE_TIME));
		String cdfVisId2 = createVisualization(oid, datasets[0],
				getNameForVisualizer(Probability.class),
				new JSONObject().put("max", 0.5D).put("time", OM_DATE_TIME));
		assertEquals(cdfVisId1, cdfVisId2);
	}

	public ObjectId[] getDataSetsForResource(ObjectId resource) {
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

	@Test
	public void addResourceAndCreateVisualizations() throws JSONException,
			UniformInterfaceException, XmlException, IOException {
		ObjectId oid = addResource(NETCDF_TYPE, getNetCDFStream());

		ObjectId[] datasets = getDataSetsForResource(oid);

		String meanVisId = createVisualization(oid, datasets[0], getNameForVisualizer(Mean.class));
		createVisualization(oid, datasets[0], getNameForVisualizer(Variance.class));
		createVisualization(oid, datasets[0], getNameForVisualizer(StandardDeviation.class));
		createVisualization(oid, datasets[0], getNameForVisualizer(Probability.class),
				new JSONObject().put(Probability.MAX_PARAMETER, 0.5D));
		createVisualization(oid, datasets[0], getNameForVisualizer(ProbabilityForInterval.class),
				new JSONObject().put(ProbabilityForInterval.MIN_PARAMETER, 0.3D)
								.put(ProbabilityForInterval.MAX_PARAMETER, 0.6D));

		String url = VISUALIZATION_SLD
				.replace(RESOURCE_PARAM_P, oid.toString())
				.replace(DATASET_PARAM_P, datasets[0].toString())
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
		ObjectId[] datasets = getDataSetsForResource(oid);
		JSONObject j = getWebResource()
				.path(VISUALIZERS_FOR_DATASET
						.replace(RESOURCE_PARAM_P, oid.toString())
						.replace(DATASET_PARAM_P, datasets[0].toString()))
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
		ObjectId oid = addResource(OM_2_TYPE, getOMStream());
		ObjectId[] datasets = getDataSetsForResource(oid);
		createVisualization(oid, datasets[0], getNameForVisualizer(Mean.class));
	}

	@Test
	public void testSameVisualization() throws JSONException {
		ObjectId oid = addResource(NETCDF_TYPE, getNetCDFStream());
		ObjectId[] datasets = getDataSetsForResource(oid);
		String visId1 = createVisualization(oid, datasets[0], getNameForVisualizer(Mean.class));
		String visId2 = createVisualization(oid, datasets[0], getNameForVisualizer(Mean.class));
		String visId3 = createVisualization(oid, datasets[0], getNameForVisualizer(Variance.class));
		assertEquals(visId1, visId2);
		assertTrue(!visId2.equals(visId3));
	}

	private String getNameForVisualizer(Class<? extends IVisualizer> vc) {
		try {
			return vc.newInstance().getShortName();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void deleteResource(ObjectId id) {
		getWebResource().path(RESOURCE.replace(RESOURCE_PARAM_P, id.toString())).delete();
	}
}
