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
package org.uncertweb.viss.core.resource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.Validate;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertml.statistic.ConstraintType;
import org.uncertml.statistic.ProbabilityConstraint;
import org.uncertweb.netcdf.NcUwUncertaintyType;
import org.uncertweb.utils.MultivaluedHashMap;
import org.uncertweb.utils.MultivaluedMap;
import org.uncertweb.utils.UwCollectionUtils;

import com.github.jmkgreen.morphia.annotations.Transient;

public class UncertaintyReference {

	private MediaType mime;
	private URI ref;
	private NcUwUncertaintyType type;
	private JSONObject json;
	private File f;

	@Transient
	private MultivaluedMap<URI, Object> additionalUris;

	private List<ProbabilityConstraint> constraints;

	@Transient
	private Object content;

	public UncertaintyReference() {
	}

	public UncertaintyReference(JSONObject o) throws IllegalArgumentException,
			JSONException, URISyntaxException {
		MediaType mime = MediaType.valueOf(o.getJSONObject("ref").getString(
				"mimeType"));
		URI ref = new URI(o.getJSONObject("ref").getString("url"));
		o.remove("ref");
		NcUwUncertaintyType type = NcUwUncertaintyType.fromUri(new URI(o
				.getString("type")));
		o.remove("type");

		if (type == NcUwUncertaintyType.PROBABILITY) {
			constraints = UwCollectionUtils.list();
			JSONArray jconstraints = o.getJSONArray("constraints");
			for (int i = 0; i < jconstraints.length(); ++i) {
				JSONObject c = jconstraints.getJSONObject(i);
				constraints.add(new ProbabilityConstraint(ConstraintType
						.valueOf(c.getString("type")), c.getDouble("value")));
			}
			o.remove("constraints");
		}

		Validate.notNull(mime);
		setMime(mime);
		Validate.notNull(ref);
		setRef(ref);
		Validate.notNull(type);
		setType(type);
		setJson(o);
	}

	public void setMime(MediaType mime) {
		this.mime = mime;
	}

	public void setRef(URI ref) {
		this.ref = ref;
	}

	public void setType(NcUwUncertaintyType type) {
		this.type = type;
	}

	public MediaType getMime() {
		return mime;
	}

	public URI getRef() {
		return ref;
	}

	public NcUwUncertaintyType getType() {
		return type;
	}

	public File getFile() {
		return f;
	}

	public void setFile(File f) {
		this.f = f;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

	public MultivaluedMap<URI, Object> getAdditionalUris() {
		if (additionalUris == null) {
			additionalUris = new MultivaluedHashMap<URI, Object>();
			if (constraints != null) {
				for (ProbabilityConstraint pc : constraints) {
					additionalUris.add(
							NcUwUncertaintyType.getURIforConstraint(pc.getType()),
							Double.valueOf(pc.getValue()));
				}
			}
		}
		return additionalUris;
	}

	public void setAdditionalUris(MultivaluedMap<URI, Object> additionalUris) {
		this.additionalUris = additionalUris;
	}

	public List<ProbabilityConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<ProbabilityConstraint> constraints) {
		this.constraints = constraints;
	}
}
