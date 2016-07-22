package org.uncertweb.austalwps.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.uncertml.sample.UnknownSample;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

public class AustalOutputReader {
	
	private static Logger LOGGER = Logger.getLogger(AustalOutputReader.class);
	private String log = "";
	//private ArrayList<Point[]> points = new ArrayList<Point[]>();
	private int noOfFiles;
	private boolean val = true;

	
	// NetCDF variables	
	private final static String TIME_VAR_NAME = "time";
	private final static String MAIN_VAR_NAME = "PM10_Austal2000";
	private final static String MAIN_VAR_LONG_NAME = "Particulate matter smaller 10 um diameter";
	private final static String X_VAR_NAME = "x";
	private final static String Y_VAR_NAME = "y";
	private final static String UNITS_ATTR_NAME = "units";
	private final static String MISSING_VALUE_ATTR_NAME = "missing_value";
	private final static String LONG_NAME_ATTR_NAME = "long_name";
	//private final static String GRID_MAPPING_VAR_NAME = "gauss_krueger_3";
	private final static String GRID_MAPPING_VAR_NAME = "crs";
	private final static String GRID_MAPPING_ATTR_NAME = "grid_mapping";
		
	private final static String MAIN_VAR_UNITS = "ug m-3";
	private final static String X_VAR_UNITS = "m";
	private final static String Y_VAR_UNITS = "m";
		
	private final static String COORD_AXIS_ATTR_NAME = "_CoordinateAxisType";
	private final static String X_COORD_AXIS = "GeoX";
	private final static String Y_COORD_AXIS = "GeoY";
		
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
		/*
		 * support english file names
		 */
		String[] filetype;
		
		if(uncertainty){
			filetype = new String[]{"pm-zbps.dmna", "pm-zbps.dmna"};//TODO: english name!?
		}
		else{
			filetype = new String[]{"pm-zbpz.dmna", "pm-tmps.dmna"};
		}
		
		if (!directory.exists()) {
			System.out.println("Directory does not exist!");
		} else {		
			File[] files = directory.listFiles();
			for (int i=0; i<files.length; i++){
					if (files[i].getPath().endsWith(filetype[0]) || files[i].getPath().endsWith(filetype[1])) {
						try {
							points = this.parseReceptorPointsFile(points, files[i].toString());
						} 
						catch (Exception e) {
							msg("Input error during parsing dmna: "+
										 e.getClass() + e.getMessage());
							}
					} else {
						
					}				
			}
		}
		
		return points;
	}
	
	public ArrayList<Point> readReceptorPointList(String foldername, boolean uncertainty) {
		ArrayList<Point> pointList = new ArrayList<Point>();
		
		File directory = new File(foldername);
		/*
		 * support english file names
		 */
		String[] filetype;
		
		if(uncertainty){
			filetype = new String[]{"pm-zbps.dmna", "pm-zbps.dmna"};//TODO: english name!?
		}
		else{
			filetype = new String[]{"pm-zbpz.dmna", "pm-tmps.dmna"};
		}

		if (!directory.exists()) {
			System.out.println("Directory does not exist!");
		} else {		
			File[] files = directory.listFiles();
			for (int i=0; i<files.length; i++){
					if (files[i].getPath().endsWith(filetype[0]) || files[i].getPath().endsWith(filetype[1])) {
						try {
							pointList = this.parseReceptorPointsFile(files[i].toString());
							//points = this.parseReceptorPointsFile(points, files[i].toString());
						} 
						catch (Exception e) {
							msg("Input error during parsing dmna: "+
										 e.getClass() + e.getMessage());
							}
					} else {
						
					}				
			}
		}
		
		return pointList;
	}
	
	private ArrayList<Point> parseReceptorPointsFile(String filename) throws Exception {
		ArrayList<Point> pointList = new ArrayList<Point>();
		
		if (filename != null && filename.endsWith("dmna")) {
			// read file
			BufferedReader in = new BufferedReader(new FileReader(filename));

			// read first row
			in.readLine();
			String row = new String("");
			double refx = 0;
			double refy = 0;			
			
			while ((row = in.readLine()) != null) {
				// Separated by "whitespace"
					String[] s = row.trim().split("\\s+");
					
					// reference coordinates
					if (s[0].trim().equals("refx"))
						refx = Double.parseDouble(s[1].trim());
					else if (s[0].trim().equals("refy"))
						refy = Double.parseDouble(s[1].trim());

					// point coordinates
					else if (s[0].trim().equals("mntx")) {
						for (int i = 1; i < s.length; i++) {
							double xdev = Double.parseDouble(s[i].trim());
							pointList.add(new Point((refx + xdev), (noOfFiles
									+ "_" + i)));
						}
					} 
					else if (s[0].trim().equals("mnty")) {
						for (int i = 1; i < s.length; i++) {
							double ydev = Double.parseDouble(s[i].trim());
							pointList.get(i-1).set_yCoordinate(refy + ydev);
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
						
						// get values for each point
						for (int i = 0; i < (s.length - 2); i++) {
							double val = Double.parseDouble(s[i].trim());
							pointList.get(i).addValue(time, val);
						}
					}
			}
			
			// write single output file
			noOfFiles++;
			in.close();
		}
		
		return pointList;
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
		//	Point[] pois = null;
			ArrayList<Point> pointList = new ArrayList<Point>();
			while ((row = in.readLine()) != null) {
				// Separated by "whitespace"
					String[] s = row.trim().split("\\s+");
					
					// reference coordinates
					if (s[0].trim().equals("refx"))
						refx = Double.parseDouble(s[1].trim());
					else if (s[0].trim().equals("refy"))
						refy = Double.parseDouble(s[1].trim());

					// point coordinates
					else if (s[0].trim().equals("mntx")) {
						// if necessary initialize point array
//						if (pois == null){
//							pois = new Point[s.length - 1];
//							}
						for (int i = 1; i < s.length; i++) {
							double xdev = Double.parseDouble(s[i].trim());
//							pois[i - 1] = new Point((refx + xdev), (noOfFiles
//									+ "_" + i));
							pointList.add(new Point((refx + xdev), (noOfFiles
									+ "_" + i)));
						}
					} 
					else if (s[0].trim().equals("mnty")) {
						for (int i = 1; i < s.length; i++) {
							double ydev = Double.parseDouble(s[i].trim());
//							pois[i - 1].set_yCoordinate(refy + ydev);
							pointList.get(i-1).set_yCoordinate(refy + ydev);
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
//							pois[i].addValue(time, val);
							pointList.get(i).addValue(time, val);
						}
					}
			}

			// add Array to ArrayList
//			points.add(pois);

			// write single output file
			noOfFiles++;
			in.close();
		}

		return points;
	}		

	public HashMap<Integer, ArrayList<Double>> readHourlyFiles(String foldername, int startID, boolean uncertainty){
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
				//TODO: check if this fits for english filenames
				if(filename.startsWith("pm")&&!filename.startsWith("pm-d")&&!filename.startsWith("pm-t")&&!filename.startsWith("pm-j")&&!filename.startsWith("pm-z")){
					// select uncertainty files if required
					if(filename.endsWith(tag+".dmna")){
						String idString = filename.substring(3, 6);
						try{
							int id = Integer.parseInt(idString);
							// parse file only if it is after the startDate
							if(id>=startID){
								ArrayList<Double> parsedVals = parseHourlyFile(files[i].toString());
								valueMap.put(id, parsedVals);							
							}

						}catch(NumberFormatException e){
							
						}						
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
	
	/**
	 * Method to create NetCDF file from Austal grid outputs
	 * @param filepath
	 * @param x
	 * @param y
	 * @param minDate
	 * @param realisationsMap
	 * @return
	 * @throws IOException 
	 * @throws NetcdfUWException 
	 * @throws InvalidRangeException 
	 */
	public NetcdfUWFile createNetCDFfile(String filepath, int[] x, int[] y, DateTime minDate, int startID, HashMap<Integer, HashMap<Integer, ArrayList<Double>>> realisationsMap) throws IOException, NetcdfUWException, InvalidRangeException{		
		// get geometry for coordinates from study area
		ArrayInt xArray = new ArrayInt.D1(x.length);
		ArrayInt yArray = new ArrayInt.D1(y.length);
		
		// get dates for the time series results
		// format: "hours since 2011-01-02 00:00:00 00:00"
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");		
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String TIME_VAR_UNITS = "hours since "+dateFormat.print(minDate.minusHours(1))+ " 00:00";
		int tn = realisationsMap.get(1).size();
		ArrayInt tArray = new ArrayInt.D1(tn);
		
		// fill arrays with values
		int xi=0;
		int yi=0;
		int r=0;
		int t=0;
		int c=0;
		
		for (xi = 0; xi < x.length; xi++) {			
			xArray.setInt(xi, x[xi]);
		}
		for (yi = 0; yi < y.length; yi++) {
			yArray.setInt(yi, y[yi]);
		}
		for(t = 0; t < tn; t++){		
			tArray.setInt(t, t+1);
		}

		// put all result arrays into a new NetCDF file
		// prepare new netCDF file for realisations
		NetcdfFileWriteable resultFile = null;
		NetcdfUWFileWriteable resultUWFile = null;
	//	try {
			resultFile = NetcdfUWFileWriteable.createNew(filepath, true);
			resultUWFile = new NetcdfUWFileWriteable(resultFile);
			
			// define dimensions
			Dimension xDim = new Dimension(X_VAR_NAME, x.length);
			Dimension yDim = new Dimension(Y_VAR_NAME, y.length);
			Dimension tDim = new Dimension(TIME_VAR_NAME, tn);
			
			if (!resultFile.isDefineMode()) {
				resultFile.setRedefineMode(true);
			}
			
			// A1) add dimensions to simple NetCDF file
			resultFile.addDimension(null, xDim);
			resultFile.addDimension(null, yDim);
			resultFile.addDimension(null, tDim);

			// A2) add dimensions as variables
			resultFile.addVariable(X_VAR_NAME, DataType.FLOAT,
					new Dimension[] {xDim });
			resultFile.addVariable(Y_VAR_NAME, DataType.FLOAT,
					new Dimension[] {yDim });
			resultFile.addVariable(TIME_VAR_NAME, DataType.FLOAT,
					new Dimension[] {tDim });
			
			// B1) add dimension variable attributes
			// units
			resultFile.addVariableAttribute(X_VAR_NAME,
					UNITS_ATTR_NAME, X_VAR_UNITS);
			resultFile.addVariableAttribute(Y_VAR_NAME,
					UNITS_ATTR_NAME, Y_VAR_UNITS);
			resultFile.addVariableAttribute(TIME_VAR_NAME,
					UNITS_ATTR_NAME, TIME_VAR_UNITS);
				
			// coordinate types
			resultFile.addVariableAttribute(X_VAR_NAME,
					COORD_AXIS_ATTR_NAME, X_COORD_AXIS);
			resultFile.addVariableAttribute(Y_VAR_NAME,
					COORD_AXIS_ATTR_NAME, Y_COORD_AXIS);		
			

			//TODO: Additional lat, lon variables?
//		    x:standard_name = "projection_x_coordinate" ;
//		    y:standard_name = "projection_y_coordinate" ;

//		    double lat(y, x) ;
//		    double lon(y, x) ;

			// add CRS variable and attributes
			resultFile.addVariable(GRID_MAPPING_VAR_NAME, DataType.CHAR, new Dimension[] {});
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"grid_mapping_name", "gauss_krueger_3");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"false_easting", "3500000");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"longitude_of_projection_origin", "9");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"false_northing", "0.0");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"latitude_of_projection_origin", "0.0");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"scale_factor_at_projection_origin", "1.0");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"semi_major_axis", "6377397.155");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"semi_minor_axis", "6356078.9628");	
			resultFile.addVariableAttribute(GRID_MAPPING_VAR_NAME,
					"inverse_flattening", "299.1528");	
			
//			crs:spatial_ref = \"EPSG\",\"3035\"

			// B2) write dimension arrays to file
			resultFile.setRedefineMode(false);	
			resultFile.write(X_VAR_NAME, xArray);
			resultFile.write(Y_VAR_NAME, yArray);
			resultFile.write(TIME_VAR_NAME, tArray);
			
			// C1) additional information for NetCDF-U file
			// a) Set dimensions for value variable			
			ArrayList<Dimension> dims = new ArrayList<Dimension>(3);
			dims.add(xDim);
			dims.add(yDim);
			dims.add(tDim);
			
			// b) add data variable with dimensions and attributes 
			Variable dataVariable = resultUWFile.addSampleVariable(
					MAIN_VAR_NAME, DataType.DOUBLE, dims, UnknownSample.class, realisationsMap.size());
						
			dataVariable.addAttribute(new Attribute(UNITS_ATTR_NAME, MAIN_VAR_UNITS));
			dataVariable.addAttribute(new Attribute(MISSING_VALUE_ATTR_NAME, -9999F));
			dataVariable.addAttribute(new Attribute(LONG_NAME_ATTR_NAME, MAIN_VAR_LONG_NAME));
			// add attribute for grid mapping
			dataVariable.addAttribute(new Attribute(GRID_MAPPING_ATTR_NAME, GRID_MAPPING_VAR_NAME));
			
			// c) add Austal values to data Array	
			// data array for PM10 values
			ArrayDouble dataArray = new ArrayDouble.D4(realisationsMap.size(),
					xDim.getLength(), yDim.getLength(), tDim.getLength());
			Index dataIndex = dataArray.getIndex();
			
			// loop through realisations
			for(r = 0; r < realisationsMap.size(); r++){
				HashMap<Integer, ArrayList<Double>> valueMap = realisationsMap.get(r+1);
				
				// loop through time steps
				for(t = 0; t < tDim.getLength(); t++){
					ArrayList<Double> currentVals = valueMap.get(t+startID);
					c=0;
					
					// loop through cells
					for (yi = (yDim.getLength()-1); yi >=0; yi--) {
						for (xi = 0; xi < xDim.getLength(); xi++) {	
							// set data for each realisation
							Double v = currentVals.get(c);						
							dataIndex.set(r, xi, yi, t);
							dataArray.set(dataIndex, v);
							c++;
						}
					}
				}
			}			
			
			//TODO: add CF conventions
			resultUWFile.getNetcdfFileWritable().setRedefineMode(true);
			Attribute conventions = resultUWFile.getNetcdfFileWritable().findGlobalAttribute("Conventions");
			String newValue =  conventions.getStringValue() + " CF-1.5";
			resultUWFile.getNetcdfFileWritable().deleteGlobalAttribute("Conventions");
			resultUWFile.getNetcdfFileWritable().addGlobalAttribute("Conventions", newValue);
			
			//TODO: add CRS EPSG code as global attribute
			resultUWFile.getNetcdfFileWritable().addGlobalAttribute("crs_epsg_code", "31467");
			
			
			// C2) write data array to NetCDF-U file
			resultUWFile.getNetcdfFileWritable().setRedefineMode(false);
			resultUWFile.getNetcdfFileWritable().write(
					MAIN_VAR_NAME, dataArray);
			resultUWFile.getNetcdfFile().close();
		
		return resultUWFile;
	}
	
	public String get_logs() {
		return log;
	}

	private void msg(String message) {
		LOGGER.debug(message);
	}

}
