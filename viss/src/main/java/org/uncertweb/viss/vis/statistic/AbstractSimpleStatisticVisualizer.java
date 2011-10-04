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
package org.uncertweb.viss.vis.statistic;

import org.uncertml.IUncertainty;
import org.uncertml.statistic.ContinuousStatistic;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer;

public abstract class AbstractSimpleStatisticVisualizer extends
    AbstractAnnotatedUncertaintyViusalizer {

	@Override
	public String getShortName() {
		return getClass().getSimpleName();
	}

	@Override
	protected double evaluate(IUncertainty u) {
		return ((ContinuousStatistic) u).getValues().get(0);
	}

	@Type(UncertaintyType.STANDARD_DEVIATION)
	@Description("Returns the Standard Deviation.")
	public static class StandardDeviationStatistic extends
	    AbstractSimpleStatisticVisualizer {}

	@Type(UncertaintyType.COEFFICIENT_OF_VARIATION)
	@Description("Returns the coefficient of variation.")
	public static class CoefficientOfVariationStatistic extends
	    AbstractSimpleStatisticVisualizer {}

	@Type(UncertaintyType.CORRELATION)
	@Description("Returns the correalation")
	public static class CorrelationStatistic extends
	    AbstractSimpleStatisticVisualizer {}

	@Type(UncertaintyType.KURTOSIS)
	@Description("Returns the kutosis.")
	public static class KurtosisStatistic extends
	    AbstractSimpleStatisticVisualizer {}

	@Type(UncertaintyType.MEAN)
	@Description("Returns the mean.")
	public static class MeanStatistic extends 
			AbstractSimpleStatisticVisualizer {}

	@Type(UncertaintyType.MEDIAN)
	@Description("Returns the median.")
	public static class MedianStatistic extends 
			AbstractSimpleStatisticVisualizer {}

	@Type(UncertaintyType.MODE)
	@Description("Returns the mode.")
	public static class ModeStatistic extends 
			AbstractSimpleStatisticVisualizer {}

	@Type(UncertaintyType.SKEWNESS)
	@Description("Returns the skewness.")
	public static class SkewnessStatistic extends
	    AbstractSimpleStatisticVisualizer {}
}
