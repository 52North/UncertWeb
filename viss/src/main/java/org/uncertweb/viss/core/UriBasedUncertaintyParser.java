package org.uncertweb.viss.core;

import static org.uncertweb.utils.UwCollectionUtils.toDoubleArray;
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

import java.net.URI;
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