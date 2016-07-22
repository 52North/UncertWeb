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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.sample.RandomSample;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.netcdf.INcUwVariable;
import org.uncertweb.netcdf.NcUwConstants;
import org.uncertweb.netcdf.NcUwFile;
import org.uncertweb.netcdf.NcUwVariableWithDimensions;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertMLBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
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
 * Process for taking random samples from a Gaussian distribution
 *
 * @author staschc, Benjamin Pross, Lydia Gerharz
 *
 */
public class Gaussian2Samples extends AbstractAlgorithm {

	private static Logger LOGGER = LoggerFactory.getLogger(Gaussian2Samples.class);

	// //////////////////////////////////////////////////////
	// constants for input/output identifiers
	private final static String INPUT_IDENTIFIER_DIST = "distribution";
	private final static String INPUT_IDENTIFIER_NUMB_REAL = "numbReal";
	private final static String OUTPUT_IDENTIFIER_SAMPLES = "samples";

	public Gaussian2Samples() {
		super();
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		// WPS specific stuff; initialize result set
		Map<String, IData> result = new HashMap<String, IData>(1);
		try {

			// get number of realisations
			IData numbRealsInput = inputData.get(INPUT_IDENTIFIER_NUMB_REAL)
					.get(0);
			Integer intNRealisations = ((LiteralIntBinding) numbRealsInput)
					.getPayload();

			// get input file containing the Gaussian Distributions
			IData dataInput = inputData.get(INPUT_IDENTIFIER_DIST).get(0);

			// support for NetCDF
			if (dataInput instanceof NetCDFBinding) {

				NcUwFile uwNcdfFile = ((NetCDFBinding) dataInput).getPayload();

				NcUwFile resultFile = getSamples4GaussianNCFile(uwNcdfFile,
						intNRealisations);

				NetCDFBinding uwData = new NetCDFBinding(resultFile);
				result.put(OUTPUT_IDENTIFIER_SAMPLES, uwData);

			}
			// support for O&M and UncertML
			else if (dataInput instanceof OMBinding) {
				UncertaintyObservationCollection uwColl = (UncertaintyObservationCollection) dataInput
						.getPayload();

				UncertaintyObservationCollection resultFile = getSamples4GaussianOMFile(
						uwColl, intNRealisations);

				OMBinding uwData = new OMBinding(resultFile);
				result.put(OUTPUT_IDENTIFIER_SAMPLES, uwData);
			}
			// support for plain UncertML
			else if (dataInput instanceof UncertMLBinding) {
				IUncertainty distribution = (IUncertainty) dataInput
						.getPayload();

				IUncertainty results = getSamples4UncertML(distribution,
						intNRealisations);

				UncertMLBinding uwData = new UncertMLBinding(results);
				result.put(OUTPUT_IDENTIFIER_SAMPLES, uwData);
			} else {
				LOGGER.error("Input data format is not supported!");
				throw new IOException("Input data format is not supported!");
			}

		} catch (Exception e) {
			LOGGER.debug("Error while getting random samples for Gaussian distribution: "
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

	/**
	 * Method for UncertML sampling
	 *
	 * @param distribution
	 * @param intNRealisations
	 * @return
	 */
	private IUncertainty getSamples4UncertML(IUncertainty distribution,
			Integer intNRealisations) {
		IUncertainty resultUncertainty = null;

		ExtendedRConnection c = null;
		try {
			// Perform R computations
			c = new ExtendedRConnection("127.0.0.1");
			if (c.needLogin()) {
				// if server requires authentication, send one
				c.login("rserve", "aI2)Jad$%");
			}
			if (distribution instanceof NormalDistribution) {
				NormalDistribution normDist = (NormalDistribution) distribution;
				List<Double> mean = normDist.getMean();
				List<Double> var = normDist.getVariance();

				// define parameters in R
				c.tryVoidEval("i <- " + intNRealisations);
				c.tryVoidEval("m.gauss <- " + mean.get(0));
				c.tryVoidEval("var.gauss <- " + var.get(0));
				c.tryVoidEval("sd.gauss <- sqrt(var.gauss)");

				// perform sampling
				REXP rSamples = c
						.tryEval("round(rnorm(i, m.gauss, sd.gauss),digits=5)");
				double[] samples = rSamples.asDoubles();

				// resultUncertainty = new ContinuousRealisation(samples);
				// TODO: Should we use RandomSample?
				// create UncertML random sample
				ContinuousRealisation cr = new ContinuousRealisation(samples);
				ContinuousRealisation[] crList = new ContinuousRealisation[1];
				crList[0] = cr;
				resultUncertainty = new RandomSample(crList, "random");
			} else {
				throw new RuntimeException(
						"Input with ID distribution must be a gaussian distribution!");
			}

			return resultUncertainty;

		} catch (Exception e) {
			LOGGER.debug("Error while getting random samples for Gaussian distribution: "
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
	 * Method for OM sampling
	 *
	 * @param inputColl
	 * @param intNRealisations
	 * @return
	 */
	private UncertaintyObservationCollection getSamples4GaussianOMFile(
			UncertaintyObservationCollection inputColl, Integer intNRealisations) {
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
				if (obs instanceof UncertaintyObservation) {
					// get UncertML distribution
					UncertaintyResult uResult = (UncertaintyResult) obs
							.getResult();
					IUncertainty distribution = uResult.getUncertaintyValue();

					// get samples for this distribution
					if (distribution instanceof NormalDistribution) {
						NormalDistribution normDist = (NormalDistribution) distribution;
						List<Double> mean = normDist.getMean();
						List<Double> var = normDist.getVariance();

						// define parameters in R
						c.tryVoidEval("i <- " + intNRealisations);
						c.tryVoidEval("m.gauss <- " + mean.get(0));
						c.tryVoidEval("var.gauss <- " + var.get(0));
						c.tryVoidEval("sd.gauss <- sqrt(var.gauss)");

						// perform sampling
						REXP rSamples = c
								.tryEval("round(rnorm(i, m.gauss, sd.gauss),digits=5)");
						double[] samples = rSamples.asDoubles();

						// create UncertML random sample
						ContinuousRealisation cr = new ContinuousRealisation(
								samples);
						// TODO: Should we use RandomSample?
						ContinuousRealisation[] crList = new ContinuousRealisation[1];
						crList[0] = cr;
						RandomSample rs = new RandomSample(crList, "random");

						// make new observation with samples
						UncertaintyResult newResult = new UncertaintyResult(rs);
						newResult.setUnitOfMeasurement(uResult
								.getUnitOfMeasurement());

						UncertaintyObservation newObs = new UncertaintyObservation(
								obs.getIdentifier(), obs.getBoundedBy(),
								obs.getPhenomenonTime(), obs.getResultTime(),
								obs.getValidTime(), obs.getProcedure(),
								obs.getObservedProperty(),
								obs.getFeatureOfInterest(),
								obs.getResultQuality(), newResult);

						// add observation to new collection
						resultColl.addObservation(newObs);
					} else {
						throw new RuntimeException(
								"Input with ID distribution must be a gaussian distribution!");
					}
				} else {
					throw new RuntimeException(
							"Input with ID distribution must contain uncertainty observations!");
				}
			}

			return resultColl;

		} catch (Exception e) {
			LOGGER.debug("Error while getting random samples for Gaussian distribution: "
					+ e.getMessage());
			throw new RuntimeException(
					"Error while getting random samples for Gaussian distribution: "
							+ e.getMessage(), e);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	/**
	 * Method for NetCDF sampling
	 *
	 * @param inputFile
	 * @param intNRealisations
	 * @return
	 */
	private NcUwFile getSamples4GaussianNCFile(NcUwFile inputFile,
			Integer intNRealisations) {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String missingValue = "-999.0";
		NetcdfUWFileWriteable resultNCFile = null;
		ExtendedRConnection c = null;
		try {
			Set<INcUwVariable> primaryVariables = inputFile
					.getPrimaryVariables();
			if (primaryVariables.size() != 1) {
				throw new RuntimeException(
						"Sampling operation only supported for NetCDF-U files with only one variable!");
			}

			Variable meanVar = null;
			Variable varianceVar = null;

			INcUwVariable primVar = (INcUwVariable) primaryVariables.toArray()[0];
			if (primVar.isUncertaintyVariable()
					&& primVar.getType().getUri().toString()
							.equals(UncertML.getURI(NormalDistribution.class))) {

				Iterator<? extends INcUwVariable> ancVars = primVar
						.getAncillaryVariables().iterator();
				while (ancVars.hasNext()) {
					NcUwVariableWithDimensions ancVar = (NcUwVariableWithDimensions) ancVars
							.next();
					if (ancVar.getName().contains("mean")) {
						meanVar = ancVar.getVariable();
						Attribute mvAttr = meanVar.findAttribute(NcUwConstants.Attributes.MISSING_VALUE);
						if (mvAttr!=null){
							missingValue = mvAttr.getNumericValue().toString();
						}
					} else if (ancVar.getName().contains("variance")) {
						varianceVar = ancVar.getVariable();
					}
				}

			} else {
				throw new RuntimeException(
						"Input NetCDF-U file has to contain NormalDistribution as input!");
			}

			// get variables for mean and variance of mean and variance from
			// list of ancillary variables
			// List<Variable> ancillaryVariables = inputFile
			// .getAncillaryVariables();
			// for (int i = 0; i < ancillaryVariables.size(); i++) {
			// Variable var = ancillaryVariables.get(i);
			// String ref =
			// var.findAttribute(NetCDFConstants.REF_ATTR_NAME).getStringValue();
			//
			// // TODO correct to UncertML URIs!!
			// if (ref
			// .equalsIgnoreCase("http://www.uncertml.org/distributions/normal#mean"))
			// {
			// meanVar = var;
			// }
			// // TODO correct to UncertML URIs!!
			// else if (ref
			// .equalsIgnoreCase("http://www.uncertml.org/distributions/normal#variance"))
			// {
			// varianceVar = var;
			// }
			// }

			// get number of realisations
			String numberOfRealisations = String.valueOf(intNRealisations);
			// LOGGER.debug("Computing " + numberOfRealisations
			// + " samples for gaussian variable "
			// + primaryVariable.getName());

			// establish connection to Rserve running on localhost
			c = new ExtendedRConnection("127.0.0.1");
			// c = new ExtendedRConnection("localhost");
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
			String absoluteResultFilePath = tmpDirPath + File.separatorChar
					+ "netCDFresult_" + System.currentTimeMillis() + ".nc";
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
			while (dimensions.hasNext()) {
				Dimension dim = dimensions.next();
				String latVarName = "lat";
				String lonVarName = "lon";
				String unitsName = NcUwConstants.Attributes.UNITS;
				if (dim.getName().equals(latVarName)) {
					if (!resultFile.isDefineMode()) {
						resultFile.setRedefineMode(true);
					}
					latDim = dim;
					resultFile.addDimension(null, dim);
					resultFile.addVariable(latVarName, DataType.FLOAT,
							new Dimension[] { dim });
					resultFile.addVariableAttribute(latVarName, unitsName,
							NcUwConstants.UNIT_LATITUDE);
					resultFile.setRedefineMode(false);
					resultFile.write(latVarName, inputFile.getFile()
							.findVariable(latVarName).read());
				} else if (dim.getName().equals(lonVarName)) {
					if (!resultFile.isDefineMode()) {
						resultFile.setRedefineMode(true);
					}
					longDim = dim;
					resultFile.addDimension(null, dim);
					resultFile.addVariable(lonVarName, DataType.FLOAT,
							new Dimension[] { dim });
					resultFile.addVariableAttribute(lonVarName, unitsName,
							NcUwConstants.UNIT_LONGITUDE);
					resultFile.setRedefineMode(false);
					resultFile.write(lonVarName, inputFile.getFile()
							.findVariable(lonVarName).read());
				}
			}



			ArrayList<Dimension> dims = new ArrayList<Dimension>(2);
			dims.add(latDim);
			dims.add(longDim);

			// add samples variable and add units and missing value attributes
			// from input file
			//add samples variable and add units and missing value attributes from input file
			Variable samplesVariable = resultNCFile.addSampleVariable(
					primVar.getName(), DataType.DOUBLE, dims,
					RandomSample.class, intNRealisations);
			samplesVariable.addAttribute(inputFile.getFile().findVariable(primVar.getName()).findAttribute(NcUwConstants.Attributes.UNITS));
			samplesVariable.addAttribute(new Attribute(NcUwConstants.Attributes.MISSING_VALUE,missingValue));
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
					primVar.getName(), samplesArray);
			resultNCFile.getNetcdfFile().close();
			return new NcUwFile(absoluteResultFilePath);
		} catch (Exception e) {
			LOGGER.error("Error while getting random samples from gaussian distribution", e);
			throw new RuntimeException(
					"Error while getting random samples from gaussian distribution: "
							+ e.getMessage(), e);
		} finally {
			if (c != null) {
				c.close();
			}
		}

	}
}
