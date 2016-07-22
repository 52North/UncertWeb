package org.uncertml.distribution.randomvariable.util;

/**
 * utility class for UncertML random variables extension
 * 
 * @author staschc
 *
 */
public class RandomVariablesUtil {
	
	/**
	 * parses a string containing space seperated trend coefficients to a double array
	 * 
	 * @param trends
	 * 			a string containing space seperated trend coefficients
	 * @return double array containing the trend coefficients
	 */
	public static double[] parseTrendCoefficients(String trends){
		String[] splittedTrends = trends.split(" ");
		double[] result = new double[splittedTrends.length];
		for (int i=0;i< splittedTrends.length;i++){
			result[i]=Double.parseDouble(splittedTrends[i]);
		}
		return result;
	}
}
