package org.uw.viss.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.visualizer.Visualizer;
import org.uncertweb.viss.core.web.Servlet;
import org.uncertweb.viss.visualizer.MeanVisualizer;

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

	private WebResource r;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		r = this.client().resource(getBaseURI());
	}

	@Test
	public void testEmpty() throws UniformInterfaceException, JSONException {
		r.path(Servlet.RESOURCES).accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class).getJSONArray("resources");
	}

	@Test
	public void testNotExistingResource() {
		try {
			r.path(Servlet.RESOURCE_WITH_ID.replace("{" + Servlet.RES_PARAM
					+ "}", UUID.randomUUID().toString()))
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(Response.class);
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
				r.path(Servlet.VISUALIZERS).get(JSONObject.class)
						.getJSONArray("visualizers").getJSONObject(0)
						.getString("id"));
	}

	@Test
	public void addResourceAndCreateVisualization() throws JSONException {
		JSONObject j = r.path(Servlet.RESOURCES)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.entity(getNetCDFStream(), Constants.NETCDF_TYPE)
				.put(JSONObject.class);

		UUID uuid = UUID.fromString(j.getString("id"));

		JSONObject req = new JSONObject().put("visualizer", "MeanVisualizer")
				.put("params", new JSONObject());

		r.path(Servlet.VISUALIZATIONS.replace("{" + Servlet.RES_PARAM + "}",
				uuid.toString())).accept(MediaType.APPLICATION_JSON_TYPE)
				.entity(req, MediaType.APPLICATION_JSON_TYPE)
				.put(JSONObject.class);

	}
}
