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
package org.uncertweb.viss.vis.netcdf;

import static org.uncertweb.utils.UwCollectionUtils.list;
import static org.uncertweb.utils.UwCollectionUtils.map;
import static org.uncertweb.utils.UwCollectionUtils.toDoubleArray;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_ANCILLARY_VARIABLES;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_COORDINATES;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_LONG_NAME;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_MISSING_VALUE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_REF;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.ATTRIBUTE_STANDARD_NAME;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_ALPHA;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_BETA;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_DEGREES_OF_FREEDOM;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_DENOMINATOR;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_LEVEL;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_LOCATION;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_LOG_SCALE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_LOWER;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_MAXIMUM;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_MEAN;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_MINIMUM;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_NUMERATOR;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_ORDER;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_RATE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_SCALE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_SHAPE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_UPPER;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_VALUE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.PARAMETER_VARIANCE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.STANDARD_NAME_HEIGHT;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.STANDARD_NAME_LATITUDE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.STANDARD_NAME_LONGITUDE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.STANDARD_NAME_TIME;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.STANDARD_NAME_X_COORDINATE;
import static org.uncertweb.viss.vis.netcdf.NetCDFConstants.STANDARD_NAME_Y_COORDINATE;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.joda.time.DateTime;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.BetaDistribution;
import org.uncertml.distribution.continuous.CauchyDistribution;
import org.uncertml.distribution.continuous.ChiSquareDistribution;
import org.uncertml.distribution.continuous.ExponentialDistribution;
import org.uncertml.distribution.continuous.FDistribution;
import org.uncertml.distribution.continuous.GammaDistribution;
import org.uncertml.distribution.continuous.InverseGammaDistribution;
import org.uncertml.distribution.continuous.LaplaceDistribution;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.LogisticDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.continuous.NormalInverseGammaDistribution;
import org.uncertml.distribution.continuous.ParetoDistribution;
import org.uncertml.distribution.continuous.PoissonDistribution;
import org.uncertml.distribution.continuous.StudentTDistribution;
import org.uncertml.distribution.continuous.UniformDistribution;
import org.uncertml.distribution.continuous.WeibullDistribution;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.SystematicSample;
import org.uncertml.sample.UnknownSample;
import org.uncertml.statistic.CentredMoment;
import org.uncertml.statistic.CoefficientOfVariation;
import org.uncertml.statistic.Correlation;
import org.uncertml.statistic.Decile;
import org.uncertml.statistic.InterquartileRange;
import org.uncertml.statistic.Kurtosis;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Median;
import org.uncertml.statistic.Mode;
import org.uncertml.statistic.Moment;
import org.uncertml.statistic.Percentile;
import org.uncertml.statistic.Quantile;
import org.uncertml.statistic.Quartile;
import org.uncertml.statistic.Range;
import org.uncertml.statistic.Skewness;
import org.uncertml.statistic.StandardDeviation;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;
import ucar.units.Unit;
import ucar.units.UnitFormatManager;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class UncertaintyVariable implements Iterable<UncertaintyValue> {

	private final class TemporalUncertaintyValueIterable implements
			Iterable<UncertaintyValue> {
		private final TimeObject to;
		private final int index;

		private TemporalUncertaintyValueIterable(TimeObject to, int index) {
			this.to = to;
			this.index = index;
		}

		@Override
		public Iterator<UncertaintyValue> iterator() {
			return new TemporalUncertaintyValueIterator(to, index);
		}
	}

	private final class TemporalUncertaintyValueIterator implements
			Iterator<UncertaintyValue> {
		private int oI = 0;
		private int aI = 0;
		private final TimeObject to;
		private final int hI = 0;
		private final int tI;
		private final int aS = getLatitudeSize();
		private final int oS = getLongitudeSize();
		private final int hS = hasZComponent() ? getHeightSize() : -1;

		private TemporalUncertaintyValueIterator(TimeObject to, int index) {
			this.to = to;
			this.tI = index;
		}

		@Override
		public boolean hasNext() {
			return aI < aS && oI < oS && (hS < 0 || hI < hS);
		}

		@Override
		public UncertaintyValue next() {
			final IUncertainty u = getValue(tI, hI, oI, aI);
			final Point p = getGeometry(oI, aI, hI);
			incIndex();
			return new UncertaintyValue(u, p, to);
		}

		private void incIndex() {
			if (oI < oS - 1) {
				++oI;
			} else {
				oI = 0;
				++aI;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private final class UncertaintyValueIterator implements
			Iterator<UncertaintyValue> {
		private int oI = 0;
		private int aI = 0;
		private int tI = 0;
		private final int hI = 0;
		private final int tS = hasTimeComponent() ? getTimeSize() : -1;
		private final int aS = getLatitudeSize();
		private final int oS = getLongitudeSize();
		private final int hS = hasZComponent() ? getHeightSize() : -1;

		@Override
		public boolean hasNext() {
			return aI < aS && oI < oS && (tS < 0 || tI < tS)
					&& (hS < 0 || hI < hS);
		}

		@Override
		public UncertaintyValue next() {
			final IUncertainty u = getValue(tI, hI, oI, aI);
			final Point p = getGeometry(oI, aI, hI);
			final TimeObject t = getTime(tI);
			incIndex();
			return new UncertaintyValue(u, p, t);
		}

		private void incIndex() {
			if (tI < tS - 1) {
				++tI;
			} else {
				tI = 0;
				if (oI < oS - 1) {
					++oI;
				} else {
					oI = 0;
					++aI;
				}
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class UncertaintyParser {

		public static IUncertainty map(final URI main,
				final Map<URI, Number[]> v) {
			final IUncertainty u = map(UncertaintyType.fromURI(main), v);
			if (u == null) {
				throw new IllegalArgumentException("not (yet) supported: "
						+ main);
			}
			return u;
		}

		protected static IUncertainty map(final UncertaintyType t,
				final Map<URI, Number[]> v) {
			switch (t) {
			case BETA_DISTRIBUTION:
				return new BetaDistribution(getDouble(t, v, PARAMETER_ALPHA),
						getDouble(t, v, PARAMETER_BETA));
			case CAUCHY_DISTRIBUTION:
				return new CauchyDistribution(getDouble(t, v,
						PARAMETER_LOCATION), getDouble(t, v, PARAMETER_SCALE));
			case CHI_SQUARE_DISTRIBUTION:
				return new ChiSquareDistribution(getIntegr(t, v,
						PARAMETER_DEGREES_OF_FREEDOM));
			case EXPONENTIAL_DISTRIBUTION:
				return new ExponentialDistribution(getIntegr(t, v,
						PARAMETER_RATE));
			case F_DISTRIBUTION:
				return new FDistribution(
						getIntegr(t, v, PARAMETER_DENOMINATOR), getIntegr(t, v,
								PARAMETER_NUMERATOR));
			case GAMMA_DISTRIBUTION:
				return new GammaDistribution(getDouble(t, v, PARAMETER_SHAPE),
						getDouble(t, v, PARAMETER_SCALE));
			case INVERSE_GAMMA_DISTRIBUTION:
				return new InverseGammaDistribution(getDouble(t, v,
						PARAMETER_SHAPE), getDouble(t, v, PARAMETER_SCALE));
			case LAPLACE_DISTRIBUTION:
				return new LaplaceDistribution(getDouble(t, v,
						PARAMETER_LOCATION), getDouble(t, v, PARAMETER_SCALE));
			case LOGISTIC_DISTRIBUTION:
				return new LogisticDistribution(getDouble(t, v,
						PARAMETER_LOCATION), getDouble(t, v, PARAMETER_SCALE));
			case LOG_NORMAL_DISTRIBUTION:
				return new LogNormalDistribution(getDouble(t, v,
						PARAMETER_LOG_SCALE), getDouble(t, v, PARAMETER_SHAPE));
			case NORMAL_DISTRIBUTION:
				return new NormalDistribution(getDouble(t, v, PARAMETER_MEAN),
						getDouble(t, v, PARAMETER_VARIANCE));
			case NORMAL_INVERSE_GAMMA_DISTRIBUTION:
				return new NormalInverseGammaDistribution(getDouble(t, v,
						PARAMETER_MEAN), getDouble(t, v, "varianceScaling"),
						getDouble(t, v, PARAMETER_SHAPE), getDouble(t, v,
								PARAMETER_SCALE));
			case PARETO_DISTRIBUTION:
				return new ParetoDistribution(getDouble(t, v, PARAMETER_SCALE),
						getDouble(t, v, PARAMETER_SHAPE));
			case POISSON_DISTRIBUTION:
				return new PoissonDistribution(getIntegr(t, v, PARAMETER_RATE));
			case STUDENT_T_DISTRIBUTION:
				return new StudentTDistribution(
						getDouble(t, v, PARAMETER_MEAN), getDouble(t, v,
								PARAMETER_VARIANCE), getIntegr(t, v,
								PARAMETER_DEGREES_OF_FREEDOM));
			case UNIFORM_DISTRIBUTION:
				return new UniformDistribution(getDouble(t, v,
						PARAMETER_MINIMUM), getDouble(t, v, PARAMETER_MAXIMUM));
			case WEIBULL_DISTRIBUTION:
				return new WeibullDistribution(
						getDouble(t, v, PARAMETER_SCALE), getDouble(t, v,
								PARAMETER_SHAPE));
			case CENTRED_MOMENT:
				return new CentredMoment(getIntegr(t, v, PARAMETER_ORDER),
						getDouble(t, v, PARAMETER_VALUE));
			case INTERQUATILE_RANGE:
				return new InterquartileRange(getDouble(t, v, PARAMETER_LOWER),
						getDouble(t, v, PARAMETER_UPPER));
			case MOMENT:
				return new Moment(getIntegr(t, v, PARAMETER_ORDER), getDouble(
						t, v, PARAMETER_VALUE));
			case DECILE:
				return new Decile(getIntegr(t, v, PARAMETER_LEVEL), getDouble(
						t, v, PARAMETER_VALUE));
			case PERCENTILE:
				return new Percentile(getIntegr(t, v, PARAMETER_LEVEL),
						getDouble(t, v, PARAMETER_VALUE));
			case QUANTILE:
				return new Quantile(getIntegr(t, v, PARAMETER_LEVEL),
						getDouble(t, v, PARAMETER_VALUE));
			case QUARTILE:
				return new Quartile(getIntegr(t, v, PARAMETER_LEVEL),
						getDouble(t, v, PARAMETER_VALUE));
			case RANGE:
				return new Range(getDouble(t, v, PARAMETER_LOWER), getDouble(t,
						v, PARAMETER_UPPER));
			case STANDARD_DEVIATION:
				return new StandardDeviation(getDouble(t, v));
			case COEFFICIENT_OF_VARIATION:
				return new CoefficientOfVariation(getDouble(t, v));
			case CORRELATION:
				return new Correlation(getDouble(t, v));
			case KURTOSIS:
				return new Kurtosis(getDouble(t, v));
			case MEAN:
				return new Mean(getDouble(t, v));
			case MEDIAN:
				return new Median(getDouble(t, v));
			case MODE:
				return new Mode(getDouble(t, v));
			case SKEWNESS:
				return new Skewness(getDouble(t, v));
			case CONTINUOUS_REALISATION:
				return new ContinuousRealisation(toDoubleArray(getNumberArray(t, v)));
			case RANDOM_SAMPLE:
				return new RandomSample(getRealisationList(t, v));
			case SYSTEMATIC_SAMPLE:
				return new SystematicSample(getRealisationList(t, v));
			case UNKNOWN_SAMPLE:
				return new UnknownSample(getRealisationList(t, v));
				// TODO case CATEGORICAL_REALISATION
				// TODO case CONFIDENCE_INTERVAL:
				// TODO case CONFUSION_MATRIX:
				// TODO case COVARIANCE_MATRIX:
				// TODO case CREDIBLE_INTERVAL:
				// TODO case PROBABILITY:
				// TODO case MIXTURE_MODEL_DISTRIBUTION:
				// TODO case STATISTIC_COLLECTION:
			default:
				return null;
			}
		}

		protected static final ContinuousRealisation[] getRealisationList(
				final UncertaintyType t, final Map<URI, Number[]> v) {
			final Number[] n = getNumberArray(t, v);
			log.debug("Realisations: [{}]", n);
			final ContinuousRealisation[] l = new ContinuousRealisation[n.length];
			for (int i = 0; i < n.length; i++) {
				l[i] = new ContinuousRealisation(new double[] { n[i].doubleValue() });
			}
			log.debug("Realisations: [{}]",l);
			return l;
		}

		protected static final Number[] getNumberArray(final UncertaintyType t,
				final Map<URI, Number[]> v) {
			return v.get(t.getURI());
		}

		protected static final Number getNumber(final UncertaintyType t,
				final Map<URI, Number[]> v) {
			return v.get(t.getURI())[0];
		}

		protected static final Number getNumber(final UncertaintyType t,
				final Map<URI, Number[]> v, final String n) {
			return v.get(t.getParamURI(n))[0];
		}

		protected static final double getDouble(final UncertaintyType t,
				final Map<URI, Number[]> v) {
			return getNumber(t, v).doubleValue();
		}

		protected static final double getDouble(final UncertaintyType t,
				final Map<URI, Number[]> v, final String name) {
			return getNumber(t, v, name).doubleValue();
		}

		protected static final int getIntegr(final UncertaintyType t,
				final Map<URI, Number[]> v, final String name) {
			return getNumber(t, v, name).intValue();
		}
	}

	private static final Logger log = LoggerFactory
			.getLogger(UncertaintyVariable.class);
	private final GeometryFactory fac = new GeometryFactory();
	private final UncertaintyNetCDF file;
	private final Variable variable;
	private Variable height = null;
	private Variable time = null;
	private Variable longitude = null;
	private Variable latitude = null;
	private Variable realizations = null;
	private final Map<Variable, URI> uris = map();
	private final Map<Variable, UncertaintyType> types = map();
	private final Map<Variable, Array> arrays = map();
	private final Map<Variable, Index> indexes = map();
	private final Map<Variable, Number> missingValues = map();
	private DateUnit dateUnit;
	private int latDimension;
	private int lonDimension;
	private int heightDimension;
	private int timeDimension;
	private int sampleDimension;
	private List<TimeObject> times;

	private static final CoordinateReferenceSystem EPSG4326;

	static {
		try {
			EPSG4326 = CRS.getAuthorityFactory(true)
					.createCoordinateReferenceSystem("EPSG:4326");
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public UncertaintyVariable(final UncertaintyNetCDF f, final Variable v) {
		this.variable = v;
		this.file = f;
		findDimensionsAndVariables();
	}
	
	private void findDimensionsAndVariables() {
		int i = 0;
		Variable var = getVariable();
		final Attribute av = getVariable().findAttribute(
				ATTRIBUTE_ANCILLARY_VARIABLES);
		if (av != null) {
			var = getFile().getNotNullVariable(
					av.getStringValue().split(" ")[0]);
		}

		for (final Dimension d : var.getDimensions()) {
			final Variable vv = getFile().getNotNullVariable(d.getName());
			if (vv.findAttribute(ATTRIBUTE_REF) != null
					&& getType(vv).getSuperType() == UncertaintyType.SAMPLE) {
				this.sampleDimension = i;
				this.realizations = vv;
			} else {
				// TODO FIND DIMENSIONS FOR X & Y
				Attribute a = vv.findAttribute(ATTRIBUTE_STANDARD_NAME);
				if (a == null) {
					a = vv.findAttribute(ATTRIBUTE_LONG_NAME);
				}
				final String name = a.getStringValue();
				if (name.equals(STANDARD_NAME_LATITUDE)) {
					this.latitude = vv;
					this.latDimension = i;
				} else if (name.equals(STANDARD_NAME_LONGITUDE)) {
					this.longitude = vv;
					this.lonDimension = i;
				} else if (name.equals(STANDARD_NAME_HEIGHT)) {
					this.height = vv;
					this.heightDimension = i;
				} else if (name.equals(STANDARD_NAME_TIME)) {
					this.time = vv;
					this.timeDimension = i;
				} else if (name.equals(STANDARD_NAME_X_COORDINATE)) {
					this.lonDimension = i;
				} else if (name.equals(STANDARD_NAME_Y_COORDINATE)) {
					this.latDimension = i;
				}
			}
			++i;
		}

		if (this.latitude == null || this.longitude == null) {

			final Attribute a = getVariable().findAttribute(
					ATTRIBUTE_COORDINATES);

			if (a != null) {
				i = 0;
				for (final String s : a.getStringValue().split(" ")) {
					final Variable v = getFile().getNotNullVariable(s);
					Attribute sn = v.findAttribute(ATTRIBUTE_STANDARD_NAME);
					if (sn == null) {
						sn = v.findAttribute(ATTRIBUTE_LONG_NAME);
					}
					if (sn != null) {
						if (sn.getStringValue().equals(STANDARD_NAME_LATITUDE)) {
							this.latitude = v;
						} else if (sn.getStringValue().equals(
								STANDARD_NAME_LONGITUDE)) {
							this.longitude = v;
						}
					} else {
						sn = v.findAttribute(ATTRIBUTE_LONG_NAME);
						if (sn != null) {
							if (sn.getStringValue().equals(
									STANDARD_NAME_LATITUDE)) {
								this.latitude = v;
							} else if (sn.getStringValue().equals(
									STANDARD_NAME_LONGITUDE)) {
								this.longitude = v;
							}
						}
					}
					++i;
				}
			}
		}
	}

	private Variable getVariable() {
		return this.variable;
	}

	private Array getArray() {
		return getArray(getVariable());
	}

	private Index getIndex() {
		return getIndex(getVariable());
	}

	private Index getIndex(final Variable v) {
		Index i = this.indexes.get(v);
		if (i == null) {
			this.indexes.put(v, i = getArray(v).getIndex());
		}
		return i;
	}

	private UncertaintyNetCDF getFile() {
		return this.file;
	}

	private URI getURI(final Variable v) {
		URI uri = this.uris.get(v);
		if (uri == null) {
			this.uris.put(
					v,
					uri = URI.create(getNotNullAttribute(v, ATTRIBUTE_REF)
							.getStringValue()));
		}
		return uri;
	}

	private Array getArray(final Variable v) {
		Array a = this.arrays.get(v);
		if (a == null) {
			try {
				this.arrays.put(v, a = v.read());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
		return a;
	}

	private UncertaintyType getType(final Variable v) {
		UncertaintyType t = this.types.get(v);
		if (t == null) {
			this.types.put(v, t = UncertaintyType.fromURI(getURI(v)));
		}
		return t;
	}

	public Number getMissingValue() {
		return getNotNullAttribute(ATTRIBUTE_MISSING_VALUE).getNumericValue();
	}

	public Unit getUnit() {
		try {
			return UnitFormatManager.instance().parse(getUnitAsString());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getUnitAsString() {
		return getVariable().getUnitsString();
	}

	public UncertaintyType getType() {
		return getType(getVariable());
	}

	@Override
	public Iterator<UncertaintyValue> iterator() {
		return new UncertaintyValueIterator();
	}

	public Iterable<UncertaintyValue> getTemporalLayer(final TimeObject to) {
		
		if (!hasTimeComponent()) {
			return new Iterable<UncertaintyValue>() {
				public Iterator<UncertaintyValue> iterator() {
					return UncertaintyVariable.this.iterator();
				}
			};
		}

		final int index = getTimes().indexOf(to);
		if (index < 0) {
			throw new IllegalArgumentException("no such time");
		}
		return new TemporalUncertaintyValueIterable(to, index);
	}

	private Attribute getNotNullAttribute(final String attribute) {
		return getNotNullAttribute(getVariable(), attribute);
	}

	private Attribute getNotNullAttribute(final Variable v,
			final String attribute) {
		final Attribute a = v.findAttribute(attribute);
		if (a == null) {
			throw new NullPointerException("No such attribute: " + attribute);
		} else {
			return a;
		}
	}

	private Number getMissingValue(final Variable v) {
		Number mv = this.missingValues.get(v);
		if (mv == null) {
			this.missingValues.put(v,
					mv = getNotNullAttribute(v, ATTRIBUTE_MISSING_VALUE)
							.getNumericValue());
		}
		return mv;
	}

	public boolean isInvalid(final Variable v, final double n) {
		final Number mv = getMissingValue(v);
		return Double.compare(mv.doubleValue(), n) == 0;
	}

	private Index setIndex(final Index i, final int time, final int height,
			final int lon, final int lat) {
		if (this.timeDimension >= 0) {
			i.setDim(this.timeDimension, time);
		}
		if (this.heightDimension >= 0) {
			i.setDim(this.heightDimension, height);
		}
		if (this.lonDimension >= 0) {
			i.setDim(this.lonDimension, lon);
		}
		if (this.latDimension >= 0) {
			i.setDim(this.latDimension, lat);
		}
		return i;
	}

	public IUncertainty getValue(final TimeObject to, final int height, final int lon, final int lat) {
		return getValue(getTimes().indexOf(to), height, lon, lat);
	}

	public IUncertainty getValue(final int time, final int height, final int lon, final int lat) {

		Map<URI, Number[]> map = null;

		if (getType() == UncertaintyType.STATISTIC_COLLECTION) {
			// TODO statistics collection
		} else if (getType().getSuperType() == UncertaintyType.SAMPLE) {
			log.debug("Realizations: {}", this.realizations);
			final int length = this.realizations.getShape()[0];
			log.debug("RealizationsShape: {}", length);
			final Number[] array = new Number[length];
			final Index i = setIndex(getIndex(), time, height, lon, lat);
			for (int j = 0; j < length; ++j) {
				i.setDim(this.sampleDimension, j);
				array[j] = new Double(getArray().getDouble(i));
				log.debug("array[{}] = {}", j, array[j]);
			}
			log.debug("Realisations: {}", array);
			
			map = map(getType().getURI(), array);
		} else {
			final Attribute av = getVariable().findAttribute(
					ATTRIBUTE_ANCILLARY_VARIABLES);
			if (av == null) {
				map = map(
						getType().getURI(),
						new Number[] { getArray().getDouble(
								setIndex(getIndex(), time, height, lon, lat)) });
			} else {
				map = map();
				for (final String avn : av.getStringValue().split(" ")) {
					final Variable avv = getFile().getNotNullVariable(avn);
					final double value = getArray(avv).getDouble(
							setIndex(getIndex(avv), time, height, lon, lat)); // assumes
																				// same
																				// dimensions...
					if (isInvalid(avv, value)) {
						return null;
					}
					map.put(getURI(avv), new Number[] { new Double(value) });
				}
			}
		}
		return UncertaintyParser.map(getType().getURI(), map);
	}

	private Variable getTime() {
		return this.time;
	}

	public int getTimeSize() {
		if (hasTimeComponent()) {
			return getTime().getShape()[0];
		} else {
			return 0;
		}
	}

	public List<TimeObject> getTimes() {
		if (!hasTimeComponent()) {
			return null;
		}
		if (this.times == null) {
			this.times = new ArrayList<TimeObject>(getTimeSize());
			final Array ta = getTimeArray();
			final DateUnit du = getDateUnit();
			for (int i = 0; i < getTimeSize(); i++) {
				this.times.add(new TimeObject(new DateTime(du.makeDate(ta
						.getDouble(i)))));
			}
		}
		return this.times;
	}

	private Array getTimeArray() {
		return getArray(getTime());
	}

	private DateUnit getDateUnit() {
		if (this.dateUnit == null) {
			try {
				this.dateUnit = new DateUnit(getTime().getUnitsString());
			} catch (final Exception e) {
				throw new RuntimeException("Could not parse TimeUnit", e);
			}
		}
		return this.dateUnit;
	}

	public TimeObject getTime(final int i) {
		if (hasTimeComponent()) {
			return getTimes().get(i);
		} else {
			return null;
		}
	}

	private Variable getHeight() {
		return this.height;
	}

	public int getHeightSize() {
		if (hasZComponent()) {
			return getHeight().getShape()[0];
		} else {
			return 0;
		}
	}

	private Array getHeightArray() {
		return getArray(getHeight());
	}

	private Variable getLongitude() {
		return this.longitude;
	}

	public int getLongitudeSize() {
		return getLongitude().getShape()[0];
	}

	private Array getLongitudeArray() {
		return getArray(getLongitude());
	}

	private Variable getLatitude() {
		return this.latitude;
	}

	public int getLatitudeSize() {
		return getLatitude().getShape()[0];
	}

	private Array getLatitudeArray() {
		return getArray(getLatitude());
	}

	public Point getGeometry(final int lon, final int lat) {
		return getGeometry(lon, lat, -1);
	}

	public boolean hasZComponent() {
		return getHeight() != null;
	}

	public boolean hasTimeComponent() {
		return getTime() != null;
	}

	public Envelope getEnvelope() {
		Variable lon = getLongitude();
		Variable lat = getLatitude();
		try {
			// X=LON,Y=LAT!!!
			int lonS = getLongitudeSize();
			int latS = getLatitudeSize();
			double lonMin = lon.read(list(new ucar.ma2.Range(0, 0))).getDouble(
					0);
			double lonMax = lon.read(
					list(new ucar.ma2.Range(lonS - 1, lonS - 1))).getDouble(0);
			double latMin = lat.read(list(new ucar.ma2.Range(0, 0))).getDouble(
					0);
			double latMax = lat.read(
					list(new ucar.ma2.Range(latS - 1, latS - 1))).getDouble(0);
			return new Envelope2D(EPSG4326, lonMin, latMin, lonMax - lonMin,
					latMax - latMin);
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public WriteableGridCoverage getCoverage(String layerName) {
		return getCoverage(layerName, getVariable(), null);
	}

	public WriteableGridCoverage getCoverage(String layerName, String uom) {
		return getCoverage(layerName, getVariable(), uom);
	}

	protected WriteableGridCoverage getCoverage(String layerName, Variable v,
			String unit) {
		int missingValue = -999;
		int latSize = getLatitudeSize();
		int lonSize = getLongitudeSize();
		final GridCoverageBuilder b = new GridCoverageBuilder();
		b.setCoordinateReferenceSystem(EPSG4326);
		log.debug("ImageSize: {}x{}", lonSize, latSize);
		// FIXME this removes the cross, but thats somewhat ugly
		b.setImageSize(lonSize - 1, latSize - 1);
		Envelope e = getEnvelope();
		log.debug("Low: {}, High: {}", e.getLowerCorner(), e.getUpperCorner());
		b.setEnvelope(getEnvelope());
		
		GridCoverageBuilder.Variable var;
		if (unit != null) {
			var = b.newVariable(layerName,
					javax.measure.unit.Unit.valueOf(getUnit().toString()));
		} else {
			var = b.newVariable(layerName, javax.measure.unit.Unit.ONE);
		}
		var.setLinearTransform(1, 0);
		log.info("MissingValue: {}", missingValue);
		var.addNodataValue("UNKNOWN", missingValue);
		return new WriteableGridCoverage(b.getGridCoverage2D());
	}

	public Point getGeometry(final int lon, final int lat, final int height) {
		final double x = getLatitudeArray().getDouble(lat);
		final double y = getLongitudeArray().getDouble(lon);
		Coordinate c = null;
		if (hasZComponent()) {
			c = new Coordinate(x, y, getHeightArray().getDouble(height));
		} else {
			c = new Coordinate(x, y);
		}
		return this.fac.createPoint(c);
	}

	public String getName() {
		return getName(getVariable());
	}

	private String getName(final Variable v) {
		final StringBuilder sb = new StringBuilder();
		sb.append(v.getName());

		final Attribute sn = v.findAttribute(ATTRIBUTE_STANDARD_NAME);
		if (sn != null) {
			sb.append(" - ").append(sn.getStringValue());
		}

		final Attribute ln = v.findAttribute(ATTRIBUTE_LONG_NAME);
		if (ln != null) {
			sb.append(" (").append(ln.getStringValue()).append(")");
		}

		return sb.toString();
	}

}