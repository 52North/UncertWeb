package org.uncertweb.wps;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.randomvariable.INormalCovarianceParameter;
import org.uncertml.distribution.randomvariable.NormalSpatialField;
import org.uncertml.distribution.randomvariable.VariogramFunction;
import org.uncertweb.netcdf.NcUwFile;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebDataConstants;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.netcdf.NetCDFParser;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.n52.wps.util.r.process.RProcessException;

public class RandomField2Samples extends AbstractAlgorithm{

	private static Logger LOGGER = LoggerFactory.getLogger(SpatialDistribution2Samples.class);

	private final String INPUT_ID_DATA = "InputData";
	//private final String INPUT_ID_VARIOGRAM = "VariogramFunction";
	private final String INPUT_ID_NOS = "NumberOfSimulations";
	private final String INPUT_ID_VAR = "Variable";
	private final String OUTPUT_ID_SAMPLES = "Samples";

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getInputDataType(String id) {
		if (id.equalsIgnoreCase(INPUT_ID_DATA)){
			return UncertWebIODataBinding.class;
		}
		else if (id.equals(INPUT_ID_NOS)){
			return LiteralIntBinding.class;
		}
		else if (id.equals(INPUT_ID_VAR)){
			return LiteralStringBinding.class;
		}
		else return null;
	}


	@Override
	public Class getOutputDataType(String id) {
		if (id.equals(OUTPUT_ID_SAMPLES)){
			return UncertWebIODataBinding.class;
		}
		else
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		// WPS specific stuff; initialize result set
		Map<String, IData> result = new HashMap<String, IData>(1);
		try {

			// get netCDF file containing the Gaussian Distributions
			IData dataInput = inputData.get(INPUT_ID_DATA).get(0);


			//TODO support U-OM as input
			if (dataInput instanceof UncertMLBinding){

			IUncertainty randomField = ((UncertMLBinding)dataInput).getPayload();


			if (randomField instanceof NormalSpatialField){

				NormalSpatialField nsField = (NormalSpatialField)randomField;

				//extract samples
				URL sampleRef  = nsField.getSamples().getReference();
				String mimeType = nsField.getSamples().getMimeType();

				if (!UncertWebDataConstants.isMimeTypeNetCDF(mimeType)){
					throw new RuntimeException("Currently, only NetCDF is supported for samples in normal fields!!");
				}

				NetCDFParser parser = new NetCDFParser();
				NcUwFile unetCdfFile = ((NetCDFBinding)parser.parse(sampleRef.openStream(),mimeType,null)).getPayload();

				//extract variogramFunction
				INormalCovarianceParameter covParameter = nsField.getCovarianceParameter();

				if (!(covParameter instanceof VariogramFunction)){
					throw new RuntimeException("Currently, only variogramFunction is supported as covariance parameter in normal fields!!");
				}

				VariogramFunction vgf = (VariogramFunction)covParameter;

				//extract number of simulations inputs
				IData numbRealsInput = inputData.get(INPUT_ID_NOS)
						.get(0);
				Integer intNSimulations = ((LiteralIntBinding) numbRealsInput)
						.getPayload();

				IData varInput = inputData.get(INPUT_ID_VAR).get(0);
				String varName = ((LiteralStringBinding) varInput).getPayload();

				//run spatial simulation
				NcUwFile resultFile = getSimulations4NCFile(unetCdfFile,
						intNSimulations,vgf,varName);


				NetCDFBinding uwData = new NetCDFBinding(resultFile);
				result.put(OUTPUT_ID_SAMPLES, uwData);

				}
			}
			//TODO add support for O&M and UncertML

		} catch (Exception e) {
			LOGGER
					.debug("Error while getting random samples for Gaussian distribution: "
							+ e.getMessage());
			throw new RuntimeException(
					"Error while getting random samples for Gaussian distribution: "
							+ e.getMessage(), e);
		}


		return result;

	}

	/**
	 * helper method that runs the spatial simulations
	 *
	 * @param inputFile
	 * @param intNSimulations
	 * @param vgFunction
	 * @return
	 */
	private NcUwFile getSimulations4NCFile(NcUwFile inputFile,
			Integer intNSimulations, VariogramFunction vgFunction, String varName) {

		NcUwFile result=null;
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String outputFilePath = tmpDirPath + "/aggResult"+System.currentTimeMillis()+".nc";
		outputFilePath = outputFilePath.replace("\\", "/");

		String inputFilePath = inputFile.getFile().getLocation();
		inputFilePath = inputFilePath.replace("\\","/");

		//initialize R Connection
		// get number of realisations
		// establish connection to Rserve running on localhost
		ExtendedRConnection c=null;
		try {
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// TODO if server requires authentication,
				c.login("rserve", "aI2)Jad$%");
			}

			//create
			File tmpDir = new File(tmpDirPath);
			if (!tmpDir.exists()) {
				tmpDir.mkdir();
			}


			//Run R Script
			//load libraries
			c.tryVoidEval("library(gstat)");

			//set FilePath in R
			c.tryVoidEval("file <- \""+inputFilePath+"\"");


			//read NetCDF file with passed variables
			String rCmd = "spUNetCDF <- readUNetCDF(file, variables=c(\""+varName+"\"))";
			c.tryVoidEval(rCmd);
			c.tryVoidEval("colnames(spUNetCDF@data) <- \"biotemp\"");
			c.tryVoidEval("spUNetCDF <- as(spUNetCDF,\"SpatialPointsDataFrame\")");

			//TODO cutoff
			//TODO add additional parameter indicating whether variogram function  should be fitted or not!
			rCmd = "empVgm <- variogram(biotemp~1,spUNetCDF[!is.na(spUNetCDF$biotemp),])";
			c.tryVoidEval(rCmd);
			rCmd = "fitVgm <- fit.variogram(empVgm,vgm("+vgFunction.getSill()+",\""+vgFunction.getModel().name()+"\","+vgFunction.getRange()+","+vgFunction.getNugget()+"))";
			c.tryVoidEval(rCmd);
			c.tryVoidEval("extent <- SpatialPolygons(list(Polygons(list(Polygon(matrix(c(5, 45,5,55,15,55,15,45,5,45),ncol=2,byrow=T))),ID=\"a\")),proj4string=CRS(proj4string(spUNetCDF)))");
			c.tryVoidEval("subSet <- overlay(extent,spUNetCDF[!is.na(spUNetCDF$biotemp),])");
			c.tryVoidEval("subSet <- spUNetCDF[!is.na(spUNetCDF$biotemp),][!is.na(subSet),]");
			c.tryVoidEval("simData <- krige(formula=biotemp~1, locations=NULL,newdata=as(subSet,\"SpatialPoints\")[sample(3343,size=400)], model=fitVgm, dummy=T, nsim=10, beta=mean(spUNetCDF$biotemp,na.rm=T))");
			c.tryVoidEval("gridded(simData) <- TRUE");
			//Create response
			c.tryVoidEval("writeUNetCDF(newfile=\""+outputFilePath+"\", simData)");


			result = new NcUwFile(outputFilePath);

		} catch (RserveException e) {
			String msg = "Error while establishing RServe connection: "+e.getLocalizedMessage();
			LOGGER.debug(msg);
			throw new RuntimeException(msg);
		} catch (RProcessException e) {
			String msg = "Error while running R script for aggregation: "+e.getLocalizedMessage();
			LOGGER.debug(msg);
			throw new RuntimeException(msg);
		} catch (IOException e) {
			String msg = "Error while running R script for aggregation: "+e.getLocalizedMessage();
			LOGGER.debug(msg);
			throw new RuntimeException(msg);
		}
		//check whether RServe connection is closed
		finally {
			if (c != null) {
				c.close();
			}
		}

		return result;
	}
}
