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
package org.uncertweb.viss.core;

import static org.uncertweb.netcdf.NcUwConstants.Fragments.*;
import static org.uncertweb.utils.UwCollectionUtils.toDoubleArray;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
import org.uncertweb.utils.UwCollectionUtils;

public class UriBasedUncertaintyParser {
	private static final Logger log = LoggerFactory.getLogger(UriBasedUncertaintyParser.class);

		public static IUncertainty map(final URI main, final Map<URI, Number[]> v) {
			
//			if (log.isDebugEnabled()) {
//				log.debug("Parsing {}", main);
//				for (Entry<URI, Number[]> e : v.entrySet()) {
//					log.debug("{} => {}", e.getKey(), e.getValue() == null ? "null" : 
//						"["	+ UwStringUtils.join(", ", (Object[]) e.getValue()) + "]");
//				}
//			}
			
			final IUncertainty u = map(UncertaintyType.fromURI(main), v);
			if (u == null) {
				throw new IllegalArgumentException("not (yet) supported: "
						+ main);
			}
			return u;
		}

		protected static IUncertainty map(final UncertaintyType t, final Map<URI, Number[]> v) {
			switch (t) {
			case BETA_DISTRIBUTION:
				return new BetaDistribution(getDouble(t, v, ALPHA),
						getDouble(t, v, BETA));
			case CAUCHY_DISTRIBUTION:
				return new CauchyDistribution(getDouble(t, v,
						LOCATION), getDouble(t, v, SCALE));
			case CHI_SQUARE_DISTRIBUTION:
				return new ChiSquareDistribution(getIntegr(t, v,
						DEGREES_OF_FREEDOM));
			case EXPONENTIAL_DISTRIBUTION:
				return new ExponentialDistribution(getIntegr(t, v,
						RATE));
			case F_DISTRIBUTION:
				return new FDistribution(
						getIntegr(t, v, DENOMINATOR), getIntegr(t, v,
								NUMERATOR));
			case GAMMA_DISTRIBUTION:
				return new GammaDistribution(getDouble(t, v, SHAPE),
						getDouble(t, v, SCALE));
			case INVERSE_GAMMA_DISTRIBUTION:
				return new InverseGammaDistribution(getDouble(t, v,
						SHAPE), getDouble(t, v, SCALE));
			case LAPLACE_DISTRIBUTION:
				return new LaplaceDistribution(getDouble(t, v,
						LOCATION), getDouble(t, v, SCALE));
			case LOGISTIC_DISTRIBUTION:
				return new LogisticDistribution(getDouble(t, v,
						LOCATION), getDouble(t, v, SCALE));
			case LOG_NORMAL_DISTRIBUTION:
				return new LogNormalDistribution(getDouble(t, v,
						LOG_SCALE), getDouble(t, v, SHAPE));
			case NORMAL_DISTRIBUTION:
				return new NormalDistribution(getDouble(t, v, MEAN),
						getDouble(t, v, VARIANCE));
			case NORMAL_INVERSE_GAMMA_DISTRIBUTION:
				return new NormalInverseGammaDistribution(getDouble(t, v,
						MEAN), getDouble(t, v, "varianceScaling"),
						getDouble(t, v, SHAPE), getDouble(t, v,
								SCALE));
			case PARETO_DISTRIBUTION:
				return new ParetoDistribution(getDouble(t, v, SCALE),
						getDouble(t, v, SHAPE));
			case POISSON_DISTRIBUTION:
				return new PoissonDistribution(getIntegr(t, v, RATE));
			case STUDENT_T_DISTRIBUTION:
				return new StudentTDistribution(
						getDouble(t, v, MEAN), getDouble(t, v,
								VARIANCE), getIntegr(t, v,
								DEGREES_OF_FREEDOM));
			case UNIFORM_DISTRIBUTION:
				return new UniformDistribution(getDouble(t, v,
						MINIMUM), getDouble(t, v, MAXIMUM));
			case WEIBULL_DISTRIBUTION:
				return new WeibullDistribution(
						getDouble(t, v, SCALE), getDouble(t, v,
								SHAPE));
			case CENTRED_MOMENT:
				return new CentredMoment(getIntegr(t, v, ORDER),
						getDouble(t, v, VALUE));
			case INTERQUATILE_RANGE:
				return new InterquartileRange(getDouble(t, v, LOWER),
						getDouble(t, v, UPPER));
			case MOMENT:
				return new Moment(getIntegr(t, v, ORDER), getDouble(
						t, v, VALUE));
			case DECILE:
				return new Decile(getIntegr(t, v, LEVEL), getDouble(
						t, v, VALUE));
			case PERCENTILE:
				return new Percentile(getIntegr(t, v, LEVEL),
						getDouble(t, v, VALUE));
			case QUANTILE:
				return new Quantile(getIntegr(t, v, LEVEL),
						getDouble(t, v, VALUE));
			case QUARTILE:
				return new Quartile(getIntegr(t, v, LEVEL),
						getDouble(t, v, VALUE));
			case RANGE:
				return new Range(getDouble(t, v, LOWER), getDouble(t,
						v, UPPER));
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
			case PROBABILITY: 

				List<ProbabilityConstraint> constraints = UwCollectionUtils.list();
				
				Number[] gt = v.get(UncertaintyType.GREATER_THAN_URI);
				if (gt != null && gt.length != 0) {
					constraints.add(new ProbabilityConstraint(ConstraintType.GREATER_THAN, gt[0].doubleValue()));
				}
				Number[] lt = v.get(UncertaintyType.LESS_THAN_URI);
				if (lt != null && lt.length != 0) {
					constraints.add(new ProbabilityConstraint(ConstraintType.LESS_THAN, lt[0].doubleValue()));
				}
				Number[] ge = v.get(UncertaintyType.GREATER_OR_EQUAL_URI);
				if (ge != null && ge.length != 0) {
					constraints.add(new ProbabilityConstraint(ConstraintType.GREATER_OR_EQUAL, ge[0].doubleValue()));
				}
				Number[] le = v.get(UncertaintyType.LESS_OR_EQUAL_URI);
				if (le != null && le.length != 0) {
					constraints.add(new ProbabilityConstraint(ConstraintType.LESS_OR_EQUAL, le[0].doubleValue()));
				}
				
				if (constraints.isEmpty()) {
					throw VissError.internal("No constraint found");
				}
				
				return new Probability(constraints, getDouble(t, v));
				// TODO case CATEGORICAL_REALISATION
				// TODO case CONFIDENCE_INTERVAL:
				// TODO case CONFUSION_MATRIX:
				// TODO case COVARIANCE_MATRIX:
				// TODO case CREDIBLE_INTERVAL:
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
			if (v == null) {
				throw new NullPointerException("v");
			} 
			if (t == null)
				throw new NullPointerException("t");
			if (v.get(t.getURI()) == null) {
				throw new NullPointerException("v.get(t.getURI()");
			}
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