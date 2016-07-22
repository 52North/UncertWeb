package org.uncertweb.wps.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInput;

/**
 * Container for the area-sd.txt file required by the InputDraw.exe.
 * 
 * @author s_voss13
 *
 */
public class LinkSDFileGenerator {
	
	private List<AlbatrossUInput> albatrossUInput;
	
	/**
	 * Constructs a new object, which is capable of creating the link-sd.txt file for the InputDraw.exe.
	 * 
	 * @param albatrossUInput
	 */
	public LinkSDFileGenerator(List<AlbatrossUInput> albatrossUInput ) {
		
		if(albatrossUInput == null)
			throw new NullPointerException("albatrossUInput can not be null");
		
		this.albatrossUInput = Collections.unmodifiableList(albatrossUInput);

	}
	
	/**
	 * Sends the content of the object to a file which will be created if it does not exist.
	 * If the file exists the content will be written either way, which is indeed not useful.
	 * Therefore it is recommend to point to a new file.
	 * 
	 * @param targetFile
	 */
	public void toFile(File targetFile){
		
		if(targetFile == null)
			throw new NullPointerException("tagetFile can not be null");
		
		/**
		 * target file may look like:
		 * 
		 * 	7	number of link groups (maximum number of links across link groups is 100		
			1	1	1	"sd, number of links, link ids"
			1	1	3	
			1.5	1	5	
			2	1	7	
			1.4	1	8	
			0.8	1	9	
			2	1	11	
		 */
		
		List<String> lines = new ArrayList<String>(albatrossUInput.size()+1);
		
		//first line is the number of link groups
		lines.add(String.valueOf(albatrossUInput.size()));
	
		for(int i = 0; i < albatrossUInput.size(); i++){
			
			//immer der erste (und hoffentlich einzige) Wert
			 AlbatrossUInput current = albatrossUInput.get(i);
			Double sd = current.getStandardDeviation().getValues().get(0);
			List<String> albatrossIds = current.getAlbatrossIDs();
			
			lines.add(sd+"\t"+albatrossIds.size()+"\t"+StringUtils.join(albatrossIds, ','));
		}
		
		UncertainOutputWriter.toFile(targetFile, lines);
		
	}
	
}
