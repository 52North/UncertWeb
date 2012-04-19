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

import java.awt.geom.Point2D;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.Unit;

import org.codehaus.jettison.json.JSONException;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.uncertml.IUncertainty;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyValue;
import org.uncertweb.viss.core.UriBasedUncertaintyParser;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.resource.UncertaintyReference;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.vis.IVisualization;
import org.uncertweb.viss.core.vis.VisualizationFactory;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;
import org.uncertweb.viss.vis.netcdf.UncertaintyVariable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public abstract class AbstractMultiResourceTypeVisualizer extends
    AbstractVisualizer implements Iterable<UncertaintyValue> {
	
	protected static class OMIterator implements Iterator<UncertaintyValue> {

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
	
	protected static class CoverageIterator implements Iterator<UncertaintyValue> {
		private final GeometryFactory f = new GeometryFactory();
		private final Point2D temp = new Point2D.Double();
		private final GridGeometry2D geom; 
		private final MathTransform2D t;
		private final int xSize;
		private final int ySize;
		private int x = 0;
		private int y = 0;
		private final URI mainUri;
		private final Map<URI, Number[]> uriMap;
		private final GridCoverage2D c;
		
		public CoverageIterator(GridCoverage2D c, URI main, Map<URI, Number[]> additionalUris) {
			this.c = c;
			mainUri = main;
			uriMap = (additionalUris != null) ? additionalUris
					: UwCollectionUtils.<URI, Number[]> map();
			geom = c.getGridGeometry();
			t = geom.getGridToCRS2D();
			ySize = geom.getGridRange2D().height;
			xSize = geom.getGridRange2D().width;
		}
		
		
		@Override
		public boolean hasNext() {
			return x < xSize && y < ySize;
		}

		@Override
		public UncertaintyValue next() {
			try {
				GridCoordinates2D gp = new GridCoordinates2D(x, y);
				//TODO match between URI's
				double[] vals = c.evaluate(gp, (double[]) null);
				Number[] a = new Number[vals.length];
				for (int i = 0; i < a.length; ++i) 
					a[i] = Double.valueOf(vals[i]);
				uriMap.put(mainUri, a);
				IUncertainty u = UriBasedUncertaintyParser.map(mainUri, uriMap);
				t.transform(gp, temp);
				Point p = f.createPoint(new Coordinate(temp.getY(), temp.getX()));
				return new UncertaintyValue(u, p, null, new GridCoordinates2D(gp)); //TODO time?
			} catch (InvalidGridGeometryException e) {
				throw VissError.internal(e);
			} catch (TransformException e) {
				throw VissError.internal(e);
			} finally {
				if (x < xSize - 1)
					++x;
				else {
					x = 0;
					++y;
				}
			}
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public AbstractMultiResourceTypeVisualizer() {
		super(GEOTIFF_TYPE, NETCDF_TYPE, X_NETCDF_TYPE, OM_2_TYPE, JSON_UNCERTAINTY_COLLECTION_TYPE);
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
		} else if (o instanceof UncertaintyReference) {
			return visualize((UncertaintyReference) o);
		} else {
			throw VissError.internal("Unknown type: " + o.getClass());
		}
	}

	protected IVisualization visualize(UncertaintyReference ur) {
		if (!ur.getMime().equals(GEOTIFF_TYPE)) {
			throw VissError.internal("Not yet supported " + ur.getMime().toString());
		}
		GridCoverage2D gc = (GridCoverage2D) ur.getContent();
		String name = "???";
		GridGeometry2D geom = gc.getGridGeometry();
		GridEnvelope2D range = geom.getGridRange2D();
		int width = range.width;
		int height = range.height;
		WriteableGridCoverage wgc = getCoverage(name, gc.getEnvelope(), width, height, gc.getSampleDimension(0).getUnits());
		return visualize(new CoverageIterator(gc, ur.getType().uri,
						ur.getAdditionalUris()), wgc);
	}
	
	protected IVisualization visualize(GridCoverage2D gc) {
		throw VissError.internal("Not yet implemented");
	}
	
	protected IVisualization visualize(Iterator<UncertaintyValue> vals, WriteableGridCoverage wgc) {
		Double min = null, max = null;
		while (vals.hasNext()) {
			UncertaintyValue nv = vals.next();
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
				wgc.setValueAtGridPos(nv.getGridLocation().getCoordinateValue(0), nv.getGridLocation().getCoordinateValue(1), value);
//				wgc.setValueAtPos(location, value);
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
	
	protected WriteableGridCoverage getCoverage(String layerName, Envelope e, int lonSize, int latSize, Unit<?> unit) {
		int missingValue = -999;
		final GridCoverageBuilder b = new GridCoverageBuilder();
		//TODO CRS for GeoTiff
		b.setCoordinateReferenceSystem(Constants.EPSG4326);
		log.debug("Image size: {}x{}",lonSize,latSize);
		b.setImageSize(lonSize, latSize);
		log.debug("Envelope: " + e);
		b.setEnvelope(e);
		GridCoverageBuilder.Variable var = b.newVariable(layerName, unit == null ? javax.measure.unit.Unit.ONE : unit);
		var.setLinearTransform(1, 0);
		log.info("MissingValue: {}", missingValue);
		var.addNodataValue("UNKNOWN", missingValue);
		return new WriteableGridCoverage(b.getGridCoverage2D());
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
		} else if (o instanceof UncertaintyReference) {
			Object gc = ((UncertaintyReference) o).getContent();
			if (gc instanceof GridCoverage) {
				Unit<?> u = ((GridCoverage) gc).getSampleDimension(0).getUnits();
				if (u != null)
					return u.toString();
				else return Unit.ONE.toString();
			}
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
		}
		throw VissError.internal("Unknown type: " + o.getClass());
	}

	protected static Iterator<UncertaintyValue> getIteratorForDataSet(IDataSet r) {
		Object o = r.getContent();
		if (o instanceof GridCoverage2D) {
			return new CoverageIterator((GridCoverage2D) o, r.getType().uri,
					((UncertaintyReference) r).getAdditionalUris());
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
