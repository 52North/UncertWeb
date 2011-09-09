package org.uncertweb.wps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RserveException;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.sample.Realisation;
import org.uncertml.statistic.CovarianceMatrix;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;

public class MultiGaussianDist2Realisations extends AbstractAlgorithm {

	// TODO Introduce MULTIVARIATE Realisation once they are available
	
	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getInputDataType(String arg0) {		
		return UncertWebDataBinding.class;
	}

	@Override
	public Class<?> getOutputDataType(String arg0) {		
		return UncertWebDataBinding.class;
	}
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> directInput) {
		
		// Get "number of realisations" input
		List<IData> iDataList1 = directInput.get("numbReal");
		IData iData1 = iDataList1.get(0);
		Integer intNumberOfRealisations = (Integer)iData1.getPayload();
		String numberOfRealisations = intNumberOfRealisations.toString();
		
		// Get "uncertML" input, convert it to IUncertainty
		List<IData> iDataList2 = directInput.get("distribution");
		IData iData2 = iDataList2.get(0);
		if(!(iData2 instanceof UncertWebDataBinding)){
			return null;
		}
		UncertWebDataBinding uwdbinding = (UncertWebDataBinding) iData2;
		UncertWebData uwdata = uwdbinding.getPayload();
		if(uwdata.getUncertaintyType() == null){
			return null;
		}
		IUncertainty iuncertaintyType = uwdata.getUncertaintyType();
		
		// Get parameters etc out of it
		MultivariateNormalDistribution mnd = null;
		if (iuncertaintyType instanceof MultivariateNormalDistribution){
			mnd = (MultivariateNormalDistribution)iuncertaintyType;
		}else{
			throw new RuntimeException("Input with ID distribution must be a multivariate gaussian distribution!");
		}
		
		// Get Covariance matrix to R format matrix(c(...,...,...), nrow=x)
		CovarianceMatrix cov = mnd.getCovarianceMatrix();
		String tempCovMat = cov.getValues().toString(); // This should look like [x, y, z]
		//System.out.println(tempCovMat);
		tempCovMat = tempCovMat.substring(1, tempCovMat.length()-1); // Now like this: x, y, z
		//System.out.println(tempCovMat);
		String covMatrixR = "matrix(c(";
		covMatrixR = covMatrixR.concat(tempCovMat); // Now: matrix(c(x,y,z
		int dim = cov.getDimension();
		covMatrixR = covMatrixR.concat("), nrow = " + dim + ")");
		//System.out.println(covMatrixR);
		
		// meanvector to R-format c(...,...,...)
		String tempMeanvector = mnd.getMean().toString(); // [x, y, z]
		tempMeanvector = tempMeanvector.substring(1, tempMeanvector.length()-1); // x, y, z
		String meanVectorR = "c(";
		meanVectorR = meanVectorR.concat(tempMeanvector+")"); // c(x, y, z)
		
		// Perform R computations
		
		// establish connection to Rserve
		ExtendedRConnection c;
		double[] sampleDoubleArray = null;
		try {
			c = new ExtendedRConnection("giv-uw.uni-muenster.de");

			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
		
			c.tryVoidEval("library(MASS)");
			c.tryVoidEval("i <- " + numberOfRealisations );
			c.tryVoidEval("meanvec.gauss <- " + meanVectorR);
			c.tryVoidEval("covmat.gauss <- " + covMatrixR);
			REXP samples = c.tryEval("mvrnorm(i, mu = meanvec.gauss, Sigma = covmat.gauss)");
			sampleDoubleArray = samples.asDoubles();
			
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		// start of re-arranging sample array!
		/*  re-arrange order of realisations
		 *  so far it is: all realisations of var1, all realisations of var2, ...
		 *  we want: first realisation of var1, var, ..., second realisation of var1, var2, ...,...
		 */
		// Needed stuff:
		int totalNumReal = sampleDoubleArray.length;
		int numberOfVariables = mnd.getMean().size();
		int numRealPerVar = totalNumReal / numberOfVariables;
		// The re-arranged realisation values will go in here:
		System.out.println("We have "+numberOfVariables+" variables with "+numRealPerVar+ " realisations each, thus "+totalNumReal+" realisation values in total\n");
		double[] sampleDoubleArraySorted = new double[totalNumReal];

		// Indices of the first realisation of each variable (in old array)
		int[] anfangswerte = new int[numberOfVariables];
		for (int g = 0; g < numberOfVariables; g++){
			anfangswerte[g] = g*numRealPerVar;
		}
		/* Outer "for loop": In each Durchlauf, one realisation per variable is fetched
		 * u counts the "sets" of realisations. u=0 -> first realisation of all variables.
		 * i is the index of the new array! in every Durchlauf, it increases of as many steps as there is variables
		 */
		int i = 0; 
		for (int u = 0; u<numRealPerVar; u++){ // note: not i++!
			System.out.println("Fetching the "+u+"th realisation of all variables");
			System.out.println("Putting them into the array-indexes "+i+", "+(i+1)+", ..  of the new array");
			
			/* inner "for loop": In each Durchlauf, the realisation values of each variable are
			 * fetched
			 */
			for (int j = 0; j <numberOfVariables; j++){
				// For each variable, we take the first index (anfangswert) plus the number of realisations we already did...
				int index = anfangswerte[j]+u;
				// We put the value into the new array, i+j together are 1,2,3,4,5...
				sampleDoubleArraySorted[(i+j)] = sampleDoubleArray[index];
				System.out.println("i+j= "+(i+j));
			}
			i=i+numberOfVariables;    
		}
		// end of re-arranging sample array!


		
		// Make and return result
		// TODO Make MULTIVARIATE realisations out of this!
		Realisation r = new Realisation(sampleDoubleArraySorted, -999999.999, "bla");
		UncertWebData uwd = new UncertWebData(r);
		UncertWebDataBinding uwdb = new UncertWebDataBinding(uwd);
		HashMap<String, IData> result = new HashMap<String, IData>();
		result.put("realisations", uwdb);
		return result;
		
	}

}