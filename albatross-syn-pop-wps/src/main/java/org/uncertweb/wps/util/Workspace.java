package org.uncertweb.wps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Functionality around the workspace generation is encapsulated in this class. By creating a new instance the workspace will be created.
 * 
 * @author s_voss13
 * 
 */
public class Workspace {

	private File originalDataFolder, workspaceFolder, publicFolder;
	private String folderNumber;
	
	protected static Logger log = Logger.getLogger(Workspace.class);

	/**
	 * By calling this ctor a new workspace and public folder will be created. Additionally the content of the original data folder is copied into the new workspace. 
	 * Some additional files may ba copied after the run into the public folder.
	 * 
	 * @param originalDataFolder the path to the original data
	 * @param workspace the path to the workspace folder
	 * @param publicFolder the path to a folder which is visible from the outside
	 */
	public Workspace(String originalDataFolder, String workspace,
			String publicFolder) {

		this.originalDataFolder = new File(originalDataFolder);

		folderNumber = this.generateUniqueWorkspaceNumber(workspace);
		// create workspace
		this.workspaceFolder = this.createDirectory(workspace + File.separator
				+ folderNumber);
		// create public folder
		this.publicFolder = this.createDirectory(publicFolder + File.separator
				+ folderNumber);

		// copy data to workspace
		this.copyDirectory(this.originalDataFolder, workspaceFolder);

		// create projectfile in workspace -> done separate
		// copy result into public folder -> done after calculation

	}
	
	/**
	 * Returns the number which was assigned to the workspace.
	 * This number is part of the workspace folder name.
	 * 
	 * @return the number assigned to the workspace.
	 */
	public String getFolderNumber(){
		
		return this.folderNumber;
	}

	/**
	 * Copies a {@link List} of (existing) files into the in the configuration file defined public folder.
	 * 
	 * @param files a {@link List} of files to copy
	 */
	public void copyResultIntoPublicFolder(List<String> files) {
		
		for(String currentFileName : files){
			
			this.copyFile(this.getWorkspaceFolder(), this.getPublicFolder(), currentFileName);
		}

	}

	/**
	 * Returns the {@link File} to the folder were the original data can be found.
	 * @return the {@link File} to the original data.
	 */
	public File getOriginalDataFolder() {
		return originalDataFolder;
	}

	/**
	 * Returns the {@link File} to the workspace folder.
	 * @return the {@link File} to the workspace folder.
	 */
	public File getWorkspaceFolder() {
		return workspaceFolder;
	}

	/**
	 * Returns the {@link File} to the public folder.
	 * 
	 * @return the {@link File} to the public folder.
	 */
	public File getPublicFolder() {
		return publicFolder;
	}

	/**
	 * Copies a single file from a given source folder into a target folder.
	 * The file name must be a valid file.
	 * 
	 * @param sourceLocation
	 * @param targetLocation
	 * @param fileName
	 */
	private void copyFile(File sourceLocation, File targetLocation,
			String fileName) {

		File inputFile = new File(sourceLocation.getAbsolutePath()
				+ File.separator + fileName);
		File outputFile = new File(targetLocation.getAbsolutePath()+ File.separator + fileName);

		try {
			FileUtils.copyFile(inputFile, outputFile);
		} catch (IOException e) {
			log.info("error while copying file: " + e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * The method copies all folders and file from the source directory into the target directory. This is done recursive. 
	 * Thus we have a deep copy of the source folder.
	 * 
	 * @param sourceLocation
	 * @param targetLocation
	 */
	private void copyDirectory(File sourceLocation, File targetLocation) {

		if (sourceLocation.isDirectory()) {

			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			try {

				if (!targetLocation.getParentFile().exists()) {

					createDirectory(targetLocation.getParentFile()
							.getAbsolutePath());

					targetLocation.createNewFile();

				} else if (!targetLocation.exists()) {

					targetLocation.createNewFile();
				}

				InputStream in = new FileInputStream(sourceLocation);
				OutputStream out = new FileOutputStream(targetLocation);

				try {
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				} catch (Exception e){
					log.info("error while copying directory: "+e.getLocalizedMessage());
					throw new RuntimeException("error while copying directory: "+e.getLocalizedMessage());
				}finally{
					in.close();
					out.close();
				}
			} catch (Exception e) {
				log.info("error while copying directory: "+e.getLocalizedMessage());
				throw new RuntimeException("error while copying directory: "+e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Creates a new directory. If the parent folder does not exist it will be created too.
	 * 
	 * @param s a valid directory
	 * @return the {@link File}
	 */
	private File createDirectory(String s) {

		if (!new File(s).getParentFile().exists()) {

			createDirectory(new File(s).getParent());
		}

		new File(s).mkdir();

		return new File(s);
	}

	/**
	 * Generates a unique number for a new workspace. To ensure that the number is really unique, the method checks the already existing numbers in the workspace. 
	 * Thus it is guaranteed that non existing number is reused.
	 * 
	 * @param workspace a valid path to the target workspace
	 * @return a unique workspace number
	 */
	private String generateUniqueWorkspaceNumber(String workspace) {

		int number = this.generateRandomNumber();
		String proposal = workspace + File.separator + number;

		File f = new File(proposal);

		while (f.exists()) {

			number = this.generateRandomNumber();
			proposal = workspace + File.separator + number;
			f = new File(proposal);
		}

		return String.valueOf(number);
	}

	/**
	 * [1000,Integer.Max[
	 * 
	 * @return
	 */
	private int generateRandomNumber() {

		int low = 1000;
		return (int) (Math.random() * (Integer.MAX_VALUE - low) + low);
	}

}
