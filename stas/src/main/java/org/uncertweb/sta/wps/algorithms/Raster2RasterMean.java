package org.uncertweb.sta.wps.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.NetCDFData;
import org.n52.wps.io.data.UncertWebIOData;
import org.n52.wps.io.data.binding.complex.NetCDFDataBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.util.r.process.ExtendedRConnection;
import org.n52.wps.util.r.process.RProcessException;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.AggregationInputs;
import org.uncertweb.sta.wps.api.AbstractProcessInput;
import org.uncertweb.sta.wps.api.ProcessOutput;
import org.uncertweb.sta.wps.api.SingleProcessInput;

import ucar.nc2.NetcdfFile;


/**
 * represents a Raster2Raster approach with 
 * 
 * @author staschc
 *
 */
public class Raster2RasterMean extends AbstractAggregationProcess{
	
	
	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(Raster2RasterMean.class);
	
	/**
	 * identifier of aggregation process
	 */
	public static final String IDENTIFIER = "urn:ogc:def:aggregationProcess:sGridding:sVariance:noTG:noTA";

	/**
	 * The URL of the SOS from which the {@link ObservationCollection} will be
	 * fetched. Can also be a GET request.
	 */
	public static final SingleProcessInput<String> TARGETGRID = new SingleProcessInput<String>(
			"TargetGrid",
			NetCDFDataBinding.class, 0, 1, null, null);
	
	/**
	 * xoffset of target grid
	 */
	public static final SingleProcessInput<String> XOFFSET = new SingleProcessInput<String>(
			"XOffset",
			LiteralDoubleBinding.class, 0, 1, null, null);
	
	/**
	 * yoffset of target grid
	 */
	public static final SingleProcessInput<String> YOFFSET = new SingleProcessInput<String>(
			"YOffset",
			LiteralDoubleBinding.class, 0, 1, null, null);
	
	/**
	 * yoffset of target grid
	 */
	public static final SingleProcessInput<String> SCALEFACTOR = new SingleProcessInput<String>(
			"ScaleFactor",
			LiteralDoubleBinding.class, 0, 1, null, null);
	
	/**
	 * The URL of the SOS in which the aggregated observations will be inserted.
	 */
	public static final SingleProcessInput<String> INPUT_DATA = new SingleProcessInput<String>(
			Constants.Process.Inputs.INPUT_DATA,
			NetCDFDataBinding.class, 1, 1, null, null);
	
	/**
	 * Process output that contains a {@code GetObservation} request to fetch
	 * the aggregated observations from a SOS.
	 * 
	 * @see Constants.Process.Inputs.Common#SOS_DESTINATION_URL
	 */
	public static final ProcessOutput AGGREGATED_OUTPUT = new ProcessOutput(
			Constants.Process.Outputs.AGGREGATED_DATA,
			NetCDFDataBinding.class);
	
	/**
	 * constructor
	 * 
	 */
	public Raster2RasterMean(){
		log.debug("Aggregation process " + IDENTIFIER+" has been initialized");
	}
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
	
	@Override
	public String getTitle() {
		return "Raster Mean Aggregation";
	}

	@Override
	protected Set<AbstractProcessInput<?>> getInputs() {
		
		//query common process inputs from abstract super class
		Set<AbstractProcessInput<?>> result = super.getCommonProcessInputs();
		
		//add specific parameters
		result.add(INPUT_DATA);
		result.add(TARGETGRID);
		result.add(XOFFSET);
		result.add(YOFFSET);
		return result;
	}

	@Override
	protected Set<ProcessOutput> getOutputs() {
		Set<ProcessOutput> output = new HashSet<ProcessOutput>();
		output.add(AGGREGATED_OUTPUT);
		return output;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		//instantiate result
		Map<String, IData> result = new HashMap<String,IData>();
		double xoffset=Double.NaN;
		double yoffset=Double.NaN;
		double scale=Double.NaN;
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String inputFilePath = null;
		String targetGridFilePath = null;
		String outputFilePath = tmpDirPath + "/aggResult"+System.currentTimeMillis()+".nc";
		outputFilePath = outputFilePath.replace("\\", "/");
		
		///////////////////////////////////////////////////////////////////////////////
		//extract Inputs
		
		//get common Inputs
		AggregationInputs commonInputs = super.getAggregationInputs4Inputs(inputData);
	
		//get specific inputs
		//xoffset
		List<IData> xoffsetInput = inputData.get(XOFFSET.getId());
		if (xoffsetInput!=null&&xoffsetInput.size()==1){
			xoffset = ((LiteralDoubleBinding)xoffsetInput.get(0)).getPayload();
		}
		
		//yoffset
		List<IData> yoffsetInput = inputData.get(YOFFSET.getId());
		if (yoffsetInput!=null&&xoffsetInput.size()==1){
			yoffset = ((LiteralDoubleBinding)yoffsetInput.get(0)).getPayload();
		}
		
		//scale factor
		List<IData> scaleInput = inputData.get(SCALEFACTOR.getId());
		if (scaleInput!=null&&scaleInput.size()==1){
			scale = ((LiteralDoubleBinding)scaleInput.get(0)).getPayload();
		}
		
		//targetGrid
		List<IData> targetGridInput = inputData.get(TARGETGRID.getId());
		if (targetGridInput!=null&&targetGridInput.size()==1){
			NetCDFData ncInput = (NetCDFData) ((NetCDFDataBinding)targetGridInput.get(0)).getPayload();
			NetcdfUWFile ncFile = ncInput.getNetcdfUWFile();
			targetGridFilePath = ncFile.getNetcdfFile().getLocation();
			targetGridFilePath = targetGridFilePath.replace("\\","/");
		}
		
		//getInputFilePath
		List<IData> inputDataInput = inputData.get(INPUT_DATA.getId());
		if (inputDataInput!=null&&inputDataInput.size()==1){
			NetCDFData ncInput = (NetCDFData) ((NetCDFDataBinding)inputDataInput.get(0)).getPayload();
			NetcdfUWFile ncFile = ncInput.getNetcdfUWFile();
			inputFilePath = ncFile.getNetcdfFile().getLocation();
			inputFilePath = inputFilePath.replace("\\","/");
		}
		
		
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
//			c.tryVoidEval("library(RNetCDF)");
//			c.tryVoidEval("library(spacetime)");
			
			//set FilePath in R
			c.tryVoidEval("file <- \""+inputFilePath+"\"");
			
			//load readUNetCDF and WriteUNetCDF functions
//			String readUnetCDFRFunction = getReadUNetCDFRFunction();
//			String writeUnetCDFRFunction = getWriteUNetCDFRFunction();
//			c.tryVoidEval(readUnetCDFRFunction);
//			c.tryVoidEval(writeUnetCDFRFunction);
			
			//read NetCDF file with passed variables
			String varString = getVariablesRVector(commonInputs.getVariables());
			String rCmd = (varString!=null)?"spUNetCDF <- readUNetCDF(file,"+varString+")":"spUNetCDF <- readUNetCDF(file))";
			c.tryVoidEval(rCmd);
			
			//calculating new Grid
			if (targetGridFilePath!=null){
				c.tryVoidEval("targetGridFile <- \""+targetGridFilePath+"\"");
				c.tryVoidEval("targetNetCDF <- readUNetCDF(targetGridFile, variables=c(\"biotemperature_mean\"))");
				c.tryVoidEval("newGrid <- SpatialGrid(targetNetCDF@grid,targetNetCDF@proj4string)");
				c.tryVoidEval("newSpatialGrid <- as(newGrid,\"SpatialGrid\")");
				c.tryVoidEval("newPixels <- as(newSpatialGrid,\"SpatialPixels\")");
			}
			
			if (xoffset!=Double.NaN&&yoffset!=Double.NaN){
				c.tryVoidEval("newCellsize <- spUNetCDF@grid@cellsize");
				c.tryVoidEval("newCellsize[[1]] <- "+xoffset);
				c.tryVoidEval("newCellsize[[2]] <- "+yoffset);
				c.tryVoidEval("newCellcentre.offset <- spUNetCDF@bbox[,1]+newCellsize");
				c.tryVoidEval("newDim <- ceiling(c(diff(spUNetCDF@bbox[1,])/newCellsize[1], diff(spUNetCDF@bbox[2,])/newCellsize[2]))");
				c.tryVoidEval("gridTopo <- GridTopology(cellcentre.offset=newCellcentre.offset, cellsize=newCellsize, cells.dim=newDim)");
				c.tryVoidEval("newGrid <- SpatialGrid(gridTopo, proj4string=\"+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs\")");
				c.tryVoidEval("newPixels <- as(newGrid,\"SpatialPixels\")");
			}
			
			else if (scale!=Double.NaN){
				c.tryVoidEval("scale <- "+scale);
				c.tryVoidEval("newCellsize <- scale*spUNetCDF@grid@cellsize");
				c.tryVoidEval("newCellcentre.offset <- spUNetCDF@bbox[,1]+newCellsize/2");
				c.tryVoidEval("newDim <- ceiling(c(diff(spUNetCDF@bbox[1,])/newCellsize[1], diff(spUNetCDF@bbox[2,])/newCellsize[2]))");
				c.tryVoidEval("gridTopo <- GridTopology(cellcentre.offset=newCellcentre.offset, cellsize=newCellsize, cells.dim=newDim)");
				c.tryVoidEval("newGrid <- SpatialGrid(gridTopo, proj4string=\"+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs\")");
				c.tryVoidEval("newPixels <- as(newGrid,\"SpatialPixels\")");
			}
			else {
				throw new RuntimeException("Execute for process is missing one of the following parameters: "+ TARGETGRID.getId()+", "+XOFFSET.getId()+"+"+YOFFSET.getId()+", or "+SCALEFACTOR.getId()+"!");
			}
			
			//executing aggregation
			c.tryVoidEval("spAgg <- aggregate.Spatial(spUNetCDF,newPixels,var)");
			
			//Create response
			c.tryVoidEval("writeUNetCDF(newfile=\""+outputFilePath+"\", spAgg)");
		
			NetcdfFile ncFile = NetcdfFile.open(outputFilePath);
			NetcdfUWFile uwFile = new NetcdfUWFile(ncFile);
			NetCDFData ncData = new NetCDFData(uwFile);
			result.put(AGGREGATED_OUTPUT.getId(), new NetCDFDataBinding(ncData));
		
		} catch (RserveException e) {
			String msg = "Error while establishing RServe connection: "+e.getLocalizedMessage();
			log.debug(msg);
			throw new RuntimeException(msg);
		} catch (RProcessException e) {
			String msg = "Error while running R script for aggregation: "+e.getLocalizedMessage();
			log.debug(msg);
			throw new RuntimeException(msg);
		} catch (IOException e) {
			String msg = "Error while running R script for aggregation: "+e.getLocalizedMessage();
			log.debug(msg);
			throw new RuntimeException(msg);
		} catch (NetcdfUWException e) {
			String msg = "Error while running R script for aggregation: "+e.getLocalizedMessage();
			log.debug(msg);
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
	
	
	private String getVariablesRVector(List<String> variables){
		Iterator<String>varIter = variables.iterator();
		String rVector = null;
		while (varIter.hasNext()){
			if (rVector==null){
				rVector="variables=c(\""+varIter.next()+"\"";
			}
			else {
				rVector+=" \""+varIter.next()+"\"";
			}
		}
		if (rVector!=null){
			rVector+=")";
		}
		return rVector;
	}
	

}
