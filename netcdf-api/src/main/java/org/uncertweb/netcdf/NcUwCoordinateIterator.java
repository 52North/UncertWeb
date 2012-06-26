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

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.MultiDimensionalIterator;
import org.uncertweb.utils.UwCollectionUtils;

public abstract class NcUwCoordinateIterator<T> extends MultiDimensionalIterator<T> {
	
	private static final Logger log = LoggerFactory.getLogger(NcUwCoordinateIterator.class);

	private final Map<Integer, NcUwDimension> indexes = UwCollectionUtils.map();
	private final NcUwCoordinate coordinate;
	private final INcUwVariable variable;
	public NcUwCoordinateIterator(INcUwVariable v, NcUwCoordinate c) {
		final ArrayList<Integer> sizes = new ArrayList<Integer>();
		for (final NcUwDimension d : v.getDimensions()) {
			if (d != NcUwDimension.S && !c.hasDimension(d)) {
				// this index changes; remember position
				sizes.add(v.getSize(d));
				this.indexes.put(sizes.size() - 1, d);
			}
		}
		
		this.coordinate = c;
		this.variable = v;
		final int[] size = new int[sizes.size()];
		for (int i = 0; i < size.length; ++i) {
			size[i] = sizes.get(i).intValue();
		}
		log.debug("Coordinate: {}", coordinate);
		log.debug("Size: {}", new Object[]{size});
		setSize(size);
	}

	@Override
	protected T value(final int[] index) {
		NcUwCoordinate coord = this.coordinate.clone();
		for (int i = 0; i < index.length; ++i) {
			coord.set(this.indexes.get(i), index[i]);
		}
		return value(coord);
	}

	public INcUwVariable getVariable() {
		return this.variable;
	}

	protected abstract T value(NcUwCoordinate i);
}
