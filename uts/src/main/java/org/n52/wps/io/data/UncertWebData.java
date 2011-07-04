package org.n52.wps.io.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.n52.wps.io.IOUtils;
import org.n52.wps.server.WebProcessingService;
import org.uncertml.IUncertainty;
import org.uncertweb.api.netcdf.NetcdfUWFile;

import ucar.nc2.NetcdfFile;
import ucar.nc2.util.IO;

/**
 * Wrapper class for wrapping data encoded in NetCDF or UncertML
 * 
 * @author staschc, Benjamin Pross
 *
 */
public class UncertWebData {

	private static Logger LOGGER = Logger.getLogger(UncertWebData.class);
	private HashMap<String, Object> uncertaintyTypesValuesMap;
	private String mimeType;
	private String fileExtension;
	public InputStream dataStream;
	private int type;
	private IUncertainty uncertaintyType;
	private NetcdfUWFile ncdfFile;
	
	/**type for single netCDF file*/
	public static int NETCDF_FILE = 4;
	
	/** type for references to geotiffs; each geotiff file might contain one realisation or a stastitics measure like mean*/
	public static int REFERENCES = 5;
	
	/**type of data is a uNetCDF file*/
	public static int NETCDFX_FILE = 6;
	
	public UncertWebData(IUncertainty uncertaintyType){
		this.uncertaintyType = uncertaintyType;		
	}
	
	public UncertWebData(InputStream stream, String mimeType){
		this.dataStream = stream;
		this.mimeType = mimeType;
		this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
		if (mimeType.equalsIgnoreCase(GenericFileDataConstants.MIME_TYPE_NETCDFX)){
			try {
				this.setNcdfFile(getNetCDFUWFile4InputStream(stream));
			} catch (Exception e) {
				LOGGER.info("Error while creating NetCDF file from reference:" +e.getMessage());
			}
		}
	}
	
	
	
	/**
	 * method for reading input files
	 * 
	 * @param primaryFile
	 * 			URL to file which contains the primary data
	 * @param mimeType
	 * 			mime type of the file
	 * @throws IOException
	 * 			if file could not read
	 */
	public UncertWebData (File primaryFile, String mimeType) throws IOException{
		this.mimeType = mimeType;
		this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
		
		InputStream is = null;
		
		if (GenericFileDataConstants.getIncludeFilesByMimeType(mimeType) != null){
			
			String baseFile = primaryFile.getName(); 
			baseFile = baseFile.substring(0, baseFile.lastIndexOf("."));
			File directory = new File(primaryFile.getParent());
			String[] extensions = GenericFileDataConstants.getIncludeFilesByMimeType(mimeType);
			
			File[] allFiles = new File[extensions.length + 1];
			
			for (int i = 0; i < extensions.length; i++)
				allFiles[i] = new File(directory, baseFile + "." + extensions[i]);
			
			allFiles[extensions.length] = primaryFile;
			
			is = new FileInputStream(IOUtils.zip(allFiles));
		}
		else {
			is = new FileInputStream(primaryFile);
		}
		
		this.dataStream = is;
		if (mimeType.equals(GenericFileDataConstants.MIME_TYPE_NETCDF)){
			this.type = NETCDF_FILE;
		}
		else if(mimeType.equals(GenericFileDataConstants.MIME_TYPE_NETCDFX)){
			this.type = NETCDFX_FILE; 
		}
		else {
			//TODO throw exception!!
		}
		
	}
	
	public UncertWebData(HashMap<String, Object> uncertaintyTypesValuesMap){		
		
		this.uncertaintyTypesValuesMap = uncertaintyTypesValuesMap;
		this.type = REFERENCES;
	}
	
	public String writeData(File workspaceDir) {

		String fileName = null;

		try {
			fileName = this.justWriteData(this.dataStream, this.fileExtension,
					workspaceDir);
		} catch (IOException e) {
			LOGGER.error("Could not write the input to " + workspaceDir);
			e.printStackTrace();
		}

		return fileName;
	}
	
	public HashMap<String, Object> getUncertaintyTypesValuesMap(){
		return uncertaintyTypesValuesMap;
	}
	
	private String justWriteData (InputStream is, String extension, File writeDirectory) throws IOException {
		
		int bufferLength = 2048;
		byte buffer[] = new byte[bufferLength];
		String fileName = null;
		String baseFileName = new Long (System.currentTimeMillis()).toString();
		
		fileName = baseFileName + "." + extension;
		File currentFile = new File(writeDirectory, fileName);
		currentFile.createNewFile();
		
		//alter FileName for return
		fileName = currentFile.getAbsolutePath();
		
		FileOutputStream fos = new FileOutputStream(currentFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos, bufferLength);
		
		int cnt;
		while ((cnt = is.read(buffer, 0, bufferLength)) != -1) {
			bos.write(buffer, 0, cnt);
		}
		
		bos.flush();
		bos.close();
		
		System.gc();
		
		return fileName;
	}
	
	public int getType(){
		return type;
	}

	public IUncertainty getUncertaintyType() {
		return uncertaintyType;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * helper method for retrieving NetCDF File from input stream created from reference in request
	 * 
	 * @param stream
	 * 			input stream created from reference in request
	 * @return Returns NetCDF file following the UncertWeb conventions
	 * @throws Exception 
	 * 			if creating the NetCDF file fails
	 */
	private NetcdfUWFile getNetCDFUWFile4InputStream(InputStream stream) throws Exception {
		NetcdfUWFile result = null;
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String fileSeparator = System.getProperty("file.separator");			
		String baseDir = WebProcessingService.BASE_DIR + fileSeparator + "resources";
		String fileName = tmpDirPath + fileSeparator + "ncInput.nc";
		
		FileOutputStream fOut = new FileOutputStream(fileName);
		
		int i = stream.read();
		
		while(i != -1){			
			fOut.write(i);
			i = stream.read();
		}
		
		stream.close();
		fOut.flush();
		fOut.close();
		
//		IO.writeToFile(stream, fileName);
		NetcdfFile ncdf = NetcdfFile.open(fileName);
		result = new NetcdfUWFile(ncdf);
		return result;
	}

	/**
	 * @param ncdfFile the ncdfFile to set
	 */
	public void setNcdfFile(NetcdfUWFile ncdfFile) {
		this.ncdfFile = ncdfFile;
	}

	/**
	 * @return the ncdfFile
	 */
	public NetcdfUWFile getNcdfFile() {
		return ncdfFile;
	}

	
}
