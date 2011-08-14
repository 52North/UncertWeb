package org.uw.viss.core;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.uncertweb.viss.core.util.Constants.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.Constants.OM_2_TYPE;
import static org.uncertweb.viss.core.util.Constants.STYLED_LAYER_DESCRIPTOR_TYPE;
import static org.uncertweb.viss.core.web.Servlet.RESOURCES;
import static org.uncertweb.viss.core.web.Servlet.RESOURCE_WITH_ID;
import static org.uncertweb.viss.core.web.Servlet.RES_PARAM_P;
import static org.uncertweb.viss.core.web.Servlet.VISUALIZATIONS;
import static org.uncertweb.viss.core.web.Servlet.VISUALIZATION_SLD;
import static org.uncertweb.viss.core.web.Servlet.VISUALIZERS;
import static org.uncertweb.viss.core.web.Servlet.VIS_PARAM_P;

import java.io.InputStream;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.uncertweb.viss.core.visualizer.Visualizer;
import org.uncertweb.viss.visualizer.MeanVisualizer;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

public class VissTest extends JerseyTest {

	public VissTest() throws Exception {
		super("org.uncertweb.viss");
	}

	private InputStream getNetCDFStream() {
		return getClass().getResourceAsStream("/biotemp.nc");
	}

	private InputStream getSLDStream() {
		return getClass().getResourceAsStream("/raster.sld");
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

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	WebResource getWebResource() {
		return this.client().resource(getBaseURI());
	}

	@Test
	public void testEmpty() throws UniformInterfaceException, JSONException {
		getWebResource().path(RESOURCES).accept(APPLICATION_JSON_TYPE)
				.get(JSONObject.class).getJSONArray("resources");
	}

	@Test
	public void testNotExistingResource() {
		try {
			getWebResource()
					.path(RESOURCE_WITH_ID.replace(RES_PARAM_P, UUID
							.randomUUID().toString()))
					.accept(APPLICATION_JSON_TYPE).get(Response.class);
		} catch (UniformInterfaceException e) {
			assertEquals(404, e.getResponse().getStatus());
			return;
		}
		fail();
	}

	@Test
	public void getVisualizers() throws UniformInterfaceException,
			JSONException {
		assertEquals(Visualizer.getShortName(MeanVisualizer.class),
				getWebResource().path(VISUALIZERS).get(JSONObject.class)
						.getJSONArray("visualizers").getJSONObject(0)
						.getString("id"));
	}

	private UUID addResource(MediaType mt, InputStream is) throws JSONException {
		JSONObject j = getWebResource().path(
				getWebResource().path(RESOURCES).accept(APPLICATION_JSON_TYPE)
						.entity(is, mt).put(ClientResponse.class).getLocation()
						.getPath()).get(JSONObject.class);

		return UUID.fromString(j.getString("id"));
	}
	
	private String createVisualization(UUID resource, String visualizer,
			JSONObject params) throws JSONException {
		JSONObject req = new JSONObject().put("visualizer", visualizer).put("params", params);
		return getWebResource()
				.path(getWebResource()
						.path(VISUALIZATIONS.replace(RES_PARAM_P, resource.toString()))
						.entity(req, APPLICATION_JSON_TYPE)
						.put(ClientResponse.class).getLocation().getPath())
				.get(JSONObject.class).getString("id");
	}

	@Test
	public void addResourceAndCreateVisualization() throws JSONException,
			UniformInterfaceException, XmlException {

		UUID uuid = addResource(NETCDF_TYPE, getNetCDFStream());

		String visId = createVisualization(uuid, "MeanVisualizer", new JSONObject());

		String url = VISUALIZATION_SLD.replace(RES_PARAM_P, uuid.toString())
				.replace(VIS_PARAM_P, visId);

		StyledLayerDescriptorDocument sld = StyledLayerDescriptorDocument.Factory
				.parse(getWebResource().path(
						getWebResource()
								.path(url)
								.accept(APPLICATION_JSON_TYPE)
								.entity(getSLDStream(),
										STYLED_LAYER_DESCRIPTOR_TYPE)
								.put(ClientResponse.class).getLocation()
								.getPath()).get(String.class));

		System.out.println(sld);
	}
	
	@Test
	public void testSameResource() throws JSONException {
		deleteAll();
		UUID uuid1 = addResource(NETCDF_TYPE, getNetCDFStream());
		UUID uuid2 = addResource(NETCDF_TYPE, getNetCDFStream());
		UUID uuid3 = addResource(OM_2_TYPE, getOMStream());
		assertEquals(uuid1, uuid2);
		assertTrue(!uuid1.equals(uuid3));
	}
	
	@Test
	public void testSameVisualization() throws JSONException {
		deleteAll();
		UUID uuid = addResource(NETCDF_TYPE, getNetCDFStream());
		String visId1 = createVisualization(uuid, "MeanVisualizer", new JSONObject());
		String visId2 = createVisualization(uuid, "MeanVisualizer", new JSONObject());
		assertEquals(visId1, visId2);
		String visId3 = createVisualization(uuid, "MeanVisualizer", new JSONObject().put("hallo", "welt"));
		assertTrue(!visId2.equals(visId3));
	}

	public void deleteAll() throws JSONException {
		JSONObject j = getWebResource().path(RESOURCES)
				.accept(APPLICATION_JSON_TYPE).get(JSONObject.class);
		JSONArray a = j.optJSONArray("resources");
		if (a != null) {
			for (int i = 0; i < a.length(); i++) {
				String id = a.getJSONObject(i).getString("id");
				System.out.println("Deleting resource " + id);
				deleteResource(id);
			}
		}

	}

	void deleteResource(String id) {
		getWebResource().path(RESOURCE_WITH_ID.replace(RES_PARAM_P, id)).delete();
	}
}
