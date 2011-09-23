package org.uncertweb.wps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.uncertml.UncertML;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.UnknownSample;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Process for calculating simple statistics from Realisations
 * 
 * @author staschc, Benjamin Pross, Lydia Gerharz
 * 
 */
public class Samples2Statistics extends AbstractAlgorithm {

	private static Logger LOGGER = Logger.getLogger(Samples2Statistics.class);

	// //////////////////////////////////////////////////////
	// constants for input/output identifiers
	private final static String INPUT_IDENTIFIER_SAMPLES = "samples";
	private final static String INPUT_IDENTIFIER_STAT = "statistics";
	private final static String OUTPUT_IDENTIFIER_STAT = "statistics";
	private final static String LAT_VAR_NAME = "lat";
	private final static String LON_VAR_NAME = "lon";
	private final static String UNITS_ATTR_NAME = "units";
	private final static String MV_ATTR_NAME = "missing_value";
	private final static String REF_ATTR_NAME = "ref";
	private static final String REAL_VAR_NAME = "realisation";

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		// WPS specific stuff; initialize result set
		Map<String, IData> result = new HashMap<String, IData>(1);
		try {

			// get netCDF file containing the Gaussian Distributions
			IData ncdfInput = inputData.get(INPUT_IDENTIFIER_SAMPLES).get(0);

			String errorMsg = "No NetCDF-U data can be loaded from input reference!";
			if (!(ncdfInput.getPayload() instanceof UncertWebData)) {
				LOGGER.error(errorMsg);
				throw new IOException(errorMsg);
			}
			UncertWebData uwNcdfInput = (UncertWebData) ncdfInput.getPayload();
			if (!uwNcdfInput.getMimeType().equalsIgnoreCase(
					GenericFileDataConstants.MIME_TYPE_NETCDFX)) {
				LOGGER.error(errorMsg);
				throw new IOException(errorMsg);
			}

			NetcdfUWFile uwNcdfFile = uwNcdfInput.getNcdfFile();
			
			//extract statistics parameters
			List<IData> statistics = inputData.get(INPUT_IDENTIFIER_STAT);
			if (statistics == null || statistics.size()==0){
				errorMsg="No statistics parameter are contained in request for samples2statistics transformation!!";
				LOGGER.error(errorMsg);
				throw new IOException(errorMsg);
			}
			List<String> statParams = extractStatisticsFromRequest(statistics);
			NetcdfUWFile resultFile = getStatistics4Samples(uwNcdfFile,
				statParams);

			// create resultfile
			String fileLocation = resultFile.getNetcdfFile().getLocation();
			File file = new File(fileLocation);
			UncertWebData uwNcdfOutput = new UncertWebData(file,
					GenericFileDataConstants.MIME_TYPE_NETCDFX);
			UncertWebDataBinding uwData = new UncertWebDataBinding(uwNcdfOutput);
			result.put(OUTPUT_IDENTIFIER_STAT, uwData);

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

	private NetcdfUWFile getStatistics4Samples(NetcdfUWFile inputFile,
			List<String> statParams) {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		NetcdfUWFileWriteable resultNCFile = null;
		ExtendedRConnection c = null;
		try {
			Variable primaryVariable = inputFile.getPrimaryVariable();
			//check whether primaryVariable is random or unknown sample
			Attribute ref = primaryVariable.findAttribute(REF_ATTR_NAME);
			//TODO remove second URI provided in example!
			if (ref==null||!(ref.getStringValue().equals(UncertML.getURI(RandomSample.class))|| ref.getStringValue().equals("http://www.uncertml.org/samples/random")||ref.getStringValue().equals("http://www.uncertml.org/samples/unknown")||ref.getStringValue().equals(UncertML.getURI(UnknownSample.class)))){
				throw new IOException("Primary variable in Input NetCDF file for samples2statistics process has to be a random sample.");
			}
			
			//get missing value
			Attribute mvAttr = primaryVariable.findAttribute(MV_ATTR_NAME);
			double missingVal = Double.NaN;
			if (mvAttr!=null){
				missingVal = mvAttr.getNumericValue().doubleValue();
			}
			
			// establish connection to Rserve running on localhost
			 c = new ExtendedRConnection("127.0.0.1");
//			c = new ExtendedRConnection("localhost");
			if (c.needLogin()) {
				// TODO if server requires authentication,
				c.login("rserve", "aI2)Jad$%");
			}

			File tmpDir = new File(tmpDirPath);
			if (!tmpDir.exists()) {
				tmpDir.mkdir();
			}

			String absoluteResultFilePath = tmpDirPath + File.separatorChar + "netCDFresult_"
					+ System.currentTimeMillis() + ".nc";
			LOGGER.debug("Writing samples to file " + absoluteResultFilePath);

			// prepare new netCDF file for realisations
			NetcdfFileWriteable resultFile = NetcdfUWFileWriteable.createNew(
					absoluteResultFilePath, true);
			resultNCFile = new NetcdfUWFileWriteable(resultFile);
			
			// adding lat long dimensions and variables to output file
			Iterator<Dimension> dimensions = inputFile.getNetcdfFile()
					.getDimensions().iterator();
			Dimension latDim = null;
			Dimension longDim = null;
			Dimension realDim = null;
			while (dimensions.hasNext()) {
				Dimension dim = dimensions.next();
				if (dim.getName().equals(LAT_VAR_NAME)) {
					if (!resultFile.isDefineMode()) {
						resultFile.setRedefineMode(true);
					}
					latDim = dim;
					resultFile.addDimension(null, dim);
					resultFile.addVariable(LAT_VAR_NAME, DataType.FLOAT,
							new Dimension[] { dim });
					resultFile.addVariableAttribute(LAT_VAR_NAME,
							UNITS_ATTR_NAME, "degrees_north");
					resultFile.setRedefineMode(false);
					resultFile.write(LAT_VAR_NAME, inputFile.getNetcdfFile()
							.findVariable(LAT_VAR_NAME).read());
					
				} else if (dim.getName().equals(LON_VAR_NAME)) {
					if (!resultFile.isDefineMode()) {
						resultFile.setRedefineMode(true);
					}
					longDim = dim;
					resultFile.addDimension(null, dim);
					resultFile.addVariable(LON_VAR_NAME, DataType.FLOAT,
							new Dimension[] { dim });
					resultFile.addVariableAttribute(LON_VAR_NAME,
							UNITS_ATTR_NAME, "degrees_east");
					resultFile.setRedefineMode(false);
					resultFile.write(LON_VAR_NAME, inputFile.getNetcdfFile()
							.findVariable(LON_VAR_NAME).read());
				}
				else if (dim.getName().equals(REAL_VAR_NAME)){
					realDim=dim;
				}
			}

			ArrayList<Dimension> dims = new ArrayList<Dimension>(2);
			dims.add(latDim);
			dims.add(longDim);

			//add samples variables and add units and missing value attributes from input file
			HashMap<String,Variable> statVars4statName = new HashMap<String,Variable>(statParams.size());
			Iterator<String> statParamIterator = statParams.iterator();
			String primaryVarName = primaryVariable.getName();
			while (statParamIterator.hasNext()){
				String statistic = statParamIterator.next().replace(UncertML.STATISTIC_URI, "");
				String varName = primaryVarName + "_" + statistic;
				if (statistic.equalsIgnoreCase("mean")){
					Variable statisticVar = resultNCFile.addStatisticVariable(varName, DataType.DOUBLE, dims, org.uncertml.statistic.Mean.class);
					statVars4statName.put(statistic, statisticVar);
					resultNCFile.setPrimaryVariable(statisticVar);
				}
				else if (statistic.equals("standard-deviation")){
					Variable statisticVar = resultNCFile.addStatisticVariable(varName, DataType.DOUBLE, dims, org.uncertml.statistic.StandardDeviation.class);
					statVars4statName.put(statistic, statisticVar);
					//resultNCFile.setPrimaryVariable(statisticVar);
				}
			}


			// running sample generation in R and adding values to output file
			Array samplesArray = primaryVariable.read();
			ArrayDouble meanArray = new ArrayDouble.D2(latDim.getLength(), longDim.getLength());
			ArrayDouble sdArray = new ArrayDouble.D2(latDim.getLength(), longDim.getLength());
			int numbOfRealisations = realDim.getLength();
			double[] values = new double[numbOfRealisations];
			int i, j, k;
			Index samplesIndex = samplesArray.getIndex();
			Index meanIndex = meanArray.getIndex();
			Index sdIndex = sdArray.getIndex();
			for (i = 0; i < latDim.getLength(); i++) {
				for (j = 0; j < longDim.getLength(); j++) {
					for (k=0;k<numbOfRealisations;k++){
						samplesIndex.set(k,i,j);
						double value = samplesArray.getDouble(samplesIndex);
						if (value==missingVal){
							values[k] = Double.NaN;
						}
						else {
							values[k]= value;
						}
					}
					REXPDouble d = new REXPDouble(values);
					c.assign("samples", d);
					
					if (statVars4statName.containsKey("mean")){
						REXP mean =  c.tryEval("mean(samples)");
						double meanD = mean.asDouble();
						meanIndex.set(i,j);
						meanArray.setDouble(meanIndex, meanD);
					}
					
					if (statVars4statName.containsKey("standard-deviation")){
						REXP sdR = c.tryEval("sd(samples)");
						double sd = sdR.asDouble();
						sdIndex.set(i,j);
						sdArray.setDouble(sdIndex, sd);
					}

				}
			}
			//write result array to NetCDF file
			resultNCFile.getNetcdfFileWritable().setRedefineMode(false);
			if (statVars4statName.containsKey("mean")){
				resultNCFile.getNetcdfFileWritable().write(primaryVarName+"_mean",meanArray);
			}
			if (statVars4statName.containsKey("standard-deviation")){
				resultNCFile.getNetcdfFileWritable().write(primaryVarName+"_standard-deviation",sdArray);
			}
			resultNCFile.getNetcdfFile().close();
		} catch (Exception e) {
			LOGGER.error(e);
			throw new RuntimeException(
					"Error while getting random samples from gaussian distribution: "
							+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}
		return resultNCFile;

	}

	@Override
	public Class getInputDataType(String id) {
		if (id.equals(INPUT_IDENTIFIER_SAMPLES)) {
			return UncertWebDataBinding.class;
		} else if (id.equals(INPUT_IDENTIFIER_STAT)) {
			return LiteralIntBinding.class;
		}
		return null;
	}

	@Override
	public Class getOutputDataType(String id) {
		if (id.equals(OUTPUT_IDENTIFIER_STAT)) {
			return UncertWebDataBinding.class;
		}
		return null;
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	private List<String> extractStatisticsFromRequest(List<IData> statParams){
		List<String> params = new ArrayList<String>(statParams.size());
		Iterator<IData> statParamsIter = statParams.iterator();
		while (statParamsIter.hasNext()){
			IData statParam = statParamsIter.next();
			String statistics = ((LiteralStringBinding) statParam)
			.getPayload();
			params.add(statistics);
		}
		
		return params;
	}

}

