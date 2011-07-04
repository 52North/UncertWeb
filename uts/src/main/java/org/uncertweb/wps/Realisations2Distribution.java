package org.uncertweb.wps;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.Realisation;

public class Realisations2Distribution extends AbstractAlgorithm {

	private String inputIDNumberOfRealisations = "realisations";	
	
	@Override
	public List<String> getErrors() {
		return null;
	}

	@Override
	public Class<?> getInputDataType(String arg0) {
		if(arg0.equals(inputIDNumberOfRealisations)){
			return UncertWebDataBinding.class;
		}
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String arg0) {
//		return LiteralDoubleBinding.class;
		return UncertWebDataBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		
		List<IData> inputDataList = inputData.get("realisations");
		
		IData firstData = inputDataList.get(0);
		
		if(!(firstData instanceof UncertWebDataBinding)){
			return null;			
		}
		
		UncertWebData uwDataInput = ((UncertWebDataBinding)firstData).getPayload();
		
		if(uwDataInput.getUncertaintyType() == null){
			return null;
		}
		
		IUncertainty uncertaintyType = uwDataInput.getUncertaintyType();
		
		Realisation r = null;
		
		if(uncertaintyType instanceof Realisation){
			
			r = (Realisation) uncertaintyType;
			
		}else{
			throw new RuntimeException("Input with ID realisation must be an UncertML realisation.");
		}
		
		double[] values = new double[r.getValues().size()];
		
		for (int i = 0; i < r.getValues().size(); i++) {
			values[i] = r.getValues().get(i);
		}
		
//		// WPS specific stuff
		Map<String, IData> result = new HashMap<String, IData>(1);try {
			
			// establish connection to Rserve running on localhost
//			ExtendedRConnection c = new ExtendedRConnection("127.0.0.1");
			ExtendedRConnection c = new ExtendedRConnection("giv-uw.uni-muenster.de");
			if (c.needLogin()) {
				// if server requires authentication,
				// send one
				c.login("rserve", "aI2)Jad$%");
			}
			
			REXPDouble d = new REXPDouble(values);		
			
			c.tryVoidEval("library(stats4)");
			
			c.assign("realisations", d);
			
			c.tryEval("m.realisations <- mean(realisations)");
			c.tryEval("s.realisations <- sd(realisations)");
			
			try {
				c.tryVoidEval("norm.dist <- function(m=m.realisations,s=s.realisations){\n-sum(log(dnorm(realisations,m,s)))\n}");								
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			REXP m_gauss_est = c.tryEval("m.gauss.est <- mle(norm.dist)@coef[1]");
			REXP s_gauss_est = c.tryEval("s.gauss.est <- mle(norm.dist)@coef[2]");
			REXP p_value = c.tryEval("ks.test(realisations, pnorm, m.gauss.est, s.gauss.est)$p.value");
			
			NormalDistribution gd = new NormalDistribution(m_gauss_est.asDouble(), s_gauss_est.asDouble());
			
			UncertWebData uwd = new UncertWebData(gd);
			
			result.put("output_distribution", new UncertWebDataBinding(uwd));
			
//			LiteralDoubleBinding outputBinding_estimated_mean = new LiteralDoubleBinding(m_gauss_est.asDouble());
//			LiteralDoubleBinding outputBindingestimated_standard_deviation = new LiteralDoubleBinding(s_gauss_est.asDouble());
//			LiteralDoubleBinding outputBindingp_value = new LiteralDoubleBinding(p_value.asDouble());
//			
//			result.put("output_estimated_mean", outputBinding_estimated_mean);
//			result.put("output_estimated_standard_deviation", outputBindingestimated_standard_deviation);
//			result.put("p_value", outputBindingp_value);
			
			return result;
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		return null;
	}

}
