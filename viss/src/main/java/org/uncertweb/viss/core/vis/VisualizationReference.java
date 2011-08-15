package org.uncertweb.viss.core.vis;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.uncertweb.viss.core.util.Utils;

public class VisualizationReference {

	private URL wmsUrl;
	private Set<String> layers;

	public VisualizationReference() {
	}

	public VisualizationReference(URL url, Set<String> layers) {
		this.wmsUrl = url;
		this.layers = layers;
	}

	public VisualizationReference(URL url, String... layers) {
		this(url, Utils.set(layers));
	}

	public VisualizationReference(String url, Set<String> layers)
			throws MalformedURLException {
		this(new URL(url), layers);
	}

	public VisualizationReference(String url, String... layers)
			throws MalformedURLException {
		this(new URL(url), layers);
	}

	public URL getWmsUrl() {
		return wmsUrl;
	}

	public Set<String> getLayers() {
		return layers;
	}

}
