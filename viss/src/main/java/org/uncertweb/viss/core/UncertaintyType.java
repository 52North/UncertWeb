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

import java.net.URI;
import java.util.Set;

import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.distribution.IDistribution;
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
import org.uncertml.distribution.continuous.MixtureModel;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.continuous.NormalInverseGammaDistribution;
import org.uncertml.distribution.continuous.ParetoDistribution;
import org.uncertml.distribution.continuous.PoissonDistribution;
import org.uncertml.distribution.continuous.StudentTDistribution;
import org.uncertml.distribution.continuous.UniformDistribution;
import org.uncertml.distribution.continuous.WeibullDistribution;
import org.uncertml.sample.CategoricalRealisation;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.ISample;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.SystematicSample;
import org.uncertml.sample.UnknownSample;
import org.uncertml.statistic.CentredMoment;
import org.uncertml.statistic.CoefficientOfVariation;
import org.uncertml.statistic.ConfidenceInterval;
import org.uncertml.statistic.ConfusionMatrix;
import org.uncertml.statistic.ConstraintType;
import org.uncertml.statistic.Correlation;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertml.statistic.CredibleInterval;
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
import org.uncertml.statistic.Quantile;
import org.uncertml.statistic.Quartile;
import org.uncertml.statistic.Range;
import org.uncertml.statistic.Skewness;
import org.uncertml.statistic.StandardDeviation;
import org.uncertml.statistic.StatisticCollection;
import org.uncertweb.utils.UwCollectionUtils;

public enum UncertaintyType {
	
	
	// UNKNOWN(IUncertainty.class),
	SAMPLE(ISample.class),
	DISTRIBUTION(IDistribution.class),
	STATISTIC(IStatistic.class),

	BETA_DISTRIBUTION(BetaDistribution.class, DISTRIBUTION),
	CAUCHY_DISTRIBUTION(CauchyDistribution.class, DISTRIBUTION),
	CHI_SQUARE_DISTRIBUTION(ChiSquareDistribution.class, DISTRIBUTION),
	EXPONENTIAL_DISTRIBUTION(ExponentialDistribution.class, DISTRIBUTION),
	F_DISTRIBUTION(FDistribution.class, DISTRIBUTION),
	GAMMA_DISTRIBUTION(GammaDistribution.class, DISTRIBUTION),
	INVERSE_GAMMA_DISTRIBUTION(InverseGammaDistribution.class, DISTRIBUTION),
	LAPLACE_DISTRIBUTION(LaplaceDistribution.class, DISTRIBUTION),
	LOGISTIC_DISTRIBUTION(LogisticDistribution.class, DISTRIBUTION),
	LOG_NORMAL_DISTRIBUTION(LogNormalDistribution.class, DISTRIBUTION),
	NORMAL_DISTRIBUTION(NormalDistribution.class, DISTRIBUTION),
	NORMAL_INVERSE_GAMMA_DISTRIBUTION(NormalInverseGammaDistribution.class, DISTRIBUTION),
	PARETO_DISTRIBUTION(ParetoDistribution.class, DISTRIBUTION),
	POISSON_DISTRIBUTION(PoissonDistribution.class, DISTRIBUTION),
	STUDENT_T_DISTRIBUTION(StudentTDistribution.class, DISTRIBUTION),
	UNIFORM_DISTRIBUTION(UniformDistribution.class, DISTRIBUTION),
	WEIBULL_DISTRIBUTION(WeibullDistribution.class, DISTRIBUTION),

	MIXTURE_MODEL_DISTRIBUTION(MixtureModel.class, DISTRIBUTION),

	RANDOM_SAMPLE(RandomSample.class, SAMPLE),
	CONTINUOUS_REALISATION(ContinuousRealisation.class, SAMPLE, URI.create("http://www.uncertml.org/samples/realisation")),
	CATEGORICAL_REALISATION(CategoricalRealisation.class, SAMPLE),
	SYSTEMATIC_SAMPLE(SystematicSample.class, SAMPLE),
	UNKNOWN_SAMPLE(UnknownSample.class, SAMPLE),

	CONFIDENCE_INTERVAL(ConfidenceInterval.class, STATISTIC),
	CONFUSION_MATRIX(ConfusionMatrix.class, STATISTIC),
	COVARIANCE_MATRIX(CovarianceMatrix.class, STATISTIC),
	CREDIBLE_INTERVAL(CredibleInterval.class, STATISTIC),
	PROBABILITY(Probability.class, STATISTIC),
	STATISTIC_COLLECTION(StatisticCollection.class),

	CENTRED_MOMENT(CentredMoment.class, STATISTIC),
	COEFFICIENT_OF_VARIATION(CoefficientOfVariation.class, STATISTIC),
	CORRELATION(Correlation.class, STATISTIC),
	DECILE(Decile.class, STATISTIC),
	INTERQUATILE_RANGE(InterquartileRange.class, STATISTIC),
	KURTOSIS(Kurtosis.class, STATISTIC),
	MEAN(Mean.class, STATISTIC),
	MEDIAN(Median.class, STATISTIC),
	MODE(Mode.class, STATISTIC),
	MOMENT(Moment.class, STATISTIC),
	PERCENTILE(Percentile.class, STATISTIC),
	QUANTILE(Quantile.class, STATISTIC),
	QUARTILE(Quartile.class, STATISTIC),
	RANGE(Range.class, STATISTIC),
	SKEWNESS(Skewness.class, STATISTIC),
	STANDARD_DEVIATION(StandardDeviation.class, STATISTIC);

	private static final String CONSTRAINT_URI = UncertML.getURI(ConstraintType.class);
	public static final URI GREATER_THAN_URI = URI.create(CONSTRAINT_URI + "/greater-than");
	public static final URI LESS_THAN_URI = URI.create(CONSTRAINT_URI + "/less-than");
	public static final URI GREATER_OR_EQUAL_URI = URI.create(CONSTRAINT_URI+ "/greater-or-equal");
	public static final URI LESS_OR_EQUAL_URI = URI.create(CONSTRAINT_URI + "/less-or-equal");
	
	public final URI uri;
	private Set<URI> alias;
	public final Class<? extends IUncertainty> clazz;
	public final UncertaintyType type;
	
	
	public static URI getURIforConstraint(ConstraintType ct) {
		switch (ct) {
		case GREATER_OR_EQUAL:
			return GREATER_OR_EQUAL_URI;
		case GREATER_THAN:
			return GREATER_THAN_URI;
		case LESS_OR_EQUAL:
			return LESS_OR_EQUAL_URI;
		case LESS_THAN:
			return LESS_THAN_URI;
		default: return null;
		}
	}
	
	public Set<URI> getAlias() {
		return alias;
	}
	
	private UncertaintyType(Class<? extends IUncertainty> clazz, URI... uris) {
		this(clazz, null, uris);
	}
	
	private UncertaintyType(Class<? extends IUncertainty> clazz, UncertaintyType type, URI... uris) {
		this.uri = URI.create(UncertML.getURI(clazz));
		this.clazz = clazz;
		this.type = type;
		if (uris != null)
			this.alias = UwCollectionUtils.asSet(uris);
		else
			this.alias = UwCollectionUtils.set();
	}

	public URI getParamURI(String name) {
		return URI.create(this.getURI().toString() + "#" + name);
	}

	public static UncertaintyType fromURI(URI uri) {
		for (UncertaintyType s : values()) {
			if (s.getURI().equals(uri) || s.alias.contains(uri)) {
				return s;
			} 
		}
		return null;
	}

	public URI getURI() {
		return this.uri;
	}
	
	public UncertaintyType getSuperType() {
		return this.type;
	}
}
