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
package org.uncertweb.viss.core.web;

import static org.uncertweb.viss.core.util.MediaTypes.GEOTIFF;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_CREATE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_DATASET;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_DATASET_LIST;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_REQUEST;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_REQUEST_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_RESOURCE_LIST;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZATION;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZATION_LIST;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER;
import static org.uncertweb.viss.core.util.MediaTypes.JSON_VISUALIZER_LIST;
import static org.uncertweb.viss.core.util.MediaTypes.NETCDF;
import static org.uncertweb.viss.core.util.MediaTypes.OM_2;
import static org.uncertweb.viss.core.util.MediaTypes.STYLED_LAYER_DESCRIPTOR;
import static org.uncertweb.viss.core.util.MediaTypes.X_NETCDF;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
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
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.Viss;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.IVisualizer;

@Path("/")
public class RESTServlet {

	public static final String RESOURCE_PARAM = "resource";
	public static final String RESOURCE_PARAM_P = "{" + RESOURCE_PARAM + "}";
	public static final String RESOURCES = "/resources";
	public static final String RESOURCE = RESOURCES + "/" + RESOURCE_PARAM_P;
	
	public static final String VISUALIZER_PARAM = "visualizer";
	public static final String VISUALIZER_PARAM_P = "{" + VISUALIZER_PARAM + "}";
	public static final String VISUALIZERS = "/visualizers";
	public static final String VISUALIZER = VISUALIZERS + "/" + VISUALIZER_PARAM_P;
	
	public static final String DATASET_PARAM = "dataset";
	public static final String DATASET_PARAM_P = "{" + DATASET_PARAM + "}";
	public static final String DATASETS = RESOURCE + "/datasets";
	public static final String DATASET = DATASETS + "/" + DATASET_PARAM_P;

	public static final String VISUALIZATION_PARAM = "visualization";
	public static final String VISUALIZATION_PARAM_P = "{" + VISUALIZATION_PARAM + "}";
	public static final String VISUALIZATIONS = DATASET + "/visualizations";
	public static final String VISUALIZATION = VISUALIZATIONS + "/" + VISUALIZATION_PARAM_P;

	public static final String VISUALIZERS_FOR_DATASET = DATASET + VISUALIZERS;
	public static final String VISUALIZER_FOR_DATASET = DATASET + VISUALIZER;
	
	public static final String VISUALIZATION_SLD = VISUALIZATION + "/sld";
	
	private static Logger log = LoggerFactory.getLogger(RESTServlet.class);

	@GET
	public Response wadl(@Context UriInfo uriI) {
		URI uri = uriI.getBaseUriBuilder().path("application.wadl").build();
		return Response.noContent().location(uri).status(Status.MOVED_PERMANENTLY).build();
	}

	@GET
	@Path(RESOURCES)
	@Produces(JSON_RESOURCE_LIST)
	public Set<IResource> getResources() {
		log.debug("Getting Resources.");
		return Viss.getInstance().getResources();
	}

	@POST
	@Path(RESOURCES)
	@Produces(JSON_RESOURCE)
	@Consumes({JSON_REQUEST, NETCDF, X_NETCDF, GEOTIFF, OM_2})
	public Response createResource(InputStream is,
			@HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType h, @Context UriInfo uriI) {
		log.debug("Putting Resource.");
		IResource r = null;
		if (JSON_REQUEST_TYPE.equals(h)) {
			try {
				JSONObject j = new JSONObject(IOUtils.toString(is));
				log.debug("Fetching resource described as json: {}\n", j.toString(4));
				URL url = new URL(j.getString("url"));
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod(j.getString("method"));
				String req = j.optString("request");
				if (req != null) {
					String reqMt = j.optString("requestMediaType");
					if (reqMt != null) {
						con.setRequestProperty("Content-Type", reqMt);
					}
					con.setDoOutput(true);
					OutputStream os = null;
					try {
						os = con.getOutputStream();
						IOUtils.write(req, os);
					} finally {
						IOUtils.closeQuietly(os);
					}
				}
				r = Viss.getInstance().createResource(con.getInputStream(),
						MediaType.valueOf(j.getString("responseMediaType")));
			} catch (Exception e) {
				throw VissError.internal(e);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			r = Viss.getInstance().createResource(is, h);
		}
		URI uri = uriI.getBaseUriBuilder().path(getClass(), "getResource").build(r.getId());
		return Response.created(uri).entity(r).build();
	}

	@GET
	@Path(RESOURCE)
	@Produces(JSON_RESOURCE)
	public IResource getResource(@PathParam(RESOURCE_PARAM) ObjectId oid) {
		log.debug("Getting Resource with ObjectId \"{}\".", oid);
		return Viss.getInstance().getResource(oid);
	}

	@DELETE
	@Path(RESOURCE)
	public void deleteResource(@PathParam(RESOURCE_PARAM) ObjectId oid) {
		log.debug("Deleting Resource with ObjectId \"{}\".", oid);
		Viss.getInstance().delete(oid);
	}
	
	@GET
	@Path(DATASETS)
	@Produces(JSON_DATASET_LIST)
	public Set<IDataSet> getDataSets(@PathParam(RESOURCE_PARAM) ObjectId oid) {
		log.debug("Getting DataSets for Resource with ObjectId \"{}\".", oid);
		return Viss.getInstance().getDataSetsForResource(oid);
	}
	
	@GET
	@Path(DATASET)
	@Produces(JSON_DATASET)
	public IDataSet getDataSet(@PathParam(RESOURCE_PARAM) ObjectId oid, @PathParam(DATASET_PARAM) String dataSet) {
		log.debug("Getting DataSet with Id \"{}\" for Resource with ObjectId \"{}\".", dataSet, oid);
		return Viss.getInstance().getDataSet(oid,dataSet);
	}

	@GET
	@Path(VISUALIZERS)
	@Produces(JSON_VISUALIZER_LIST)
	public Set<IVisualizer> getVisualizers() {
		log.debug("Getting Visualizers.");
		return Viss.getInstance().getVisualizers();
	}

	@GET
	@Path(VISUALIZERS_FOR_DATASET)
	@Produces(JSON_VISUALIZER_LIST)
	public Set<IVisualizer> getVisualizersForDataSet(
			@PathParam(RESOURCE_PARAM) ObjectId oid,
			@PathParam(DATASET_PARAM) String dataset) {
		log.debug("Getting Visualizers for Resource with ObjectId \"{}\".", oid);
		return Viss.getInstance().getVisualizers(oid, dataset);
	}

	@GET
	@Path(VISUALIZER_FOR_DATASET)
	@Produces(JSON_VISUALIZER)
	public IVisualizer getVisualizerForResource(
			@PathParam(RESOURCE_PARAM) ObjectId oid,
			@PathParam(DATASET_PARAM) String dataset,
			@PathParam(VISUALIZER_PARAM) String visualizer) {
		log.debug("Getting Visualizer with ID {} for Resource with ObjectId \"{}\".",
				visualizer, oid);
		return Viss.getInstance().getVisualizer(oid, dataset, visualizer);
	}

	@GET
	@Path(VISUALIZER)
	@Produces(JSON_VISUALIZER)
	public IVisualizer getVisualizer(@PathParam(VISUALIZER_PARAM) String visualizer) {
		log.debug("Request for Description of \"{}\".", visualizer);
		return Viss.getInstance().getVisualizer(visualizer);
	}

	@GET
	@Path(VISUALIZATIONS)
	@Produces(JSON_VISUALIZATION_LIST)
	public Set<IVisualization> getVisualizations(
			@PathParam(RESOURCE_PARAM) ObjectId oid, 
			@PathParam(DATASET_PARAM) String dataset) {
		log.debug("Getting Visualizations of resource with ObjectId \"{}\"", oid);
		return Viss.getInstance().getVisualizations(oid, dataset);
	}

	@POST
	@Path(VISUALIZER_FOR_DATASET)
	@Produces(JSON_VISUALIZATION)
	@Consumes(JSON_CREATE)
	public Response createVisualization(
			@PathParam(RESOURCE_PARAM) ObjectId oid,
			@PathParam(DATASET_PARAM) String dataset,
			@PathParam(VISUALIZER_PARAM) String visualizer,
			JSONObject options,
			@Context UriInfo uriI) {
		log.debug("Creating Visualizaton for resource with ObjectId \"{}\".", oid);
		IVisualization v = Viss.getInstance().getVisualization(visualizer, oid, dataset, options);
		URI uri = uriI.getBaseUriBuilder().path(VISUALIZATION).build(
				v.getDataSet().getResource().getId(),
				v.getDataSet().getId(), 
				v.getVisId());
		log.debug("Created URI: {}",uri);
		return Response.created(uri).entity(v).build();
	}

	@GET
	@Path(VISUALIZATION)
	@Produces(JSON_VISUALIZATION)
	public IVisualization getVisualization(
			@PathParam(RESOURCE_PARAM) ObjectId resource,
			@PathParam(DATASET_PARAM) String dataset,
			@PathParam(VISUALIZATION_PARAM) String vis) {
		log.debug("Getting visualization for resource with ObjectId \"{}\".", resource);
		return Viss.getInstance().getVisualization(resource, dataset, vis);
	}

	@DELETE
	@Path(VISUALIZATION)
	public void deleteVisualization(
			@PathParam(RESOURCE_PARAM) ObjectId oid,
			@PathParam(DATASET_PARAM) String dataset,
			@PathParam(VISUALIZATION_PARAM) String vis) {
		log.debug("Deleting visualization for resource with ObjectId \"{}\".");
		Viss.getInstance().deleteVisualization(oid, dataset, vis);
	}

	@POST
	@Path(VISUALIZATION_SLD)
	@Consumes(STYLED_LAYER_DESCRIPTOR)
	public Response setSldForVisualization(
			@PathParam(RESOURCE_PARAM) ObjectId oid,
			@PathParam(DATASET_PARAM) String dataset,
			@PathParam(VISUALIZATION_PARAM) String vis, 
			StyledLayerDescriptorDocument sld,
			@Context UriInfo uriI) {
		log.debug("Posting SLD for visualization of resource with ObjectId \"{}\"",
				oid);
		Viss.getInstance().setSldForVisualization(oid, dataset, vis, sld);
		URI uri = uriI.getBaseUriBuilder().path(getClass(), "getSldForVisualization").build(oid, dataset, vis);
		return Response.created(uri).build();
	}

	@GET
	@Path(VISUALIZATION_SLD)
	@Produces(STYLED_LAYER_DESCRIPTOR)
	public StyledLayerDescriptorDocument getSldForVisualization(
			@PathParam(RESOURCE_PARAM) ObjectId oid, 
			@PathParam(DATASET_PARAM) String dataset,
			@PathParam(VISUALIZATION_PARAM) String vis) {
		log.debug("Getting SLD for Visualization with ObjectId \"{}\"", oid);
		return Viss.getInstance().getSldForVisualization(oid, dataset, vis);
	}
}
