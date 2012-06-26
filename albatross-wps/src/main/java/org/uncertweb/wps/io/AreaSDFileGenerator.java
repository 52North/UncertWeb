package org.uncertweb.wps.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInput;

/**
 * Container for the area-sd.txt file required by the InputDraw.exe
 *  
 * @author s_voss13
 *
 */
public class AreaSDFileGenerator {
	
	private List<AlbatrossUInput> albatrossUInput;
	
	/**
	 * Constructs a new object, which is capable of creating the area-sd.txt file for the InputDraw.exe.
	 *
	 * @param albatrossUInput
	 */
	public AreaSDFileGenerator(List<AlbatrossUInput> albatrossUInput) {
		
		if(albatrossUInput == null)
			throw new NullPointerException("albatrossUInput can not be null");
		
		this.albatrossUInput = Collections.unmodifiableList(albatrossUInput);
		
	}
	
	/**
	 * Sends the content of the object to a file which will be created if it does not exist.
	 * If the file exists the content will be written either way, which is indeed not useful.
	 * Therefore it is recommend to point to a new file.
	 * 
	 * @param targetFile a (preferably) non existing file
	 */
	public void toFile(File targetFile){
		
		if(targetFile == null)
			throw new NullPointerException("target File can not be null");
		
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
			
			lines.add(sd+"\t"+loc+"\t"+StringUtils.join(albatrossIds, ','));
		}
		
		UncertainOutputWriter.toFile(targetFile, lines);
		
	}
}
