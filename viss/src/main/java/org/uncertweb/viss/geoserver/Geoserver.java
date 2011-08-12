package org.uncertweb.viss.geoserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.util.HttpMethod;
import org.uncertweb.viss.core.util.Utils;

public class Geoserver {

	private static final boolean PRINT_CURL_COMMAND = Boolean.TRUE;

	private static final Logger log = LoggerFactory
			.getLogger(GeoserverWCSAdapter.class);

	protected static String buildCurlString(String user, String pass,
			HttpMethod method, URL url, String mediaType, String returnType,
			Object content) {
		StringBuilder sb = new StringBuilder();
		sb.append("curl -v");
		if (user != null) {
			sb.append(" -u ").append(user);
			if (pass != null)
				sb.append(":").append(pass);
		}
		sb.append(" -X").append(method);
		if (mediaType != null)
			sb.append(" -H 'Content-Type: ").append(mediaType).append("'");
		if (returnType != null)
			sb.append(" -H 'Accept: ").append(returnType).append("'");
		if (content != null)
			sb.append(" -d '").append(content).append("'");
		sb.append(" ").append(url.toString());
		return sb.toString();
	}

	private String auth;
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
		this.auth = "Basic "
				+ Base64.encodeBase64String((user + ":" + pass).getBytes());
		this.baseUrl = baseurl;
		this.sameServer = path != null;
		this.path = path;

		if (sameServer && !path.exists()) {
			path.mkdirs();
		}
		this.cacheWorkspaceList = cacheWorkspaceList;
		if (cacheWorkspaceList) {
			workspaces = Collections
					.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
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

	public boolean createCoverageStore(String name, String ws, String type,
			boolean enabled) throws JSONException, IOException {
		log.debug("Creating CoverageStore '{}' in Workspace '{}'", name, ws);
		JSONObject json = new JSONObject().put(
				"coverageStore",
				new JSONObject().put("name", name).put("type", type)
						.put("enabled", enabled).put("workspace", ws));

		return restRequest(HttpMethod.POST,
				url("workspaces/%s/coveragestores.json", ws),
				MediaType.APPLICATION_JSON, null, json).getResponseCode() == Status.CREATED
				.getStatusCode();

	}

	public boolean createWorkspace(String name) throws IOException,
			JSONException {
		log.debug("Creating Workspace '{}'", name);
		JSONObject json = new JSONObject().put("workspace",
				new JSONObject().put("name", name));
		boolean r = restRequest(HttpMethod.POST, url("workspaces.json"),
				MediaType.APPLICATION_JSON, null, json).getResponseCode() == Status.CREATED
				.getStatusCode();
		if (r && cacheWorkspaceList) {
			workspaces.add(name);
		}
		return r;
	}

	public boolean deleteCoverageStore(String ws, String cs) throws IOException {
		log.debug("Deleting CoverageStore '{}' from Workspace '{}'", cs, ws);
		return restRequest(HttpMethod.DELETE,
				url("workspaces/%s/coveragestores/%s", ws, cs))
				.getResponseCode() == Status.OK.getStatusCode();
	}

	public boolean deleteWorkspace(String ws) throws IOException, JSONException {
		log.debug("Deleting Workspace '{}'", ws);
		for (String cs : getCoverageStores(ws))
			deleteCoverageStore(ws, cs);
		boolean r = restRequest(HttpMethod.DELETE,
				url("workspaces/%s.json", ws)).getResponseCode() == Status.OK
				.getStatusCode();
		if (r && cacheWorkspaceList) {
			workspaces.remove(ws);
		}
		return r;
	}

	public Set<String> getCoverageStores(String ws) throws IOException,
			JSONException {
		log.debug("Requesting CoverageStores");
		HttpURLConnection con = restRequest(HttpMethod.GET,
				url("workspaces/%s/coveragestores.json", ws));
		if (con.getResponseCode() != Status.OK.getStatusCode())
			return Collections.emptySet();
		Object cs = new JSONObject(IOUtils.toString(con.getInputStream()))
				.get("coverageStores");
		if (cs instanceof String)
			return Collections.emptySet();
		JSONArray css = ((JSONObject) cs).getJSONArray("coverageStore");
		HashSet<String> res = new HashSet<String>(css.length());
		for (int i = 0; i < css.length(); ++i) {
			res.add(css.getJSONObject(i).getString("name"));
		}
		log.debug("Current CoverageStores in Workspace '{}': {}", ws,
				Utils.join(",", res));
		return res;
	}

	public String getUrl() {
		return baseUrl + "/wms";
	}

	public Set<String> getWorkspaces() throws IOException, JSONException {
		return (cacheWorkspaceList) ? workspaces : requestWorkspaces();
	}

	public boolean insertCoverage(String ws, String cs, String mime,
			InputStream is) throws IOException {
		String name = "file", contentType = "mime";
		Object content = is;
		try {
			if (sameServer && path != null) {
				OutputStream os = null;
				try {
					String dirName = Utils.join(File.separator,
							path.getAbsolutePath(), ws);
					File dir = new File(dirName);
					if (!dir.exists())
						dir.mkdirs();
					File f = new File(Utils.join(File.separator, dirName, cs));
					log.info("Saving Coverage to file: " + f.getAbsolutePath());
					os = new FileOutputStream(f);
					IOUtils.copy(is, os);
					content = f.toURI();
					name = "external";
					contentType = MediaType.TEXT_PLAIN;
				} finally {
					IOUtils.closeQuietly(os);
				}
			}
			return restRequest(
					HttpMethod.PUT,
					url("workspaces/%s/coveragestores/%s/%s.geotiff", ws, cs,
							name), contentType, MediaType.APPLICATION_JSON,
					content).getResponseCode() == Status.CREATED
					.getStatusCode();
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	protected Set<String> requestWorkspaces() throws IOException, JSONException {
		InputStream is = null;
		try {
			HttpURLConnection con = restRequest(HttpMethod.GET,
					url("workspaces.json"));
			if (con.getResponseCode() != Status.OK.getStatusCode())
				throw VissError.internal("Could not fetch workspace list.");
			is = con.getInputStream();
			JSONObject j = new JSONObject(IOUtils.toString(is));
			if (j.get("workspaces") instanceof String)
				return Collections.emptySet();
			JSONArray a = j.getJSONObject("workspaces").getJSONArray(
					"workspace");
			HashSet<String> list = new HashSet<String>(a.length());
			for (int i = 0; i < a.length(); i++)
				list.add(a.getJSONObject(i).getString("name"));
			log.debug("Requested Workspaces: {}", Utils.join(",", list));
			return list;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	protected HttpURLConnection restRequest(HttpMethod method, String path)
			throws IOException {
		return restRequest(method, path, null);
	}

	protected HttpURLConnection restRequest(HttpMethod method, String path,
			String mediaType) throws IOException {
		return restRequest(method, path, mediaType, null);
	}

	protected HttpURLConnection restRequest(HttpMethod method, String path,
			String mediaType, String returnType) throws IOException {
		return restRequest(method, path, mediaType, returnType, null);
	}

	protected HttpURLConnection restRequest(HttpMethod method, String path,
			String mediaType, String returnType, Object content)
			throws IOException {
		URL url = new URL(path);

		if (log.isDebugEnabled() && PRINT_CURL_COMMAND) {
			log.debug(buildCurlString(user, pass, method, url, mediaType,
					returnType, (content instanceof InputStream) ? "!DATA!"
							: content));
		}

		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setRequestMethod(method.toString());
		if (mediaType != null)
			httpCon.setRequestProperty("Content-Type", mediaType);
		if (returnType != null)
			httpCon.setRequestProperty("Accept", returnType);
		httpCon.setRequestProperty("Authorization", auth);
		if (content != null) {
			httpCon.setDoOutput(true);
			if (content instanceof InputStream) {
				OutputStream out = null;
				try {
					out = httpCon.getOutputStream();
					IOUtils.copy((InputStream) content, out);
				} finally {
					IOUtils.closeQuietly(out);
				}
			} else {
				OutputStreamWriter out = null;
				try {
					out = new OutputStreamWriter(httpCon.getOutputStream());
					out.write(content.toString());
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
		}
		return httpCon;
	}

	protected String url(String format, Object... param) {
		return baseUrl + "/rest/" + String.format(format, param);
	}
}
