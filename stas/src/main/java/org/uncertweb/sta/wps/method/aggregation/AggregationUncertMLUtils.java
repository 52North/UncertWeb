package org.uncertweb.sta.wps.method.aggregation;

import java.util.List;

import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Variance;

/**
 * class provides utility methods for computing aggregates from UncertML Realisations
 *
 * @author staschc
 *
 */
public class AggregationUncertMLUtils {

	public static Mean computeMean(ContinuousRealisation real){
		List<Double> values = real.getValues();
		double sum = 0;
		for (Double value:values){
			sum+=value;
		}
		sum=sum/values.size();
		return new Mean(sum);
	}

	public static Variance computeVariance(ContinuousRealisation real){
		double mean = computeMean(real).getValues().get(0);
		List<Double> values = real.getValues();
		double sum = 0;
		for (Double value:values){
			sum+=((mean-value)*(mean-value));
		}
		sum=sum/values.size();
		return new Variance(sum);
	}
}
