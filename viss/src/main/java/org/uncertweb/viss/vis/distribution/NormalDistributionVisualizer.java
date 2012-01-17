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
package org.uncertweb.viss.vis.distribution;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.util.FastMath;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.UncertaintyType;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.JSONSchema;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer;
import org.uncertweb.viss.vis.AbstractAnnotatedUncertaintyViusalizer.Type;
import org.uncertweb.viss.vis.netcdf.UncertaintyValue;

@Type(UncertaintyType.NORMAL_DISTRIBUTION)
public abstract class NormalDistributionVisualizer extends
		AbstractAnnotatedUncertaintyViusalizer {

	@Id("Distribution-Normal-ProbabilityForInterval")
	@Description("Returns P(min <= X <= max).")
	public static class ProbabilityForInterval extends
			NormalDistributionVisualizer {
		public static final String MIN_DESCRIPTION = "the (inclusive) lower bound";
		public static final String MAX_DESCRIPTION = "the (inclusive) upper bound";
		public static final String MIN_PARAMETER = "min";
		public static final String MAX_PARAMETER = "max";

		private static JSONObject createMinOption() throws JSONException {
			return new JSONObject()
					.put(JSONSchema.Key.DESCRIPTION, MIN_DESCRIPTION)
					.put(JSONSchema.Key.TYPE, JSONSchema.Type.NUMBER)
					.put(JSONSchema.Key.REQUIRED, true);
		}

		private static JSONObject createMaxOption() throws JSONException {

			return new JSONObject()
					.put(JSONSchema.Key.DESCRIPTION, MAX_DESCRIPTION)
					.put(JSONSchema.Key.TYPE, JSONSchema.Type.NUMBER)
					.put(JSONSchema.Key.REQUIRED, true);
		}

		@Override
		public String getUom() {
			return "%";
		}

		@Override
		public Map<String, JSONObject> getOptionsForDataSet(IDataSet r) {
			try {
				Map<String, JSONObject> options = UwCollectionUtils.map();
				double[] minmax = getRange(r);
				options.put(
						MIN_PARAMETER,
						createMinOption()
								.put(JSONSchema.Key.MINIMUM, minmax[0]).put(
										JSONSchema.Key.MAXIMUM, minmax[1]));
				options.put(
						MAX_PARAMETER,
						createMaxOption()
								.put(JSONSchema.Key.MINIMUM, minmax[0]).put(
										JSONSchema.Key.MAXIMUM, minmax[1]));
				return options;
			} catch (JSONException e) {
				throw VissError.internal(e);
			}
		}

		private double getMin() {
			return getMin(getParams());
		}

		private double getMin(JSONObject j) {
			try {
				return j.getDouble(MIN_PARAMETER);
			} catch (JSONException e) {
				throw VissError.invalidParameter(MIN_PARAMETER);
			}
		}

		private double getMax() {
			return getMax(getParams());
		}

		private double getMax(JSONObject j) {
			try {
				return j.getDouble(MAX_PARAMETER);
			} catch (JSONException e) {
				throw VissError.invalidParameter(MAX_PARAMETER);
			}
		}

		@Override
		protected double evaluate(NormalDistributionImpl nd) {
			try {
				return nd.cumulativeProbability(getMin(), getMax());
			} catch (MathException e) {
				throw VissError.internal(e);
			}
		}

		@Override
		public Map<String, JSONObject> getOptions() {
			try {
				Map<String, JSONObject> options = UwCollectionUtils.map();
				options.put(MIN_PARAMETER, createMinOption());
				options.put(MAX_PARAMETER, createMaxOption());
				return options;
			} catch (JSONException e) {
				throw VissError.internal(e);
			}
		}
	}

	@Id("Distribution-Normal-Probability")
	@Description("Returns P(X <= max).")
	public static class Probability extends NormalDistributionVisualizer {
		public static final String MAX_DESCRIPTION = "the (inclusive) upper bound";
		public static final String MAX_PARAMETER = "max";

		private static JSONObject createMaxOption() {
			try {
				return new JSONObject()
						.put(JSONSchema.Key.DESCRIPTION, MAX_DESCRIPTION)
						.put(JSONSchema.Key.TYPE, JSONSchema.Type.NUMBER)
						.put(JSONSchema.Key.REQUIRED, true);
			} catch (JSONException e) {
				throw VissError.internal(e);
			}

		}

		@Override
		protected String getUom() {
			return "%";
		}

		@Override
		public Map<String, JSONObject> getOptionsForDataSet(IDataSet r) {
			try {
				double[] minmax = getRange(r);
				return UwCollectionUtils.map(
						MAX_PARAMETER,
						createMaxOption()
								.put(JSONSchema.Key.MINIMUM, minmax[0]).put(
										JSONSchema.Key.MAXIMUM, minmax[1]));
			} catch (JSONException e) {
				throw VissError.internal(e);
			}
		}

		private double getMax() {
			return getMax(getParams());
		}

		private double getMax(JSONObject j) {
			try {
				return j.getDouble(MAX_PARAMETER);
			} catch (JSONException e) {
				throw VissError.invalidParameter(MAX_PARAMETER);
			}
		}

		@Override
		protected double evaluate(NormalDistributionImpl nd) {
			try {
				return nd.cumulativeProbability(getMax());
			} catch (MathException e) {
				throw VissError.internal(e);
			}
		}

		@Override
		public Map<String, JSONObject> getOptions() {
			return UwCollectionUtils.map(MAX_PARAMETER, createMaxOption());
		}

	}

	@Id("Distribution-Normal-StandardDeviation")
	@Description("Returns the standard deviation.")
	public static class StandardDeviation extends NormalDistributionVisualizer {

		@Override
		protected double evaluate(NormalDistributionImpl nd) {
			return nd.getStandardDeviation();
		}
	}

	@Id("Distribution-Normal-Variance")
	@Description("Returns the variance.")
	public static class Variance extends NormalDistributionVisualizer {

		@Override
		protected double evaluate(NormalDistributionImpl nd) {
			return nd.getStandardDeviation() * nd.getStandardDeviation();
		}

		@Override
		protected String getUom() {
			return "(" + super.getUom() + ")^2";
		}
	}

	@Id("Distribution-Normal-Mean")
	@Description("Returns the mean of the distribution.")
	public static class Mean extends NormalDistributionVisualizer {

		@Override
		protected double evaluate(NormalDistributionImpl nd) {
			return nd.getMean();
		}

	}

	@Id("Distribution-Normal-ExceedanceProbabilityForInterval")
	@Description("Returns 1-P(min <= X <= max).")
	public static class ExceedanceProbabilityForInterval extends
			ProbabilityForInterval {

		@Override
		protected double evaluate(NormalDistributionImpl nd) {
			return 1 - super.evaluate(nd);
		}
	}

	@Id("Distribution-Normal-ExceedanceProbability")
	@Description("Returns 1-P(X <= max).")
	public static class ExceedanceProbability extends Probability {

		@Override
		protected double evaluate(NormalDistributionImpl nd) {
			return 1 - super.evaluate(nd);
		}
	}

	private static final int TIMES_STANDARD_DEVIATION = 3;

	@Override
	public double evaluate(IUncertainty u) {
		NormalDistribution d = (NormalDistribution) u;
		return evaluate(new NormalDistributionImpl(d.getMean().get(0),
				FastMath.sqrt(d.getVariance().get(0))));
	}

	protected double[] getRange(IDataSet r) {
		return getRange(r, TIMES_STANDARD_DEVIATION);
	}

	protected double[] getRange(IDataSet r, int tsd) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		Iterator<UncertaintyValue> i = getIteratorForDataSet(r);		
		while (i.hasNext()){
			UncertaintyValue val = i.next();
			if (val.getValue() != null) {
				NormalDistribution nd = (NormalDistribution) val.getValue();
				double m = nd.getMean().get(0);
				double sd = FastMath.sqrt(nd.getVariance().get(0));
				min = FastMath.min(min, m - tsd * sd);
				max = FastMath.max(max, m + tsd * sd);
			}
		}

		return new double[] { min, max };
	}

	protected abstract double evaluate(NormalDistributionImpl d);

}
