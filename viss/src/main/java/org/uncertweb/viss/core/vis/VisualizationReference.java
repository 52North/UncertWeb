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
