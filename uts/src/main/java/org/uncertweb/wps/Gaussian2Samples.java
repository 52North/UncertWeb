package org.uncertweb.wps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.rosuda.REngine.REXP;
import org.uncertml.sample.RandomSample;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Process for taking random samples from a Gaussian distribution
 * 
 * @author staschc, Benjamin Pross, Lydia Gerharz
 * 
 */
public class Gaussian2Samples extends AbstractAlgorithm {

	private static Logger LOGGER = Logger.getLogger(Gaussian2Samples.class);

	// //////////////////////////////////////////////////////
	// constants for input/output identifiers
	private final static String INPUT_IDENTIFIER_DIST = "distribution";
	private final static String INPUT_IDENTIFIER_NUMB_REAL = "numbReal";
	private final static String OUTPUT_IDENTIFIER_SAMPLES = "samples";
	private final static String LAT_VAR_NAME = "lat";
	private final static String LON_VAR_NAME = "lon";
	private final static String UNITS_ATTR_NAME = "units";
	private final static String MV_ATTR_NAME = "missing_value";
	private final static String REF_ATTR_NAME = "ref";
	
	public Gaussian2Samples(){
		super();
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		// WPS specific stuff; initialize result set
		Map<String, IData> result = new HashMap<String, IData>(1);
		try {

			// get netCDF file containing the Gaussian Distributions
			IData dataInput = inputData.get(INPUT_IDENTIFIER_DIST).get(0);

		
//			UncertWebIODataBinding data = (UncertWebIODataBinding) dataInput.getPayload();
//			
			if (dataInput instanceof NetCDFBinding){
			
			NetcdfUWFile uwNcdfFile = ((NetCDFBinding)dataInput).getPayload();
			IData numbRealsInput = inputData.get(INPUT_IDENTIFIER_NUMB_REAL)
					.get(0);
			Integer intNRealisations = ((LiteralIntBinding) numbRealsInput)
					.getPayload();
			NetcdfUWFile resultFile = getSamples4GaussianNCFile(uwNcdfFile,
					intNRealisations);

			// create resultfile
//			String fileLocation = resultFile.getNetcdfFile().getLocation();
//			File file = new File(fileLocation);
			
//			String fileLocation = resultFile.getNetcdfFile().getLocation();
//			File file = new File(fileLocation);
//		
			NetCDFBinding uwData = new NetCDFBinding(resultFile);
			result.put(OUTPUT_IDENTIFIER_SAMPLES, uwData);
			
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

	private NetcdfUWFile getSamples4GaussianNCFile(NetcdfUWFile inputFile,
			Integer intNRealisations) {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		NetcdfUWFileWriteable resultNCFile = null;
		ExtendedRConnection c = null;
		try {
			Variable primaryVariable = inputFile.getPrimaryVariable();
			Variable meanVar = null;
			Variable varianceVar = null;

			// get variables for mean and variance of mean and variance from
			// list of ancillary variables
			List<Variable> ancillaryVariables = inputFile
					.getAncillaryVariables();
			for (int i = 0; i < ancillaryVariables.size(); i++) {
				Variable var = ancillaryVariables.get(i);
				String ref = var.findAttribute(REF_ATTR_NAME).getStringValue();

				// TODO correct to UncertML URIs!!
				if (ref
						.equalsIgnoreCase("http://www.uncertml.org/distributions/normal#mean")) {
					meanVar = var;
				}
				// TODO correct to UncertML URIs!!
				else if (ref
						.equalsIgnoreCase("http://www.uncertml.org/distributions/normal#variance")) {
					varianceVar = var;
				}
			}

			// get number of realisations
			String numberOfRealisations = String.valueOf(intNRealisations);
			LOGGER.debug("Computing " + numberOfRealisations
					+ " samples for gaussian variable "
					+ primaryVariable.getName());

			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			//c = new ExtendedRConnection("localhost");
			if (c.needLogin()) {
				// TODO if server requires authentication,
				c.login("rserve", "aI2)Jad$%");
			}

			File tmpDir = new File(tmpDirPath);
			if (!tmpDir.exists()) {
				tmpDir.mkdir();
			}

			Array meanArray = meanVar.read();
			Array varArray = varianceVar.read();
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
			}

			ArrayList<Dimension> dims = new ArrayList<Dimension>(2);
			dims.add(latDim);
			dims.add(longDim);

			//add samples variable and add units and missing value attributes from input file
			Variable samplesVariable = resultNCFile.addSampleVariable(
					primaryVariable.getName(), DataType.DOUBLE, dims,
					RandomSample.class, intNRealisations);
			samplesVariable.addAttribute(primaryVariable
					.findAttribute(UNITS_ATTR_NAME));
			samplesVariable.addAttribute(meanVar.findAttribute(MV_ATTR_NAME));
			ArrayDouble samplesArray = new ArrayDouble.D3(intNRealisations,
					latDim.getLength(), longDim.getLength());


			// running sample generation in R and adding values to output file
			int i, j, k;
			Index meanIndex = meanArray.getIndex();
			Index varIndex = varArray.getIndex();
			Index samplesIndex = samplesArray.getIndex();
			for (i = 0; i < latDim.getLength(); i++) {
				for (j = 0; j < longDim.getLength(); j++) {
					meanIndex.set(i, j);
					varIndex.set(i, j);
					double mean = meanArray.getDouble(meanIndex);
					double stdDev = Math.sqrt(varArray.getDouble(varIndex));

					c.tryVoidEval("i <- " + numberOfRealisations);
					c.tryVoidEval("m.gauss <- " + mean);
					c.tryVoidEval("sd.gauss <- " + stdDev);
					REXP samples = c.tryEval("rnorm(i, m.gauss, sd.gauss)");

					double[] sampleDoubleArray = samples.asDoubles();
					for (k = 0; k < sampleDoubleArray.length; k++) {
						samplesIndex.set(k, i, j);
						samplesArray.set(samplesIndex, sampleDoubleArray[k]);
					}

				}
			}
			//write result array to NetCDF file
			resultNCFile.getNetcdfFileWritable().setRedefineMode(false);
			resultNCFile.getNetcdfFileWritable().write(
					primaryVariable.getName(), samplesArray);
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

//	@Override
//	public List<String> getInputIdentifiers() {
//		ArrayList<String> inputIdentifiers = new ArrayList<String>(2);
//		inputIdentifiers.add(INPUT_IDENTIFIER_DIST);
//		inputIdentifiers.add(INPUT_IDENTIFIER_NUMB_REAL);
//		return inputIdentifiers;
//	}
//
//	@Override
//	public List<String> getOutputIdentifiers() {
//		ArrayList<String> outputIdentifiers = new ArrayList<String>(2);
//		outputIdentifiers.add(OUTPUT_IDENTIFIER_SAMPLES);
//		return outputIdentifiers;
//	}

}
