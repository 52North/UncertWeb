/**
 * 
 */
package org.uncertweb.wps.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author s_voss13
 *
 */
class UncertainOutputWriter {
	
	private UncertainOutputWriter() {}
	
	
	/**
	 * Writes the content into the given file. If the file does not exist the method creates a new file.
	 * 
	 * @param f
	 * @throws IOException
	 */
	public static void toFile(File f, List<String> lines) {
		
		if(!f.exists()){
		
			try {
				f.createNewFile();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		} catch (IOException e) {
			
			e.printStackTrace();
		} 
		
		for(String currentLine : lines){
			
			out.println(currentLine);
			
		}
		
		out.flush();
		out.close();
		
	}

}
