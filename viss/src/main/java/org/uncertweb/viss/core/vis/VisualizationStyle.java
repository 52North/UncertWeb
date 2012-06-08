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

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Transient;

public class VisualizationStyle {

	@Id
	private ObjectId oid;
	@Transient
	private IVisualization vis;
	@Transient
	private StyledLayerDescriptorDocument sld;

	public VisualizationStyle() {}

	public VisualizationStyle(IVisualization vis, StyledLayerDescriptorDocument doc) {
		this.oid = new ObjectId();
		this.sld = doc;
		this.vis = vis;
	}

	public ObjectId getId() {
		return oid;
	}

	public void setId(ObjectId oid) {
		this.oid = oid;
	}

	public StyledLayerDescriptorDocument getSld() {
		return this.sld;
	}

	public void setSld(StyledLayerDescriptorDocument sld) {
		this.sld = sld;
	}

	public IVisualization getVis() {
		return vis;
	}

	public void setVis(IVisualization vis) {
		this.vis = vis;
	}
}
