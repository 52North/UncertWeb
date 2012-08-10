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

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.distribution.IDistribution;
import org.uncertml.distribution.categorical.CategoricalUniformDistribution;
import org.uncertml.distribution.categorical.ICategoricalDistribution;
import org.uncertml.distribution.continuous.BetaDistribution;
import org.uncertml.distribution.continuous.CauchyDistribution;
import org.uncertml.distribution.continuous.ChiSquareDistribution;
import org.uncertml.distribution.continuous.ExponentialDistribution;
import org.uncertml.distribution.continuous.FDistribution;
import org.uncertml.distribution.continuous.GammaDistribution;
import org.uncertml.distribution.continuous.IContinuousDistribution;
import org.uncertml.distribution.continuous.InverseGammaDistribution;
import org.uncertml.distribution.continuous.LaplaceDistribution;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.LogisticDistribution;
import org.uncertml.distribution.continuous.MixtureModel;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.continuous.NormalInverseGammaDistribution;
import org.uncertml.distribution.continuous.ParetoDistribution;
import org.uncertml.distribution.continuous.PoissonDistribution;
import org.uncertml.distribution.continuous.StudentTDistribution;
import org.uncertml.distribution.continuous.UniformDistribution;
import org.uncertml.distribution.continuous.WeibullDistribution;
import org.uncertml.distribution.discrete.BernoulliDistribution;
import org.uncertml.distribution.discrete.BinomialDistribution;
import org.uncertml.distribution.discrete.DiscreteUniformDistribution;
import org.uncertml.distribution.discrete.GeometricDistribution;
import org.uncertml.distribution.discrete.HypergeometricDistribution;
import org.uncertml.distribution.discrete.IDiscreteDistribution;
import org.uncertml.distribution.discrete.NegativeBinomialDistribution;
import org.uncertml.distribution.multivariate.DirichletDistribution;
import org.uncertml.distribution.multivariate.IMultivariateDistribution;
import org.uncertml.distribution.multivariate.MultinomialDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateStudentTDistribution;
import org.uncertml.distribution.multivariate.WishartDistribution;
import org.uncertml.sample.CategoricalRealisation;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.ISample;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.SystematicSample;
import org.uncertml.sample.UnknownSample;
import org.uncertml.statistic.CategoricalMode;
import org.uncertml.statistic.CentredMoment;
import org.uncertml.statistic.CoefficientOfVariation;
import org.uncertml.statistic.ConfidenceInterval;
import org.uncertml.statistic.ConfusionMatrix;
import org.uncertml.statistic.ConstraintType;
import org.uncertml.statistic.Correlation;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertml.statistic.CredibleInterval;
import org.uncertml.statistic.Decile;
import org.uncertml.statistic.DiscreteProbability;
import org.uncertml.statistic.IStatistic;
import org.uncertml.statistic.InterquartileRange;
import org.uncertml.statistic.Kurtosis;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Median;
import org.uncertml.statistic.Mode;
import org.uncertml.statistic.Moment;
import org.uncertml.statistic.Percentile;
import org.uncertml.statistic.Probability;
import org.uncertml.statistic.Quantile;
import org.uncertml.statistic.Quartile;
import org.uncertml.statistic.Range;
import org.uncertml.statistic.Skewness;
import org.uncertml.statistic.StandardDeviation;
import org.uncertml.statistic.StatisticCollection;
import org.uncertml.statistic.Variance;
import org.uncertweb.utils.UwCollectionUtils;

public enum NcUwUncertaintyType {
	UNCERTAINTY(IUncertainty.class), 
	SAMPLE(ISample.class, UNCERTAINTY),
	DISTRIBUTION(IDistribution.class,UNCERTAINTY), 
	STATISTIC(IStatistic.class, UNCERTAINTY),
	
	CONTINUOUS_REALISATION(ContinuousRealisation.class, SAMPLE, URI.create("http://www.uncertml.org/samples/realisation")),
	CATEGORICAL_REALISATION(CategoricalRealisation.class, SAMPLE),
	
	CONTINUOUS_DISTRIBUTION(IContinuousDistribution.class, DISTRIBUTION),
	DISCRETE_DISTRIBUTION(IDiscreteDistribution.class, DISTRIBUTION),
	CATEGORICAL_DISTRIBUTION(ICategoricalDistribution.class, DISTRIBUTION),
	MULTIVARIATE_DISTRIBUTION(IMultivariateDistribution.class, DISTRIBUTION),
	
	BETA_DISTRIBUTION(BetaDistribution.class, CONTINUOUS_DISTRIBUTION),
	CAUCHY_DISTRIBUTION(CauchyDistribution.class, CONTINUOUS_DISTRIBUTION),
	CHI_SQUARE_DISTRIBUTION(ChiSquareDistribution.class, CONTINUOUS_DISTRIBUTION),
	EXPONENTIAL_DISTRIBUTION(ExponentialDistribution.class, CONTINUOUS_DISTRIBUTION),
	F_DISTRIBUTION(FDistribution.class, CONTINUOUS_DISTRIBUTION),
	GAMMA_DISTRIBUTION(GammaDistribution.class, CONTINUOUS_DISTRIBUTION),
	INVERSE_GAMMA_DISTRIBUTION(InverseGammaDistribution.class, CONTINUOUS_DISTRIBUTION),
	LAPLACE_DISTRIBUTION(LaplaceDistribution.class, CONTINUOUS_DISTRIBUTION),
	LOGISTIC_DISTRIBUTION(LogisticDistribution.class, CONTINUOUS_DISTRIBUTION),
	LOG_NORMAL_DISTRIBUTION(LogNormalDistribution.class, CONTINUOUS_DISTRIBUTION),
	MIXTURE_MODEL_DISTRIBUTION(MixtureModel.class, CONTINUOUS_DISTRIBUTION),
	NORMAL_DISTRIBUTION(NormalDistribution.class, CONTINUOUS_DISTRIBUTION),
	NORMAL_INVERSE_GAMMA_DISTRIBUTION(NormalInverseGammaDistribution.class, CONTINUOUS_DISTRIBUTION),
	PARETO_DISTRIBUTION(ParetoDistribution.class, CONTINUOUS_DISTRIBUTION),
	POISSON_DISTRIBUTION(PoissonDistribution.class, CONTINUOUS_DISTRIBUTION),
	STUDENT_T_DISTRIBUTION(StudentTDistribution.class, CONTINUOUS_DISTRIBUTION),
	UNIFORM_DISTRIBUTION(UniformDistribution.class, CONTINUOUS_DISTRIBUTION),
	WEIBULL_DISTRIBUTION(WeibullDistribution.class, CONTINUOUS_DISTRIBUTION),
	
	BERNOULLI_DISTRIBUTION(BernoulliDistribution.class, DISCRETE_DISTRIBUTION),
	BINOMINAL_DISTRIBUTION(BinomialDistribution.class, DISCRETE_DISTRIBUTION),
	
	DISCRETE_UNIFORM_DISTRIBUTION(DiscreteUniformDistribution.class, DISCRETE_DISTRIBUTION),
	GEOMETRIC_DISTRIBUTION(GeometricDistribution.class, DISCRETE_DISTRIBUTION),
	HYPERGEOMETRIC_DISTRIBUTION(HypergeometricDistribution.class, DISCRETE_DISTRIBUTION),
	NEGATIVE_BINOMINAL_DISTRIBUTION(NegativeBinomialDistribution.class, DISCRETE_DISTRIBUTION),
	
	CATEGORICAL_UNIFORM_DISTRIBUTION(CategoricalUniformDistribution.class, CATEGORICAL_DISTRIBUTION),
	
	DIRICHLET_DISTRIBUTION(DirichletDistribution.class, MULTIVARIATE_DISTRIBUTION),
	MULTINOMINAL_DISTRIBUTION(MultinomialDistribution.class, MULTIVARIATE_DISTRIBUTION),
	MULTIVARIATE_NORMAL_DISTRIBUTION(MultivariateNormalDistribution.class, MULTIVARIATE_DISTRIBUTION),
	MULTIVARIATE_STUDENT_T_DISTRIBUTION(MultivariateStudentTDistribution.class, MULTIVARIATE_DISTRIBUTION),
	WISHART_DISTRIBUTION(WishartDistribution.class, MULTIVARIATE_DISTRIBUTION),
	
	RANDOM_SAMPLE(RandomSample.class, SAMPLE),
	SYSTEMATIC_SAMPLE(SystematicSample.class, SAMPLE),
	UNKNOWN_SAMPLE(UnknownSample.class, SAMPLE),
	
	CATEGORICAL_MODE(CategoricalMode.class, STATISTIC),
	CENTRED_MOMENT(CentredMoment.class, STATISTIC),
	COEFFICIENT_OF_VARIATION(CoefficientOfVariation.class, STATISTIC),
	CONFIDENCE_INTERVAL(ConfidenceInterval.class, STATISTIC),
	CONFUSION_MATRIX(ConfusionMatrix.class, STATISTIC),
	CORRELATION(Correlation.class, STATISTIC),
	COVARIANCE_MATRIX(CovarianceMatrix.class, STATISTIC),
	CREDIBLE_INTERVAL(CredibleInterval.class, STATISTIC),
	DECILE(Decile.class, STATISTIC),
	DISCRETE_PROBABILITY(DiscreteProbability.class, STATISTIC),
	INTERQUATILE_RANGE(InterquartileRange.class, STATISTIC),
	KURTOSIS(Kurtosis.class, STATISTIC),
	MEAN(Mean.class, STATISTIC),
	MEDIAN(Median.class, STATISTIC),
	CONTINOUS_MODE(Mode.class, STATISTIC),
	MOMENT(Moment.class, STATISTIC),
	PERCENTILE(Percentile.class, STATISTIC),
	PROBABILITY(Probability.class, STATISTIC),
	QUANTILE(Quantile.class, STATISTIC),
	QUARTILE(Quartile.class, STATISTIC),
	RANGE(Range.class, STATISTIC),
	SKEWNESS(Skewness.class, STATISTIC),
	STANDARD_DEVIATION(StandardDeviation.class, STATISTIC),
	VARIANCE(Variance.class, STATISTIC),
	
	STATISTIC_COLLECTION(StatisticCollection.class, STATISTIC);
	

	private static final String CONSTRAINT_URI = UncertML.getURI(ConstraintType.class);
	private static final URI GREATER_THAN_URI = URI.create(CONSTRAINT_URI + "/greater-than");
	private static final URI LESS_THAN_URI = URI.create(CONSTRAINT_URI + "/less-than");
	private static final URI GREATER_OR_EQUAL_URI = URI.create(CONSTRAINT_URI+ "/greater-or-equal");
	private static final URI LESS_OR_EQUAL_URI = URI.create(CONSTRAINT_URI + "/less-or-equal");
	
	private final Set<URI> alias;
	private final Class<? extends IUncertainty> clazz;
	private final NcUwUncertaintyType type;
	private final URI uri;
	
	private NcUwUncertaintyType(Class<? extends IUncertainty> clazz, URI... uris) {
		this(clazz, null, uris);
	}
	
	private NcUwUncertaintyType(Class<? extends IUncertainty> clazz, NcUwUncertaintyType type, URI... uris) {
		this.uri = getUri(clazz);
		this.clazz = clazz;
		this.type = type;
		this.alias = Collections.unmodifiableSet(uris == null ? 
				UwCollectionUtils.<URI>set() : UwCollectionUtils.asSet(uris));
	}

	public URI getParamURI(String name) {
		return URI.create(this.getUri().toString() + "#" + name);
	}

	public URI getUri() {
		return this.uri;
	}
	
	public Set<URI> getAlias(){
		return this.alias;
	}

	public NcUwUncertaintyType getSuperType() {
		return this.type;
	}
	
	public boolean isUri(URI uri) {
		return getUri().equals(uri) || getAlias().contains(uri);
	}
	public boolean isClass(Class<? extends IUncertainty> c) {
		return getImplementationClass().equals(c);
	}
	
	public boolean isDistribtution() {
		if (getSuperType() != null && getSuperType() != UNCERTAINTY) {
			return getSuperType().isDistribtution();
		}
		return this == DISTRIBUTION;
	}
	
	public boolean isSample() {
		if (getSuperType() != null && getSuperType() != UNCERTAINTY) {
			return getSuperType().isSample();
		}
		return this == SAMPLE;
	}
	
	public boolean isStatistic() {
		if (getSuperType() != null && getSuperType() != UNCERTAINTY) {
			return getSuperType().isStatistic();
		} else return this == STATISTIC;
	}

	public Class<? extends IUncertainty> getImplementationClass() {
		return this.clazz;
	}
	
	public static URI getUri(Class<? extends IUncertainty> u) {
		String spec = UncertML.getURI(u);
		if (spec == null) {
			throw new IllegalArgumentException(u + " can not be used to generate a URI");
		}
		return URI.create(spec);
	}
	
	public static NcUwUncertaintyType fromUri(URI uri) {
		for (NcUwUncertaintyType s : values())
			if (s.isUri(uri)) return s;
		return null;
	}
	
	public static NcUwUncertaintyType fromClass(Class<? extends IUncertainty> c) {
		for (NcUwUncertaintyType s : values())
			if (s.isClass(c)) return s;
		return null;
	}
	
	public static URI getURIforConstraint(ConstraintType ct) {
		switch (ct) {
		case GREATER_OR_EQUAL: 	return GREATER_OR_EQUAL_URI;
		case GREATER_THAN:		return GREATER_THAN_URI;
		case LESS_OR_EQUAL:		return LESS_OR_EQUAL_URI;
		case LESS_THAN:			return LESS_THAN_URI;
		default:
			throw new UnsupportedOperationException(ct + " is not yet supported.");
		}
	}
}
