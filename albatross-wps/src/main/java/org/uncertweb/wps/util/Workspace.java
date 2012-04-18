package org.uncertweb.wps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * The workspace is the core folder for a albatross run. All files are located inside this workspace. This is also true for the prostprocessing files. They are encapsulated into a single
 * folder, but still located inside the workspace. To make sure that every run is separated from each other every workspace has a unique number. Nevertheless one original workspace exists.
 * This workspace contains all static files and runnables. After coping this data into one of the generated workspace the WPS starts to run the runnables and local changes are made in
 * the specific workspace. In order to publish results on the web (e.g. via tomcat) a method {@link Workspace#copyResultIntoPublicFolder(List)} to copy selected files exist. The list of files
 * to copy can be defined by the user in the corresponding config file for the this WPS. Moreover methods to get the workspace and public folder are available.
 * 
 * @author s_voss13
 * 
 */
public class Workspace {

	private File originalDataFolder, workspaceFolder, publicFolder;
	private String folderNumber;

	/**
	 * Creates the workspace folder, the public folder and copies the original data into the workspace folder. If the folders does not exist the will be created.
	 * 
	 * @param originalDataFolder
	 * @param workspace
	 * @param publicFolder
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
	
	public String getFolderNumber(){
		
		return this.folderNumber;
	}

	/**
	 * Copies the given files from the workspace into the public folder.
	 * 
	 * @param files
	 */
	public void copyResultIntoPublicFolder(List<String> files) {
		
		for(String currentFileName : files){
			
			this.copyFile(this.getWorkspaceFolder(), this.getPublicFolder(), currentFileName);
		}

	}

	public File getOriginalDataFolder() {
		return originalDataFolder;
	}

	public File getWorkspaceFolder() {
		return workspaceFolder;
	}

	public File getPublicFolder() {
		return publicFolder;
	}

	private void copyFile(File sourceLocation, File targetLocation,
			String fileName) {

		File inputFile = new File(sourceLocation.getAbsolutePath()
				+ File.separator + fileName);
		File outputFile = new File(targetLocation.getAbsolutePath()+ File.separator + fileName);

		try {
			FileUtils.copyFile(inputFile, outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

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

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

			} catch (Exception e) {

			}
		}
	}

	private File createDirectory(String s) {

		if (!new File(s).getParentFile().exists()) {

			createDirectory(new File(s).getParent());
		}

		new File(s).mkdir();

		return new File(s);
	}

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
