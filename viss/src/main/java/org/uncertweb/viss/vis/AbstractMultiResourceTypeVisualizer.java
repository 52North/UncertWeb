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
import static org.uncertweb.viss.core.util.MediaTypes.JSON_UNCERTAINTY_COLLECTION_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.OM_2_TYPE;
import static org.uncertweb.viss.core.util.MediaTypes.X_NETCDF_TYPE;

import java.util.Iterator;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.opengis.coverage.grid.GridCoverage;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.netcdf.INcUwVariable;
import org.uncertweb.netcdf.NcUwObservation;
import org.uncertweb.netcdf.NcUwVariableWithDimensions;
import org.uncertweb.netcdf.util.WriteableGridCoverage;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.UncertaintyReference;
import org.uncertweb.viss.core.util.VissConstants;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.VisualizationFactory;

public abstract class AbstractMultiResourceTypeVisualizer extends
		AbstractVisualizer implements Iterable<NcUwObservation> {

	public AbstractMultiResourceTypeVisualizer() {
		super(GEOTIFF_TYPE, NETCDF_TYPE, X_NETCDF_TYPE, OM_2_TYPE,
				JSON_UNCERTAINTY_COLLECTION_TYPE);
	}

	@Override
	protected IVisualization visualize() {
		return visualize(getDataSet().getContent());
	}

	protected IVisualization visualize(Object o) {
		if (o instanceof GridCoverage2D) {
			return visualize((GridCoverage2D) o);
		} else if (o instanceof INcUwVariable) {
			return visualize((INcUwVariable) o);
		} else if (o instanceof IObservationCollection) {
			return visualize((IObservationCollection) o);
		} else if (o instanceof UncertaintyReference) {
			return visualize((UncertaintyReference) o);
		} else {
			throw VissError.internal("Unknown type: %s", o.getClass());
		}
	}

	protected IVisualization visualize(UncertaintyReference ur) {
		if (!ur.getMime().equals(GEOTIFF_TYPE)) {
			throw VissError.internal("Not yet supported %s", ur.getMime());
		}
		GridCoverage2D gc = (GridCoverage2D) ur.getContent();

		String name = "???";
		GridGeometry2D geom = gc.getGridGeometry();
		GridEnvelope2D range = geom.getGridRange2D();
		int width = range.width;
		int height = range.height;

		int missingValue = -999;
		final GridCoverageBuilder b = new GridCoverageBuilder();
		// TODO CRS for GeoTiff
		b.setCoordinateReferenceSystem(VissConstants.EPSG4326);
		log.debug("Image size: {}x{}", width, height);
		b.setImageSize(width, height);
		log.debug("Envelope: " + gc.getEnvelope());
		b.setEnvelope(gc.getEnvelope());
		GridCoverageBuilder.Variable var = b
				.newVariable(
						name,
						gc.getSampleDimension(0).getUnits() == null ? javax.measure.unit.Unit.ONE
								: gc.getSampleDimension(0).getUnits());
		var.setLinearTransform(1, 0);
		log.info("MissingValue: {}", missingValue);
		var.addNodataValue("UNKNOWN", missingValue);

		WriteableGridCoverage wgc = new WriteableGridCoverage(
				b.getGridCoverage2D());
		return visualize(
				new CoverageIterator(gc, ur.getType().uri,
						ur.getAdditionalUris()), wgc);
	}

	protected IVisualization visualize(GridCoverage2D gc) {
		throw VissError.internal("Not yet implemented");
	}

	protected IVisualization visualize(Iterator<NcUwObservation> vals,
			WriteableGridCoverage wgc) {
		Double min = null, max = null;
		while (vals.hasNext()) {
			NcUwObservation nv = vals.next();
			if (nv == null) continue;
			Double value = null;
			if (nv != null && nv.getResult() != null) {
				double v = evaluate(nv.getResult().getValue());
				if (!Double.isNaN(v) && !Double.isInfinite(v)) {
					value = Double.valueOf(v);
					if (min == null || min.doubleValue() > v)
						min = Double.valueOf(v);
					if (max == null || max.doubleValue() < v)
						max = Double.valueOf(v);
				}
			}
			try {
				wgc.setValueAtGridPos(nv.getGridCoordinates()
						.getCoordinateValue(0), nv.getGridCoordinates()
						.getCoordinateValue(1), value);
			} catch (ArrayIndexOutOfBoundsException t) {
				if (nv.getFeatureOfInterest().getShape() != null)
					log.debug("Tryed setting value @{} to {}", nv
							.getFeatureOfInterest().getShape(), value);
				throw t;
			}
		}
		log.debug("min: {}; max: {}", min, max);
		return VisualizationFactory.getBuilder().setDataSet(getDataSet())
				.setId(getId(getParams())).setCreator(this)
				.setParameters(getParams()).setMin(min).setMax(max)
				.setUom(getUom()).setCoverage(wgc.getGridCoverage()).build();
	}

	protected String getUom() {
		return getDataSet().getUom();
	}

	protected IVisualization visualize(INcUwVariable gc) {
		Iterable<NcUwObservation> iterable = gc;
		if (isTimeAware()) {
			try {
				log.debug("Temporal Extent: {}", getDataSet()
						.getTemporalExtent()== null ? null : getDataSet()
						.getTemporalExtent().toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			TimeObject to = getSelectedTime();
			log.debug("Selected Time: {}", to);
			iterable = gc.getTimeLayer(to);
		}

		WriteableGridCoverage wgc = gc.getCoverage();
		return visualize(iterable.iterator(), wgc);
	}

	protected IVisualization visualize(IObservationCollection gc) {
		Set<GridCoverage> coverages = UwCollectionUtils.set();
		Double min = null, max = null;
		String uom = null;
		for (AbstractObservation ao : gc.getObservations()) {
			if (!(ao.getResult().getValue() instanceof IResource)) {
				throw VissError.internal("Resource is not compatible");
			}
			// TODO support for more than one dataset
			IDataSet rs = ((IResource) ao.getResult().getValue()).getDataSets()
					.iterator().next();
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
		return VisualizationFactory.getBuilder().setDataSet(getDataSet())
				.setId(getId(getParams())).setCreator(this)
				.setParameters(getParams()).setMin(min).setMax(max).setUom(uom)
				.setCoverage(coverages).build();
	}

	protected static Iterator<NcUwObservation> getIteratorForDataSet(IDataSet r) {
		Object o = r.getContent();
		if (o instanceof GridCoverage2D) {
			return new CoverageIterator((GridCoverage2D) o, r.getType().uri,
					((UncertaintyReference) r).getAdditionalUris());
		} else if (o instanceof NcUwVariableWithDimensions) {
			return ((NcUwVariableWithDimensions) o).iterator();
		} else if (o instanceof IObservationCollection) {
			return new OMIterator((IObservationCollection) o);
		} else {
			throw VissError.internal("Unknown type: " + o.getClass());
		}
	}

	@Override
	public Iterator<NcUwObservation> iterator() {
		return getIteratorForDataSet(getDataSet());
	}

	protected abstract double evaluate(IUncertainty u);
}
