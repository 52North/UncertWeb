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

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.apache.commons.lang.Validate;
import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
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
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.vis.WriteableGridCoverage;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class UncertaintyNetCDF implements Iterable<Value> {

	protected class NetCDFIterator implements Iterator<Value> {

		private int lonIndex = 0;
		private int latIndex = 0;
		private final int latSize;
		private final int lonSize;
		private final Array lonValues;
		private final Array latValues;
		private final GeometryFactory fac;
		private final URI primaryURI;
		private final Set<Variable> variables;
		private final Map<Variable, Integer> missingValues = UwCollectionUtils
				.map();
		private final Map<Variable, Array> arrays = UwCollectionUtils.map();
		private final Map<Variable, Index> indexes = UwCollectionUtils.map();
		private final Map<Variable, URI> uris = UwCollectionUtils.map();
		private final UncertaintyParser p = new UncertaintyParser();

		public NetCDFIterator() throws IOException {
			this.lonValues = getLongitude().read();
			this.latValues = getLatitude().read();
			this.lonSize = this.lonValues.getShape()[0];
			this.latSize = this.latValues.getShape()[0];
			Variable p = getPrimaryVariable();
			this.primaryURI = getURI(p);
			this.fac = new GeometryFactory();
			this.variables = getRelevantVariables(getPrimaryVariable());

			for (Variable v : this.variables) {
				this.missingValues.put(v, getMissingValue(v));
				Array a = v.read();
				this.arrays.put(v, a);
				this.indexes.put(v, a.getIndex());
				this.uris.put(v, getURI(v));
			}
		}

		@Override
		public boolean hasNext() {
			return this.latIndex < this.latSize && this.lonIndex < this.lonSize;
		}

		@Override
		public Value next() {
			double lat = this.latValues.getDouble(this.latIndex);
			double lon = this.lonValues.getDouble(this.lonIndex);
			Point location = this.fac.createPoint(new Coordinate(lon, lat));
			IUncertainty value = parseValue(this.lonIndex, this.latIndex);

			if (this.lonIndex == this.lonSize - 1) {
				this.lonIndex = 0;
				++this.latIndex;
			} else {
				++this.lonIndex;
			}

			return new Value(value, location, null);
		}

		private IUncertainty parseValue(int lonIndex, int latIndex) {
			Map<URI, Number> values = UwCollectionUtils.map();
			for (Variable v : this.variables) {
				Number n = Double.valueOf(this.arrays.get(v).getDouble(
						this.indexes.get(v).set(this.latIndex, this.lonIndex)));
				if (n.intValue() != this.missingValues.get(v).intValue()) {
					values.put(this.uris.get(v), n);
				}
			}

			if (values.isEmpty()) {
				return null;
			} else {
				return p.map(this.primaryURI, values);
			}
		}

		private Set<Variable> getRelevantVariables(Variable v) {
			Set<Variable> aV = getAnciallaryVariables(v);
			if (aV == null || aV.isEmpty()) {
				return UwCollectionUtils.set(v);
			} else {
				Set<Variable> result = UwCollectionUtils.set();
				for (Variable a : aV) {
					result.addAll(getRelevantVariables(a));
				}
				return result;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	protected static class UncertaintyParser {

		public IUncertainty map(URI main, Map<URI, Number> v) {
			IUncertainty u = map(UncertaintyType.fromURI(main), v);
			if (u == null)
				throw new IllegalArgumentException("not (yet) supported: "
						+ main);
			return u;
		}

		protected IUncertainty map(UncertaintyType t, Map<URI, Number> v) {
			switch (t) {
			case BETA_DISTRIBUTION:
				return new BetaDistribution(getDouble(t, v, "alpha"),
						getDouble(t, v, "beta"));
			case CAUCHY_DISTRIBUTION:
				return new CauchyDistribution(getDouble(t, v, "location"),
						getDouble(t, v, "scale"));
			case CHI_SQUARE_DISTRIBUTION:
				return new ChiSquareDistribution(getIntegr(t, v,
						"degreesOfFreedom"));
			case EXPONENTIAL_DISTRIBUTION:
				return new ExponentialDistribution(getIntegr(t, v, "rate"));
			case F_DISTRIBUTION:
				return new FDistribution(getIntegr(t, v, "denominator"),
						getIntegr(t, v, "numerator"));
			case GAMMA_DISTRIBUTION:
				return new GammaDistribution(getDouble(t, v, "shape"),
						getDouble(t, v, "scale"));
			case INVERSE_GAMMA_DISTRIBUTION:
				return new InverseGammaDistribution(getDouble(t, v, "shape"),
						getDouble(t, v, "scale"));
			case LAPLACE_DISTRIBUTION:
				return new LaplaceDistribution(getDouble(t, v, "location"),
						getDouble(t, v, "scale"));
			case LOGISTIC_DISTRIBUTION:
				return new LogisticDistribution(getDouble(t, v, "location"),
						getDouble(t, v, "scale"));
			case LOG_NORMAL_DISTRIBUTION:
				return new LogNormalDistribution(getDouble(t, v, "logScale"),
						getDouble(t, v, "shape"));
			case NORMAL_DISTRIBUTION:
				return new NormalDistribution(getDouble(t, v, "mean"),
						getDouble(t, v, "variance"));
			case NORMAL_INVERSE_GAMMA_DISTRIBUTION:
				return new NormalInverseGammaDistribution(getDouble(t, v,
						"mean"), getDouble(t, v, "varianceScaling"), getDouble(
						t, v, "shape"), getDouble(t, v, "scale"));
			case PARETO_DISTRIBUTION:
				return new ParetoDistribution(getDouble(t, v, "scale"),
						getDouble(t, v, "shape"));
			case POISSON_DISTRIBUTION:
				return new PoissonDistribution(getIntegr(t, v, "rate"));
			case STUDENT_T_DISTRIBUTION:
				return new StudentTDistribution(getDouble(t, v, "mean"),
						getDouble(t, v, "variance"), getIntegr(t, v,
								"degreesOfFreedom"));
			case UNIFORM_DISTRIBUTION:
				return new UniformDistribution(getDouble(t, v, "minimum"),
						getDouble(t, v, "maximum"));
			case WEIBULL_DISTRIBUTION:
				return new WeibullDistribution(getDouble(t, v, "scale"),
						getDouble(t, v, "shape"));
			case CENTRED_MOMENT:
				return new CentredMoment(getIntegr(t, v, "order"), getDouble(t,
						v, "value"));
			case INTERQUATILE_RANGE:
				return new InterquartileRange(getDouble(t, v, "lower"),
						getDouble(t, v, "upper"));
			case MOMENT:
				return new Moment(getIntegr(t, v, "order"), getDouble(t, v,
						"value"));
			case DECILE:
				return new Decile(getIntegr(t, v, "level"), getDouble(t, v,
						"value"));
			case PERCENTILE:
				return new Percentile(getIntegr(t, v, "level"), getDouble(t, v,
						"value"));
			case QUANTILE:
				return new Quantile(getIntegr(t, v, "level"), getDouble(t, v,
						"value"));
			case QUARTILE:
				return new Quartile(getIntegr(t, v, "level"), getDouble(t, v,
						"value"));
			case RANGE:
				return new Range(getDouble(t, v, "lower"), getDouble(t, v,
						"upper"));
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
				// TODO case CONFIDENCE_INTERVAL:
				// TODO case CONFUSION_MATRIX:
				// TODO case COVARIANCE_MATRIX:
				// TODO case CREDIBLE_INTERVAL:
				// TODO case PROBABILITY:
				// TODO case MIXTURE_MODEL_DISTRIBUTION:
				// TODO case STATISTIC_COLLECTION:
				// TODO case RANDOM_SAMPLE:
				// TODO case REALISATION:
				// TODO case SYSTEMATIC_SAMPLE:
				// TODO case UNKNOWN_SAMPLE:
			default:
				return null;
			}
		}

		protected static final Number getNumber(UncertaintyType t,
				Map<URI, Number> v) {
			return v.get(t.getURI());
		}

		protected static final Number getNumber(UncertaintyType t,
				Map<URI, Number> v, String n) {
			return v.get(t.getParamURI(n));
		}

		protected static final double getDouble(UncertaintyType t,
				Map<URI, Number> v) {
			return getNumber(t, v).doubleValue();
		}

		protected static final double getDouble(UncertaintyType t,
				Map<URI, Number> v, String name) {
			return getNumber(t, v, name).doubleValue();
		}

		protected static final int getIntegr(UncertaintyType t,
				Map<URI, Number> v) {
			return getNumber(t, v).intValue();
		}

		protected static final int getIntegr(UncertaintyType t,
				Map<URI, Number> v, String name) {
			return getNumber(t, v, name).intValue();
		}
	}

	private static final Logger log = LoggerFactory
			.getLogger(UncertaintyNetCDF.class);
	private static final String MISSING_VALUE_ATTRIBUTE = "missing_value";
	private static final String PRIMARY_VARIABLES_ATTRIBUTE = "primary_variables";
	private static final String UNITS_ATTRIBUTE = "units";
	private static final String REF_ATTRIBUTE = "ref";
	private static final String ANCIALLARY_VARIABLES_ATTRIBUTE = "ancillary_variables";
	private static final String CONVENTIONS_ATTRIBUTE = "Conventions";
	private static final String UW_CONVENTION = "UW-1.0";
	private static final CoordinateReferenceSystem EPSG4326;

	static {
		try {
			EPSG4326 = CRS.getAuthorityFactory(true)
					.createCoordinateReferenceSystem("EPSG:4326");
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	protected static boolean checkForUWConvention(NetcdfFile f) {
		Attribute a = f.findGlobalAttribute(CONVENTIONS_ATTRIBUTE);
		if (a == null)
			return false;
		for (String s : a.getStringValue().split(" "))
			if (s.equals(UW_CONVENTION))
				return true;
		return false;
	}

	private NetcdfFile netCDF;
	private Variable primaryVariable;

	public Set<Variable> getAnciallaryVariables(Variable v) {
		Set<Variable> vars = UwCollectionUtils.set();
		Attribute a = v.findAttribute(ANCIALLARY_VARIABLES_ATTRIBUTE);
		if (a != null) {
			for (String name : a.getStringValue().split(" ")) {
				Variable av = getNotNullVariable(name);
				vars.add(av);
			}
		}
		return vars;
	}

	public URI getURI(Variable v) {
		Attribute ref = v.findAttribute(REF_ATTRIBUTE);
		if (ref != null)
			return URI.create(ref.getStringValue());
		return null;
	}

	public UncertaintyType getType(Variable v) {
		return UncertaintyType.fromURI(getURI(v));
	}

	public NetcdfFile getNetCDF() {
		return this.netCDF;
	}

	public URI getPrimaryURI() {
		return getURI(getPrimaryVariable());
	}

	public Variable getPrimaryVariable() {
		if (this.primaryVariable == null) {
			Attribute a = getNetCDF().findGlobalAttribute(
					PRIMARY_VARIABLES_ATTRIBUTE);
			if (a.getLength() == 1) {
				this.primaryVariable = getNotNullVariable(a.getStringValue(0));
			} else {
				throw VissError
						.internal("Only a single primary value is currently supported");
			}
		}
		return this.primaryVariable;
	}

	public UncertaintyNetCDF(NetcdfFile f) {
		Validate.notNull(f);
		if (!checkForUWConvention(f))
			throw VissError.internal("NetCDF file is not " + UW_CONVENTION
					+ " compliant.");
		this.netCDF = f;
	}

	@Override
	public Iterator<Value> iterator() {
		try {
			return new NetCDFIterator();
		} catch (IOException e) {
			throw VissError.internal(e);
		}
	}

	public Variable getLongitude() {
		return getNotNullVariable("lon");
	}

	public Variable getLatitude() {
		return getNotNullVariable("lat");
	}

	public Envelope getEnvelope() {
		Variable lon = getLongitude();
		Variable lat = getLatitude();
		try {
			// X=LON,Y=LAT!!!
			int lonSize = lon.getShape()[0];
			int latSize = lat.getShape()[0];
			double lonMin = lon.read(
					UwCollectionUtils.list(new ucar.ma2.Range(0, 0)))
					.getDouble(0);
			double lonMax = lon.read(
					UwCollectionUtils.list(new ucar.ma2.Range(lonSize - 1,
							lonSize - 1))).getDouble(0);
			double latMin = lat.read(
					UwCollectionUtils.list(new ucar.ma2.Range(0, 0)))
					.getDouble(0);
			double latMax = lat.read(
					UwCollectionUtils.list(new ucar.ma2.Range(latSize - 1,
							latSize - 1))).getDouble(0);
			return new Envelope2D(EPSG4326, lonMin, latMin, lonMax - lonMin,
					latMax - latMin);
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	public WriteableGridCoverage getCoverage(String layerName) {
		return getCoverage(layerName, getPrimaryVariable(), null);
	}

	public WriteableGridCoverage getCoverage(String layerName, String uom) {
		return getCoverage(layerName, getPrimaryVariable(), uom);
	}

	protected WriteableGridCoverage getCoverage(String layerName, Variable v,
			String unit) {
		Attribute a = v.findAttribute(ANCIALLARY_VARIABLES_ATTRIBUTE);
		if (a == null)
			throw VissError
					.internal("Can not determine shape of variable: no \""
							+ ANCIALLARY_VARIABLES_ATTRIBUTE + "\" attribute.");

		int missingValue = -999;
		for (String s : a.getStringValue().split(" ")) {
			Attribute mv = getNotNullVariable(s).findAttribute(
					MISSING_VALUE_ATTRIBUTE);
			if (mv != null) {
				missingValue = mv.getNumericValue().intValue();
				break;
			}
		}
		int latSize = getLatitude().getShape()[0];
		int lonSize = getLongitude().getShape()[0];
		final GridCoverageBuilder b = new GridCoverageBuilder();
		b.setCoordinateReferenceSystem(EPSG4326);
		log.debug("ImageSize: {}x{}", lonSize, latSize);
		// FIXME this removes the cross, but thats somewhat ugly
		b.setImageSize(lonSize - 1, latSize - 1);
		b.setEnvelope(getEnvelope());
		GridCoverageBuilder.Variable var;
		if (unit == null) {
			var = b.newVariable(layerName, getUnit());
		} else {
			var = b.newVariable(layerName, getUnit(unit));
		}
		var.setLinearTransform(1, 0);
		log.info("MissingValue: {}", missingValue);
		var.addNodataValue("UNKNOWN", missingValue);
		return new WriteableGridCoverage(b.getGridCoverage2D());
	}

	public String getUnitAsString() {
		Attribute a = getPrimaryVariable().findAttribute(UNITS_ATTRIBUTE);
		if (a != null) {
			return a.getStringValue();
		} else {
			return "";
		}
	}

	public Unit<? extends Quantity> getUnit() {
		return getUnit(getUnitAsString());
	}

	protected Unit<? extends Quantity> getUnit(String uom) {
		if (uom != null) {
			try {
				return Unit.valueOf(uom);
			} catch (IllegalArgumentException e) {
				return Unit.ONE;
			}
		} else {
			return Unit.ONE;
		}
	}

	protected Variable getNotNullVariable(String name) {
		Variable v = getNetCDF().findVariable(name);
		if (v == null) {
			throw VissError.internal("Variable with name '" + name
					+ "' could not be found.");
		}
		return v;
	}

	public Integer getMissingValue(Variable v) {
		Attribute a = v.findAttribute(MISSING_VALUE_ATTRIBUTE);
		if (a == null) {
			return Integer.MIN_VALUE;
		} else {
			return a.getNumericValue().intValue();
		}
	}
}
