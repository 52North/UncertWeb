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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.viss.core.util.HttpMethod;
import org.uncertweb.viss.core.util.Utils;

public class RestBuilder {

	private static final Logger log = LoggerFactory.getLogger(RestBuilder.class);

	private static final boolean PRINT_CURL_COMMAND = Boolean.TRUE;

	private MediaType response;
	private MediaType content;
	private Object entity;
	private String path;
	private String user;
	private String pass;
	private String auth;
	private Map<String, String> map = Utils.map();

	public RestBuilder auth(String user, String pass) {
		this.user = user;
		this.pass = pass;
		this.auth = "Basic "
		    + Base64.encodeBase64String((user + ":" + pass).getBytes());
		return this;
	}

	public static RestBuilder path(String path, Object... param) {
		return new RestBuilder(path, param);
	}

	public HttpURLConnection get() throws IOException {
		return build(HttpMethod.GET);
	}

	public HttpURLConnection post() throws IOException {
		return build(HttpMethod.POST);
	}

	public HttpURLConnection put() throws IOException {
		return build(HttpMethod.PUT);
	}

	public HttpURLConnection delete() throws IOException {
		return build(HttpMethod.DELETE);
	}

	public HttpURLConnection options() throws IOException {
		return build(HttpMethod.OPTIONS);
	}

	public HttpURLConnection head() throws IOException {
		return build(HttpMethod.HEAD);
	}

	private RestBuilder(String path, Object... param) {
		this.path = String.format(path, param);
	}

	public RestBuilder param(Object key, Object value) {
		this.map.put(key.toString(), value.toString());
		return this;
	}

	public RestBuilder contentType(MediaType mt) {
		this.content = mt;
		return this;
	}

	public RestBuilder responseType(MediaType mt) {
		this.response = mt;
		return this;
	}

	public RestBuilder contentType(String mt) {
		return contentType(MediaType.valueOf(mt));
	}

	public RestBuilder responseType(String mt) {
		return responseType(MediaType.valueOf(mt));
	}

	public RestBuilder entity(Object o) {
		this.entity = o;
		return this;
	}

	private HttpURLConnection build(HttpMethod method) throws IOException {
		if (!this.map.isEmpty()) {
			StringBuilder sb = new StringBuilder(this.path).append("?");
			boolean first = true;
			for (Entry<String, String> e : this.map.entrySet()) {
				if (!first) {
					sb.append("&");
				} else {
					first = false;
				}
				sb.append(e.getKey()).append("=").append(e.getValue());
			}
			path = sb.toString();
		}
		URL url = new URL(path);

		if (log.isDebugEnabled() && PRINT_CURL_COMMAND) {
			log.debug(buildCurlString(user, pass, method, url, content, response,
			    (entity instanceof InputStream) ? "!DATA!" : entity));
		}

		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setRequestMethod(method.toString());

		if (response != null) {
			httpCon.setRequestProperty("Accept", response.toString());
		}
		if (auth != null) {
			httpCon.setRequestProperty("Authorization", auth);
		}
		if (entity != null) {
			httpCon.setDoOutput(true);

			if (content != null) {
				httpCon.setRequestProperty("Content-Type", content.toString());
			}

			if (entity instanceof InputStream) {
				OutputStream out = null;
				try {
					out = httpCon.getOutputStream();
					IOUtils.copy((InputStream) entity, out);
				} finally {
					IOUtils.closeQuietly(out);
				}
			} else if (entity instanceof XmlObject) {
				OutputStream out = null;
				try {
					out = httpCon.getOutputStream();
					((XmlObject) entity).save(out);
				} finally {
					IOUtils.closeQuietly(out);
				}

			} else {
				OutputStreamWriter out = null;
				try {
					out = new OutputStreamWriter(httpCon.getOutputStream());
					out.write(entity.toString());
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
		}
		return httpCon;
	}

	protected static String buildCurlString(String user, String pass,
	    HttpMethod method, URL url, MediaType mediaType, MediaType returnType,
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

}