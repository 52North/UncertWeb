package org.uncertweb.viss.core.web;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.uncertweb.viss.core.util.Constants.GEOTIFF;
import static org.uncertweb.viss.core.util.Constants.NETCDF;
import static org.uncertweb.viss.core.util.Constants.OM_2;
import static org.uncertweb.viss.core.util.Constants.STYLED_LAYER_DESCRIPTOR;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.Viss;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.Visualizer;

@Path("/")
public class Servlet {

	public static final String RES_PARAM = "resource";
	public static final String VIS_PARAM = "visualization";
	public static final String VIR_PARAM = "visualizer";

	public static final String RESOURCES = "/resources";
	public static final String RESOURCE_WITH_ID = RESOURCES + "/{" + RES_PARAM
			+ "}";

	public static final String VISUALIZERS = "/visualizers";
	public static final String VISUALIZERS_FOR_RESOURCE = RESOURCE_WITH_ID
			+ VISUALIZERS;
	public static final String VISUALIZER_WITH_ID = VISUALIZERS + "/{"
			+ VIR_PARAM + "}";

	public static final String VISUALIZATIONS = RESOURCE_WITH_ID
			+ "/visualizations";
	public static final String VISUALIZATION_WITH_ID = VISUALIZATIONS + "/{"
			+ VIS_PARAM + "}";
	public static final String VISUALIZATION_SLD = VISUALIZATION_WITH_ID
			+ "/sld";

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

	@PUT
	@Path(RESOURCES)
	@Consumes({ APPLICATION_JSON, NETCDF, X_NETCDF, GEOTIFF, OM_2 })
	@Produces(APPLICATION_JSON)
	public Resource putResource(InputStream is, @Context HttpHeaders h) {
		log.debug("Putting Resource.");
		return Viss.getInstance().createResource(is, h.getMediaType());
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
	public void deleteResource(@PathParam(RES_PARAM) UUID uuid) {
		log.debug("Deleting Resource with UUID \"{}\".", uuid);
		Viss.getInstance().delete(uuid);
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
	public Visualization putVisualization(@PathParam(RES_PARAM) UUID uuid,
			VisualizationRequest req) {
		log.debug("Creating Visualizaton for resource with UUID \"{}\".", uuid);
		return Viss.getInstance().getVisualization(req.getVisualizer(), uuid,
				req.getParameters());
	}

	@GET
	@Path(VISUALIZATION_WITH_ID)
	@Produces(APPLICATION_JSON)
	public Visualization getVisualization(@PathParam(RES_PARAM) UUID resource,
			@PathParam(VIS_PARAM) String vis) {
		log.debug("Getting visualization for resource with UUID \"{}\".");
		return Viss.getInstance().getVisualization(resource, vis);
	}

	@DELETE
	@Path(VISUALIZATION_WITH_ID)
	@Produces(APPLICATION_JSON)
	public void deleteVisualization(@PathParam(RES_PARAM) UUID uuid,
			@PathParam(VIS_PARAM) String vis) {
		log.debug("Deleting visualization for resource with UUID \"{}\".");
		Viss.getInstance().deleteVisualization(uuid, vis);
	}

	@PUT
	@Path(VISUALIZATION_SLD)
	@Consumes(STYLED_LAYER_DESCRIPTOR)
	@Produces(APPLICATION_JSON)
	public void putSldForVisualization(@PathParam(RES_PARAM) UUID uuid,
			@PathParam(VIS_PARAM) String vis, StyledLayerDescriptorDocument sld) {
		log.debug("Putting SLD for visualization of resource with UUID \"{}\"",
				uuid);
		Viss.getInstance().setSldForVisualization(uuid, vis, sld);
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
