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
package org.uncertweb.viss.geoserver;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.valueOf;
import static org.uncertweb.viss.core.util.MediaTypes.STYLED_LAYER_DESCRIPTOR_TYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwIOUtils;
import org.uncertweb.utils.UwStringUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;

public class Geoserver {

	private static final Logger log = LoggerFactory
			.getLogger(GeoserverAdapter.class);

	private String baseUrl;
	private URL wmsUrl;
	private boolean cacheWorkspaceList;
	private boolean sameServer;
	private File path;
	private String pass;
	private String user;

	private Set<String> workspaces = null;

	public Geoserver(String user, String pass, String baseurl,
			boolean cacheWorkspaceList, File path) throws IOException,
			JSONException {
		this.user = user;
		this.pass = pass;
		this.baseUrl = baseurl;
		this.wmsUrl = new URL(baseUrl + "/wms");
		this.sameServer = path != null;
		this.path = path;

		if (sameServer && !path.exists()) {
			if (!path.mkdirs()) {
				throw VissError.internal("Could not create directory: "
						+ path.getAbsolutePath());
			}
		}
		this.cacheWorkspaceList = cacheWorkspaceList;
		if (cacheWorkspaceList) {
			workspaces = UwCollectionUtils.tsSet();
			workspaces.addAll(requestWorkspaces());
		}
	}

	public Geoserver(String user, String pass, String baseurl,
			boolean cacheWorkspaceList) throws IOException, JSONException {
		this(user, pass, baseurl, cacheWorkspaceList, null);
	}

	public boolean containsWorkspace(String name) throws IOException,
			JSONException {
		return getWorkspaces().contains(name);
	}

	public URL getUrl() {
		return this.wmsUrl;
	}

	protected String url(String path) {
		return baseUrl + "/rest/" + path;
	}

	public Set<String> getWorkspaces() throws IOException, JSONException {
		return (cacheWorkspaceList) ? workspaces : requestWorkspaces();
	}

	public boolean createStyle(StyledLayerDescriptorDocument style, String name)
			throws IOException {
		log.debug("Creating Style '{}'.", name);
		HttpURLConnection con;
		if (getStyles().contains(name)) {
			log.debug("Style '{}' already existent... updating.", name);

			con = RestBuilder.path(url("styles/%s.json"), name)
					.auth(this.user, this.pass)
					.contentType(STYLED_LAYER_DESCRIPTOR_TYPE)
					.responseType(APPLICATION_JSON_TYPE).entity(style).put();

			logCon("Style updating", con);

			return isOk(con);

		} else {
			con = RestBuilder.path(url("styles.json")).entity(style)
					.auth(this.user, this.pass).param("name", name)
					.contentType(STYLED_LAYER_DESCRIPTOR_TYPE)
					.responseType(APPLICATION_JSON_TYPE).post();
			logCon("Style creation", con);

			return isCreated(con);
		}

	}

	public List<String> getStyles() throws IOException {
		try {
			log.debug("Getting Styles");
			HttpURLConnection con;

			con = RestBuilder.path(url("styles.json"))
					.auth(this.user, this.pass)
					.responseType(APPLICATION_JSON_TYPE).get();

			logCon("Style list fetch", con);

			if (!isOk(con)) {
				return Collections.emptyList();
			}

			JSONArray styles;
			styles = new JSONObject(IOUtils.toString(con.getInputStream()))
					.getJSONObject("styles").getJSONArray("style");
			List<String> res = UwCollectionUtils.list();
			for (int i = 0; i < styles.length(); ++i) {
				res.add(styles.getJSONObject(i).getString("name"));
			}
			log.debug("Current Styles: {}", UwStringUtils.join(",", res));

			return res;
		} catch (JSONException e) {
			throw new IOException(e);
		}

	}

	public boolean createCoverageStore(String name, String ws, String type,
			boolean enabled) throws JSONException, IOException {
		log.debug("Creating CoverageStore '{}' in Workspace '{}'", name, ws);
		HttpURLConnection con;

		JSONObject json = new JSONObject().put(
				"coverageStore",
				new JSONObject()
					.put("name", name)
					.put("type", type)
					.put("enabled", enabled)
					.put("workspace", ws));

		con = RestBuilder.path(url("workspaces/%s/coveragestores.json"), ws)
				.auth(this.user, this.pass).contentType(APPLICATION_JSON_TYPE)
				.entity(json).post();
		logCon("CStore creation", con);

		return isCreated(con);
	}

	public boolean createWorkspace(String name) throws IOException,
			JSONException {
		log.debug("Creating Workspace '{}'", name);
		HttpURLConnection con;

		JSONObject json = Utils.flatJSON("workspace", "name", name);

		con = RestBuilder.path(url("workspaces.json"))
				.contentType(APPLICATION_JSON_TYPE).entity(json)
				.auth(this.user, this.pass).post();

		logCon("Workspaces creation", con);

		boolean r = isCreated(con);

		if (r && cacheWorkspaceList) {
			workspaces.add(name);
		}

		return r;
	}

	public boolean deleteCoverageStore(String ws, String cs) throws IOException {
		log.debug("Deleting CoverageStore '{}' from Workspace '{}'", cs, ws);
		HttpURLConnection con;

		// delete layer...
		// con = RestBuilder.path(url("layers/%s:%s"), ws, cs).auth(this.user,
		// this.pass).delete();
		// logCon("Layer deletion", con);

		// delete style...
		String name = ws + "-" + cs;
		if (getStyles().contains(name)) {
			deleteStyle(name);
		}

		// delete coverage...
		// con =
		// RestBuilder.path(url("workspaces/%s/coveragestores/%s/coverages/%s"),
		// ws,
		// cs, cs)
		// .auth(this.user, this.pass).delete();
		// logCon("Coverage deletion", con);

		// delete store...
		con = RestBuilder.path(url("workspaces/%s/coveragestores/%s"), ws, cs)
				.param("recurse", true).auth(this.user, this.pass).delete();
		logCon("CStore deletion", con);

		if (sameServer && path != null) {
			UwIOUtils.deleteRecursively(getCoverageFile(ws, cs));
		}

		return isOk(con);
	}

	public boolean setStyle(String layer, String style)
			throws IOException {
		try {
			log.debug("Setting style '{}' for layer '{}'", style, layer);
			HttpURLConnection con;

			JSONObject j = new JSONObject().put(
					"layer", new JSONObject()
						.put("enabled", true)
						.put("defaultStyle", new JSONObject()
							.put("name", style)));

			con = RestBuilder.path(url("layers/%s.json"), layer)
					.auth(this.user, this.pass)
					.responseType(APPLICATION_JSON_TYPE)
					.contentType(APPLICATION_JSON_TYPE).entity(j).put();

			logCon("Setting of Layer style", con);

			return isOk(con);

		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	public boolean deleteWorkspace(String ws) throws IOException, JSONException {
		log.debug("Deleting Workspace '{}'", ws);
		HttpURLConnection con;

		con = RestBuilder.path(url("workspaces/%s"), ws).param("recurse", true)
				.auth(this.user, this.pass).delete();

		logCon("Workspace deletion", con);

		boolean r = isOk(con);

		if (r && cacheWorkspaceList) {
			workspaces.remove(ws);
		}

		if (sameServer && path != null) {
			UwIOUtils.deleteRecursively(new File(getWorkspacePath(ws)));
		}
		
		// delete styles...
		for (String s : getStyles()) {
			if (s.startsWith(ws)) {
				deleteStyle(s);
			}
		}

		return r;
	}

	public boolean deleteStyle(String s) throws IOException {
		log.debug("Deleting Style '{}'", s);
		HttpURLConnection con;
		con = RestBuilder.path(url("styles/%s"), s).auth(this.user, this.pass)
				.param("purge", true).delete();
		logCon("Style deletion", con);
		return isOk(con);
	}

	public Set<String> getCoverageStores(String ws) throws IOException,
			JSONException {
		log.debug("Requesting CoverageStores");
		HttpURLConnection con;

		con = RestBuilder.path(url("workspaces/%s/coveragestores.json"), ws)
				.auth(this.user, this.pass).responseType(APPLICATION_JSON_TYPE)
				.get();

		logCon("CStore list fetch", con);

		if (!isOk(con)) {
			return Collections.emptySet();
		}

		Object cs = new JSONObject(IOUtils.toString(con.getInputStream()))
				.get("coverageStores");

		if (cs instanceof String) {
			return Collections.emptySet();
		}

		JSONArray css = ((JSONObject) cs).getJSONArray("coverageStore");

		HashSet<String> res = new HashSet<String>(css.length());
		for (int i = 0; i < css.length(); ++i) {
			res.add(css.getJSONObject(i).getString("name"));
		}

		log.debug("Current CoverageStores in Workspace '{}': {}", ws,
				UwStringUtils.join(",", res));

		return res;
	}

	public boolean insertCoverage(String ws, String cs, String mime,
			InputStream is) throws IOException {
		if (createCoverage(ws, cs, mime, is)) {
			return enableLayer(ws, cs, true);
		}
		return false;
	}

	private boolean enableLayer(String ws, String layerName, boolean enable)
			throws IOException {
		log.debug("Enabling layer {}", layerName);
		try {
			HttpURLConnection con;

			JSONObject j = Utils.flatJSON("layer", "enabled",
					String.valueOf(enable));

			con = RestBuilder.path(url("layers/%s:%s.json"), ws, layerName)
					.auth(this.user, this.pass)
					.contentType(APPLICATION_JSON_TYPE)
					.responseType(APPLICATION_JSON_TYPE).entity(j).put();

			logCon("Layer enablement", con);

			return isOk(con);

		} catch (JSONException e) {
			throw VissError.internal(e);
		}
	}

	private String getWorkspacePath(String ws) {
		return UwStringUtils.join(File.separator, path.getAbsolutePath(), ws);
	}

	private File getCoverageFile(String ws, String c) {
		return new File(UwStringUtils.join(File.separator,
				getWorkspacePath(ws), c));
	}

	private boolean createCoverage(String ws, String cs, String mime,
			InputStream is) throws IOException {
		String name = "file";
		String contentType = mime;
		Object content = is;
		HttpURLConnection con;
		try {
			if (sameServer && path != null) {
				OutputStream os = null;
				try {
					String dirName = getWorkspacePath(ws);
					File dir = new File(dirName);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File f = getCoverageFile(ws, cs);
					log.info("Saving Coverage to file: " + f.getAbsolutePath());
					os = new FileOutputStream(f);
					IOUtils.copy(is, os);
					content = f.toURI();
					name = "external";
					contentType = TEXT_PLAIN;
				} finally {
					IOUtils.closeQuietly(os);
				}
			}

			con = RestBuilder
					.path(url("workspaces/%s/coveragestores/%s/%s.geotiff"),
							ws, cs, name).auth(this.user, this.pass)
					.contentType(valueOf(contentType))
					.responseType(APPLICATION_JSON_TYPE).entity(content).put();

			logCon("Coverage creation", con);

			return isCreated(con);

		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	protected Set<String> requestWorkspaces() throws IOException, JSONException {
		InputStream is = null;
		HttpURLConnection con;
		try {
			con = RestBuilder.path(url("workspaces.json"))
					.auth(this.user, this.pass)
					.responseType(APPLICATION_JSON_TYPE).get();

			logCon("Workspace list fetch", con);

			if (!isOk(con)) {
				throw VissError.internal("Could not fetch workspace list.");
			}

			is = con.getInputStream();
			JSONObject j = new JSONObject(IOUtils.toString(is));

			if (j.get("workspaces") instanceof String) {
				return Collections.emptySet();
			}

			JSONArray a = j.getJSONObject("workspaces").getJSONArray(
					"workspace");

			HashSet<String> list = new HashSet<String>(a.length());
			for (int i = 0; i < a.length(); i++) {
				list.add(a.getJSONObject(i).getString("name"));
			}

			log.debug("Requested Workspaces: {}", list.size());

			return list;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public StyledLayerDescriptorDocument getStyle(String stylename)
			throws IOException, XmlException {

		HttpURLConnection con;

		con = RestBuilder.path(url("styles/%s.sld"), stylename)
				.responseType(STYLED_LAYER_DESCRIPTOR_TYPE)
				.auth(this.user, this.pass).get();

		logCon("style fetch", con);

		if (!isOk(con)) {
			return null;
		}
		InputStream is = null;
		try {
			return StyledLayerDescriptorDocument.Factory.parse(con
					.getInputStream());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	protected static void logCon(String requestDesc, HttpURLConnection con)
			throws IOException {
		if (con.getResponseCode() >= 300) {
			log.warn(requestDesc + " status: " + con.getResponseCode() + " "
					+ con.getResponseMessage());
		}
	}

	protected static boolean isOk(HttpURLConnection con) throws IOException {
		return isStatus(con, Status.OK);
	}

	protected static boolean isCreated(HttpURLConnection con)
			throws IOException {
		return isStatus(con, Status.CREATED);
	}

	protected static boolean isStatus(HttpURLConnection con, Status s)
			throws IOException {
		return con.getResponseCode() == s.getStatusCode();
	}
}
