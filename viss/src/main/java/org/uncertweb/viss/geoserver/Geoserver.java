package org.uncertweb.viss.geoserver;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.valueOf;
import static org.uncertweb.viss.core.util.Constants.STYLED_LAYER_DESCRIPTOR_TYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.Utils;

public class Geoserver {

	private static final Logger log = LoggerFactory
			.getLogger(GeoserverWCSAdapter.class);

	private String baseUrl;
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
		this.sameServer = path != null;
		this.path = path;

		if (sameServer && !path.exists()) {
			path.mkdirs();
		}
		this.cacheWorkspaceList = cacheWorkspaceList;
		if (cacheWorkspaceList) {
			workspaces = Utils.setMT();
			workspaces.addAll(requestWorkspaces());
		}
	}

	public Geoserver(String user, String pass, String baseurl,
			boolean cacheWorkspaceList) throws IOException, JSONException {
		this(user, pass, baseurl, cacheWorkspaceList, null);
	}

	public boolean containsWorkspace(String name) throws IOException, JSONException {
		return getWorkspaces().contains(name);
	}
	
	public String getUrl() {
		return baseUrl + "/wms";
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
		if (getStyleList().contains(name)) {
			log.debug("Style '{}' already existent... updating.", name);

			con = RestBuilder.path(url("styles/%s.json"), name)
					.auth(this.user, this.pass)
					.contentType(STYLED_LAYER_DESCRIPTOR_TYPE)
					.responseType(APPLICATION_JSON_TYPE)
					.entity(style).put();
			
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

	public Set<String> getStyleList() throws IOException {
		InputStream is = null;
		HttpURLConnection con;
		try {

			con = RestBuilder.path(url("styles.json")).auth(this.user, this.pass)
					.responseType(APPLICATION_JSON_TYPE).get();
			logCon("Style list fetch", con);
			
			if (!isOk(con)) {
				throw VissError.internal("Could not fetch Style list.");
			}
			
			is = con.getInputStream();
			JSONObject j = new JSONObject(IOUtils.toString(is));
			JSONArray a = j.getJSONObject("styles").getJSONArray("style");
			Set<String> names = Utils.set();
			for (int i = 0; i < a.length(); i++) {
				names.add(a.getJSONObject(i).getString("name"));
			}
			return names;
		} catch (JSONException e) {
			throw VissError.internal(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public boolean createCoverageStore(String name, String ws, String type,
			boolean enabled) throws JSONException, IOException {
		log.debug("Creating CoverageStore '{}' in Workspace '{}'", name, ws);
		HttpURLConnection con;
		
		JSONObject json = new JSONObject().put("coverageStore", new JSONObject()
				.put("name", name)
				.put("type", type)
				.put("enabled", enabled)
				.put("workspace", ws));
		
		con = RestBuilder.path(url("workspaces/%s/coveragestores.json"), ws).auth(this.user, this.pass)
				.contentType(APPLICATION_JSON_TYPE).entity(json).post();
		logCon("CStore creation", con);

		return isCreated(con);
	}

	public boolean createWorkspace(String name) throws IOException,
			JSONException {
		log.debug("Creating Workspace '{}'", name);
		JSONObject json = new JSONObject().put("workspace",
				new JSONObject().put("name", name));

		HttpURLConnection con;
		
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
		con = RestBuilder.path(url("layers/%s"), cs).auth(this.user, this.pass).delete();
		logCon("Layer deletion", con);
		
		// delete style...
		con = RestBuilder.path(url("styles/%s"), cs).auth(this.user, this.pass).delete();
		logCon("Style deletion", con);
		
		// delete coverage...
		con = RestBuilder.path(url("workspaces/%s/coveragestores/%s/coverages/%s"), ws, cs, cs)
				.auth(this.user, this.pass).delete();
		logCon("Coverage deletion", con);
		
		//delete store...
		con = RestBuilder.path(url("workspaces/%s/coveragestores/%s"), ws, cs)
				.auth(this.user, this.pass).delete();
		logCon("CStore deletion", con);
		
		return isOk(con);
	}

	public boolean deleteWorkspace(String ws) throws IOException, JSONException {
		log.debug("Deleting Workspace '{}'", ws);
		HttpURLConnection con;
		
		// delete coverage stores...
		for (String cs : getCoverageStores(ws)) {
			deleteCoverageStore(ws, cs);
		}

		con = RestBuilder.path(url("workspaces/%s.json"), ws).auth(this.user, this.pass).delete();
		logCon("Workspace deletion", con);
		
		boolean r = isOk(con);
		
		if (r && cacheWorkspaceList) {
			workspaces.remove(ws);
		}
		
		return r;
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
		
		log.debug("Current CoverageStores in Workspace '{}': {}", ws, Utils.join(",", res));
		
		return res;
	}

	public boolean insertCoverage(String ws, String cs, String mime,
			InputStream is) throws IOException {
		if (createCoverage(ws, cs, mime, is)) {
			return enableLayer(cs, true);
		}
		return false;
	}

	private boolean enableLayer(String layerName, boolean enable)
			throws IOException {
		try {
			HttpURLConnection con;
			
			JSONObject j = new JSONObject()
				.put("layer", new JSONObject()
					.put("enabled", enable));
			
			
			con = RestBuilder.path(url("layers/%s.json"), layerName).auth(this.user, this.pass)
					.contentType(APPLICATION_JSON_TYPE).responseType(APPLICATION_JSON_TYPE)
					.entity(j).put();
			
			logCon("Layer enablement", con);
			
			return isOk(con);
			
		} catch (JSONException e) {
			throw VissError.internal(e);
		}
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
					String dirName = Utils.join(File.separator,
							path.getAbsolutePath(), ws);
					File dir = new File(dirName);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File f = new File(Utils.join(File.separator, dirName, cs));
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

			con = RestBuilder.path(url("workspaces/%s/coveragestores/%s/%s.geotiff"), ws, cs, name)
					.auth(this.user, this.pass).contentType(valueOf(contentType))
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
			con = RestBuilder.path(url("workspaces.json")).auth(this.user, this.pass)
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
			
			JSONArray a = j.getJSONObject("workspaces").getJSONArray("workspace");
			
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
	
	protected static void logCon(String requestDesc, HttpURLConnection con) throws IOException {
		if (con.getResponseCode() >= 300) {
			log.warn(requestDesc + " status: " + con.getResponseCode() +" "+ con.getResponseMessage());
		}
	}

	protected static boolean isOk(HttpURLConnection con) throws IOException {
		return isStatus(con, Status.OK);
	}

	protected static boolean isCreated(HttpURLConnection con) throws IOException {
		return isStatus(con, Status.CREATED);
	}

	protected static boolean isStatus(HttpURLConnection con, Status s) throws IOException {
		return con.getResponseCode() == s.getStatusCode();
	}

}
