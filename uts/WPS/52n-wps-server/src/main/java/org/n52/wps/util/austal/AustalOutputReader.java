package org.n52.wps.util.austal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.jms.Destination;

public class AustalOutputReader {
	private String log = "";
	private ArrayList<Point[]> points = new ArrayList<Point[]>();
	private int noOfFiles;
	private boolean val = true;

	public AustalOutputReader() {
		noOfFiles = 1;
	}

	public void parseFile(String filename) throws Exception {
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

	}

	public void writePointCsv(String newFile) throws Exception {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(newFile)));
		// write header
		out.write("fid,x,y");
		out.newLine();

		// loop through point arrays
		for (int j = 0; j < points.size(); j++) {
			Point[] p = points.get(j);
			for (int i = 0; i < p.length; i++) { // write point id and
				// coordinates
				double[] coords = p[i].coordinates();
				out.write(p[i].get_fid() + "," + coords[0] + "," + coords[1]);
				out.newLine();
			}
		}
		out.close();
	}

	public void writeValueCsv(String newFile) throws Exception {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(newFile)));
		// write header
		out.write("fid,time,PM10");
		out.newLine();

		// loop through point arrays
		for (int j = 0; j < points.size(); j++) {
			Point[] p = points.get(j);
			for (int i = 0; i < p.length; i++) {
				ArrayList<Value> vals = p[i].values();
				for (int k = 0; k < vals.size(); k++) {

					// add values from timeseries
					out.write(p[i].get_fid() + "," + vals.get(k).TimeStamp()
							+ "," + vals.get(k).PM10val());
					out.newLine();
				}
			}
		}
		out.close();
	}

	//pm-zbps.dmna
	public void readFilesInFolders(String foldername, boolean uncertainty, int start, int end){
		// new point arraylist
		points = new ArrayList<Point[]>();
		File directory = new File(foldername);
		String pointscsv;
		String valscsv;
		String filetype;
		String[] filename = new String[end-start+1];
		int count = start;
		// create filenames
		for(int i = 0; i<filename.length; i++){
			filename[i] = foldername + "/" + "PO" + count;
			count++;
		}
		
		if(uncertainty){
			filetype = "pm-zbps.dmna";
			valscsv = "results/uncertainties"+start+".csv";
			pointscsv =  "results/points_uncert"+start+".csv";
			//val = false;
		}
		else{
			filetype = "pm-zbpz.dmna";
			valscsv = "results/values"+start+".csv";
			pointscsv =  "results/points"+start+".csv";
			//val = true;
		}

		if (!directory.exists()) {
			System.out.println("Directory does not exist!");
		} else {		
			for (int i=0; i<filename.length; i++){
				File file = new File(filename[i]);
				if(file.isDirectory()){
					msg("folder: "+ file);
					File[] files = file.listFiles();
					for (int k = 0; k < files.length; k++) {
						if (files[k].getPath().endsWith(filetype)) {
							msg("parse: " + files[k].toString());
							try {
								this.parseFile(files[k].toString());
							} catch (Exception e) {
								msg("Input error during parsing dmna: "
										+ e.getClass() + e.getMessage());
							}
						} else {
							msg("File is not parsed: " + files[k].getPath());
						}
					}
				}
			}
		}
			
		msg("Number of parsed files: " + (noOfFiles-1));

		try {
			this.writePointCsv(foldername + "/" + pointscsv);
			this.writeValueCsv(foldername + "/" + valscsv);
		} catch (Exception e) {
			msg("Input error during parsing csv: " + e.getClass()
					+ e.getMessage());
		}
	}
	
	public void readAustalFiles(String foldername, boolean uncertainty, String destination) {
		points = new ArrayList<Point[]>();
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
							this.parseFile(files[i].toString());
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

		try {
			this.writePointCsv(destination + "/" + pointscsv);
			this.writeValueCsv(destination + "/"+ valscsv);
		} catch (Exception e) {
			msg("Input error during parsing csv: " + e.getClass()
					+ e.getMessage());
		}
	}

	public ArrayList<Point[]> createPoints(String foldername, boolean uncertainty) {
		points = new ArrayList<Point[]>();
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
							this.parseFile(files[i].toString());
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
		
		try {
			this.writePointCsv("csvOutput" + "/" + pointscsv);
			this.writeValueCsv("csvOutput" + "/"+ valscsv);
		} catch (Exception e) {
			msg("Input error during parsing csv: " + e.getClass()
					+ e.getMessage());
		}
		
		return points;
	}
	
	
	public String get_logs() {
		return log;
	}

	private void msg(String message) {
		System.out.println(message);
	}

}
