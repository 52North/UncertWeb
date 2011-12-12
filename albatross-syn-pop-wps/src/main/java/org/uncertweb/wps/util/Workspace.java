/**
 * 
 */
package org.uncertweb.wps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author s_voss13
 *
 */
public class Workspace {
	
	/**
	 * Makes copy of the data files and adda the projectfile
	 * 
	 * @param location
	 * @param DataLocation
	 * @param projectFile
	 * @return the workspace
	 */
	public File getWorkspace(File dataLocation, File location, ProjectFile projectFile){
		
		//copy the data files
		this.copyDirectory(dataLocation, location);
		
		try {
			this.copyDirectory(projectFile.getProjectFile(), location);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dataLocation;
		

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
 
                	createDirectory(targetLocation.getParentFile().getAbsolutePath());

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
 
    private void createDirectory(String s) {
 
        if (!new File(s).getParentFile().exists()) {
 
            createDirectory(new File(s).getParent());
        }
 
        new File(s).mkdir();
    }

}
