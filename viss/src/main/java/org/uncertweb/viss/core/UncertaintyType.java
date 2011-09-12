/* Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
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
import org.uncertml.sample.ISample;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.Realisation;
import org.uncertml.sample.SystematicSample;
import org.uncertml.sample.UnknownSample;
import org.uncertml.statistic.CentredMoment;
import org.uncertml.statistic.CoefficientOfVariation;
import org.uncertml.statistic.ConfidenceInterval;
import org.uncertml.statistic.ConfusionMatrix;
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

public enum UncertaintyType {
	// UNKNOWN(IUncertainty.class),
	SAMPLE(ISample.class),
	DISTRIBUTION(IDistribution.class),
	STATISTIC(IStatistic.class),

	BETA_DISTRIBUTION(BetaDistribution.class),
	CAUCHY_DISTRIBUTION(CauchyDistribution.class),
	CHI_SQUARE_DISTRIBUTION(ChiSquareDistribution.class),
	EXPONENTIAL_DISTRIBUTION(ExponentialDistribution.class),
	F_DISTRIBUTION(FDistribution.class),
	GAMMA_DISTRIBUTION(GammaDistribution.class),
	INVERSE_GAMMA_DISTRIBUTION(InverseGammaDistribution.class),
	LAPLACE_DISTRIBUTION(LaplaceDistribution.class),
	LOGISTIC_DISTRIBUTION(LogisticDistribution.class),
	LOG_NORMAL_DISTRIBUTION(LogNormalDistribution.class),
	NORMAL_DISTRIBUTION(NormalDistribution.class),
	NORMAL_INVERSE_GAMMA_DISTRIBUTION(NormalInverseGammaDistribution.class),
	PARETO_DISTRIBUTION(ParetoDistribution.class),
	POISSON_DISTRIBUTION(PoissonDistribution.class),
	STUDENT_T_DISTRIBUTION(StudentTDistribution.class),
	UNIFORM_DISTRIBUTION(UniformDistribution.class),
	WEIBULL_DISTRIBUTION(WeibullDistribution.class),

	MIXTURE_MODEL_DISTRIBUTION(MixtureModel.class),

	RANDOM_SAMPLE(RandomSample.class),
	REALISATION(Realisation.class),
	SYSTEMATIC_SAMPLE(SystematicSample.class),
	UNKNOWN_SAMPLE(UnknownSample.class),

	CONFIDENCE_INTERVAL(ConfidenceInterval.class),
	CONFUSION_MATRIX(ConfusionMatrix.class),
	COVARIANCE_MATRIX(CovarianceMatrix.class),
	CREDIBLE_INTERVAL(CredibleInterval.class),
	PROBABILITY(Probability.class),
	STATISTIC_COLLECTION(StatisticCollection.class),

	CENTRED_MOMENT(CentredMoment.class),
	COEFFICIENT_OF_VARIATION(CoefficientOfVariation.class),
	CORRELATION(Correlation.class),
	DECILE(Decile.class),
	INTERQUATILE_RANGE(InterquartileRange.class),
	KURTOSIS(Kurtosis.class),
	MEAN(Mean.class),
	MEDIAN(Median.class),
	MODE(Mode.class),
	MOMENT(Moment.class),
	PERCENTILE(Percentile.class),
	QUANTILE(Quantile.class),
	QUARTILE(Quartile.class),
	RANGE(Range.class),
	SKEWNESS(Skewness.class),
	STANDARD_DEVIATION(StandardDeviation.class);

	public final URI uri;
	public final Class<? extends IUncertainty> clazz;

	private UncertaintyType(Class<? extends IUncertainty> clazz) {
		this.uri = URI.create(UncertML.getURI(clazz));
		this.clazz = clazz;
	}

	public URI getParamURI(String name) {
		return URI.create(this.getURI().toString() + "#" + name);
	}

	public static UncertaintyType fromURI(URI uri) {
		for (UncertaintyType s : values())
			if (s.getURI().equals(uri))
				return s;
		throw new IllegalArgumentException();
	}

	public URI getURI() {
		return this.uri;
	}
}
