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
public class UncertainOutput {
	
	private List<LinkRow> linkRowList;
	private List<PCARow> pcaRowList;
	private int randomNumberSeed;
	
	public UncertainOutput(List<LinkRow> linkRowList, List<PCARow> pcaRowList,
			int randomNumberSeed) {
		
		this.linkRowList = linkRowList;
		this.pcaRowList = pcaRowList;
		this.randomNumberSeed = randomNumberSeed;
	}
	
	
	/**
	 * Writes the content into the given file. If the file does not exist the method creates a new file.
	 * 
	 * @param f
	 * @throws IOException
	 */
	public void toFile(File f) throws IOException{
		
		if(!f.exists()){
		
			f.createNewFile();
		}
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));  

		out.println(randomNumberSeed);
		
		for(LinkRow currentLinkRow : linkRowList){
			
			out.println(currentLinkRow.toString());
		}
		
		for(PCARow currentPCARow : pcaRowList){
			
			out.println(currentPCARow.toString());
		}
		
		out.flush();
		out.close();
		
	}

}
