package org.uncertweb.wps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.NetCDFBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.LocalAlgorithmRepository;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.wps.util.FTPUtil;

import ucar.nc2.NetcdfFile;

public class AirQualityForecastProcess extends AbstractObservableAlgorithm {

	private static Logger logger = Logger.getLogger(AirQualityForecastProcess.class);

	private List<String> errors;
	
	private final String inputIDStartDate = "sdate";
	private final String inputIDEndDate = "edate";
	private final String inputIDSite = "site";
	private final String inputIDComponent = "cmpd";
	private final String inputIDNumberOfRealisations = "nens";
	private final String inputIDNumberOfForecastHours = "nhrs";
	private final String inputIDReceptorPoints = "recp";
	private final String outputIDPredictedConcentrations = "predicted-concentrations";
	
	public static final String OS_Name = System.getProperty("os.name");

	private boolean finished;
	private boolean abort;
	private boolean testRun;
	private String resultFileName;
	private String parametersFileName;
	private String tmpDir;

	private FTPUtil ftpUtil;
	
	public AirQualityForecastProcess(){
		this.errors = new ArrayList<String>();
		ftpUtil  = new FTPUtil();		
		Property[] propertyArray = WPSConfig.getInstance()
				.getPropertiesForRepositoryClass(
						LocalAlgorithmRepository.class.getCanonicalName());
		for (Property property : propertyArray) {
			// check the name and active state
			if (property.getName().equalsIgnoreCase("resultFileName")
					&& property.getActive()) {
				resultFileName = property.getStringValue();
			} else if (property.getName().equalsIgnoreCase("parametersFileName")
					&& property.getActive()) {
				parametersFileName = property.getStringValue();
			}else if(property.getName().equalsIgnoreCase("testRun")
					&& property.getActive()){
				testRun = Boolean.parseBoolean(property.getStringValue());
			}
		}
		if(OS_Name.contains("Windows")){
			tmpDir = System.getenv("TMP");
		}else{
			tmpDir = System.getenv("CATALINA_TMPDIR");
		}
	}
	
	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
	public Class<?> getInputDataType(String arg0) {
		
		if (arg0.equals(inputIDStartDate)) {
//			return LiteralDateTimeBinding.class;//TODO: check whether datetime can be used
			return LiteralStringBinding.class;
		} else if (arg0.equals(inputIDEndDate)) {	
//			return LiteralDateTimeBinding.class;
			return LiteralStringBinding.class;
		} else if (arg0.equals(inputIDSite)) {
			return LiteralStringBinding.class;
		} else if (arg0.equals(inputIDComponent)) {
			return LiteralStringBinding.class;
		} else if (arg0.equals(inputIDNumberOfForecastHours)) {
			return LiteralIntBinding.class;
		} else if (arg0.equals(inputIDReceptorPoints)) {
			return LiteralStringBinding.class;
		} else if (arg0.equals(inputIDNumberOfRealisations)) {	
			return LiteralIntBinding.class;	
		}		
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String arg0) {
		if (arg0.equals(outputIDPredictedConcentrations)) {
			return UncertWebIODataBinding.class;
		}
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		String resultFileFullPath = "";
		
		if(!testRun){
		/*
		 * get input data
		 * create txt out of them
		 * send txt to ftp server
		 * check constantly if result exists on ftp server
		 * download result netCDF-U
		 * output result netCDF-U
		 */
		
		if (inputData == null)
			throw new NullPointerException("inputData cannot be null");

		String startDate;
		String endDate;
		String site;
		String component;
		List<IData> receptorPoints;
		int numberOfRealisations;
		int numberOfForecastHours;

		startDate = ((LiteralStringBinding) extractData(inputData, inputIDStartDate))
				.getPayload().toString();
		
		endDate = ((LiteralStringBinding) extractData(inputData, inputIDEndDate))
				.getPayload().toString();
		
		site = ((LiteralStringBinding) extractData(inputData, inputIDSite))
				.getPayload().toString();
		
		component = ((LiteralStringBinding) extractData(inputData, inputIDComponent))
				.getPayload().toString();
		
		numberOfRealisations = ((LiteralIntBinding) extractData(inputData, inputIDNumberOfRealisations))
				.getPayload();
		
		numberOfForecastHours = ((LiteralIntBinding) extractData(inputData, inputIDNumberOfForecastHours))
				.getPayload();
		
		receptorPoints = inputData
				.get(inputIDReceptorPoints);

		if (receptorPoints == null || receptorPoints.isEmpty())
			throw new IllegalArgumentException(
					"could not find input for " + inputIDReceptorPoints);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
		
		String parametersFileNameID = parametersFileName.replace("id", sdf.format(new Date()));
		
		String parametersFileFullPath = tmpDir + File.separatorChar + parametersFileNameID;
		
		try {
			BufferedWriter parametersFileWriter = new BufferedWriter(new FileWriter(parametersFileFullPath));

			parametersFileWriter.write(inputIDSite + " = " + site + "\n");
			parametersFileWriter.write(inputIDStartDate + " = " + startDate + "\n");
			parametersFileWriter.write(inputIDEndDate + " = " + endDate + "\n");
			parametersFileWriter.write(inputIDNumberOfForecastHours + " = " + numberOfForecastHours + "\n");
			parametersFileWriter.write(inputIDNumberOfRealisations + " = " + numberOfRealisations + "\n");
			parametersFileWriter.write(inputIDComponent+ " = " + component + "\n");
			
			for (IData iData : receptorPoints) {
				parametersFileWriter.write(inputIDReceptorPoints + " = " + (String)iData.getPayload() + "\n");
			}
			
			parametersFileWriter.flush();
			parametersFileWriter.close();
			
		} catch (IOException e1) {
			logger.error(e1);
		} 
		
		try {
			ftpUtil.upload(parametersFileFullPath, parametersFileNameID);
		} catch (IOException e1) {
			logger.error(e1);
		}	
		
		while (true) {

			if (abort) {
				break;
			}

			try {
				String[] files = ftpUtil.list();

				for (String string : files) {
					logger.info("found file: " + string);
					if (string.contains(resultFileName)) {
						logger.info("found result");
						finished = true;
						break;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			if (finished) {
				break;
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}

		}
		
		resultFileFullPath = tmpDir + File.separator + resultFileName;
		
		try {
			ftpUtil.download(resultFileFullPath, resultFileName);
		} catch (IOException e) {
			logger.error(e);
		}
		
		}else{		

			logger.debug("################ Test run ###############");
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resultFileFullPath = tmpDir + File.separator + resultFileName;
			
			try {
				URL url = new URL("http://v-soknos.uni-muenster.de:8080/data/oslo_mete_20110103.nc");
				
//				InputStream in = url.openStream();
//				
//				int i;
//				
//				FileOutputStream fOut = new FileOutputStream(new File(resultFileFullPath));
//				
//				while ((i = in.read()) != -1) {
//					System.out.print((char)i);
//					fOut.write(i);
//				}
				
				org.apache.commons.io.FileUtils.copyURLToFile(url, new File(resultFileFullPath));
				
//				in.close();
//				fOut.flush();
//				fOut.close();
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		Map<String, IData> result = new HashMap<String, IData>();
		
		try {
//			NetCDFBinding ndw = new NetCDFBinding(new NetcdfUWFile(new NetcdfFile(resultFileFullPath)));
			NetCDFBinding ndw = new NetCDFBinding(new NetcdfUWFile(NetcdfFile.open(resultFileFullPath)));
			result.put(outputIDPredictedConcentrations, ndw);
		} catch (NetcdfUWException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private IData extractData(Map<String, List<IData>> inputData, String id){
		List<IData> dataList = inputData
				.get(id);

		if (dataList == null || dataList.isEmpty())
			throw new IllegalArgumentException(
					"could not find input for " + id);
		
		return dataList.get(0);		
	}

}
