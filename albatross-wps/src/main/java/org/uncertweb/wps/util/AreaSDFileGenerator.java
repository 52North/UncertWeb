/**
 * 
 */
package org.uncertweb.wps.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInput;

/**
 * @author s_voss13
 *
 */
public class AreaSDFileGenerator {
	
	private List<AlbatrossUInput> albatrossUInput;
	
	public AreaSDFileGenerator(List<AlbatrossUInput> albatrossUInput) {
		
		this.albatrossUInput = albatrossUInput;
		
	}
	
	public void toFile(File targetFile){
		
		/**
		 * the target File may look like:
		 * 
		 * 	10	number of postcode area		
			1	3	20	record number in loc++ text file
			2	3	12	
			3	3	45	
			4	3	8	
			5	3	65	
			6	3	12	
			7	3	10	
			8	3	35	
			9	3	9	
			10	3	27	 
		 */
		
		List<String> lines = new ArrayList<String>(albatrossUInput.size()+1);
			
		//first line is the number of postcode areas
		lines.add(String.valueOf(albatrossUInput.size()));
		
		for(int i = 0; i < albatrossUInput.size(); i++){
			
			Double sd = albatrossUInput.get(i).getStandardDeviation().getValues().get(0);
			
			String loc = albatrossUInput.get(i).getParameters().get("sector");
			
			List<String> albatrossIds = albatrossUInput.get(0).getAlbatrossIDs();
			
			lines.add(sd+"\t"+loc+"\t"+albatrossIds.toArray().toString());
		}
		
		UncertainOutputWriter.toFile(targetFile, lines);
		
		
	}
}
