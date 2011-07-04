package org.uncertweb.wps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.n52.wps.util.r.process.RProcessException;
import org.rosuda.REngine.Rserve.RserveException;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Process implementation for converting a gaussian distribution into a set of
 * realisations; process is invoking R using Rserve and then doing the
 * statistical calculations in R
 * 
 * 
 * @author Benjamin Proﬂ, Lydia Gerharz, Christoph Stasch
 * 
 */
public class Distribution2Realisation extends AbstractAlgorithm {

	private String inputIdentifierDistribution;
	private String inputIdentifierDatasetName;
	private String inputIdentifierVariogram;
	private String inputIdentifierNumberOfRealisations;
	private String inputIdentifierSamplingMethod;
	private String outputIdentifierRealisations;

	private static Logger LOGGER = Logger
			.getLogger(Distribution2Realisation.class);

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		try {

			String tmpDirPath = System.getProperty("java.io.tmpdir");
			String fileSeparator = System.getProperty("file.separator");

			String baseDir = WebProcessingService.BASE_DIR + fileSeparator
					+ "resources";

			// WPS specific stuff
			Map<String, IData> result = new HashMap<String, IData>();

			IData data1 = inputData.get(getInputIdentifierDistribution())
					.get(0);

			File tmpDir = new File(tmpDirPath);

			if (!tmpDir.exists()) {
				tmpDir.mkdir();
			}

			// establish connection to Rserve running on localhost
			ExtendedRConnection c = new ExtendedRConnection("127.0.0.1");
			// ExtendedRConnection c = new
			// ExtendedRConnection("giv-uw.uni-muenster.de");
			if (c.needLogin()) {
				// if server requires authentication,
				// send one
				c.login("rserve", "aI2)Jad$%");
			}

			File outputFile = null;

			if (((UncertWebDataBinding) data1).getPayload().getType() == UncertWebData.REFERENCES) {

				HashMap<String, Object> uVMap = ((UncertWebDataBinding) data1)
						.getPayload().getUncertaintyTypesValuesMap();

				String filename1 = "c:\\tmp\\jrcepratio.tif";
				String filename2 = "c:\\tmp\\jrcuepratio.tif";

				for (String s : uVMap.keySet()) {

					if (s.contains("mean")) {

						try {
							URL url = new URL((String) uVMap.get(s));

							InputStream in = url.openStream();

							FileOutputStream fOut = new FileOutputStream(
									new File(filename1));

							int i = 0;

							while ((i = in.read()) != -1) {
								fOut.write(i);
							}

							in.close();
							fOut.close();

						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (s.contains("variance")) {

						try {
							URL url = new URL((String) uVMap.get(s));

							InputStream in = url.openStream();

							FileOutputStream fOut = new FileOutputStream(
									new File(filename2));

							int i = 0;

							while ((i = in.read()) != -1) {
								fOut.write(i);
							}

							in.close();
							fOut.close();

						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				}

				c.tryEval("library(rgdal)");
				c.tryEval("library(automap)");
				c.tryEval("source(\"" + baseDir.replace("\\", "/") + "/"
						+ "make_Realizations.R\")");
				
				filename1 = filename1.replace("\\", "/");
				filename2 = filename2.replace("\\", "/");

				c.tryVoidEval("spdf <- readGDAL(\"" + filename1 + "\")");
				c.tryVoidEval("uspdf <- readGDAL(\"" + filename2 + "\")");

				c.tryVoidEval("nsims <- makeRealizations(spdf, uspdf, nsim = 10)");

				String outputFilename = "c:/tmp/out"
						+ System.currentTimeMillis() + ".tif";

				c.tryEval("writeGDAL(nsims, \"" + outputFilename
						+ "\", driver=\"GTiff\")");

				outputFile = new File(outputFilename);

				if (!outputFile.exists()) {
					return null;
				}

				GenericFileData outputFileData = new GenericFileData(
						outputFile, GenericFileDataConstants.MIME_TYPE_GEOTIFF);

				GenericFileDataBinding outputData = new GenericFileDataBinding(
						outputFileData);

				result.put(getOutputIdentifierRealisations(), outputData);

				return result;// TODO remove tifs

			} else {

				IData secondData1 = null;

				if (data1 != null) {
					secondData1 = inputData.get(
							getInputIdentifierNumberOfRealisations()).get(0);
				}

				IData secondData = inputData.get(
						getInputIdentifierDatasetName()).get(0);

				String parMean = "epratio";

				if (secondData instanceof LiteralStringBinding) {

					parMean = ((LiteralStringBinding) secondData).getPayload();
				}

				String filename = ((UncertWebDataBinding) data1).getPayload()
						.writeData(tmpDir);

				// load libraries
				c.tryEval("library(ncdf)");
				c.tryEval("library(rgdal)");
				c.tryEval("library(automap)");

				// allocate some variables
				c.tryVoidEval("par.mean <- \"" + parMean + "\"");
				c.tryVoidEval("par.sd <- paste(\"u\",par.mean,sep=\"\")");
				c.tryVoidEval("dir <- \""
						+ tmpDir.getAbsolutePath().replace("\\", "/") + "/\"");

				c.tryVoidEval("nc <- open.ncdf(paste(dir,\""
						+ new File(filename).getName() + "\",sep=\"\"))");

				if (secondData1 != null) {

					if (secondData1 instanceof LiteralIntBinding) {
						int numbReal = ((LiteralIntBinding) secondData1)
								.getPayload();
						c.tryVoidEval("ns <- " + numbReal);
					} else if (secondData1 instanceof LiteralLongBinding) {
						long numbReal = ((LiteralLongBinding) secondData1)
								.getPayload();
						c.tryVoidEval("ns <- " + numbReal);
					}

				} else {
					c.tryVoidEval("ns <- 10");
				}

				// load sourcecode for loading a netcdf file
				c.tryVoidEval("source(\"" + baseDir.replace("\\", "/") + "/"
						+ "loadNetCDF.r\")");

				c.tryVoidEval("source(\"" + baseDir.replace("\\", "/") + "/"
						+ "realisations.r\")");

				String outputFilename = Constants.getInstance().getTmpDir()
						.replace("\\", "/")
						+ "/" + "out" + System.currentTimeMillis() + ".nc";

				// create netCDF file
				c.tryEval("nc.sim <- create.ncdf(\"" + outputFilename
						+ "\", simdata)");

				// load some more source code (includes writing the file out)
				c.tryVoidEval("source(\"" + baseDir.replace("\\", "/") + "/"
						+ "realisations_end.r\")");

				outputFile = new File(outputFilename);

				if (!outputFile.exists()) {
					return null;
				}

				outputFile = modifyNetCDFFile(outputFile);

				// WPS specific stuff
				GenericFileData outputFileData = new GenericFileData(
						outputFile, GenericFileDataConstants.MIME_TYPE_NETCDF);

				GenericFileDataBinding outputData = new GenericFileDataBinding(
						outputFileData);

				result.put(getOutputIdentifierRealisations(), outputData);

				return result;

			}

		} catch (RserveException e) {
			e.printStackTrace();
		} catch (RProcessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private File modifyNetCDFFile(File file) {
		File result = null;
		String fileName = file.getAbsolutePath();
		String outputFileName = fileName.replace(".nc", "_output.nc");
		NetcdfFile inputFile = null;
		try {
			inputFile = NetcdfFile.open(fileName);

			NetcdfFileWriteable outputFile = NetcdfFileWriteable
					.createNew(outputFileName);
			List<Variable> varIter = inputFile.getVariables();
			Attribute att = new Attribute(
					"uncertml_ref",
					"http://giv-uw.uni-muenster.de:8080/uts/schemas/uncertml2.0.0/GaussianDistribution.xsd");

			List<Dimension> dims = inputFile.getDimensions();
			for (Dimension d : dims) {
				outputFile.addDimension(null, d);
			}

			for (Variable var : varIter) {
				String name = var.getName();
				if (!name.equals("Lon") && !name.equals("Lat")) {
					var.addAttribute(att);
				}
				outputFile.addVariable(null, var);
			}
			// outputFile.close();
			// outputFile.setFill(true);
			outputFile.create();
			result = new File(outputFileName);
		} catch (IOException ioe) {
			LOGGER.debug("Error while encoding NetCDF File in WPS: "
					+ ioe.getLocalizedMessage());
		} finally {
			if (null != inputFile)
				try {
					inputFile.close();
				} catch (IOException ioe) {
					LOGGER.debug("Error while encoding NetCDF File in WPS: "
							+ ioe.getLocalizedMessage());
				}
		}

		return result;
	}

	@Override
	public Class getInputDataType(String id) {

		if (id.equals(getInputIdentifierDistribution())) {
			return UncertWebDataBinding.class;
		} else if (id.equals(getInputIdentifierVariogram())) {
			return GenericFileDataBinding.class;
		} else if (id.equals(getInputIdentifierNumberOfRealisations())) {
			return LiteralIntBinding.class;
		} else if (id.equals(getInputIdentifierSamplingMethod())) {
			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public Class getOutputDataType(String id) {
		if (id.equals(getOutputIdentifierRealisations())) {
			return GenericFileDataBinding.class;
		}
		return null;
	}

	// @Override
	// public List<String> getInputIdentifiers() {
	// List<String> identifiers = new ArrayList<String>();
	//
	// identifiers.add(getInputIdentifierDistribution());
	// identifiers.add(getInputIdentifierVariogram());
	// identifiers.add(getInputIdentifierNumberOfRealisations());
	// identifiers.add(getInputIdentifierSamplingMethod());
	//
	// return identifiers;
	// }
	//
	// @Override
	// public List<String> getOutputIdentifiers() {
	// List<String> identifiers = new ArrayList<String>();
	//
	// identifiers.add(getOutputIdentifierRealisations());
	//
	// return identifiers;
	// }

	public String getInputIdentifierDistribution() {
		if (inputIdentifierDistribution == null) {
			inputIdentifierDistribution = "distribution";
		}
		return inputIdentifierDistribution;
	}

	public String getInputIdentifierVariogram() {
		if (inputIdentifierVariogram == null) {
			inputIdentifierVariogram = "variogram";
		}
		return inputIdentifierVariogram;
	}

	public String getInputIdentifierNumberOfRealisations() {
		if (inputIdentifierNumberOfRealisations == null) {
			inputIdentifierNumberOfRealisations = "numbReal";
		}
		return inputIdentifierNumberOfRealisations;
	}

	public String getInputIdentifierSamplingMethod() {
		if (inputIdentifierSamplingMethod == null) {
			inputIdentifierSamplingMethod = "samplMethod";
		}
		return inputIdentifierSamplingMethod;
	}

	public String getOutputIdentifierRealisations() {
		if (outputIdentifierRealisations == null) {
			outputIdentifierRealisations = "realisations";
		}
		return outputIdentifierRealisations;
	}

	public String getInputIdentifierDatasetName() {
		if (inputIdentifierDatasetName == null) {
			inputIdentifierDatasetName = "datasetNameMean";
		}
		return inputIdentifierDatasetName;
	}

	@Override
	public List<String> getErrors() {
		return null;
	}

}
