package org.uncertweb.wps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RserveException;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.Realisation;
import org.uncertml.statistic.CovarianceMatrix;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;

public class UnivGaussianDist2Realisations extends AbstractAlgorithm {
	
	private static Logger logger = Logger.getLogger(UnivGaussianDist2Realisations.class);

	
	
	public UnivGaussianDist2Realisations() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getInputDataType(String arg0) {		
		return UncertWebIODataBinding.class;
	}

	@Override
	public Class<?> getOutputDataType(String arg0) {		
		return UncertWebIODataBinding.class;
	}
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> directInput) {
		
		HashMap<String, IData> result = new HashMap<String, IData>();
		
		// Get "number of realisations" input
		List<IData> iDataList1 = directInput.get("numbReal");
		IData iData1 = iDataList1.get(0);
		Integer intNumberOfRealisations = (Integer)iData1.getPayload();
		String numberOfRealisations = intNumberOfRealisations.toString();
		
		// Get "uncertML" input, convert it to IUncertainty
		IData distributionInput = directInput.get("distribution").get(0);
		if(!(distributionInput instanceof UncertMLBinding)){
			
		}
		
		UncertMLBinding distBinding = (UncertMLBinding) distributionInput;
		IUncertainty uncertainty = distBinding.getPayload();
		
		// Get parameters etc out of it
		NormalDistribution mg = null;
		if (uncertainty instanceof NormalDistribution){
			mg = (NormalDistribution)uncertainty;
		}else{
			throw new RuntimeException("Input with ID distribution must be a univariate gaussian distribution!");
		}
		
		List<Double> meanlist =
			mg.getMean();
		Double mean = meanlist.get(0);
		String meanString = mean.toString();
		List<Double> varlist =
			mg.getVariance();
		Double var = varlist.get(0);
		String sdString = var.toString();
		ExtendedRConnection c = null;
		try {
			// Perform R computations
			
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
//				c.login("", "");
			}
		
			c.tryVoidEval("i <- " + numberOfRealisations);
			c.tryVoidEval("m.gauss <- " + meanString);
			c.tryVoidEval("var.gauss <- " + sdString);
			c.tryVoidEval("sd.gauss <- sqrt(var.gauss)");
			REXP samples =  c.tryEval("rnorm(i, m.gauss, sd.gauss)");
			
			double[] sampleDoubleArray = samples.asDoubles();
			
			// Realisation-Constructor laeuft nicht
			Realisation r = new Realisation(sampleDoubleArray, -999999.999, "bla");
			
			// Make and return result
			UncertMLBinding uwdb = new UncertMLBinding(r);
			result.put("realisations", uwdb);
			
		
		} catch (Exception e) {
			logger.error("Error while calculating samples for Gaussian Distribution: "+e.getLocalizedMessage());
		}
		
		finally {
			if (c != null) {
				c.close();
			}
		}
		
		return result;
		
	}

}