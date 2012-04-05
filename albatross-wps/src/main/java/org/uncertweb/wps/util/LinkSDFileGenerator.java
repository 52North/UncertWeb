/**
 * 
 */
package org.uncertweb.wps.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.uncertweb.wps.io.data.binding.complex.AlbatrossUInput;

/**
 * @author s_voss13
 *
 */
public class LinkSDFileGenerator {
	
	private List<AlbatrossUInput> albatrossUInput;
	
	public LinkSDFileGenerator(List<AlbatrossUInput> albatrossUInput ) {
		
		this.albatrossUInput = albatrossUInput;
	}
	
	public void toFile(File targetFile){
		
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
			Double sd = albatrossUInput.get(i).getStandardDeviation().getValues().get(0);
			List<String> albatrossIds = albatrossUInput.get(i).getAlbatrossIDs();
			
			lines.add(sd+"\t"+"1"+"\t"+StringUtils.join(albatrossIds, ','));
		}
		
		UncertainOutputWriter.toFile(targetFile, lines);
		
	}
	
}
