package org.uncertweb.wps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.sample.AbstractSample;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.ISample;
import org.uncertml.sample.RandomSample;
import org.uncertml.sample.UnknownSample;
import org.uncertml.statistic.ConfidenceInterval;
import org.uncertml.statistic.InterquartileRange;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.Quantile;
import org.uncertml.statistic.Range;
import org.uncertml.statistic.StandardDeviation;
import org.uncertml.statistic.StatisticCollection;
import org.uncertml.statistic.Variance;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.netcdf.INcUwVariable;
import org.uncertweb.netcdf.NcUwFile;
import org.uncertweb.netcdf.NcUwVariableWithDimensions;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.util.r.process.ExtendedRConnection;

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

	private static Logger LOGGER = LoggerFactory.getLogger(Samples2Statistics.class);

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

	public Samples2Statistics(){
		super();
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		// WPS specific stuff; initialize result set
		Map<String, IData> result = new HashMap<String, IData>(1);
		try {

			//extract statistics parameters
			List<IData> statistics = inputData.get(INPUT_IDENTIFIER_STAT);
			if (statistics == null || statistics.size()==0){
				String errorMsg="No statistics parameter are contained in request for samples2statistics transformation!!";
				LOGGER.error(errorMsg);
				throw new IOException(errorMsg);
			}

			List<String> statParams = extractStatisticsFromRequest(statistics);

			// get input file containing the Gaussian Distributions
			IData dataInput = inputData.get(INPUT_IDENTIFIER_SAMPLES).get(0);

			// support for NetCDF
			if (dataInput instanceof NetCDFBinding){
				// get netCDF file containing the Gaussian Distributions
				NcUwFile uwNcdfFile = ((NetCDFBinding) dataInput).getPayload();
				NcUwFile resultFile = getStatistics4SamplesNCFile(uwNcdfFile,
					statParams);

				// create resultfile
				NetCDFBinding uwNcdfOutput = new NetCDFBinding(resultFile);
				result.put(OUTPUT_IDENTIFIER_STAT, uwNcdfOutput);
			}
			// support for O&M and UncertML
			else if(dataInput instanceof OMBinding){
				UncertaintyObservationCollection uwColl = (UncertaintyObservationCollection)dataInput.getPayload();
				UncertaintyObservationCollection resultFile = getStatistics4SamplesOMFile(uwColl,
						statParams);

				OMBinding uwData = new OMBinding(resultFile);
				result.put(OUTPUT_IDENTIFIER_STAT, uwData);
			}
			// support for plain UncertML
			else if(dataInput instanceof UncertMLBinding){
				IUncertainty uncertainty = (IUncertainty)dataInput.getPayload();
				IUncertainty results = getStatistics4UncertML(uncertainty, statParams);

				UncertMLBinding uwData = new UncertMLBinding(results);
				result.put(OUTPUT_IDENTIFIER_STAT, uwData);
			}
			else{
				LOGGER.error("Input data format is not supported!");
				throw new IOException("Input data format is not supported!");
			}
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

	private IUncertainty getStatistics4UncertML(IUncertainty uncertainty, List<String> statParams){
		IUncertainty resultUncertainty = null;

		ExtendedRConnection c = null;
		try {
			// Perform R computations
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
			ContinuousRealisation realisations = null;

			// get samples for this distribution
			if(uncertainty instanceof ISample){
				AbstractSample sample = (AbstractSample) uncertainty;
				realisations = (ContinuousRealisation) sample.getRealisations().get(0);
			}
			else if(uncertainty instanceof ContinuousRealisation){
					realisations = (ContinuousRealisation) uncertainty;
			}
			if(realisations!=null){
				double[] values = new double[realisations.getValues().size()];
				for(int i=0; i<values.length; i++){
					values[i] = realisations.getValues().get(i).doubleValue();
				}

				// define parameters in R
				REXPDouble d = new REXPDouble(values);
				c.assign("samples", d);

				// calculate statistics and add them to collection
				StatisticCollection statColl = new StatisticCollection();
				//IteratorystatParams.iterator()
				for(String para : statParams){
					if(para.contains("mean")){
						double mean = c.tryEval("mean(samples)").asDouble();
						statColl.add(new Mean(mean));
					}
					if(para.contains("standard-deviation")){
						double sd = c.tryEval("sd(samples)").asDouble();
						statColl.add(new StandardDeviation(sd));
					}
					if(para.contains("variance")){
						double var = c.tryEval("var(samples)").asDouble();
						statColl.add(new Variance(var));
					}
					if(para.contains("interquartile-range")){
						double lower = c.tryEval("quantile(samples,0.25)").asDouble();
						double upper = c.tryEval("quantile(samples,0.75)").asDouble();
						statColl.add(new InterquartileRange(lower, upper));
					}
					if(para.contains("/range")){
						double min = c.tryEval("min(samples)").asDouble();
						double max = c.tryEval("max(samples)").asDouble();
						statColl.add(new Range(min, max));
					}
					if(para.contains("confidence-interval")){
						double lower = c.tryEval("quantile(samples,0.025)").asDouble();
						double upper = c.tryEval("quantile(samples,0.975)").asDouble();
						statColl.add(new ConfidenceInterval(new Quantile(0.025, lower), new Quantile(0.975, upper)));
					}
				}
				resultUncertainty = statColl;
			}else{
				throw new RuntimeException(
					"Input with ID distribution must be a sample or realisation!");
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
	 * Method to calculate statistics for OM input
	 * @param inputColl
	 * @param statParams
	 * @return
	 */
	private UncertaintyObservationCollection getStatistics4SamplesOMFile(UncertaintyObservationCollection inputColl,
			List<String> statParams){
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
					IUncertainty uncertainty = uResult.getUncertaintyValue();
					ContinuousRealisation realisations = null;

					// get samples for this distribution
//					if(uncertainty instanceof ISample){
//						AbstractSample sample = (AbstractSample) uncertainty;
//						realisations = (ContinuousRealisation) sample.getRealisations().get(0);
//					}
//					else
					if(uncertainty instanceof ContinuousRealisation){
							realisations = (ContinuousRealisation) uncertainty;
					}
					if(realisations!=null){
						double[] values = new double[realisations.getValues().size()];
						for(int i=0; i<values.length; i++){
							values[i] = realisations.getValues().get(i).doubleValue();
						}

						// define parameters in R
						REXPDouble d = new REXPDouble(values);
						c.assign("samples", d);

						// calculate statistics and add them to collection
						StatisticCollection statColl = new StatisticCollection();
						//IteratorystatParams.iterator()
						for(String para : statParams){
							if(para.contains("mean")){
								double mean = c.tryEval("mean(samples)").asDouble();
								statColl.add(new Mean(mean));
							}
							if(para.contains("standard-deviation")){
								double sd = c.tryEval("sd(samples)").asDouble();
								statColl.add(new StandardDeviation(sd));
							}
							if(para.contains("variance")){
								double var = c.tryEval("var(samples)").asDouble();
								statColl.add(new Variance(var));
							}
							if(para.contains("interquartile-range")){
								double lower = c.tryEval("quantile(samples,0.25)").asDouble();
								double upper = c.tryEval("quantile(samples,0.75)").asDouble();
								statColl.add(new InterquartileRange(lower, upper));
							}
							if(para.contains("range")){
								double min = c.tryEval("min(samples)").asDouble();
								double max = c.tryEval("max(samples)").asDouble();
								statColl.add(new Range(min, max));
							}
							if(para.contains("confidence-interval")){
								double lower = c.tryEval("quantile(samples,0.025)").asDouble();
								double upper = c.tryEval("quantile(samples,0.975)").asDouble();
								statColl.add(new ConfidenceInterval(new Quantile(0.025, lower), new Quantile(0.975, upper)));
							}
						}

						// make new observation
						UncertaintyResult newResult = new UncertaintyResult(statColl, "ug/m3");
						newResult.setUnitOfMeasurement(uResult.getUnitOfMeasurement());

						UncertaintyObservation newObs = new UncertaintyObservation(
								obs.getIdentifier(), obs.getBoundedBy(), obs.getPhenomenonTime(),
								obs.getResultTime(), obs.getValidTime(), obs.getProcedure(),
								obs.getObservedProperty(), obs.getFeatureOfInterest(),
								obs.getResultQuality(), newResult);

						// add observation to new collection
						resultColl.addObservation(newObs);
					}else{
						throw new RuntimeException(
							"Input with ID distribution must be a sample or realisation!");
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

	/**
	 * Method for NetCDF samples
	 * @param inputFile
	 * @param statParams
	 * @return
	 */
	private NcUwFile getStatistics4SamplesNCFile(NcUwFile inputFile,
			List<String> statParams) {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		NetcdfUWFileWriteable resultNCFile = null;
		ExtendedRConnection c = null;
		try {

			Set<INcUwVariable> primaryVariables = inputFile
					.getPrimaryVariables();
			if (primaryVariables.size() != 1) {
				throw new RuntimeException(
						"Statistics operation only supported for NetCDF-U files with only one variable!");
			}
			NcUwVariableWithDimensions primVar = (NcUwVariableWithDimensions) primaryVariables.toArray()[0];

			//check whether primaryVariable is random or unknown sample
			Attribute ref = primVar.getVariable().findAttribute(REF_ATTR_NAME);
			//TODO remove second URI provided in example!
			if (ref==null||!(ref.getStringValue().equals(UncertML.getURI(RandomSample.class))|| ref.getStringValue().equals("http://www.uncertml.org/samples/random")||ref.getStringValue().equals("http://www.uncertml.org/samples/unknown")||ref.getStringValue().equals(UncertML.getURI(UnknownSample.class)))){
				throw new IOException("Primary variable in Input NetCDF file for samples2statistics process has to be a random sample.");
			}

			//get missing value
			Attribute mvAttr = primVar.getVariable().findAttribute(MV_ATTR_NAME);
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
			Iterator<Dimension> dimensions = inputFile.getFile()
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
					resultFile.write(LAT_VAR_NAME, inputFile.getFile()
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
					resultFile.write(LON_VAR_NAME, inputFile.getFile()
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
			if (!resultFile.isDefineMode()) {
				resultFile.setRedefineMode(true);
			}

			HashMap<String,Variable> statVars4statName = new HashMap<String,Variable>(statParams.size());
			Iterator<String> statParamIterator = statParams.iterator();
			String primaryVarName = primVar.getName();
			Variable primVariable = resultNCFile.getNetcdfFileWritable().addVariable(primaryVarName,DataType.DOUBLE,dims);
			String ancillaryVariablesValue = "";
			while (statParamIterator.hasNext()){
				String statistic = statParamIterator.next().replace("http://www.uncertml.org/statistics/", "");
				String varName = primaryVarName + "_" + statistic;
				if (statistic.equalsIgnoreCase("mean")){
					Variable statisticVar = resultNCFile.addStatisticVariable(varName, DataType.DOUBLE, dims, org.uncertml.statistic.Mean.class);
					statVars4statName.put(statistic, statisticVar);
					if (ancillaryVariablesValue.isEmpty()){
						ancillaryVariablesValue+=primaryVarName+"_mean";
					} else {
                        ancillaryVariablesValue+=" "+primaryVarName+"_mean";
                    }
					//resultNCFile.setPrimaryVariable(statisticVar);
				}
				else if (statistic.equals("standard-deviation")){
					Variable statisticVar = resultNCFile.addStatisticVariable(varName, DataType.DOUBLE, dims, org.uncertml.statistic.StandardDeviation.class);
					statVars4statName.put(statistic, statisticVar);
					if (ancillaryVariablesValue.isEmpty()){
						ancillaryVariablesValue+=primaryVarName+"_standard-deviation";
					} else {
                        ancillaryVariablesValue+=" "+primaryVarName+"_standard-deviation";
                    }
					//resultNCFile.setPrimaryVariable(statisticVar);
				}
			}

			resultNCFile.getNetcdfFileWritable().addVariableAttribute(primaryVarName, new Attribute("ancillary_variables",ancillaryVariablesValue));
			resultNCFile.getNetcdfFileWritable().addVariableAttribute(primaryVarName, new Attribute("ref","http://www.uncertml.org/statistics/statistics-collection"));
			resultNCFile.setPrimaryVariable(primVariable);

			// running sample generation in R and adding values to output file
			Array samplesArray = primVar.getArray();
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
			return new NcUwFile(absoluteResultFilePath);
		} catch (Exception e) {
			LOGGER.error("Error while getting random samples from gaussian distribution", e);
			throw new RuntimeException(
					"Error while getting random samples from gaussian distribution: "
							+ e.getMessage(), e);
		}
		finally {
			if (c != null) {
				c.close();
			}
		}


	}

	@Override
	public Class<?> getInputDataType(String id) {
		if (id.equals(INPUT_IDENTIFIER_SAMPLES)) {
			return UncertWebIODataBinding.class;
		} else if (id.equals(INPUT_IDENTIFIER_STAT)) {
			return LiteralIntBinding.class;
		}
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		if (id.equals(OUTPUT_IDENTIFIER_STAT)) {
			return UncertWebIODataBinding.class;
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

//	@Override
//	public List<String> getInputIdentifiers() {
//		List<String> identifiers = new ArrayList<String>();
//		identifiers.add(INPUT_IDENTIFIER_SAMPLES);
//		identifiers.add(INPUT_IDENTIFIER_STAT);
//		return identifiers;
//	}
//
//	@Override
//	public List<String> getOutputIdentifiers() {
//		List<String> identifiers = new ArrayList<String>();
//		identifiers.add(OUTPUT_IDENTIFIER_STAT);
//		return identifiers;
//	}

}

