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

import org.codehaus.jettison.json.JSONException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.VisualizationFactory;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;
import org.uncertweb.viss.vis.netcdf.UncertaintyValue;
import org.uncertweb.viss.vis.netcdf.UncertaintyVariable;

public abstract class AbstractMultiResourceTypeVisualizer extends
    AbstractVisualizer implements Iterable<UncertaintyValue> {

	private static class OMIterator implements Iterator<UncertaintyValue> {

		private final Iterator<? extends AbstractObservation> resultIterator;
		private Iterator<UncertaintyValue> valueIterator;

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
		public UncertaintyValue next() {
			if (!valueIterator.hasNext()) {
				valueIterator = getNextObservation();
			}
			return valueIterator.next();
		}

		private Iterator<UncertaintyValue> getNextObservation() {
			return getIteratorForDataSet(((IResource) this.resultIterator
					.next().getResult().getValue()).getDataSets().iterator()
					.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public AbstractMultiResourceTypeVisualizer() {
		super(GEOTIFF_TYPE, NETCDF_TYPE, X_NETCDF_TYPE, OM_2_TYPE);
	}

	@Override
	protected IVisualization visualize() {
		return visualize(getDataSet().getContent());
	}

	protected IVisualization visualize(Object o) {
		if (o instanceof GridCoverage2D) {
			return visualize((GridCoverage2D) o);
		} else if (o instanceof UncertaintyVariable) {
			return visualize((UncertaintyVariable) o);
		} else if (o instanceof IObservationCollection) {
			return visualize((IObservationCollection) o);
		} else {
			throw VissError.internal("Unknown type: " + o.getClass());
		}
	}

	protected IVisualization visualize(GridCoverage2D gc) {
		throw VissError.internal("Not yet implemented");
	}
	
	protected IVisualization visualize(UncertaintyVariable gc) {
		Iterable<UncertaintyValue> iterable = gc;
		if (isTimeAware()) {
			log.debug("Temporal Extent: {}", getDataSet().getTemporalExtent());
			try {
				log.debug("Temporal Extent: {}", getDataSet().getTemporalExtent().toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			TimeObject to = getSelectedTime();
			log.debug("Selected Time: {}", to);
			iterable = gc.getTemporalLayer(to);
		}
		
		WriteableGridCoverage wgc = gc.getCoverage(getCoverageName(), getUom());
		Double min = null, max = null;
		for (UncertaintyValue nv : iterable) {
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
			Point2D.Double location = 
					new Point2D.Double(
							nv.getLocation().getY(), 
							nv.getLocation().getX());
			try{
			wgc.setValueAtPos(location, value);
			} catch (ArrayIndexOutOfBoundsException t) {
				log.debug("Tryed setting value @{} to {}", location, value);
				throw t;
			}
		}
		log.debug("min: {}; max: {}", min, max);
		return VisualizationFactory.getBuilder()
			.setDataSet(getDataSet())
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
			//TODO support for more than one dataset
			IDataSet rs = ((IResource) ao.getResult().getValue()).getDataSets().iterator().next();
			IVisualization v = visualize(rs.getContent());

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
				.setDataSet(getDataSet())
				.setId(getId(getParams()))
				.setCreator(this)
				.setParameters(getParams())
				.setMin(min)
				.setMax(max)
				.setUom(uom)
				.setCoverage(coverages)
				.build();
	}

	

	protected String getUom() {
		return getUom(getDataSet());
	}

	protected String getUom(IDataSet r) {
		Object o = r.getContent();
		if (o instanceof GridCoverage2D) {
			throw VissError.internal("Not yet implemented");
		} else if (o instanceof UncertaintyVariable) {
			return ((UncertaintyVariable) o).getUnitAsString();
		} else if (o instanceof IObservationCollection) {
			String uom = null;
			for (AbstractObservation ao : ((IObservationCollection) o)
			    .getObservations()) {
				IResource referencedResource = (IResource) ao.getResult().getValue();
				String uom2 = getUom(referencedResource.getDataSets().iterator().next());
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

	protected static Iterator<UncertaintyValue> getIteratorForDataSet(IDataSet r) {
		Object o = r.getContent();
		if (o instanceof GridCoverage2D) {
			throw VissError.internal("Not yet implemented");
		} else if (o instanceof UncertaintyVariable) {
			return ((UncertaintyVariable) o).iterator();
		} else if (o instanceof IObservationCollection) {
			return new OMIterator((IObservationCollection) o);
		} else {
			throw VissError.internal("Unknown type: " + o.getClass());
		}
	}
	
	@Override
	public Iterator<UncertaintyValue> iterator() {
		return getIteratorForDataSet(getDataSet());
	}

	protected abstract double evaluate(IUncertainty u);
}
