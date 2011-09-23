package org.uncertweb.wps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.util.FileWriter;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.n52.wps.util.r.process.RProcessException;
import org.rosuda.REngine.Rserve.RserveException;

public class Distribution2Probabilities extends AbstractAlgorithm {

	private String inputIdentifierDistribution;
	private String inputIdentifierDatasetName;
	private String inputIdentifierVariogram;
	private String inputIdentifierNumberOfRealisations;
	private String inputIdentifierSamplingMethod;
	private String outputIdentifierRealisations;
	
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		try {			

			String tmpDirPath = System.getProperty("java.io.tmpdir");
			String fileSeparator = System.getProperty("file.separator");			
			
			String baseDir = WebProcessingService.BASE_DIR + fileSeparator + "resources";
			
			//WPS specific stuff
			Map<String, IData> result = new HashMap<String, IData>();			
	
			IData data1 = inputData.get(getInputIdentifierDistribution()).get(0);
			
			IData secondData = inputData.get(getInputIdentifierDatasetName()).get(0);
			
			String parMean = "epratio";
			
			if(secondData instanceof LiteralStringBinding){
				
				parMean = ((LiteralStringBinding)secondData).getPayload();				
			}
			
			File tmpDir = new File(tmpDirPath);
			
			if(!tmpDir.exists()){
				tmpDir.mkdir();
			}
			
			String filename = ((GenericFileDataBinding)data1).getPayload().writeData(tmpDir);
			
			// establish connection to Rserve running on localhost
			ExtendedRConnection c = new ExtendedRConnection("127.0.0.1");
//			ExtendedRConnection c = new ExtendedRConnection("giv-uw.uni-muenster.de");
			if (c.needLogin()) { 
				// if server requires authentication,
				// send one
				c.login("rserve", "aI2)Jad$%");
			}
			
			//load libraries
			c.tryEval("library(ncdf)");
			c.tryEval("library(rgdal)");
			
			//allocate some variables
			c.tryEval("probs <- c(1,5,8,10,12,15,18,20)");
			c.tryVoidEval("par.mean <- \"" + parMean + "\"");
			c.tryVoidEval("par.sd <- paste(\"u\",par.mean,sep=\"\")");
			c.tryVoidEval("dir <- \"" + tmpDir.getAbsolutePath().replace("\\", "/") + "/\"");
				
			c.tryVoidEval("nc <- open.ncdf(paste(dir,\"" + new File(filename).getName() +"\",sep=\"\"))");
			
			//load sourcecode for loading a netcdf file
			c.tryVoidEval("source(\"" + baseDir.replace("\\", "/") + "/" + "loadNetCDF.r\")");
			
			//load the source code for the actual probability creation
			c.tryVoidEval("source(\"" + baseDir.replace("\\", "/") + "/" + "exceedanceProbability.r\")");
			
			String outputFilename = Constants.getInstance().getTmpDir().replace("\\", "/") + "/" + "out" + System.currentTimeMillis() + ".nc";		
			
			//create netCDF file
			c.tryEval("nc.sim <- create.ncdf(\"" + outputFilename + "\", simdata)");
			
			//load some more source code (includes writing the file out)
			c.tryVoidEval("source(\"" + baseDir.replace("\\", "/") + "/" + "exceedanceProbability_end.r\")");
			
			File outputFile = new File(outputFilename);
			
			if(!outputFile.exists()){
				return null;
			}
			
			//WPS specific stuff
			GenericFileData outputFileData = new GenericFileData(outputFile, GenericFileDataConstants.MIME_TYPE_NETCDF);
			
			GenericFileDataBinding outputData = new GenericFileDataBinding(outputFileData);
					
			result.put(getOutputIdentifierRealisations(), outputData);//TODO die tifs werden immer zweimal geschrieben...auch bei grass, evtl. da mal gucken
			
			return result;
			
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		return null;
	}
	
	@Override
	public Class getInputDataType(String id) {	
	
		if(id.equals(getInputIdentifierDistribution())){
			return GenericFileDataBinding.class;
		}else if(id.equals(getInputIdentifierVariogram())){
			return GenericFileDataBinding.class;
		}else if(id.equals(getInputIdentifierNumberOfRealisations())){
			return LiteralIntBinding.class;
		}else if(id.equals(getInputIdentifierSamplingMethod())){
			return LiteralStringBinding.class;
		}		
		return null;
	}

	@Override
	public Class getOutputDataType(String id) {
		if(id.equals(getOutputIdentifierRealisations())){
			return GenericFileDataBinding.class;
		}
		return null;
	}

	public String getInputIdentifierDistribution() {
		if(inputIdentifierDistribution == null){
			inputIdentifierDistribution = "distribution";
		}
		return inputIdentifierDistribution;
	}

	public String getInputIdentifierVariogram() {
		if(inputIdentifierVariogram == null){
			inputIdentifierVariogram = "variogram";
		}
		return inputIdentifierVariogram;
	}

	public String getInputIdentifierNumberOfRealisations() {
		if(inputIdentifierNumberOfRealisations == null){
			inputIdentifierNumberOfRealisations = "numbReal";
		}
		return inputIdentifierNumberOfRealisations;
	}

	public String getInputIdentifierSamplingMethod() {
		if(inputIdentifierSamplingMethod == null){
			inputIdentifierSamplingMethod = "samplMethod";
		}
		return inputIdentifierSamplingMethod;
	}

	public String getOutputIdentifierRealisations() {
		if(outputIdentifierRealisations == null){
			outputIdentifierRealisations = "probabilities";
		}
		return outputIdentifierRealisations;
	}

	public String getInputIdentifierDatasetName() {
		if(inputIdentifierDatasetName == null){
			inputIdentifierDatasetName = "datasetNameMean";
		}
		return inputIdentifierDatasetName;
	}
	
	@Override
	public List<String> getErrors() {
		return null;
	}

}
