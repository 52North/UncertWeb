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
package org.uncertweb.netcdf;

public abstract class AbstractNcUwIterator extends
		NcUwCoordinateIterator<NcUwObservation> {

	public AbstractNcUwIterator(final INcUwVariable v, final NcUwCoordinate c) {
		super(v, c);
	}

	@Override
	protected NcUwObservation value(final NcUwCoordinate i) {
		return getVariable().getObservation(i);
	}

}
