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
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.Realisation;
import org.uncertml.statistic.CovarianceMatrix;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;

public class UnivGaussianDist2Realisations extends AbstractAlgorithm {

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
		NormalDistribution mg = null;
		if (iuncertaintyType instanceof NormalDistribution){
			mg = (NormalDistribution)iuncertaintyType;
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
		
		try {
			// Perform R computations
			
			// establish connection to Rserve
			ExtendedRConnection c = new ExtendedRConnection("giv-uw.uni-muenster.de");
			if (c.needLogin()) {
				// if server requires authentication, send one		
				c.login("rserve", "aI2)Jad$%");
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
			UncertWebData uwd = new UncertWebData(r);
			UncertWebDataBinding uwdb = new UncertWebDataBinding(uwd);
			HashMap<String, IData> result = new HashMap<String, IData>();
			result.put("realisations", uwdb);
			return result;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return(null);
		
	}

}