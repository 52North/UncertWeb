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

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.uncertweb.viss.core.resource.time.TemporalExtent;
import org.uncertweb.viss.core.vis.Visualization;

public interface Resource {
	/**
	 * @return the UUID of the resource
	 */
	public UUID getUUID();

	/**
	 * @return the MediaType of the resource
	 */
	public MediaType getMediaType();

	/**
	 * @return the underlying object
	 */
	public Object getResource();

	/**
	 * @return the already generated visualizations
	 */
	public Set<Visualization> getVisualizations();

	/**
	 * Adds a visualization to the resource
	 * 
	 * @param the
	 *            visualization
	 */
	public void addVisualization(Visualization v);

	/**
	 * @return the temporal extent of this resource
	 */
	public TemporalExtent getTemporalExtent();

	/**
	 * @return the phenomenon this resource describes
	 */
	public String getPhenomenon();

	/**
	 * Loads the resource.
	 * 
	 * @throws IOException
	 *             if an IO error occurs
	 */
	public void load() throws IOException;

	/**
	 * @return true if the resource is loaded, false otherwise
	 */
	public boolean isLoaded();
}