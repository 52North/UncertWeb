package org.uncertweb.austalwps.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;

import ucar.nc2.NetcdfFileWriteable;

public class AustalOutputReader {
	
	private static Logger LOGGER = Logger.getLogger(AustalOutputReader.class);
	private String log = "";
	//private ArrayList<Point[]> points = new ArrayList<Point[]>();
	private int noOfFiles;
	private boolean val = true;

	public AustalOutputReader() {
		noOfFiles = 1;
	}

	/**
	 * 
	 * @param foldername
	 * @param uncertainty
	 * @return list of receptor point results
	 */
	public ArrayList<Point[]> readReceptorPoints(String foldername, boolean uncertainty) {
		ArrayList<Point[]> points = new ArrayList<Point[]>();
		File directory = new File(foldername);
		String pointscsv;
		String valscsv;
		String filetype;
		int count = noOfFiles;
		// create filenames

		//msg("Parsing "+count+". folder: "+foldername);
		if(uncertainty){
			filetype = "pm-zbps.dmna";
			valscsv = "uncertainties"+count+".csv";
			pointscsv =  "points_uncert"+count+".csv";
			//val = false;
		}
		else{
			filetype = "pm-zbpz.dmna";
			valscsv = "values"+count+".csv";
			pointscsv =  "points"+count+".csv";
			//val = true;
		}

		if (!directory.exists()) {
			System.out.println("Directory does not exist!");
		} else {		
			File[] files = directory.listFiles();
			for (int i=0; i<files.length; i++){
					if (files[i].getPath().endsWith(filetype)) {
						msg("parse: " + files[i].toString());
						try {
							points = this.parseReceptorPointsFile(points, files[i].toString());
						} 
						catch (Exception e) {
							msg("Input error during parsing dmna: "+
										 e.getClass() + e.getMessage());
							}
					} else {
							//msg("File is not parsed: " + files[i].getPath());
					}
				
			}
		}
			
		msg("Number of parsed files: " + (noOfFiles-1));
		
		return points;
	}
	
	/**
	 * Method to parse Austal result file
	 * @param filename
	 * @throws Exception
	 */
	private ArrayList<Point[]> parseReceptorPointsFile(ArrayList<Point[]> points, String filename) throws Exception {
		msg("start parsing");
		if (filename != null && filename.endsWith("dmna")) {
			// read file
			BufferedReader in = new BufferedReader(new FileReader(filename));

			// read first row
			in.readLine();
			String row = new String("");
			double refx = 0;
			double refy = 0;
			Point[] pois = null;
			while ((row = in.readLine()) != null) {
				// Separated by "whitespace"
					String[] s = row.trim().split("\\s+");
					//msg(""+s.length);
					//msg(row);
					
					// reference coordinates
					if (s[0].trim().equals("refx"))
						refx = Double.parseDouble(s[1].trim());
					else if (s[0].trim().equals("refy"))
						refy = Double.parseDouble(s[1].trim());

					// point coordinates
					else if (s[0].trim().equals("mntx")) {
						// if necessary initialize point array
						if (pois == null){
							pois = new Point[s.length - 1];
							//msg("No of files: "+noOfFiles);
							}
						for (int i = 1; i < s.length; i++) {
							double xdev = Double.parseDouble(s[i].trim());
							pois[i - 1] = new Point((refx + xdev), (noOfFiles
									+ "_" + i));
						}
					} 
					else if (s[0].trim().equals("mnty")) {
						for (int i = 1; i < s.length; i++) {
							double ydev = Double.parseDouble(s[i].trim());
							//msg("ydev: "+ydev);
							pois[i - 1].set_yCoordinate(refy + ydev);
						}
					}
					else if (s[0].trim().equals("unit")){
						//unit	"µg/m³"
						if(s[1].trim().equals("\"µg/m³\""))
							val = true;
						else if(s[1].trim().equals("%"))
							val = false;
						
					}

					// parse values
					else if (s.length>2&&s[s.length - 2].equals("'")) {
						// get and format timestamp
						// 2008-07-01.01:00:00
						String time = s[s.length - 1].trim().replace(".", " ");
					//	msg(time);
						
						// get values for each point
						for (int i = 0; i < (s.length - 2); i++) {
							double val = Double.parseDouble(s[i].trim());
							pois[i].addValue(time, val);
						}
					}
			}

			// add Array to ArrayList
			points.add(pois);

			// write single output file

			noOfFiles++;
			in.close();
		}

		return points;
	}

//	public void writePointCsv(ArrayList<Point[]> points, String newFile) throws Exception {
//		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
//				new FileOutputStream(newFile)));
//		// write header
//		out.write("fid,x,y");
//		out.newLine();
//
//		// loop through point arrays
//		for (int j = 0; j < points.size(); j++) {
//			Point[] p = points.get(j);
//			for (int i = 0; i < p.length; i++) { // write point id and
//				// coordinates
//				double[] coords = p[i].coordinates();
//				out.write(p[i].get_fid() + "," + coords[0] + "," + coords[1]);
//				out.newLine();
//			}
//		}
//		out.close();
//	}
//
//	public void writeValueCsv(ArrayList<Point[]> points, String newFile) throws Exception {
//		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
//				new FileOutputStream(newFile)));
//		// write header
//		out.write("fid,time,PM10");
//		out.newLine();
//
//		// loop through point arrays
//		for (int j = 0; j < points.size(); j++) {
//			Point[] p = points.get(j);
//			for (int i = 0; i < p.length; i++) {
//				ArrayList<Value> vals = p[i].values();
//				for (int k = 0; k < vals.size(); k++) {
//
//					// add values from timeseries
//					out.write(p[i].get_fid() + "," + vals.get(k).TimeStamp()
//							+ "," + vals.get(k).PM10val());
//					out.newLine();
//				}
//			}
//		}
//		out.close();
//	}

	//pm-zbps.dmna
	/**
	 * Method to read a number of Austal result files within a folder. 
	 * Writes resulting points to csv files and returns results for receptor points.
	 * @param foldername
	 * @param uncertainty
	 * @param start
	 * @param end
	 * @return
	 */
//	public ArrayList<Point[]> readFilesInFolders(String foldername, boolean uncertainty, int start, int end){
//		// new point arraylist
//		ArrayList<Point[]> points = new ArrayList<Point[]>();
//		File directory = new File(foldername);
//		String pointscsv;
//		String valscsv;
//		String filetype;
//		String[] filename = new String[end-start+1];
//		int count = start;
//		// create filenames
//		for(int i = 0; i<filename.length; i++){
//			filename[i] = foldername + "/" + "PO" + count;
//			count++;
//		}
//		
//		if(uncertainty){
//			filetype = "pm-zbps.dmna";
//			valscsv = "results/uncertainties"+start+".csv";
//			pointscsv =  "results/points_uncert"+start+".csv";
//			//val = false;
//		}
//		else{
//			filetype = "pm-zbpz.dmna";
//			valscsv = "results/values"+start+".csv";
//			pointscsv =  "results/points"+start+".csv";
//			//val = true;
//		}
//
//		if (!directory.exists()) {
//			System.out.println("Directory does not exist!");
//		} else {		
//			for (int i=0; i<filename.length; i++){
//				File file = new File(filename[i]);
//				if(file.isDirectory()){
//					msg("folder: "+ file);
//					File[] files = file.listFiles();
//					for (int k = 0; k < files.length; k++) {
//						if (files[k].getPath().endsWith(filetype)) {
//							msg("parse: " + files[k].toString());
//							try {
//								this.parseReceptorPointsFile(points, files[k].toString());
//							} catch (Exception e) {
//								msg("Input error during parsing dmna: "
//										+ e.getClass() + e.getMessage());
//							}
//						} else {
//							msg("File is not parsed: " + files[k].getPath());
//						}
//					}
//				}
//			}
//		}
//			
//		msg("Number of parsed files: " + (noOfFiles-1));
//
//		try {
//			this.writePointCsv(points, foldername + "/" + pointscsv);
//			this.writeValueCsv(points, foldername + "/" + valscsv);
//		} catch (Exception e) {
//			msg("Input error during parsing csv: " + e.getClass()
//					+ e.getMessage());
//		}
//		
//		return points;
//	}
	
//	public ArrayList<Point[]> readAustalFiles(String foldername, boolean uncertainty, String destination) {
//		ArrayList<Point[]> points = new ArrayList<Point[]>();
//		File directory = new File(foldername);
//		String pointscsv;
//		String valscsv;
//		String filetype;
//		int count = noOfFiles;
//		// create filenames
//
//		//msg("Parsing "+count+". folder: "+foldername);
//		if(uncertainty){
//			filetype = "pm-zbps.dmna";
//			valscsv = "uncertainties"+count+".csv";
//			pointscsv =  "points_uncert"+count+".csv";
//			//val = false;
//		}
//		else{
//			filetype = "pm-zbpz.dmna";
//			valscsv = "values"+count+".csv";
//			pointscsv =  "points"+count+".csv";
//			//val = true;
//		}
//
//		if (!directory.exists()) {
//			System.out.println("Directory does not exist!");
//		} else {		
//			File[] files = directory.listFiles();
//			for (int i=0; i<files.length; i++){
//					if (files[i].getPath().endsWith(filetype)) {
//						msg("parse: " + files[i].toString());
//						try {
//							points = this.parseReceptorPointsFile(points, files[i].toString());
//						} 
//						catch (Exception e) {
//							msg("Input error during parsing dmna: "+
//										 e.getClass() + e.getMessage());
//							}
//					} else {
//							//msg("File is not parsed: " + files[i].getPath());
//					}
//				
//			}
//		}
//			
//		msg("Number of parsed files: " + (noOfFiles-1));
//
//		try {
//			this.writePointCsv(points, destination + "/" + pointscsv);
//			this.writeValueCsv(points, destination + "/"+ valscsv);
//		} catch (Exception e) {
//			msg("Input error during parsing csv: " + e.getClass()
//					+ e.getMessage());
//		}
//		return points;
//	}
	
	
	public HashMap<Integer, ArrayList<Double>> readHourlyFiles(String foldername, boolean uncertainty){
		File directory = new File(foldername);
		HashMap<Integer, ArrayList<Double>> valueMap = new HashMap<Integer, ArrayList<Double>>();
		
		// define filenames for uncertainty
		String tag = "";
		if(uncertainty){
			tag = "s";
		} else{
			tag = "z";
		}
		
		// try to find files in directory
		if (!directory.exists()) {
			System.out.println("Directory does not exist!");
		} else {		
			File[] files = directory.listFiles();
			// loop through files in folder
			for (int i=0; i<files.length; i++){
				String filename = files[i].getName();
				// select only hourly files
				if(filename.startsWith("pm")&&!filename.startsWith("pm-d")&&!filename.startsWith("pm-j")&&!filename.startsWith("pm-z")){
					// select uncertainty files if required
					if(filename.endsWith(tag+".dmna")){
						ArrayList<Double> parsedVals = parseHourlyFile(files[i].toString());
						String id = filename.substring(3, 6);
						valueMap.put(Integer.parseInt(id), parsedVals);
					}
				}
			}
		}
		
		return valueMap;
	}

	
	private ArrayList<Double> parseHourlyFile(String filename) {
		msg("start parsing");
		ArrayList<Double> pVals = new ArrayList<Double>();
		if (filename != null && filename.endsWith("dmna")) {
			BufferedReader in;
		try {
				in = new BufferedReader(new FileReader(filename));
			
			// read header
			String row = null;
			while(!(row = in.readLine()).contains("*")){
				//String[] tags = row.split(" ");
				String[] tags = row.split("\t");
				if(tags[0].trim().contains("index")){
					
				}else if(tags[0].trim().contains("unit")){
					
				}else if(tags[0].trim().contains("hghb")){
					//int nCells = Integer.parseInt(tags[1].trim()) * Integer.parseInt(tags[2].trim());
					//pVals = new Double[nCells];
				}						
			}
			
			// read values
			while(!(row = in.readLine()).contains("*")){
				String[] v = row.split(" ");
				//String[] v = row.split("\t");
				if(v.length>0){
					for(String v0 : v){
						if(!v0.trim().equals("")){
							pVals.add(Double.parseDouble(v0.trim()));
						}
					}
//					for(int i=0; i<v.length; i++){
//						pVals.add(Double.parseDouble(v[i].trim()));
//					}
				}
			}	
			
			// close input stream
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return pVals;
	}
	
	
	
	
	public String get_logs() {
		return log;
	}

	private void msg(String message) {
		LOGGER.debug(message);
	}

}
