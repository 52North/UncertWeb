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

import static org.uncertweb.utils.UwCollectionUtils.enumMap;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.Unit;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.coverage.grid.ViewType;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.joda.time.DateTime;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.netcdf.NcUwConstants.Order;
import org.uncertweb.netcdf.NcUwConstants.Origin;
import org.uncertweb.netcdf.util.TimeObjectComparator;
import org.uncertweb.netcdf.util.WriteableGridCoverage;
import org.uncertweb.utils.MultivaluedHashMap;
import org.uncertweb.utils.MultivaluedMap;
import org.uncertweb.utils.UwCollectionUtils;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
/*
 * X = LON
 * Y = LAT
 */
public class NcUwVariableWithDimensions extends AbstractNcUwVariable {
	private final Map<NcUwDimension, Integer> index = enumMap(NcUwDimension.class);
	private final Map<NcUwDimension, Index> arrayindex = enumMap(NcUwDimension.class);
	private final Map<NcUwDimension, Order> orders = enumMap(NcUwDimension.class);
	private final Map<NcUwDimension, Variable> variables = enumMap(NcUwDimension.class);
	private final Map<NcUwDimension, Array> arrays = enumMap(NcUwDimension.class);
	private final Map<NcUwDimension, Integer> sizes = enumMap(NcUwDimension.class);

	private Envelope2D envelope;
	private Origin origin;
	private DateUnit tUnit;
	private List<TimeObject> times;

	public NcUwVariableWithDimensions(NcUwFile file, Variable variable, NcUwArrayCache cache, AbstractNcUwVariable parent) {
		super(file, variable, cache, parent);
		findDimensions();
	}
	
	@Override
	public List<TimeObject> getTimes() {
		if (!hasDimension(NcUwDimension.T)) {
			log.warn("No temporal dimension in this variable");
			return UwCollectionUtils.list();
		}
		if (this.times == null) {
			try {
				this.times = NcUwHelper.parseTimes(getArray(NcUwDimension.T),
						getDateUnit());
			} catch (final IOException e) {
				throw new NcUwException(e);
			}
		}
		return this.times;
	}

	public TimeObject getTime(NcUwCoordinate c) {
		if (hasDimension(NcUwDimension.T) && c.hasDimension(NcUwDimension.T)) {
			return getTimes().get(c.get(NcUwDimension.T));
		} else {
			return null;
		}
	}

	@Override
	public NcUwCoordinate getIndex(TimeObject t) {
		if (t != null) {
			int i = 0;
			for (final TimeObject to : getTimes()) {
//				log.debug("{} == {} = {}", new Object[] { to, t, to.equals(t) });
				if (to.equals(t)) {
					return new NcUwCoordinate().set(NcUwDimension.T, i);
				}
			}
			++i;
		}
		return null;
	}
	
	@Override
	public NcUwCoordinate getIndex(final Point p) {
		return getIndex(NcUwHelper.toDirectPosition(p, getCRS()));
	}

	@Override
	public NcUwCoordinate getIndex(DirectPosition p) {
		if (contains(p)) {
			try {
				final GeodeticCalculator calc = new GeodeticCalculator(getCRS());
				calc.setStartingPosition(CRS.findMathTransform(
						p.getCoordinateReferenceSystem(), getCRS()).transform(
						p, null));
				double mindist = Double.POSITIVE_INFINITY;
				int minx = -1, miny = -1;
				for (int x = 0; x < getSize(NcUwDimension.X); ++x) {
					for (int y = 0; y < getSize(NcUwDimension.Y); ++y) {
						calc.setDestinationPosition(getPosition(x, y));
						final double dist = calc.getOrthodromicDistance();
						if (dist < mindist) {
							mindist = dist;
							minx = x;
							miny = y;
						}
					}
				}
				return new NcUwCoordinate()
					.set(NcUwDimension.X, minx)
					.set(NcUwDimension.Y, miny);
			} catch (final FactoryException e) {
				throw new NcUwException(e);
			} catch (final MismatchedDimensionException e) {
				throw new NcUwException(e);
			} catch (final TransformException e) {
				throw new NcUwException(e);
			}
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public boolean contains(DirectPosition p) {
		return ((Envelope2D) getEnvelope()).contains(p);
	}
	
	
	
	@Override
	public boolean contains(Point p) {
		return contains(NcUwHelper.toDirectPosition(p, getCRS()));
	}

	public DirectPosition getPosition(int x, int y) {
		checkExistingDimension(NcUwDimension.X, NcUwDimension.Y);
		final double xv = getArray(NcUwDimension.X).getDouble(x);
		final double yv = getArray(NcUwDimension.Y).getDouble(y);
		return new DirectPosition2D(getCRS(), xv, yv);
	}

	protected DateUnit getDateUnit() {
		checkExistingDimension(NcUwDimension.T);
		if (this.tUnit == null) {
			try {
				this.tUnit = new DateUnit(getVariable(NcUwDimension.T)
						.getUnitsString());
			} catch (final Exception e) {
				throw new NcUwException("Could not parse TimeUnit", e);
			}
		}
		return this.tUnit;
	}

	public int getIndex(NcUwDimension d) {
		final Integer i = this.index.get(d);
		return i != null ? i : -1;
	}

	public Index getArrayIndex(NcUwDimension d) {
		Index i = this.arrayindex.get(d);
		if (i == null && hasDimension(d)) {
			this.arrayindex.put(d, i = getArray(d).getIndex());
		}
		return i;
	}

	@Override
	public boolean hasDimension(NcUwDimension... ds) {
		for (NcUwDimension d : ds) {
			if (!this.index.containsKey(d)) {
				return false;
			}
		}
		return true;
	}
	
	private void checkExistingDimension(NcUwDimension... ds) {
		for (final NcUwDimension d : ds) {
			if (!hasDimension(d)) {
				throw new NcUwException("Dimension %s is not present.", d);
			}
		}
	}

	public Variable getVariable(NcUwDimension d) {
		checkExistingDimension(d);
		Variable v = this.variables.get(d);
		if (v == null) {
			this.variables.put(	d, v = getFile().getVariable(getVariable().getDimension(getIndex(d)).getName(), true));
		}
		return v;
	}

	public Array getArray(NcUwDimension d) {
		checkExistingDimension(d);
		Array a = this.arrays.get(d);
		if (a == null) {
			this.arrays.put(d, a = getCache().getArray(getVariable(d)));
		}
		return a;
	}

	public Array getArray() {
		return getCache().getArray(getVariable());
	}

	public Object getMissingValue() {
		final Attribute a = getVariable().findAttributeIgnoreCase(
				NcUwConstants.Attributes.MISSING_VALUE);
		if (a == null) {
			return null;
		}
		if (!a.getDataType().isNumeric()) {
			new NcUwException("Non-numeric missing values are not supported");
		}
		return a.getNumericValue();
	}

	public boolean isValid(Number n) {
		return n != null && !n.equals(getMissingValue());
	}

	@Override
	public int getSize(NcUwDimension d) {
		checkExistingDimension(d);
		Integer s = this.sizes.get(d);
		if (s == null) {
			this.sizes.put(d, s = getVariable(d).getShape(0));
		}
		return s.intValue();
	}

	public Object[] getRange(NcUwDimension d) {
		checkExistingDimension(d);
		switch (d) {
		case X: case Y: case Z:
			return NcUwHelper.getRangeOfOrderedVariable(getVariable(d));
		case T:
			final Number[] n = NcUwHelper.getRangeOfOrderedVariable(getVariable(d));
			return new TimeObject[] {
					new TimeObject(new DateTime(getDateUnit().makeDate(	n[0].doubleValue()))),
					new TimeObject(new DateTime(getDateUnit().makeDate(	n[1].doubleValue()))) 
			};
		case S:
			return new Integer[] { 1, getSize(d) };
		default:
			return null;
		}

	}

	public Order getOrder(NcUwDimension d) {
		checkExistingDimension(d);
		Order o = this.orders.get(d);
		if (o == null) {
			final Object[] range = getRange(d);
			int compare;
			if (range instanceof Number[]) {
				final Number[] nos = (Number[]) range;
				compare = NcUwHelper.compare(nos[0], nos[1]);
			} else if (range instanceof TimeObject[]) {
				final TimeObject[] nos = (TimeObject[]) range;
				compare = new TimeObjectComparator().compare(nos[0], nos[1]);
			} else {
				throw new NcUwException("range class is not supported: %s", range.getClass());
			}
			this.orders.put(d, o = compare <= 0 ? Order.ASCENDING : Order.DESCENDING);
		}
		return o;
	}

	@Override
	public Set<NcUwDimension> getDimensions() {
		return this.index.keySet();
	}

	private double xDiff;
	private double yDiff;
	private double xMin;
	private double yMin;
	private double xMax;
	private double yMax;
	
	protected double getCellWidth() {
		getEnvelope();
		return xDiff;
	}
	
	protected double getCellHeight() {
		getEnvelope();
		return yDiff;
	}
	
	@Override
	public Envelope2D getEnvelope() {
		if (this.envelope == null) {
			NcUwVariableWithDimensions v = NcUwHelper.findGriddedVariable(this);
			if (v == null)
				return null;

			final int xSize = v.getSize(NcUwDimension.X);
			final int ySize = v.getSize(NcUwDimension.Y);
			final Array x = v.getArray(NcUwDimension.X);
			final Array y = v.getArray(NcUwDimension.Y);

			xMin = Double.POSITIVE_INFINITY;
			yMin = Double.POSITIVE_INFINITY;
			xMax = Double.NEGATIVE_INFINITY;
			yMax = Double.NEGATIVE_INFINITY;

			for (int i = 0; i < xSize; ++i) {
				final double d = x.getDouble(i);
				xMax = Math.max(xMax, d);
				xMin = Math.min(xMin, d);
			}

			for (int i = 0; i < ySize; ++i) {
				final double d = y.getDouble(i);
				yMax = Math.max(yMax, d);
				yMin = Math.min(yMin, d);
			}

			this.xDiff = (xMax - xMin) / (xSize - 1);
			this.yDiff = (yMax - yMin) / (ySize - 1);

			xMin -= xDiff/2;
			yMin -= yDiff/2;
			xMax += xDiff/2;
			yMax += yDiff/2;

			this.envelope = new Envelope2D(getCRS(), 
					xMin, yMin, xMax - xMin, yMax - yMin);
		}
		return this.envelope;
	}

	private static final String LAYER_NAME = "dasisttotalirrelevant";
	private static final double SCALE = 1.0D;
	private static final double OFFSET = 0.0D;
	private static final int NODATA_VALUE = -999999;// TODO do this dynamically
	private static final String NODATA_VALUE_NAME = "UNKNOWN";

	
	@Override
	public WriteableGridCoverage getCoverage() {
		INcUwVariable v = NcUwHelper.findGriddedVariable(this);
		if (v == null)
			return null;

		final int ySize = v.getSize(NcUwDimension.Y);
		final int xSize = v.getSize(NcUwDimension.X);
		final Envelope envelope = v.getEnvelope();
		final GridCoverageBuilder b = new GridCoverageBuilder();

		b.setCoordinateReferenceSystem(envelope.getCoordinateReferenceSystem());

		log.debug("ImageSize: {}x{}; Envelope: {}", new Object[] {
				xSize, ySize, envelope });
		b.setImageSize(xSize, ySize);
		b.setEnvelope(envelope);
		final GridCoverageBuilder.Variable var = b.newVariable(
				LAYER_NAME, Unit.ONE);
		var.setLinearTransform(SCALE,
				OFFSET);

		var.addNodataValue(NODATA_VALUE_NAME, NODATA_VALUE);
		return new WriteableGridCoverage(b.getGridCoverage2D().view(
				ViewType.GEOPHYSICS));
	}

	public Origin getOrigin() {
		if (this.origin == null) {
			NcUwVariableWithDimensions v = NcUwHelper.findGriddedVariable(this);
			if (v == null) return null;
			if (v.getOrder(NcUwDimension.X) == Order.ASCENDING) {
				this.origin = v.getOrder(NcUwDimension.Y) == Order.ASCENDING ?
						Origin.LOWER_LEFT : Origin.UPPER_LEFT;
			} else {
				this.origin = v.getOrder(NcUwDimension.Y) == Order.ASCENDING ?
						Origin.LOWER_RIGHT : Origin.UPPER_RIGHT;
			}
		}
		return this.origin;
	}

	public int[] translateIndex(NcUwCoordinate c) {
		final int[] index = new int[getDimensions().size()];
		for (final NcUwDimension d : c.getDimensions()) {
			index[getIndex(d)] = c.get(d);
		}
		return index;
	}

	protected void findDimensions() {
		final int existingDimensions = getVariable().getDimensions().size();
		for (int i = 0; i < existingDimensions; ++i) {
			final Variable v = getFile().getVariable(getVariable().getDimension(i).getName(), true);
			if (v == null) {
				new NcUwException("Can not find dimension variable %s", 
						getVariable().getDimension(i).getName());
			}
			final NcUwDimension d = NcUwDimension.fromVariable(v);
			if (d != null) {
				setIndex(d, i);
			}
		}
		if (getDimensions().size() != existingDimensions) {
			throw new NcUwException(
					"could not recognize all dimensions (%d of %d)",
					getDimensions().size(), existingDimensions);
		}
		if (hasDimension(NcUwDimension.X) && getSize(NcUwDimension.X) < 2) {
			throw new NcUwException("can not process NetCDF with %d %s-cells",
					getSize(NcUwDimension.X), NcUwDimension.X);
		}
		if (hasDimension(NcUwDimension.Y) && getSize(NcUwDimension.Y) < 2) {
			throw new NcUwException("can not process NetCDF with %d %s-cells",
					getSize(NcUwDimension.Y), NcUwDimension.Y);
		}
	}

	protected void setIndex(NcUwDimension d, int i) {
		if (hasDimension(d)) {
			throw new NcUwException(
					"duplicate %s dimension found at %d and %d", d,
					getIndex(d), i);
		}
		this.index.put(d, new Integer(i));
	}

	public DataType getDataType() {
		return getVariable().getDataType();
	}

	public GridCoordinates getGridCoordinates(final int x, final int y) {
		final int size_x = getSize(NcUwDimension.X);
		final int size_y = getSize(NcUwDimension.Y);
		switch (getOrigin()) {
			case UPPER_LEFT: return new GridCoordinates2D(x             , y             );
			case UPPER_RIGHT:return new GridCoordinates2D(size_x - x - 1, y             );
			case LOWER_LEFT: return new GridCoordinates2D(x             , size_y - y - 1);
			case LOWER_RIGHT:return new GridCoordinates2D(size_x - x - 1, size_y - y - 1);
			default: 
				return null;
		}
	}

	@Override
	public GridCoordinates getGridCoordinates(NcUwCoordinate d) {
		INcUwVariable v = getGriddedVariable();
		if (v == null) {
			return null;
		} else { 
			return ((NcUwVariableWithDimensions)v).getGridCoordinates(d.get(NcUwDimension.X), d.get(NcUwDimension.Y));
		}
	}
	
	public INcUwVariable getGriddedVariable() {
		return NcUwHelper.findGriddedVariable(this);
	}
	
	protected double getCoordinateValue(NcUwDimension d, NcUwCoordinate c) {
		if (!hasDimension(d) || !c.hasDimension(d)) {
			throw new IllegalArgumentException("Dimension " + d
					+ "is not present");
		}
		return getArray(d).getDouble(getArrayIndex(d).set(c.get(d)));
	}

	protected MultivaluedMap<URI, Object> getValueMap(NcUwCoordinate c) {
		log.debug("Getting value: {}", c);
		final MultivaluedMap<URI, Object> map = MultivaluedHashMap.create();
		
		for (final NcUwDimension d : getDimensions()) {
			if (d != NcUwDimension.S && !c.hasDimension(d)) {
				throw new NcUwException("Dimension %s is missing.", d);
			}
		}
		
		if (hasDimension(NcUwDimension.S) && !c.hasDimension(NcUwDimension.S)) {
			for (int i = 0; i < getSize(NcUwDimension.S); ++i) {
				map.addAll(getValueMap(c.set(NcUwDimension.S, i)));
			}
		} 
		
		else {
			final Object o = getArray().getObject(getArray().getIndex().set(translateIndex(c)));
			if (o == null || o.equals(getMissingValue())) {
				return null;
			}
			map.add(getRef(), o);
		}
		
		
		for (final INcUwVariable v : getAncillaryVariables()) {
			final MultivaluedMap<URI, Object> submap = ((AbstractNcUwVariable) v).getValueMap(c);
			if (submap == null) { return null; }
			map.addAll(submap);
		}
		return map;
	}
	
	private Integer epsgCode = null;
	
	public int getEpsgCode() {
		if (epsgCode == null) {
			epsgCode = Integer.valueOf(NcUwHelper.getEpsgCodeForCrs(getCRS()));
		}
		return epsgCode.intValue();
	}
	
	protected Envelope positionToEnvelope(DirectPosition dp) {
		final double[] coordinate = dp.getCoordinate();
		final double x = coordinate[0];
		final double y = coordinate[1];
		final double width = getCellWidth();
		final double height = getCellHeight();
		return new Envelope2D(dp.getCoordinateReferenceSystem(),
				x - width/2, y - height/2, width, height);
	}
	

	@Override
	public SpatialSamplingFeature getFeature(NcUwCoordinate c) {
		int epsgCode = getEpsgCode();
		final boolean transformToWGS84 = epsgCode < 0;
		if (transformToWGS84) { epsgCode = 4326; }
		Envelope e = (c == null || !c.hasDimension(NcUwDimension.X, NcUwDimension.Y)) ?
			getEnvelope() : positionToEnvelope(getDirectPosition(c));
		Geometry g = NcUwHelper.envelopeToPolygon(e, transformToWGS84);
		g.setSRID(epsgCode);
		return new SpatialSamplingFeature(NcUwConstants.DEFAULT_SAMPLING_FEATURE, g);
	}
	
	public SpatialSamplingFeature getFeature() {
		return getFeature(null);
	}

	private DirectPosition getDirectPosition(NcUwCoordinate c) {
		final double x = getCoordinateValue(NcUwDimension.X, c);
		final double y = getCoordinateValue(NcUwDimension.Y, c);
		return new DirectPosition2D(getCRS(), x, y);
	}

}
