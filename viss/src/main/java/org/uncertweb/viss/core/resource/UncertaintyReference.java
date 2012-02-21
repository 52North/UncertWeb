package org.uncertweb.viss.core.resource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.Validate;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.UncertaintyType;

import com.google.code.morphia.annotations.Transient;

public class UncertaintyReference {
	
	private MediaType mime;
	private URI ref;
	private UncertaintyType type;
	private JSONObject json;
	private File f;
	
	@Transient
	private Object content;

	public UncertaintyReference() {
	}
	
	public UncertaintyReference(JSONObject o) throws IllegalArgumentException, JSONException, URISyntaxException {
		MediaType mime = MediaType.valueOf(o.getJSONObject("ref").getString("mimeType"));
		URI ref = new URI(o.getJSONObject("ref").getString("url"));
		o.remove("ref");
		UncertaintyType type  = UncertaintyType.fromURI(new URI(o.getString("type")));
		o.remove("type");

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
}
