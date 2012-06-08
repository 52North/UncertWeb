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

import static org.uncertweb.utils.UwCollectionUtils.toDoubleArray;

import java.net.URI;
import java.util.List;

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
import org.uncertml.statistic.ConstraintType;
import org.uncertml.statistic.Correlation;
import org.uncertml.statistic.Decile;
import org.uncertml.statistic.IStatistic;
import org.uncertml.statistic.InterquartileRange;
import org.uncertml.statistic.Kurtosis;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Median;
import org.uncertml.statistic.Mode;
import org.uncertml.statistic.Moment;
import org.uncertml.statistic.Percentile;
import org.uncertml.statistic.Probability;
import org.uncertml.statistic.ProbabilityConstraint;
import org.uncertml.statistic.Quantile;
import org.uncertml.statistic.Quartile;
import org.uncertml.statistic.Range;
import org.uncertml.statistic.Skewness;
import org.uncertml.statistic.StandardDeviation;
import org.uncertml.statistic.StatisticCollection;
import org.uncertweb.netcdf.NcUwConstants.Fragments;
import org.uncertweb.utils.MultivaluedMap;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.UriBasedUncertaintyParser;

public class NcUwUriParser {
	protected static final Logger log = LoggerFactory
			.getLogger(UriBasedUncertaintyParser.class);

//	private final JSONEncoder enc = new JSONEncoder();
	private final UncertaintyType type;
	private final MultivaluedMap<URI, Object> values;
	
	public NcUwUriParser(final UncertaintyType type, final MultivaluedMap<URI, Object> v) {
		this.type = type;
		this.values = v;
	}

	public MultivaluedMap<URI, Object> getValues() {
		return this.values;
	}

	public UncertaintyType getType() {
		return this.type;
	}
	
	public IUncertainty parse() {
//		if (log.isDebugEnabled()) {
//			StringBuilder sb = new StringBuilder();
//			for (Entry<URI, List<Object>> e : getValues().entrySet()) {
//				sb.append(e.getKey()).append(":\n");
//				StringBuilder line = new StringBuilder(90).append("\t");
//				for (Object o : e.getValue()) {
//					line.append(o).append(", ");
//					if (line.length()>=80) {
//						sb.append(line.append("\n"));
//						line = new StringBuilder("\t");
//					}
//				}
//				if (line.length() > 1) 
//					sb.append(line);
//				
//			}
//			log.debug("Parsing map:\n{}", sb);
//		}
		IUncertainty u = _parse();
//		if (log.isDebugEnabled()) {
//			log.debug("Parsed Uncertainty: {}", enc.encode(u));
//		}
		return u;
	}

	private IUncertainty _parse() {
		switch (getType()) {
		case BETA_DISTRIBUTION:
			return parseBetaDistribution();
		case CAUCHY_DISTRIBUTION:
			return parseCauchyDistribution();
		case CHI_SQUARE_DISTRIBUTION:
			return parseChiSquareDistribution();
		case EXPONENTIAL_DISTRIBUTION:
			return parseExponentialDistribution();
		case F_DISTRIBUTION:
			return parseFDistribution();
		case GAMMA_DISTRIBUTION:
			return parseGammaDistribution();
		case INVERSE_GAMMA_DISTRIBUTION:
			return parseInverseGammaDistribution();
		case LAPLACE_DISTRIBUTION:
			return parseLaplaceDistribution();
		case LOGISTIC_DISTRIBUTION:
			return parseLogisticDistribution();
		case LOG_NORMAL_DISTRIBUTION:
			return parseLogNormalDistribution();
		case NORMAL_DISTRIBUTION:
			return parseNormalDistribution();
		case NORMAL_INVERSE_GAMMA_DISTRIBUTION:
			return parseNormalInverseGammaDistribution();
		case PARETO_DISTRIBUTION:
			return parseParetoDistribution();
		case POISSON_DISTRIBUTION:
			return parsePoissonDistribution();
		case STUDENT_T_DISTRIBUTION:
			return parseStudentTDistribution();
		case UNIFORM_DISTRIBUTION:
			return parseUniformDistribution();
		case WEIBULL_DISTRIBUTION:
			return parseWeibullDistribution();
		case CENTRED_MOMENT:
			return parseCentredMoment();
		case INTERQUATILE_RANGE:
			return parseInterquatileRange();
		case MOMENT:
			return parseMoment();
		case DECILE:
			return parseDecile();
		case PERCENTILE:
			return parsePercentile();
		case QUANTILE:
			return parseQuantile();
		case QUARTILE:
			return parseQuartile();
		case RANGE:
			return parseRange();
		case STANDARD_DEVIATION:
			return parseStandardDeviation();
		case COEFFICIENT_OF_VARIATION:
			return parseCoefficientOfVariation();
		case CORRELATION:
			return parseCorrelation();
		case KURTOSIS:
			return parseKurtosis();
		case MEAN:
			return parseMean();
		case MEDIAN:
			return parseMedian();
		case MODE:
			return parseMode();
		case SKEWNESS:
			return parseSkewness();
		case CONTINUOUS_REALISATION:
			return parseContinuousRealisation();
		case RANDOM_SAMPLE:
			return parseRandomSample();
		case SYSTEMATIC_SAMPLE:
			return parseSystematicSample();
		case UNKNOWN_SAMPLE:
			return parseUnknownSample();
		case PROBABILITY:
			return parseProbability();
		case STATISTIC_COLLECTION:
			return parseStatisticCollection();
			// TODO case CATEGORICAL_REALISATION
			// TODO case CONFIDENCE_INTERVAL:
			// TODO case CONFUSION_MATRIX:
			// TODO case COVARIANCE_MATRIX:
			// TODO case CREDIBLE_INTERVAL:
			// TODO case MIXTURE_MODEL_DISTRIBUTION:
		default:
			throw new UnsupportedOperationException(getType()
					+ " is not yet implemented.");
		}
	}

	public BetaDistribution parseBetaDistribution() {
		return new BetaDistribution(getDouble(Fragments.ALPHA), getDouble(Fragments.BETA));
	}

	public CauchyDistribution parseCauchyDistribution() {
		return new CauchyDistribution(getDouble(Fragments.LOCATION), getDouble(Fragments.SCALE));
	}

	public ChiSquareDistribution parseChiSquareDistribution() {
		return new ChiSquareDistribution(getInteger(Fragments.DEGREES_OF_FREEDOM));
	}

	public ExponentialDistribution parseExponentialDistribution() {
		return new ExponentialDistribution(getInteger(Fragments.RATE));
	}

	public FDistribution parseFDistribution() {
		return new FDistribution(getInteger(Fragments.DENOMINATOR), getInteger(Fragments.NUMERATOR));
	}

	public GammaDistribution parseGammaDistribution() {
		return new GammaDistribution(getDouble(Fragments.SHAPE), getDouble(Fragments.SCALE));
	}

	public InverseGammaDistribution parseInverseGammaDistribution() {
		return new InverseGammaDistribution(getDouble(Fragments.SHAPE), getDouble(Fragments.SCALE));
	}

	public LaplaceDistribution parseLaplaceDistribution() {
		return new LaplaceDistribution(getDouble(Fragments.LOCATION), getDouble(Fragments.SCALE));
	}

	public LogisticDistribution parseLogisticDistribution() {
		return new LogisticDistribution(getDouble(Fragments.LOCATION), getDouble(Fragments.SCALE));
	}

	public LogNormalDistribution parseLogNormalDistribution() {
		return new LogNormalDistribution(getDouble(Fragments.LOG_SCALE), getDouble(Fragments.SHAPE));
	}

	public NormalDistribution parseNormalDistribution() {
		return new NormalDistribution(getDouble(Fragments.MEAN), getDouble(Fragments.VARIANCE));
	}

	public NormalInverseGammaDistribution parseNormalInverseGammaDistribution() {
		return new NormalInverseGammaDistribution(getDouble(Fragments.MEAN),
				getDouble(Fragments.VARIANCE_SCALING), getDouble(Fragments.SHAPE), getDouble(Fragments.SCALE));
	}

	public ParetoDistribution parseParetoDistribution() {
		return new ParetoDistribution(getDouble(Fragments.SCALE), getDouble(Fragments.SHAPE));
	}

	public PoissonDistribution parsePoissonDistribution() {
		return new PoissonDistribution(getInteger(Fragments.RATE));
	}

	public StudentTDistribution parseStudentTDistribution() {
		return new StudentTDistribution(getDouble(Fragments.MEAN), getDouble(Fragments.VARIANCE),
				getInteger(Fragments.DEGREES_OF_FREEDOM));
	}

	public UniformDistribution parseUniformDistribution() {
		return new UniformDistribution(getDouble(Fragments.MINIMUM), getDouble(Fragments.MAXIMUM));
	}

	public WeibullDistribution parseWeibullDistribution() {
		return new WeibullDistribution(getDouble(Fragments.SCALE), getDouble(Fragments.SHAPE));
	}

	public CentredMoment parseCentredMoment() {
		return new CentredMoment(getInteger(Fragments.ORDER), getDouble(Fragments.VALUE));
	}

	public InterquartileRange parseInterquatileRange() {
		return new InterquartileRange(getDouble(Fragments.LOWER), getDouble(Fragments.UPPER));
	}

	public Moment parseMoment() {
		return new Moment(getInteger(Fragments.ORDER), getDouble(Fragments.VALUE));
	}

	public Decile parseDecile() {
		return new Decile(getInteger(Fragments.LEVEL), getDouble(Fragments.VALUE));
	}

	public Percentile parsePercentile() {
		return new Percentile(getInteger(Fragments.LEVEL), getDouble(Fragments.VALUE));
	}

	public Quantile parseQuantile() {
		return new Quantile(getInteger(Fragments.LEVEL), getDouble(Fragments.VALUE));
	}

	public Quartile parseQuartile() {
		return new Quartile(getInteger(Fragments.LEVEL), getDouble(Fragments.VALUE));
	}

	public Range parseRange() {
		return new Range(getDouble(Fragments.LOWER), getDouble(Fragments.UPPER));
	}

	public StandardDeviation parseStandardDeviation() {
		return new StandardDeviation(getDouble());
	}

	public CoefficientOfVariation parseCoefficientOfVariation() {
		return new CoefficientOfVariation(getDouble());
	}

	public Correlation parseCorrelation() {
		return new Correlation(getDouble());
	}

	public Kurtosis parseKurtosis() {
		return new Kurtosis(getDouble());
	}

	public Mean parseMean() {
		return new Mean(getDouble());
	}

	public Median parseMedian() {
		return new Median(getDouble());
	}

	public Mode parseMode() {
		return new Mode(getDouble());
	}

	public Skewness parseSkewness() {
		return new Skewness(getDouble());
	}

	public ContinuousRealisation parseContinuousRealisation() {
		return new ContinuousRealisation(toDoubleArray(getNumberArray()));
	}

	public RandomSample parseRandomSample() {
		return new RandomSample(getRealisationList());
	}

	public SystematicSample parseSystematicSample() {
		return new SystematicSample(getRealisationList());
	}

	public UnknownSample parseUnknownSample() {
		return new UnknownSample(getRealisationList());
	}

	public Probability parseProbability() {

		final List<ProbabilityConstraint> constraints = UwCollectionUtils.list();

		final List<Object> gt = getValues().get(UncertaintyType.GREATER_THAN_URI);
		if (gt != null && gt.size() != 0) {
			constraints.add(new ProbabilityConstraint(
					ConstraintType.GREATER_THAN, ((Number) gt.get(0))
					.doubleValue()));
		}
		final List<Object> lt = getValues().get(UncertaintyType.LESS_THAN_URI);
		if (lt != null && lt.size() != 0) {
			constraints.add(new ProbabilityConstraint(ConstraintType.LESS_THAN,
					((Number) lt.get(0)).doubleValue()));
		}
		final List<Object> ge = getValues().get(UncertaintyType.GREATER_OR_EQUAL_URI);
		if (ge != null && ge.size() != 0) {
			constraints.add(new ProbabilityConstraint(
					ConstraintType.GREATER_OR_EQUAL, ((Number) ge.get(0))
					.doubleValue()));
		}
		final List<Object> le = getValues().get(UncertaintyType.LESS_OR_EQUAL_URI);
		if (le != null && le.size() != 0) {
			constraints.add(new ProbabilityConstraint(
					ConstraintType.LESS_OR_EQUAL, ((Number) le.get(0))
					.doubleValue()));
		}

		if (constraints.isEmpty()) {
			throw new NcUwException("No constraint found");
		}

		return new Probability(constraints, getDouble());
	}
	
	public StatisticCollection parseStatisticCollection() {
		StatisticCollection col = new StatisticCollection();
		for (URI uri : getValues().keySet()) {
			UncertaintyType type = UncertaintyType.fromURI(uri);
			if (type != UncertaintyType.STATISTIC_COLLECTION
					&& type.getSuperType() == UncertaintyType.STATISTIC) {
				IStatistic u = (IStatistic) parse(uri, getValues());
				col.add(u);
			}
		}
		return col;
	}

	protected  final ContinuousRealisation[] getRealisationList() {
		Number[] n = getNumberArray();
		final ContinuousRealisation[] l = new ContinuousRealisation[n.length];
		for (int i = 0; i < n.length; i++) {
			l[i] = new ContinuousRealisation(new double[] { n[i].doubleValue() });
		}
		return l;
	}

	protected  final Number[] getNumberArray() {
		return getNumberArray(getType().getURI());
	}
	
	protected  final Number[] getNumberArray(URI uri) {
		return getValues().get(uri).toArray(new Number[0]);
	}

	protected  final Number getNumber() {
		return (Number) getValues().get(getType().getURI()).get(0);
	}

	protected final Number getNumber(final String n) {
		return (Number) getValues().get(getType().getParamURI(n)).get(0);
	}

	protected final double getDouble() {
		return getNumber().doubleValue();
	}

	protected final double getDouble(final String name) {
		return getNumber(name).doubleValue();
	}

	protected final int getInteger(final String name) {
		return getNumber(name).intValue();
	}

	public static IUncertainty parse(final URI main, final MultivaluedMap<URI, Object> v) {
		return parse(UncertaintyType.fromURI(main), v);
	}

	public static IUncertainty parse(final UncertaintyType t, final MultivaluedMap<URI, Object> v) {
		return new NcUwUriParser(t, v).parse();
	}
}