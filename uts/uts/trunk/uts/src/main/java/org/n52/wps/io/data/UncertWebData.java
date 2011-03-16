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
import org.uncertml.IUncertainty;

public class UncertWebData {

	private static Logger LOGGER = Logger.getLogger(UncertWebData.class);
	private HashMap<String, Object> uncertaintyTypesValuesMap;
	private String mimeType;
	private String fileExtension;
	private InputStream dataStream;
	private int type;
	private IUncertainty uncertaintyType;
	public static int NETCDF_FILE = 4;
	public static int REFERENCES = 5;
	
	
	public UncertWebData(IUncertainty uncertaintyType){
		this.uncertaintyType = uncertaintyType;		
	}
	
	public UncertWebData(InputStream stream, String mimeType){
		this.dataStream = stream;
		this.mimeType = mimeType;
		this.fileExtension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
	}
	
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
		this.type = NETCDF_FILE;
		
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
	
}
