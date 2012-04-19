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
package org.uncertweb.viss.core;

import org.opengis.coverage.grid.GridCoordinates;
import org.uncertml.IUncertainty;
import org.uncertml.io.JSONEncoder;
import org.uncertweb.api.om.TimeObject;

import com.vividsolutions.jts.geom.Point;

public class UncertaintyValue {
	private static final JSONEncoder enc = new JSONEncoder();
	
	private IUncertainty v;
	private Point l;
	private GridCoordinates g;
	private TimeObject t;

	public UncertaintyValue(IUncertainty v, Point l, TimeObject t, GridCoordinates gridLocation) {
		this.v = v;
		this.l = l;
		this.t = t;
		this.g = gridLocation;
	}

	public IUncertainty getValue() {
		return v;
	}

	public Point getLocation() {
		return l;
	}
	public GridCoordinates getGridLocation() {
		return g;
	}

	public TimeObject getTime() {
		return t;
	}
	
	public String toString() {
		return new StringBuilder().append(getLocation()).append(" ").append(enc.encode(getValue()))
				.append(" @").append(getTime() == null ? "NO_TIME" : getTime()).toString();
		
	}
}