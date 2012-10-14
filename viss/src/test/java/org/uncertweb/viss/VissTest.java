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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.Response;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.xmlbeans.XmlException;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.viss.core.util.JSONConstants;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.core.web.RESTServlet;
import org.uncertweb.viss.core.web.provider.DataSetProvider;
import org.uncertweb.viss.vis.AbstractVisualizer;
import org.uncertweb.viss.vis.distribution.NormalDistributionVisualizer;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;


public class VissTest extends AbstractVissTest {
		
	@Test
	public void testGetOptions() throws JSONException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP_T);
		ObjectId d = getDataSetsForResource(r)[0];
		JSONObject j = getVisualizerForDataset(r,d, getVisualizersForDataset(r, d)[0]);
		System.err.println(j.toString(4));
		assertFalse(j.get(JSONConstants.OPTIONS_KEY) instanceof String);
	}
	
	@Test
	public void testGetDataSetWithEpsgCode() throws JSONException, OMEncodingException {
		ObjectId r = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP_T);
		ObjectId d = getDataSetsForResource(r)[0];
		
		String path = RESTServlet.DATASET
				.replace(RESTServlet.RESOURCE_PARAM_P, r.toString())
				.replace(RESTServlet.DATASET_PARAM_P, d.toString());
		JSONObject j = getWebResource().path(path).queryParam(DataSetProvider.EPSG_CODE_QUERY_PARAMETER, "3857").get(JSONObject.class);
		System.err.println(j.getJSONObject(JSONConstants.SPATIAL_EXTENT_KEY).toString(4));
		j = getWebResource().path(path).queryParam(DataSetProvider.EPSG_CODE_QUERY_PARAMETER, "4326").get(JSONObject.class);
		System.err.println(j.getJSONObject(JSONConstants.SPATIAL_EXTENT_KEY).toString(4));
	}
	
	@Test
	public void testEmpty() throws UniformInterfaceException, JSONException {
		getWebResource().path(RESTServlet.RESOURCES).accept(MediaTypes.JSON_RESOURCE_LIST_TYPE)
				.get(JSONObject.class).getJSONArray(JSONConstants.RESOURCES_KEY);
	}

	@Test
	public void testNotExistingResource() {
		try {
			getWebResource()
					.path(RESTServlet.RESOURCE.replace(RESTServlet.RESOURCE_PARAM_P,
							new ObjectId().toString()))
					.accept(MediaTypes.JSON_RESOURCE_TYPE).get(Response.class);
		} catch (UniformInterfaceException e) {
			assertEquals(404, e.getResponse().getStatus());
			return;
		}
		fail();
	}
	
	@Test
	public void sameVisualizationWithParameters() throws JSONException {
		ObjectId oid = addResource(MediaTypes.OM_2_TYPE, getOMStream());
		ObjectId[] datasets = getDataSetsForResource(oid);
		String cdfVisId1 = createVisualization(oid, datasets[0],
				getNameForVisualizer(NormalDistributionVisualizer.Probability.class), new JSONObject()
				.put(NormalDistributionVisualizer.Probability.MAX_PARAMETER, 0.5D)
				.put(AbstractVisualizer.TIME_PARAMETER, OM_DATE_TIME));
		String cdfVisId2 = createVisualization(oid, datasets[0],
				getNameForVisualizer(NormalDistributionVisualizer.Probability.class), new JSONObject()
				.put(NormalDistributionVisualizer.Probability.MAX_PARAMETER, 0.5D)
				.put(NormalDistributionVisualizer.Probability.TIME_PARAMETER, OM_DATE_TIME));
		assertEquals(cdfVisId1, cdfVisId2);
	}

	@Test
	public void addResourceAndCreateVisualizations() throws JSONException,
			UniformInterfaceException, XmlException, IOException {
		ObjectId oid = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP);

		ObjectId[] datasets = getDataSetsForResource(oid);

		String meanVisId = createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Mean.class));
		createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Variance.class));
		createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.StandardDeviation.class));
		createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Probability.class),
				new JSONObject().put(NormalDistributionVisualizer.Probability.MAX_PARAMETER, 0.5D));
		createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.ProbabilityForInterval.class),
				new JSONObject().put(NormalDistributionVisualizer.ProbabilityForInterval.MIN_PARAMETER, 0.3D)
								.put(NormalDistributionVisualizer.ProbabilityForInterval.MAX_PARAMETER, 0.6D));

		String url = RESTServlet.STYLES_FOR_VISUALIZATION
				.replace(RESTServlet.RESOURCE_PARAM_P, oid.toString())
				.replace(RESTServlet.DATASET_PARAM_P, datasets[0].toString())
				.replace(RESTServlet.VISUALIZATION_PARAM_P, meanVisId);

		ClientResponse cr = getWebResource()
				.path(url)
				.entity(getSLDStream(), MediaTypes.STYLED_LAYER_DESCRIPTOR_TYPE)
				.post(ClientResponse.class);
		
		JSONObject j = cr.getEntity(JSONObject.class);
		System.out.println("StyleID:" + j.getString(JSONConstants.ID_KEY));
		System.out.println(j.toString(4));
		String xml = getWebResource().path(URI.create(j.getString(JSONConstants.HREF_KEY)).getPath()).get(String.class);
		
		System.err.println(xml);
		StyledLayerDescriptorDocument.Factory.parse(xml);
		
		cr = getWebResource().path(cr.getLocation().getPath())
				.entity(getSLDStream(), MediaTypes.STYLED_LAYER_DESCRIPTOR_TYPE)
				.put(ClientResponse.class);
		
		j = cr.getEntity(JSONObject.class);
		System.out.println("StyleID:" + j.getString(JSONConstants.ID_KEY));
		xml = getWebResource().path(URI.create(j.getString(JSONConstants.HREF_KEY)).getPath()).get(String.class);
		StyledLayerDescriptorDocument.Factory.parse(xml);
		
	}

	@Test
	public void visualizersForResource() throws JSONException {
		ObjectId oid = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP);
		ObjectId[] datasets = getDataSetsForResource(oid);
		JSONObject j = getWebResource()
				.path(RESTServlet.VISUALIZERS_FOR_DATASET
						.replace(RESTServlet.RESOURCE_PARAM_P, oid.toString())
						.replace(RESTServlet.DATASET_PARAM_P, datasets[0].toString()))
				.accept(MediaTypes.JSON_VISUALIZER_LIST_TYPE).get(JSONObject.class);

		System.out.println(j.toString(4));
	}

	@Test
	public void testSameResource() throws JSONException {
		ObjectId oid1 = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP);
		ObjectId oid2 = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP);
		ObjectId oid3 = addResource(MediaTypes.OM_2_TYPE, getOMStream());
		assertEquals(oid1, oid2);
		assertTrue(!oid1.equals(oid3));
	}

	@Test
	public void testSldFile() throws XmlException, IOException {
		StyledLayerDescriptorDocument.Factory.parse(getSLDStream());
	}

	@Test
	public void testOMResource() throws JSONException {
		ObjectId oid = addResource(MediaTypes.OM_2_TYPE, getOMStream());
		ObjectId[] datasets = getDataSetsForResource(oid);
		createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Mean.class));
	}

	@Test
	public void testSameVisualization() throws JSONException {
		ObjectId oid = addResource(MediaTypes.NETCDF_TYPE, BIOTEMP);
		ObjectId[] datasets = getDataSetsForResource(oid);
		String visId1 = createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Mean.class));
		String visId2 = createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Mean.class));
		String visId3 = createVisualization(oid, datasets[0], getNameForVisualizer(NormalDistributionVisualizer.Variance.class));
		assertEquals(visId1, visId2);
		assertTrue(!visId2.equals(visId3));
	}
}
