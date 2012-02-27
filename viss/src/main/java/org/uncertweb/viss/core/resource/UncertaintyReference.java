package org.uncertweb.viss.core.resource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.Validate;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertml.statistic.ConstraintType;
import org.uncertml.statistic.ProbabilityConstraint;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;

import com.google.code.morphia.annotations.Transient;

public class UncertaintyReference {

	private MediaType mime;
	private URI ref;
	private UncertaintyType type;
	private JSONObject json;
	private File f;

	@Transient
	private Map<URI, Number[]> additionalUris;

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
		UncertaintyType type = UncertaintyType.fromURI(new URI(o
				.getString("type")));
		o.remove("type");

		if (type == UncertaintyType.PROBABILITY) {
			constraints = UwCollectionUtils.list();
			JSONArray jconstraints = o.getJSONArray("constraints");
			for (int i = 0; i < jconstraints.length(); ++i) {
				JSONObject c = jconstraints.getJSONObject(i);
				constraints.add(new ProbabilityConstraint(ConstraintType
						.valueOf(c.getString("type")), c.getDouble("value")));
			}
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

	public void setType(UncertaintyType type) {
		this.type = type;
	}

	public MediaType getMime() {
		return mime;
	}

	public URI getRef() {
		return ref;
	}

	public UncertaintyType getType() {
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

	public Map<URI, Number[]> getAdditionalUris() {
		if (additionalUris == null) {
			additionalUris = UwCollectionUtils.map();
			if (constraints != null) {
				for (ProbabilityConstraint pc : constraints) {
					additionalUris.put(
							UncertaintyType.getURIforConstraint(pc.getType()),
							new Number[] { Double.valueOf(pc.getValue()) });
				}
			}
		}
		return additionalUris;
	}

	public void setAdditionalUris(Map<URI, Number[]> additionalUris) {
		this.additionalUris = additionalUris;
	}

	public List<ProbabilityConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<ProbabilityConstraint> constraints) {
		this.constraints = constraints;
	}
}
