package org.uncertweb.wps.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author s_voss13
 *
 */
class UncertainOutputWriter {
	
	protected static Logger log = Logger.getLogger(UncertainOutputWriter.class);
	
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
				log.info ("Error while creating config file for uncertain input executable "+e.getLocalizedMessage());
				throw new RuntimeException(e);
			}
		}
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
				for(String currentLine : lines){
					out.println(currentLine);
				}
		} catch (IOException e) {
			log.info ("Error while creating config file for uncertain input executable "+e.getLocalizedMessage());
			throw new RuntimeException(e);
		} 
		finally {
			if (out!=null){
				out.flush();
				out.close();
			}
		}
		
	}

}
