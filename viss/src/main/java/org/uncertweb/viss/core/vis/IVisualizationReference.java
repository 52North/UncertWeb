package org.uncertweb.viss.core.vis;

import java.net.URL;
import java.util.Set;

public interface IVisualizationReference {

	/**
	 * @return the wmsUrl
	 */
	public abstract URL getWmsUrl();

	/**
	 * @return the layers
	 */
	public abstract Set<String> getLayers();

	/**
	 * @param wmsUrl
	 *          the wmsUrl to set
	 */
	public abstract void setWmsUrl(URL wmsUrl);

	/**
	 * @param layers
	 *          the layers to set
	 */
	public abstract void setLayers(Set<String> layers);

}