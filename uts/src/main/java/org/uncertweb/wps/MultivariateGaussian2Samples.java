package org.uncertweb.wps;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;

/**
 * Process for taking random samples from a multivariate Gaussian distribution
 * 
 * @author staschc, Benjamin Pross, Lydia Gerharz
 * 
 */
public class MultivariateGaussian2Samples extends AbstractAlgorithm {

	private static Logger LOGGER = Logger.getLogger(MultivariateGaussian2Samples.class);
	// constants for input/output identifiers
	private final static String INPUT_IDENTIFIER_DIST = "distribution";
	private final static String INPUT_IDENTIFIER_NUMB_REAL = "numbReal";
	private final static String OUTPUT_IDENTIFIER_SAMPLES = "samples";
		
	public MultivariateGaussian2Samples(){
		super();
	}
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		// WPS specific stuff; initialize result set
				Map<String, IData> result = new HashMap<String, IData>(1);
				try {					
					// get number of realisations
					IData numbRealsInput = inputData.get(INPUT_IDENTIFIER_NUMB_REAL)
							.get(0);
					Integer intNRealisations = ((LiteralIntBinding) numbRealsInput)
							.getPayload();
					
					// get input file containing the Gaussian Distributions
					IData dataInput = inputData.get(INPUT_IDENTIFIER_DIST).get(0);

					// support for O&M and UncertML
					if(dataInput instanceof OMBinding){
						UncertaintyObservationCollection uwColl = (UncertaintyObservationCollection)dataInput.getPayload();
						
						UncertaintyObservationCollection resultFile = getSamples4MultiGaussianOMFile(uwColl,
								intNRealisations);
						
						OMBinding uwData = new OMBinding(resultFile);
						result.put(OUTPUT_IDENTIFIER_SAMPLES, uwData);
					}
					// support for plain UncertML
					else if(dataInput instanceof UncertMLBinding){
						IUncertainty distribution = (IUncertainty)dataInput.getPayload();
						
						IUncertainty results = getSamples4UncertML(distribution,
								intNRealisations);
						
						UncertMLBinding uwData = new UncertMLBinding(results);
						result.put(OUTPUT_IDENTIFIER_SAMPLES, uwData);
					}
					else{
						LOGGER.error("Input data format is not supported!");
						throw new IOException("Input data format is not supported!");
					}

				} catch (Exception e) {
					LOGGER
							.debug("Error while getting random samples for multivariate Gaussian distribution: "
									+ e.getMessage());
					throw new RuntimeException(
							"Error while getting random samples for multivariate Gaussian distribution: "
									+ e.getMessage(), e);
				} 
				

				return result;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if (id.equals(INPUT_IDENTIFIER_DIST)) {
			return UncertWebIODataBinding.class;
		} else if (id.equals(INPUT_IDENTIFIER_NUMB_REAL)) {
			return LiteralIntBinding.class;
		}
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		if (id.equals(OUTPUT_IDENTIFIER_SAMPLES)) {
			return UncertWebIODataBinding.class;
		}
		return null;
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 * Method for UncertML sampling
	 * @param distribution
	 * @param intNRealisations
	 * @return
	 */
	private IUncertainty getSamples4UncertML(IUncertainty distribution, Integer intNRealisations){
		IUncertainty resultUncertainty = null;
		
		ExtendedRConnection c = null;
		try {
			// Perform R computations
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
			if(distribution instanceof MultivariateNormalDistribution){
				// extract distribution details and prepare for R
				MultivariateNormalDistribution mnd = (MultivariateNormalDistribution) distribution;
				
				// Covariance matrix
				CovarianceMatrix cov = mnd.getCovarianceMatrix();
				int dim = cov.getDimension();			
				String tempCovMat = cov.getValues().toString(); 
				// remove brackets []
				tempCovMat = tempCovMat.substring(1, tempCovMat.length() - 1);
				c.tryVoidEval("covmat.gauss <- matrix(c(" + tempCovMat + "), nrow = " + dim + ")");

				// Mean vector 
				String tempMeanVec = mnd.getMean().toString(); // [x, y, z]
				// remove brackets []
				tempMeanVec = tempMeanVec.substring(1,tempMeanVec.length() - 1); // x, y, z
				c.tryVoidEval("meanvec.gauss <- c(" + tempMeanVec+")");
				
				// perform sampling
				c.tryVoidEval("library(MASS)");
				c.tryVoidEval("i <- " + intNRealisations);
				double[][] mvSamples = c.tryEval("mvrnorm(i, mu = meanvec.gauss, Sigma = covmat.gauss)").asDoubleMatrix();
				
				// sort realisations
				// we want: first realisation of var1, var, ..., second realisation of var1, var2,
				//TODO: or use multiple realisations in one random sample?
				int varLength = mnd.getMean().size();
				double[] samples = new double[intNRealisations * varLength];
			//	ContinuousRealisation[] cRealList = new ContinuousRealisation[intNRealisations];
				for(int i=0; i<intNRealisations; i++){
			//		double[] realisation = mvSamples[i];
					for(int j=0; j<varLength; j++){
						samples[i*varLength+j] = mvSamples[i][j];
					}
			//		ContinuousRealisation cReal = new ContinuousRealisation(realisation);
			//		cRealList[i] = cReal;
				}
				
				//resultUncertainty = new ContinuousRealisation(samples);
				//TODO: Should we use RandomSample?
				// create UncertML random sample
				ContinuousRealisation cr = new ContinuousRealisation(samples);
				ContinuousRealisation[] crList = new ContinuousRealisation[1];
				crList[0] = cr;
				resultUncertainty = new RandomSample(crList);
			}else{
				throw new RuntimeException(
					"Input with ID distribution must be a gaussian distribution!");
			}	
				
			return resultUncertainty;
						
			} catch (Exception e) {
				LOGGER
				.debug("Error while getting random samples for Gaussian distribution: "
						+ e.getMessage());
				throw new RuntimeException(
				"Error while getting random samples for Gaussian distribution: "
						+ e.getMessage(), e);	}
				
			finally {
				if (c != null) {
					c.close();
				}
			}		
		
	}
	
	
	/**
	 * Method for OM sampling
	 * @param inputColl
	 * @param intNRealisations
	 * @return
	 */
	private UncertaintyObservationCollection getSamples4MultiGaussianOMFile(UncertaintyObservationCollection inputColl, Integer intNRealisations){
		UncertaintyObservationCollection resultColl = new UncertaintyObservationCollection();
		
		ExtendedRConnection c = null;			 
		try {		
			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
			
			// loop through observation collection
			for (AbstractObservation obs : inputColl.getObservations()) {  		
				if(obs instanceof UncertaintyObservation){
					// get UncertML distribution
					UncertaintyResult uResult = (UncertaintyResult) obs.getResult();
					IUncertainty distribution = uResult.getUncertaintyValue();
						
					//TODO: alternatively call getSamples4UncertML here
					//IUncertainty resultUncertainty = this.getSamples4UncertML(distribution, intNRealisations);
					
					// get samples for this distribution
					if(distribution instanceof MultivariateNormalDistribution){					
						// extract distribution details and prepare for R
						MultivariateNormalDistribution mnd = (MultivariateNormalDistribution) distribution;
						
						// Covariance matrix
						CovarianceMatrix cov = mnd.getCovarianceMatrix();
						int dim = cov.getDimension();			
						String tempCovMat = cov.getValues().toString(); 
						// remove brackets []
						tempCovMat = tempCovMat.substring(1, tempCovMat.length() - 1);
						c.tryVoidEval("covmat.gauss <- matrix(c(" + tempCovMat + "), nrow = " + dim + ")");

						// Mean vector 
						String tempMeanVec = mnd.getMean().toString(); // [x, y, z]
						// remove brackets []
						tempMeanVec = tempMeanVec.substring(1,tempMeanVec.length() - 1); // x, y, z
						c.tryVoidEval("meanvec.gauss <- c(" + tempMeanVec+")");
						
						// perform sampling
						c.tryVoidEval("library(MASS)");
						c.tryVoidEval("i <- " + intNRealisations);
						double[][] mvSamples = c.tryEval("mvrnorm(i, mu = meanvec.gauss, Sigma = covmat.gauss)").asDoubleMatrix();
						
						// sort realisations
						// we want: first realisation of var1, var, ..., second realisation of var1, var2,
						//TODO: or use multiple realisations in one random sample?
						int varLength = mnd.getMean().size();
						double[] samples = new double[intNRealisations * varLength];
					//	ContinuousRealisation[] cRealList = new ContinuousRealisation[intNRealisations];
						for(int i=0; i<intNRealisations; i++){
					//		double[] realisation = mvSamples[i];
							for(int j=0; j<varLength; j++){
								samples[i*varLength+j] = mvSamples[i][j];
							}
					//		ContinuousRealisation cReal = new ContinuousRealisation(realisation);
					//		cRealList[i] = cReal;
						}
						
						//resultUncertainty = new ContinuousRealisation(samples);
						//TODO: Should we use RandomSample?
						// create UncertML random sample
						ContinuousRealisation cr = new ContinuousRealisation(samples);
						ContinuousRealisation[] crList = new ContinuousRealisation[1];
						crList[0] = cr;
						RandomSample rs = new RandomSample(crList);
						
						// make new observation with samples
						UncertaintyResult newResult = new UncertaintyResult(rs);
						newResult.setUnitOfMeasurement(uResult.getUnitOfMeasurement());
						
						UncertaintyObservation newObs = new UncertaintyObservation(
								obs.getIdentifier(), obs.getBoundedBy(), obs.getPhenomenonTime(), 
								obs.getResultTime(), obs.getValidTime(), obs.getProcedure(), 
								obs.getObservedProperty(), obs.getFeatureOfInterest(), 
								obs.getResultQuality(), newResult);

						// add observation to new collection
						resultColl.addObservation(newObs);
					}	
					else{
						throw new RuntimeException(
							"Input with ID distribution must be a gaussian distribution!");
					}
				}else{
					throw new RuntimeException(
							"Input with ID distribution must contain uncertainty observations!");
					}					
			}		
			
			return resultColl;
			
		}catch (Exception e) {
			LOGGER
			.debug("Error while getting random samples for Gaussian distribution: "
					+ e.getMessage());
			throw new RuntimeException(
			"Error while getting random samples for Gaussian distribution: "
					+ e.getMessage(), e);
		} 
		finally {
			if (c != null) {
				c.close();
			}
		}
	}
	
}
