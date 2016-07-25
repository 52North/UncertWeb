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

import java.net.URL;
import java.util.Collections;
import java.util.Set;

public class DefaultVisualizationReference implements IVisualizationReference {

    private URL wmsUrl;
    private Set<String> layers;

    /**
     * @param wmsUrl
     *               the url of the WMS
     * @param layers
     *               the layers
     */
    public DefaultVisualizationReference(URL wmsUrl, Set<String> layers) {
        this.wmsUrl = wmsUrl;
        this.layers = layers;
    }

    public DefaultVisualizationReference() {
    }

    @Override
    public URL getWmsUrl() {
        return wmsUrl;
    }

    @Override
    public void setWmsUrl(URL wmsUrl) {
        this.wmsUrl = wmsUrl;
    }

    @Override
    public Set<String> getLayers() {
        return Collections.unmodifiableSet(layers);
    }

    @Override
    public void setLayers(Set<String> layers) {
        this.layers = layers;
    }

}
