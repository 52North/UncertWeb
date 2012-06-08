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

import java.util.Set;

import org.bson.types.ObjectId;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.resource.time.ITemporalExtent;
import org.uncertweb.viss.core.vis.IVisualization;

public interface IDataSet {
	public ObjectId getId();

	public void setId(Object id);

	public String getPhenomenon();

	public ITemporalExtent getTemporalExtent();

	public UncertaintyType getType();

	public IResource getResource();

	public Set<IVisualization> getVisualizations();

	public void addVisualization(IVisualization v);

	public void removeVisualization(IVisualization v);

	public void setResource(IResource r);

	public Object getContent();
	
	public String getUom();

	public void setContent(Object c);
}
