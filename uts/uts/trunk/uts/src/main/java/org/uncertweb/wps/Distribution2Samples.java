package org.uncertweb.wps;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;

public class Distribution2Samples extends AbstractAlgorithm {

	private String inputIDNumberOfRealisations = "NumberOfRealisations";
	private String inputIDMean = "Mean";
	private String inputIDStandardDeviation = "StandardDeviation";	
	
	
	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getInputDataType(String arg0) {
		if(arg0.equals(inputIDNumberOfRealisations)){
			return LiteralIntBinding.class;
		}else if(arg0.equals(inputIDMean)){
			return LiteralDoubleBinding.class;
		}else if(arg0.equals(inputIDStandardDeviation)){
			return LiteralDoubleBinding.class;
		}
		return null;
	}

	@Override
	public Class getOutputDataType(String arg0) {
		return LiteralStringBinding.class;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String fileSeparator = System.getProperty("file.separator");

		String baseDir = WebProcessingService.BASE_DIR + fileSeparator
				+ "resources";

		// WPS specific stuff
		Map<String, IData> result = new HashMap<String, IData>(1);

		IData data1 = inputData.get(inputIDNumberOfRealisations)
				.get(0);

		String numberOfRealisations = String.valueOf(((LiteralIntBinding)data1).getPayload());
		
		
		IData data2 = inputData.get(inputIDMean)
		.get(0);

		String mean = String.valueOf(((LiteralDoubleBinding)data2).getPayload());

		IData data3 = inputData.get(inputIDStandardDeviation)
		.get(0);

		String stdDev = String.valueOf(((LiteralDoubleBinding)data3).getPayload());

		
		File tmpDir = new File(tmpDirPath);

		if (!tmpDir.exists()) {
			tmpDir.mkdir();
		}

		try {
			
			// establish connection to Rserve running on localhost
//			ExtendedRConnection c = new ExtendedRConnection("127.0.0.1");
			 ExtendedRConnection c = new ExtendedRConnection("giv-uw.uni-muenster.de");
			if (c.needLogin()) {
				// if server requires authentication,
				// send one
				c.login("rserve", "aI2)Jad$%");
			}
			
			c.tryVoidEval("i <- " + numberOfRealisations);
			c.tryVoidEval("m.gauss <- " + mean);
			c.tryVoidEval("sd.gauss <- " + stdDev);
			REXP samples =  c.tryEval("rnorm(i, m.gauss, sd.gauss)");
			
			double[] sampleDoubleArray = samples.asDoubles();
			
			String outputString = "";
			
			for (double d : sampleDoubleArray) {
				outputString = outputString.concat(d + ";");
			}
			
			LiteralStringBinding outputBinding = new LiteralStringBinding(outputString);
			
			result.put("output", outputBinding);
			
			return result;
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		return null;
	}

}
