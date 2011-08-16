package org.uncertweb.viss.core.web;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.uncertweb.viss.core.util.Constants.GEOTIFF;
import static org.uncertweb.viss.core.util.Constants.NETCDF;
import static org.uncertweb.viss.core.util.Constants.OM_2;
import static org.uncertweb.viss.core.util.Constants.STYLED_LAYER_DESCRIPTOR;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.Viss;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.vis.Visualization;
import org.uncertweb.viss.core.vis.Visualizer;

@Path("/")
public class Servlet {

	public static final String RES_PARAM = "resource";
	public static final String VIS_PARAM = "visualization";
	public static final String VIR_PARAM = "visualizer";

	public static final String RES_PARAM_P = "{" + RES_PARAM + "}";
	public static final String VIS_PARAM_P = "{" + VIS_PARAM + "}";
	public static final String VIR_PARAM_P = "{" + VIR_PARAM + "}";

	
	
	public static final String RESOURCES = "/resources";
	public static final String RESOURCE_WITH_ID = RESOURCES + "/" + RES_PARAM_P;
	public static final String VISUALIZERS = "/visualizers";
	public static final String VISUALIZERS_FOR_RESOURCE = RESOURCE_WITH_ID + VISUALIZERS;
	public static final String VISUALIZER_WITH_ID = VISUALIZERS + "/" + VIR_PARAM_P;
	public static final String VISUALIZATIONS = RESOURCE_WITH_ID + "/visualizations";
	public static final String VISUALIZATION_WITH_ID = VISUALIZATIONS + "/" + VIS_PARAM_P;
	public static final String VISUALIZATION_SLD = VISUALIZATION_WITH_ID + "/sld";

	private static Logger log = LoggerFactory.getLogger(Servlet.class);

	@GET
	public Response wadl(@Context UriInfo uriI) {
		URI uri = uriI.getBaseUriBuilder().path("application.wadl").build();
		return Response.noContent().location(uri)
				.status(Status.MOVED_PERMANENTLY).build();
	}

	@GET
	@Path(RESOURCES)
	@Produces(APPLICATION_JSON)
	public Set<Resource> getResources() {
		log.debug("Getting Resources.");
		return Viss.getInstance().getResources();
	}

	/*
	 * [ "url", "request", "requestMediaType", "responseMediaType", "method" ]
	 */
	
	@PUT
	@Path(RESOURCES)
	@Produces(APPLICATION_JSON)
	@Consumes({ APPLICATION_JSON, NETCDF, X_NETCDF, GEOTIFF, OM_2 })
	public Response putResource(InputStream is, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType h, @Context UriInfo uriI) {
		log.debug("Putting Resource.");
		Resource r = null;
		if (MediaType.APPLICATION_JSON_TYPE.equals(h)) {
			try {
				JSONObject j = new JSONObject(IOUtils.toString(is));
				log.debug("Fetching resource described as json: {}\n", j.toString(4));
				URL url = new URL(j.getString("url"));
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				con.setRequestMethod(j.getString("method"));
				String req = j.optString("request");
				if (req != null) {
					String reqMt = j.optString("requestMediaType");
					if (reqMt != null)
						con.setRequestProperty("Content-Type", reqMt);
					con.setDoOutput(true);
					OutputStream os = null;
					try {
						os = con.getOutputStream();
						IOUtils.write(req, os);
					} finally {
						IOUtils.closeQuietly(os);
					}
				}
				r = Viss.getInstance().createResource(con.getInputStream(), MediaType.valueOf(j.getString("responseMediaType")));
			} catch (Exception e) {
				throw VissError.internal(e);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			r = Viss.getInstance().createResource(is, h);
		}
		URI uri = uriI.getBaseUriBuilder().path(getClass(), "getResource").build(r.getUUID());
		return Response.created(uri).entity(r).build();
	}

	@GET
	@Path(RESOURCE_WITH_ID)
	@Produces(APPLICATION_JSON)
	public Resource getResource(@PathParam("resource") UUID uuid) {
		log.debug("Getting Resource with UUID \"{}\".", uuid);
		return Viss.getInstance().getResource(uuid);
	}

	@DELETE
	@Path(RESOURCE_WITH_ID)
	public Response deleteResource(@PathParam(RES_PARAM) UUID uuid) {
		log.debug("Deleting Resource with UUID \"{}\".", uuid);
		Viss.getInstance().delete(uuid);
		return Response.ok().build();
	}

	@GET
	@Path(VISUALIZERS)
	@Produces(APPLICATION_JSON)
	public Set<Visualizer> getVisualizers() {
		log.debug("Getting Visualizers.");
		return Viss.getInstance().getVisualizers();
	}

	@GET
	@Path(VISUALIZERS_FOR_RESOURCE)
	@Produces(APPLICATION_JSON)
	public Set<Visualizer> getVisualizersForResource(
			@PathParam(RES_PARAM) UUID uuid) {
		log.debug("Getting Visualizers for Resource with UUID \"{}\".", uuid);
		return Viss.getInstance().getVisualizers(uuid);
	}

	@GET
	@Path(VISUALIZER_WITH_ID)
	@Produces(APPLICATION_JSON)
	public Visualizer getVisualizer(@PathParam(VIR_PARAM) String visualizer) {
		log.debug("Request for Description of \"{}\".", visualizer);
		return Viss.getInstance().getVisualizer(visualizer);
	}

	@GET
	@Path(VISUALIZATIONS)
	@Produces(APPLICATION_JSON)
	public Set<Visualization> getVisualizations(@PathParam(RES_PARAM) UUID uuid) {
		log.debug("Getting Visualizations of resource with UUID \"{}\"", uuid);
		return Viss.getInstance().getVisualizations(uuid);
	}

	@PUT
	@Path(VISUALIZATIONS)
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response putVisualization(@PathParam(RES_PARAM) UUID uuid,
			VisualizationRequest req, @Context UriInfo uriI) {
		log.debug("Creating Visualizaton for resource with UUID \"{}\".", uuid);
		Visualization v = Viss.getInstance().getVisualization(req.getVisualizer(), uuid,
				req.getParameters());
		URI uri = uriI.getBaseUriBuilder().path(getClass(), "getVisualization").build(v.getUuid(), v.getVisId());
		return Response.created(uri).entity(v).build();
	}

	@GET
	@Path(VISUALIZATION_WITH_ID)
	@Produces(APPLICATION_JSON)
	public Visualization getVisualization(@PathParam(RES_PARAM) UUID resource,
			@PathParam(VIS_PARAM) String vis) {
		log.debug("Getting visualization for resource with UUID \"{}\".", resource);
		return Viss.getInstance().getVisualization(resource, vis);
	}

	@DELETE
	@Path(VISUALIZATION_WITH_ID)
	public Response deleteVisualization(@PathParam(RES_PARAM) UUID uuid,
			@PathParam(VIS_PARAM) String vis) {
		log.debug("Deleting visualization for resource with UUID \"{}\".");
		Viss.getInstance().deleteVisualization(uuid, vis);
		return Response.ok().build();
	}

	@PUT
	@Path(VISUALIZATION_SLD)
	@Consumes(STYLED_LAYER_DESCRIPTOR)
	public Response putSldForVisualization(@PathParam(RES_PARAM) UUID uuid,
			@PathParam(VIS_PARAM) String vis, StyledLayerDescriptorDocument sld, @Context UriInfo uriI) {
		log.debug("Putting SLD for visualization of resource with UUID \"{}\"", uuid);
		Viss.getInstance().setSldForVisualization(uuid, vis, sld);
		URI uri = uriI.getBaseUriBuilder().path(getClass(),"getSldForVisualization").build(uuid, vis);
		return Response.created(uri).build();
	}

	@GET
	@Path(VISUALIZATION_SLD)
	@Produces(STYLED_LAYER_DESCRIPTOR)
	public StyledLayerDescriptorDocument getSldForVisualization(
			@PathParam(RES_PARAM) UUID uuid, @PathParam(VIS_PARAM) String vis) {
		log.debug("Getting SLD for Visualization with UUID \"{}\"", uuid);
		return Viss.getInstance().getSldForVisualization(uuid, vis);
	}
	
}
