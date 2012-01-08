package org.uncertweb.wps.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

import au.com.bytecode.opencsv.CSVReader;

/**
 * class maps Albatross outputs to U-O&M format
 * 
 * @author staschc
 *
 */
public class OutputMapper {
	
	/**
	 * maps the O-D-Matrix output to an om:TextObservation with O-D-matrix as result value
	 * 
	 * @param absoluteFilePath
	 * 			absolute path to output file of Albatros model (OUT_odmatrix.csv)
	 * @return observation collection that contains the observations
	 * @throws OMParsingException
	 * 			if the file cannot be found or an error occured during parsing
	 */
	public IObservationCollection encodeODMatrix(String absoluteFilePath) throws IOException, OMParsingException{
		IObservationCollection result = null;
//		try {
//			
//			TODO might implement an observation for each timestamp/origin
//			CSVReader reader = new CSVReader(new FileReader(absoluteFilePath));
//			List<String[]> allLines = reader.readAll();
//			Iterator<String[]> iter = allLines.iterator();
//			String[] line = iter.next();
//			
//			while (iter.hasNext()){
//			
//			}
			
//			
//		} catch (FileNotFoundException e) {
//			throw new OMParsingException("Outputfile OUT_odmatrix.csv cannot be found!");
//		}
		return result;
	}

	/**
	 * maps the O-D-Matrix output to an om:TextObservation with O-D-matrix as result value
	 * 
	 * @param file
	 * 			output file of Albatros model (OUT_indicators.csv)
	 * @return absolute path to observation collection that contains the observations
	 * @throws OMParsingException 
	 */
	public IObservationCollection encodeIndicators(String absoluteFilePath) throws OMParsingException{
		IObservationCollection result = null;
		try {
		FileInputStream fstream = new FileInputStream(absoluteFilePath);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while ((strLine = br.readLine()) != null)   {

		}
	} catch (FileNotFoundException e) {
		throw new OMParsingException("Outputfile OUT_odmatrix.csv cannot be found!");
	} catch (IOException e) {
		throw new OMParsingException("Outputfile OUT_odmatrix.csv cannot be found!");
	}
		return result;
	}
}
