package org.uw.viss.core;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.uncertweb.viss.core.util.Constants.NETCDF_TYPE;
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

import javax.ws.rs.core.Response;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.xmlbeans.XmlException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
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
		assertEquals(
				Visualizer.getShortName(MeanVisualizer.class),
				getWebResource().path(VISUALIZERS).get(JSONObject.class)
						.getJSONArray("visualizers").getJSONObject(0)
						.getString("id"));
	}

	@Test
	public void addResourceAndCreateVisualization() throws JSONException,
			UniformInterfaceException, XmlException {

		JSONObject j = getWebResource().path(
				getWebResource().path(RESOURCES).accept(APPLICATION_JSON_TYPE)
						.entity(getNetCDFStream(), NETCDF_TYPE)
						.put(ClientResponse.class).getLocation().getPath())
				.get(JSONObject.class);

		UUID uuid = UUID.fromString(j.getString("id"));

		JSONObject req = new JSONObject().put("visualizer", "MeanVisualizer")
				.put("params", new JSONObject());

		String visId = getWebResource()
				.path(getWebResource()
						.path(VISUALIZATIONS.replace(RES_PARAM_P,
								uuid.toString()))
						.entity(req, APPLICATION_JSON_TYPE)
						.put(ClientResponse.class).getLocation().getPath())
				.get(JSONObject.class).getString("id");

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

	@After
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
