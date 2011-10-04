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
package org.uw.viss.core;

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
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCES;
import static org.uncertweb.viss.core.web.RESTServlet.RESOURCE_WITH_ID;
import static org.uncertweb.viss.core.web.RESTServlet.RES_PARAM_P;
import static org.uncertweb.viss.core.web.RESTServlet.VIR_PARAM_P;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZATION_SLD;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZERS_FOR_RESOURCE;
import static org.uncertweb.viss.core.web.RESTServlet.VISUALIZER_FOR_RESOURCE;
import static org.uncertweb.viss.core.web.RESTServlet.VIS_PARAM_P;

import java.io.IOException;
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
import org.uncertweb.viss.core.vis.IVisualizer;
import org.uncertweb.viss.vis.distribution.normal.Mean;
import org.uncertweb.viss.vis.distribution.normal.Probability;
import org.uncertweb.viss.vis.distribution.normal.ProbabilityForInterval;
import org.uncertweb.viss.vis.distribution.normal.StandardDeviation;
import org.uncertweb.viss.vis.distribution.normal.Variance;

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
        .get(JSONObject.class).getJSONArray("resources");
  }

  @Test
  public void testNotExistingResource() {
    try {
      getWebResource()
          .path(
              RESOURCE_WITH_ID.replace(RES_PARAM_P, UUID.randomUUID()
                  .toString())).accept(JSON_RESOURCE_TYPE).get(Response.class);
    } catch (UniformInterfaceException e) {
      assertEquals(404, e.getResponse().getStatus());
      return;
    }
    fail();
  }

  private UUID addResource(MediaType mt, InputStream is) throws JSONException {
    JSONObject j = getWebResource().path(
        getWebResource().path(RESOURCES).accept(JSON_RESOURCE_TYPE)
            .entity(is, mt).post(ClientResponse.class).getLocation().getPath())
        .get(JSONObject.class);

    return UUID.fromString(j.getString("id"));
  }

  private String createVisualization(UUID resource, String visualizer,
      JSONObject params) throws JSONException {
    return getWebResource()
        .path(
            getWebResource()
                .path(
                    VISUALIZER_FOR_RESOURCE.replace(RES_PARAM_P,
                        resource.toString()).replace(VIR_PARAM_P, visualizer))
                .entity(params, JSON_CREATE_TYPE).post(ClientResponse.class)
                .getLocation().getPath()).get(JSONObject.class).getString("id");
  }

  @Test
  public void sameVisualizationWithParameters() throws JSONException {
    UUID uuid = addResource(OM_2_TYPE, getOMStream());
    String cdfVisId1 = createVisualization(uuid,
        getNameForVisualizer(Probability.class),
        new JSONObject().put("max", 0.5D));
    String cdfVisId2 = createVisualization(uuid,
        getNameForVisualizer(Probability.class),
        new JSONObject().put("max", 0.5D));
    assertEquals(cdfVisId1, cdfVisId2);
  }

  @Test
  public void addResourceAndCreateVisualizations() throws JSONException,
      UniformInterfaceException, XmlException {
    deleteAll();
    UUID uuid = addResource(NETCDF_TYPE, getNetCDFStream());

    String meanVisId = createVisualization(uuid,
        getNameForVisualizer(Mean.class), new JSONObject());
    createVisualization(uuid, getNameForVisualizer(Variance.class),
        new JSONObject());
    createVisualization(uuid, getNameForVisualizer(StandardDeviation.class),
        new JSONObject());
    createVisualization(uuid, getNameForVisualizer(Probability.class),
        new JSONObject().put("max", 0.5D));
    createVisualization(uuid,
        getNameForVisualizer(ProbabilityForInterval.class), new JSONObject()
            .put("min", 0.3D).put("max", 0.6D));

    String url = VISUALIZATION_SLD.replace(RES_PARAM_P, uuid.toString())
        .replace(VIS_PARAM_P, meanVisId);

    StyledLayerDescriptorDocument.Factory.parse(getWebResource().path(
        getWebResource().path(url)
            .entity(getSLDStream(), STYLED_LAYER_DESCRIPTOR_TYPE)
            .post(ClientResponse.class).getLocation().getPath()).get(
        String.class));

  }

  @Test
  public void visualizersForResource() throws JSONException {
    deleteAll();
    UUID uuid = addResource(NETCDF_TYPE, getNetCDFStream());
    JSONObject j = getWebResource()
        .path(VISUALIZERS_FOR_RESOURCE.replace(RES_PARAM_P, uuid.toString()))
        .accept(JSON_VISUALIZER_LIST_TYPE).get(JSONObject.class);

    System.out.println(j.toString(4));
  }

  @Test
  public void testSameResource() throws JSONException {
    UUID uuid1 = addResource(NETCDF_TYPE, getNetCDFStream());
    UUID uuid2 = addResource(NETCDF_TYPE, getNetCDFStream());
    UUID uuid3 = addResource(OM_2_TYPE, getOMStream());
    assertEquals(uuid1, uuid2);
    assertTrue(!uuid1.equals(uuid3));
  }

  @Test
  public void testSldFile() throws XmlException, IOException {
    StyledLayerDescriptorDocument.Factory.parse(getSLDStream());
  }

  @Test
  public void testOMResource() throws JSONException {
    deleteAll();
    UUID uuid = addResource(OM_2_TYPE, getOMStream());
    createVisualization(uuid, getNameForVisualizer(Mean.class),
        new JSONObject());
  }

  @Test
  public void testSameVisualization() throws JSONException {
    UUID uuid = addResource(NETCDF_TYPE, getNetCDFStream());
    String visId1 = createVisualization(uuid, getNameForVisualizer(Mean.class),
        new JSONObject());
    String visId2 = createVisualization(uuid, getNameForVisualizer(Mean.class),
        new JSONObject());
    assertEquals(visId1, visId2);
    String visId3 = createVisualization(uuid,
        getNameForVisualizer(Variance.class), new JSONObject());
    assertTrue(!visId2.equals(visId3));
  }

  public void deleteAll() throws JSONException {
    JSONObject j = getWebResource().path(RESOURCES)
        .accept(JSON_RESOURCE_LIST_TYPE).get(JSONObject.class);
    JSONArray a = j.optJSONArray("resources");
    if (a != null) {
      for (int i = 0; i < a.length(); i++) {
        String id = a.getJSONObject(i).getString("id");
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
    getWebResource().path(RESOURCE_WITH_ID.replace(RES_PARAM_P, id)).delete();
  }
}
