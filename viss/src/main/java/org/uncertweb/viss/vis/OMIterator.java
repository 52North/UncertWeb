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
package org.uncertweb.viss.vis;

import java.util.Iterator;

import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.netcdf.NcUwObservation;
import org.uncertweb.viss.core.resource.IResource;

class OMIterator implements Iterator<NcUwObservation> {

	private final Iterator<? extends AbstractObservation> resultIterator;
	private Iterator<NcUwObservation> valueIterator;

	public OMIterator(IObservationCollection o) {
		this.resultIterator = o.getObservations().iterator();
		if (this.resultIterator.hasNext()) {
			this.valueIterator = getNextObservation();
		}
	}

	@Override
	public boolean hasNext() {
		return valueIterator != null
		    && (valueIterator.hasNext() || resultIterator.hasNext());
	}

	@Override
	public NcUwObservation next() {
		if (!valueIterator.hasNext()) {
			valueIterator = getNextObservation();
		}
		return valueIterator.next();
	}

	private Iterator<NcUwObservation> getNextObservation() {
		return AbstractMultiResourceTypeVisualizer.getIteratorForDataSet(((IResource) this.resultIterator
				.next().getResult().getValue()).getDataSets().iterator().next());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}