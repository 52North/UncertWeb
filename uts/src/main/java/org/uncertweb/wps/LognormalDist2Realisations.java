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
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.sample.Realisation;
import org.uncertml.statistic.CovarianceMatrix;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;

public class LognormalDist2Realisations extends AbstractAlgorithm {
	

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
		
		HashMap<String, IData> result = new HashMap<String, IData>();
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
		LogNormalDistribution mg = null;
		if (iuncertaintyType instanceof LogNormalDistribution){
			mg = (LogNormalDistribution)iuncertaintyType;
		}else{
			throw new RuntimeException("Input with ID distribution must be a univariate lognormal distribution!");
		}
		
		List<Double> shapelist = mg.getShape();
		Double shape = shapelist.get(0);
		String shapeString = shape.toString();
		List<Double> logscalelist = mg.getLogScale();
		Double logScale = logscalelist.get(0);
		String logScaleString = logScale.toString();
		
		ExtendedRConnection c = null;
		
		try {
			
			// establish connection to Rserve
			//c = new ExtendedRConnection("giv-uw.uni-muenster.de");
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
//				c.login("", "");
			}

			// Perform R computations
			// I use the definition that Scale = meanlog and shape = sdlog
			// Source: http://en.wikipedia.org/wiki/Log-normal_distribution, infobox on the right
			c.tryVoidEval("i <- " + numberOfRealisations);
			c.tryVoidEval("m.lognormal <- " + logScaleString);
			c.tryVoidEval("var.lognormal <- " + shapeString);
			c.tryVoidEval("sd.lognormal <- sqrt(var.lognormal)");
			REXP samples =  c.tryEval("rlnorm(i, m.lognormal, sd.lognormal)");
			
			double[] sampleDoubleArray = samples.asDoubles();
			
			Realisation r = new Realisation(sampleDoubleArray, -999999.999, "bla");
			
			// Make and return result
			UncertWebData uwd = new UncertWebData(r);
			UncertWebDataBinding uwdb = new UncertWebDataBinding(uwd);
			
			result.put("realisations", uwdb);
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		finally {
			if (c != null) {
				c.close();
			}
		}
		
		return result;
	}
}