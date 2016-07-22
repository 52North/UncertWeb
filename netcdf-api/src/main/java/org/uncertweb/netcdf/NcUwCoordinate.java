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

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.uncertweb.utils.UwCollectionUtils;


public class NcUwCoordinate implements Cloneable {
	private final Map<NcUwDimension, Integer> values = new EnumMap<NcUwDimension, Integer>(
			NcUwDimension.class);

	public NcUwCoordinate(Integer x, Integer y, Integer z, Integer t) {
		set(NcUwDimension.X, x);
		set(NcUwDimension.Y, y);
		set(NcUwDimension.Z, z);
		set(NcUwDimension.T, t);
	}

	public NcUwCoordinate() {
	}

	public NcUwCoordinate(NcUwDimension d, Integer value) {
		set(d, value);
	}

	public NcUwCoordinate(Map<NcUwDimension, Integer> values) {
		for (final Entry<NcUwDimension, Integer> e : values.entrySet()) {
			set(e.getKey(), e.getValue());
		}
	}

	public NcUwCoordinate set(NcUwDimension d, Integer i) {
		if (i != null && i.intValue() >= 0) {
			this.values.put(d, new Integer(i));
		} else {
			this.values.remove(d);
		}
		return this;
	}

	boolean hasDimension(NcUwDimension d) {
		return this.values.containsKey(d)
				&& this.values.get(d) != null
				&& this.values.get(d).intValue() >= 0;
	}

	public boolean hasDimension(NcUwDimension... ds) {
		for (final NcUwDimension d : ds) {
			if (!hasDimension(d)) {
				return false;
			}
		}
		return true;
	}

	public boolean isEmpty() {
		for (NcUwDimension d : NcUwDimension.values()) {
			if (hasDimension(d))
				return false;
		}
		return true;
	}

	public Integer get(NcUwDimension d) {
		if (!hasDimension(d)) {
			return null;
		}
		return this.values.get(d).intValue();
	}

	public Set<NcUwDimension> getDimensions() {
		Set<NcUwDimension> set =UwCollectionUtils.set();
		for (NcUwDimension d : this.values.keySet()) {
			if (hasDimension(d)) {
				set.add(d);
			}
		}
		return set;
	}

	public int[] toArray(NcUwDimension... dimensions) {
		final int[] index = new int[dimensions.length];
		for (int i = 0; i < dimensions.length; ++i) {
			index[i] = get(dimensions[i]);
		}
		return index;
	}

	@Override
	public NcUwCoordinate clone() {
		NcUwCoordinate clone = new NcUwCoordinate();
		for (NcUwDimension d : getDimensions()) {
			clone.set(d, get(d));
		}
		return clone;
	}

	public NcUwCoordinate clear() {
		this.values.clear();
		return this;
	}

	public NcUwCoordinate merge(NcUwCoordinate changes) {
		for (NcUwDimension d : changes.getDimensions()) {
			set(d, changes.get(d));
		}
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NcUwCoordinate other = (NcUwCoordinate) obj;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("NcCoordinate[ ");
		for (NcUwDimension d : getDimensions()) {
			sb.append(d).append(": ").append(get(d)).append("; ");
		}
		sb.append("]");
		return sb.toString();
	}

}