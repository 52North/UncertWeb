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

import static org.uncertweb.viss.core.util.MediaTypes.GEOTIFF_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.OM_2_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.X_NETCDF_TYPE;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.VisualizationFactory;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;

public abstract class AbstractMultiResourceTypeVisualizer extends
    AbstractVisualizer implements Iterable<Value> {

	public AbstractMultiResourceTypeVisualizer() {
		super(GEOTIFF_TYPE, NETCDF_TYPE, X_NETCDF_TYPE, OM_2_TYPE);
	}

	@Override
	protected IVisualization visualize() {
		return visualize(getResource().getResource());
	}

	protected IVisualization visualize(IResource r) {
		return visualize(r.getResource());
	}

	protected IVisualization visualize(Object o) {
		if (o instanceof GridCoverage2D) {
			return visualize((GridCoverage2D) o);
		} else if (o instanceof UncertaintyNetCDF) {
			return visualize((UncertaintyNetCDF) o);
		} else if (o instanceof IObservationCollection) {
			return visualize((IObservationCollection) o);
		} else {
			throw VissError.internal("Unknown type: " + o.getClass());
		}
	}

	protected IVisualization visualize(GridCoverage2D gc) {
		throw VissError.internal("Not yet implemented");
	}

	protected IVisualization visualize(UncertaintyNetCDF gc) {
		WriteableGridCoverage wgc = gc.getCoverage(getCoverageName(), getUom());
		Double min = null, max = null;
		for (Value nv : gc) {
			Double value = null;
			if (nv.getValue() != null) {
				double v = evaluate(nv.getValue());
				if (!Double.isNaN(v) && !Double.isInfinite(v)) {
					value = Double.valueOf(v);
					if (min == null || min.doubleValue() > v)
						min = Double.valueOf(v);
					if (max == null || max.doubleValue() < v)
						max = Double.valueOf(v);
				}
			}
			Point2D.Double location = new Point2D.Double(nv.getLocation().getX(), nv
			    .getLocation().getY());
			wgc.setValueAtPos(location, value);
		}
		log.debug("min: {}; max: {}", min, max);
		return VisualizationFactory.getBuilder()
			.setUuid(getResource().getUUID())
			.setId(getId(getParams()))
			.setCreator(this)
			.setParameters(getParams())
			.setMin(min)
			.setMax(max)
			.setUom(getUom())
			.setCoverage(wgc.getGridCoverage())
			.build();
	}

	protected IVisualization visualize(IObservationCollection gc) {
		Set<GridCoverage> coverages = UwCollectionUtils.set();
		Double min = null, max = null;
		String uom = null;
		for (AbstractObservation ao : gc.getObservations()) {
			if (!(ao.getResult().getValue() instanceof IResource)) {
				throw VissError.internal("Resource is not compatible");
			}
			IResource rs = (IResource) ao.getResult().getValue();
			IVisualization v = visualize(rs);

			if (uom == null) {
				uom = v.getUom();
			} else if (!uom.equals(v.getUom())) {
				throw VissError.internal("Different UOM");
			}

			if (min == null || v.getMinValue() < min.doubleValue()) {
				min = v.getMinValue();
				log.debug("Setting min to {}", min);
			}
			if (max == null || v.getMaxValue() > max.doubleValue()) {
				max = v.getMaxValue();
				log.debug("Setting max to {}", max);
			}

			coverages.addAll(v.getCoverages());
		}
		return VisualizationFactory.getBuilder()
				.setUuid(getResource().getUUID())
				.setId(getId(getParams()))
				.setCreator(this)
				.setParameters(getParams())
				.setMin(min)
				.setMax(max)
				.setUom(uom)
				.setCoverage(coverages)
				.build();
	}

	private static class OMIterator implements Iterator<Value> {

		private final Iterator<? extends AbstractObservation> resultIterator;
		private Iterator<Value> valueIterator;

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
		public Value next() {
			if (!valueIterator.hasNext()) {
				valueIterator = getNextObservation();
			}
			return valueIterator.next();
		}

		private Iterator<Value> getNextObservation() {
			return getIteratorForResource((IResource) this.resultIterator.next()
			    .getResult().getValue());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	protected String getUom() {
		return getUom(getResource());
	}

	protected String getUom(IResource r) {
		Object o = r.getResource();
		if (o instanceof GridCoverage2D) {
			throw VissError.internal("Not yet implemented");
		} else if (o instanceof UncertaintyNetCDF) {
			return ((UncertaintyNetCDF) o).getUnitAsString();
		} else if (o instanceof IObservationCollection) {
			String uom = null;
			for (AbstractObservation ao : ((IObservationCollection) o)
			    .getObservations()) {
				IResource referencedResource = (IResource) ao.getResult().getValue();
				String uom2 = getUom(referencedResource);
				if (uom == null) {
					uom = uom2;
				} else if (!uom.equals(uom2)) {
					throw VissError.internal("Different UOMs");
				}
			}
			return uom;
		} else {
			throw VissError.internal("Unknown type: " + o.getClass());
		}
	}

	protected static Iterator<Value> getIteratorForResource(IResource r) {
		Object o = r.getResource();
		if (o instanceof GridCoverage2D) {
			throw VissError.internal("Not yet implemented");
		} else if (o instanceof UncertaintyNetCDF) {
			return ((UncertaintyNetCDF) o).iterator();
		} else if (o instanceof IObservationCollection) {
			return new OMIterator((IObservationCollection) o);
		} else {
			throw VissError.internal("Unknown type: " + o.getClass());
		}
	}

	@Override
	public Iterator<Value> iterator() {
		return getIteratorForResource(getResource());
	}

	protected abstract double evaluate(IUncertainty u);
}
