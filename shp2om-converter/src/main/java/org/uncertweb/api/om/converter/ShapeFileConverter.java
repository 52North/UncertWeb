package org.uncertweb.api.om.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.uncertml.distribution.continuous.LogNormalDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.converter.ShapeFileConverterProperties.FILETYPE;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.CategoryResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import au.com.bytecode.opencsv.CSVReader;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

/**
 * converter which can be used to read data from an ESRI shapefile and to write
 * these data in an XML document containing O&M 2.0 observations according to
 * the UncertWeb profile.
 *
 * @author staschc
 *
 */
public class ShapeFileConverter {

	private int counter = 0;


	/** properties read from shp2om.properties file */
	private ShapeFileConverterProperties props;

	/**
	 * constructor
	 *
	 * @throws IOException
	 * 				if initialisation of properties from shp2om.properties file
	 *             failed
	 * @throws FileNotFoundException
	 *             if initialisation of properties from shp2om.properties file
	 *             failed
	 */
	public ShapeFileConverter() throws FileNotFoundException, IOException {
		props = new ShapeFileConverterProperties();
	}

	public ShapeFileConverter(String propsFilePath) throws FileNotFoundException, IOException {
		props = new ShapeFileConverterProperties(propsFilePath);
	}

	public boolean run() throws MalformedURLException, URISyntaxException, IOException, OMEncodingException{
		FILETYPE fileType = props.getFileType();
		if (fileType== FILETYPE.valueOf("csv")){
			convertSHPnCSV2OM(props.getOmPropsFilePath(), props.getShpFilePath(), props.getOutFilePath());
		}
		else if (fileType== FILETYPE.valueOf("dbf")){
			convertSHPnDBF2OM(props.getOmPropsFilePath(), props.getShpFilePath(), props.getOutFilePath());
		}
		else if(fileType== FILETYPE.valueOf("shp")){
			try {
				convertShp2OM(props.getShpFilePath(), props.getOutFilePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * method for converting a shapefile containing the features with geometries
	 * and the
	 *
	 * @param shpFilePat
	 *
	 * @param dbfFilePath
	 *
	 * @param outputFilePath
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws OMEncodingException
	 *
	 */
	public void convertSHPnCSV2OM(String csvFilePath, String shpFilePath,
			String outputFilePath) throws URISyntaxException, MalformedURLException, IOException, OMEncodingException  {
		counter=0;
		IObservationCollection result = null;

		FeatureCollection<SimpleFeatureType, SimpleFeature> foiCollection = getFCollectionFromShpFile(new File(shpFilePath).toURL(), props.getFeatClassName());
		Map<String,SpatialSamplingFeature> sf4featureID=new HashMap<String,SpatialSamplingFeature>(foiCollection.size());
		FeatureIterator<SimpleFeature> features = foiCollection.features();

		/*
		 * TODO workaround because Geotools seems to add 1 to the FID when read in;
		 * so, if number starts with 1
		 */
		while (features.hasNext()) {
			SimpleFeature feature = features.next();

			/*
			 * Getting FID
			 *
			 * TODO workaround because Geotools seems to add 1 to the FID when read in;
			 */
			String fidPrefix = props.getFeatClassName()+".";
			String textFID = feature.getID().replace(fidPrefix,"");
			int id = new Integer(textFID).intValue()-1;
			textFID = fidPrefix+id;
			Object geom = feature.getDefaultGeometry();
			SpatialSamplingFeature sf = null;
			// create samplingFeature
			if (geom instanceof Geometry) {
				sf = ShapeFileConverterUtil.createSamplingFeature(textFID, (Geometry) geom);
			}
			sf4featureID.put(textFID, sf);
			//TODO only encode feature once and then use xlink:href to reference the feature
		}


		//TODO currently only one procedure per
		String procID = props.getProcPrefix()+props.getProcId();
		String obsProp = props.getObsPropsPrefix()+props.getObsProps().get(0);
		List<String> uncertaintyTypes = props.getUncertaintyType();
		String obsType = props.getObsPropsType();

		// decide if uncertainties are available or if provided data is not certain
		if(uncertaintyTypes.get(0).equals("certain")||uncertaintyTypes.get(0).equals("")){
			if (obsType.equals("double")) {
				result = new MeasurementCollection();
			}
		}else{
			result = new UncertaintyObservationCollection();
		}

		// read CSV File
		CSVReader reader = new CSVReader(new FileReader(props.getOmPropsFilePath()));
		String[] nextLine;
		Map<String,Integer> pos4ColumnName = null;
		int obsCounter =0;
	    while ((nextLine = reader.readNext()) != null) {

	    	obsCounter++;
	    	//if first row, initialize mapping between column names and column position
	    	if (pos4ColumnName==null){
	    		pos4ColumnName = new HashMap<String, Integer>(nextLine.length);
	    		for (int i=0;i<nextLine.length;i++){
	    			pos4ColumnName.put(nextLine[i], new Integer(i));
	    		}
	    	}

	    	else {
		    	//parse featureID
		    	String featureID = props.getFeatClassName()+"."+nextLine[pos4ColumnName.get("fid")];
		    	SpatialSamplingFeature ssf = sf4featureID.get(featureID);

		    	//retrieve phenomenonTime
	    	  	String phenTime = nextLine[pos4ColumnName.get(props.getPhenTimeColName())].trim();
	    	  	TimeObject to = ShapeFileConverterUtil.parsePhenTime(phenTime);

	    	  	// for certain data
	    	  	if(result instanceof MeasurementCollection){
	    	  		String value = nextLine[pos4ColumnName.get(props.getCertainDataColName())].trim();
	    	  		MeasureResult r = new MeasureResult(Double.parseDouble(value), props.getUom());
	    	  		Measurement obs = new Measurement(to,to,new URI(procID),new URI(obsProp),ssf,r);
	    	  		result.addObservation(obs);
	    	  	}

	    	  	//check if MultivariateNormalDistribution parameters are set, then parse uncertainty and add observations
	    	  	if (uncertaintyTypes.contains("MultivariateNormalDistribution")&&props.getMultivarNormalMeanColName()!=null&&props.getMultivarNormalCovarianceColName()!=null){
	    	  		String means = nextLine[pos4ColumnName.get(props.getMultivarNormalMeanColName())].trim();
	    	  		String covariances = nextLine[pos4ColumnName.get(props.getMultivarNormalCovarianceColName())].trim();
	    	  		double[] meanDoubles = createDoubles(means);
	    	  		CovarianceMatrix cm = createCovarianceMatrix(meanDoubles.length,covariances);
	    	  		MultivariateNormalDistribution mgd = new MultivariateNormalDistribution(meanDoubles,cm);
	    	  		UncertaintyResult ur = new UncertaintyResult(mgd);
	    	  		ur.setUnitOfMeasurement(props.getUom());
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}

	    	  	//check if NormalDistribution parameters are set, then also create uncertainty type and create new observation
	    	  	if (uncertaintyTypes.contains("NormalDistribution")&&props.getNormalMeanColName()!=null&&props.getNormalVarianceColName()!=null){
	    	  		Double mean = new Double(nextLine[pos4ColumnName.get(props.getNormalMeanColName())]);
	    	  		Double var = new Double(nextLine[pos4ColumnName.get(props.getNormalVarianceColName())]);
	    	  		NormalDistribution gd = new NormalDistribution(mean.doubleValue(),var.doubleValue());
	    	  		UncertaintyResult ur = new UncertaintyResult(gd);
	    	  		ur.setUnitOfMeasurement(props.getUom());
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}
	    	  //check if NormalDistribution parameters are set, then also create uncertainty type and create new observation
	    	  if (uncertaintyTypes.contains("LogNormalDistribution")&&props.getLogNormalMeanColName()!=null&&props.getLogNormalVarianceColName()!=null){
	    	  		Double mean = new Double(nextLine[pos4ColumnName.get(props.getLogNormalMeanColName())]);
	    	  		Double var = new Double(nextLine[pos4ColumnName.get(props.getLogNormalVarianceColName())]);
	    	  		LogNormalDistribution gd = new LogNormalDistribution(mean.doubleValue(),var.doubleValue());
	    	  		UncertaintyResult ur = new UncertaintyResult(gd);
	    	  		ur.setUnitOfMeasurement(props.getUom());
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  }
	    	}
	    }

	    reader.close();
	    long maxMem = Runtime.getRuntime().maxMemory();
	    long freeMem = Runtime.getRuntime().freeMemory();
        System.out.println("Remaining Heap Size: " + freeMem + " of max size: " + maxMem);
	    StaxObservationEncoder encoder = new StaxObservationEncoder();
		File out = new File(outputFilePath);
		encoder.encodeObservationCollection(result, out);
//        CSVEncoder encoder = new CSVEncoder();
//        File out = new File(outputFilePath);
//        encoder.encodeObservationCollection(result,out);



		// TODO write to file

	}





	/**
	 * method for converting a shapefile containing the features with geometries
	 * and the
	 *
	 * @param shpFilePat
	 *
	 * @param dbfFilePath
	 *
	 * @param outputFilePath
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws OMEncodingException
	 * @throws Exception
	 *
	 */
	public void convertSHPnDBF2OM(String dbfFilePath, String shpFilePath,
			String outputFilePath) throws IOException, URISyntaxException, OMEncodingException {
		counter=0;

		IObservationCollection result = new UncertaintyObservationCollection();

		// extract feature class from feature source
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		ShapefileDataStore store =null;
		try {
			store = new ShapefileDataStore(new URL(
					shpFilePath));
			source = store.getFeatureSource(props.getFeatClassName());
		} catch (Exception e) {
			throw new IOException(
					"Error while extracting feature class from shapefile: "
							+ e.getMessage());
		}

		FeatureCollection<SimpleFeatureType, SimpleFeature> foiCollection = source
				.getFeatures();

		Map<String,SpatialSamplingFeature> sf4featureID=new HashMap<String,SpatialSamplingFeature>(foiCollection.size());
		FeatureIterator<SimpleFeature> features = foiCollection.features();

		while (features.hasNext()) {
			SimpleFeature feature = features.next();

			// get GML ID
			String id = feature.getID();
			Object geom = feature.getDefaultGeometry();
			SpatialSamplingFeature sf = null;
			// create samplingFeature
			if (geom instanceof Geometry) {
				sf = ShapeFileConverterUtil.createSamplingFeature(id, (Geometry) geom);
			}
			sf4featureID.put(id, sf);
			//TODO only encode feature once and then use xlink:href to reference the feature
		}

		// read DBF File
		InputStream inputStream = new FileInputStream(dbfFilePath); // take dbf
																	// file as
																	// program
																	// argument
		DBFReader reader = new DBFReader(inputStream);
		int numberOfFields = reader.getFieldCount();
		// store names of fields in
		Map<String, Integer> number4fieldName = new HashMap<String, Integer>(
				numberOfFields);
		for (int i = 0; i < numberOfFields; i++) {
			DBFField f = reader.getField(i);
			String name = f.getName();
			System.out.println(name);
			number4fieldName.put(name, new Integer(i));
		}

		Object[] rowObjects;
		String procID = props.getProcPrefix()+props.getProcId();
		String obsProp = props.getObsPropsPrefix()+props.getObsProps().get(0);
		List<String> uncertaintyType = props.getUncertaintyType();
		String uom = props.getUom();

	      while( (rowObjects = reader.nextRecord()) != null) {
	    	  	//retrieve featureID of row
	    	  	String featureID = props.getFeatClassName()+"."+((Double)rowObjects[0]).intValue();
	    	  	SpatialSamplingFeature ssf = sf4featureID.get(featureID);

	    	  	//retrieve phenomenonTime
	    	  	String phenTime = ((String) rowObjects[number4fieldName.get(props.getPhenTimeColName())]).trim();
	    	  	TimeObject to = ShapeFileConverterUtil.parsePhenTime(phenTime);

	    	  	//check if MultivariateNormalDistribution parameters are set, then parse uncertainty and add observations
	    	  	if (uncertaintyType.contains("MultivariateNormalDistribution")&&props.getMultivarNormalMeanColName()!=null&&props.getMultivarNormalCovarianceColName()!=null){
	    	  		String means = ((String) rowObjects[number4fieldName.get(props.getMultivarNormalMeanColName())]).trim();
	    	  		String covariances = ((String) rowObjects[number4fieldName.get(props.getMultivarNormalCovarianceColName())]).trim();
	    	  		double[] meanDoubles = createDoubles(means);
	    	  		CovarianceMatrix cm = createCovarianceMatrix(meanDoubles.length,covariances);
	    	  		MultivariateNormalDistribution mgd = new MultivariateNormalDistribution(meanDoubles,cm);
	    	  		UncertaintyResult ur = new UncertaintyResult(mgd);
	    	  		ur.setUnitOfMeasurement(uom);
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}

	    	  	//check if NormalDistribution parameters  are set, then also create uncertainty type and create new observation
	    	  	if (uncertaintyType.contains("NormalDistribution")&&props.getNormalMeanColName()!=null&&props.getNormalVarianceColName()!=null){
	    	  		Double mean = ((Double) rowObjects[number4fieldName.get(props.getNormalMeanColName())]);
	    	  		Double var = ((Double) rowObjects[number4fieldName.get(props.getNormalVarianceColName())]);
	    	  		NormalDistribution gd = new NormalDistribution(mean.doubleValue(),var.doubleValue());
	    	  		UncertaintyResult ur = new UncertaintyResult(gd);
	    	  		ur.setUnitOfMeasurement(uom);
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}

	    	}

	      inputStream.close();
	      StaxObservationEncoder encoder = new StaxObservationEncoder();
	      File out = new File(outputFilePath);
	      encoder.encodeObservationCollection(result,out);



		// TODO write to file

	}

	private double[] createDoubles(String means) {
		means = means.trim();
		String[] meanStrings = means.split(" ");
		double[] meanDoubles = new double[meanStrings.length];
		for (int i = 0; i<meanStrings.length;i++){
			meanDoubles[i]= Double.parseDouble(meanStrings[i]);
		}
		return meanDoubles;
	}

	private CovarianceMatrix createCovarianceMatrix(int dim, String covariances) throws IOException {
		covariances = covariances.trim();
		String[] elements = covariances.split(" ");
		int elementsCount = dim*dim;
		if (elements.length!=elementsCount){
			throw new IOException("Covariance matrix has to contain nxn elements for n means!!");
		}
		double[] elementDoubles = new double[elements.length];
		for (int i = 0; i<elements.length;i++){
			elementDoubles[i] = Double.parseDouble(elements[i]);
		}

		CovarianceMatrix cm = new CovarianceMatrix(dim,elementDoubles);

		return cm;
	}

	/**
	 *
	 *
	 * @param shpFilePath
	 * @param outputFilePath
	 * @throws Exception
	 */
	public void convertShp2OM(String shpFilePath, String outputFilePath)
			throws Exception {
		counter = 0;
		IObservationCollection result = null;

		String obsType = props.getObsPropsType();
		if (obsType.equals("double")) {
			result = new MeasurementCollection();
		}else if(obsType.equals("integer")) {
			result = new DiscreteNumericObservationCollection();
		}else if(obsType.equals("text")) {
			result = new TextObservationCollection();
		}else if(obsType.equals("category")) {
			result = new CategoryObservationCollection();
		}else if(obsType.equals("boolean")) {
			result = new BooleanObservationCollection();
		}
		FeatureCollection<SimpleFeatureType, SimpleFeature> foiCollection = getFCollectionFromShpFile(new File(shpFilePath).toURL(), props.getFeatClassName());
		Map<String,SpatialSamplingFeature> sf4featureID=new HashMap<String,SpatialSamplingFeature>(foiCollection.size());
		FeatureIterator<SimpleFeature> features = foiCollection.features();
		SpatialSamplingFeature lastSF = null;

//		// read shapefile
//		ShapefileDataStore store = new ShapefileDataStore(new URL(shpFilePath));
//		// extract feature class from feature source
//		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
//		try {
//			source = store.getFeatureSource(props.getFeatClassName());
//		} catch (Exception e) {
//			throw new Exception(
//					"Error while extracting feature class from shapefile: "
//							+ e.getMessage());
//		}
//
//		// reading features
//		FeatureCollection<SimpleFeatureType, SimpleFeature> foiCollection = source
//				.getFeatures();
//		FeatureIterator<SimpleFeature> features = foiCollection.features();

		while (features.hasNext()) {
			SimpleFeature feature = features.next();

			/*
			 * TODO workaround because Geotools seems to add 1 to the FID when read in;
			 * so, if number starts with 1
			 */
			// get GML ID
			String fidPrefix = props.getFeatClassName()+".";
			String textFID = feature.getID().replace(fidPrefix,"");
			int id = new Integer(textFID).intValue()-1;
			textFID = fidPrefix+id;
			Object geom = feature.getDefaultGeometry();


			SpatialSamplingFeature sf = null;

			// create samplingFeature
			if (geom instanceof Geometry) {

				// check if this feature already exists in the observations list
				boolean same = true;
				if(lastSF!=null){
					Coordinate[] coords = ((Geometry)geom).getCoordinates();
					Coordinate[] lastCoords = lastSF.getShape().getCoordinates();
					// loop through coordinates and see if all are the same
					for(int i=0; i<coords.length; i++){
						same = same & (coords[i].x==lastCoords[i].x) & (coords[i].y==lastCoords[i].y);
					}
				}else
					same= false;

				if(same)
					sf = lastSF;
				else{
					sf = ShapeFileConverterUtil.createSamplingFeature(textFID, (Geometry) geom);
					lastSF = sf;
				}
			}


	//		sf.setSampledFeature("http://giv-uw2.uni-muenster.de/profileDictionary#p2");
			// retrieve phenomenonTime
			TimeObject to = props.getPhenTime();

			// else get time in the shapefile attribute table
			if(!props.getPhenTimeColName().equals("")){
				String phenTime = (String) feature.getAttribute(props.getPhenTimeColName());
				to = ShapeFileConverterUtil.parsePhenTime(phenTime);
			}

			// retrieve procedure(s)
			String procID = props.getProcId();
			if (procID != null) {
				procID = props.getProcPrefix() + procID;
			}

			// get observed Properties and create values
			Iterator<String> iter = props.getObsProps().iterator();
			while (iter.hasNext()) {
				String attName = iter.next();
				Object value = feature.getAttribute(attName);
				// for double values
				if (value != null && value instanceof Double) {
					double numericResult = ((Double) value).doubleValue();
					MeasureResult meas = new MeasureResult(numericResult, props
							.getUom());
					AbstractObservation obs = new Measurement(
							to, to, new URI(procID), new URI(props
									.getObsPropsPrefix()
									+ attName), sf, meas);
					result.addObservation(obs);
					counter += 1;
				}
				// for boolean values
				else if (value != null && value instanceof Boolean) {
					boolean booleanResult = (Boolean) value;
					BooleanResult res = new BooleanResult(booleanResult);
					AbstractObservation obs = new BooleanObservation(
							to, to, new URI(procID), new URI(props
									.getObsPropsPrefix()
									+ attName), sf, res);
					result.addObservation(obs);
					counter += 1;
				}
				// for text values
				else if (value != null && value instanceof String && obsType.equals("text")) {
					String textResult = (String) value;
					TextResult res = new TextResult(textResult);
					AbstractObservation obs = new TextObservation(
							to, to, new URI(procID), new URI(props
									.getObsPropsPrefix()
									+ attName), sf, res);
					result.addObservation(obs);
					counter += 1;
				}
				// for category values
				else if (value != null && value instanceof String && obsType.equals("category")) {
					String textResult = (String) value;
					CategoryResult res = new CategoryResult(textResult, props.getUom());
					AbstractObservation obs = new CategoryObservation(
							to, to, new URI(procID), new URI(props
									.getObsPropsPrefix()
									+ attName), sf, res);
					result.addObservation(obs);
					counter += 1;
				}
			}
		}

		StaxObservationEncoder encoder = new StaxObservationEncoder();
		 File out = new File(outputFilePath);
	      encoder.encodeObservationCollection(result,out);
	//	System.out.println(encoder.encodeObservationCollection(result));
	}



	public void convertSHP2OM(String csvFilePath, String shpFilePath,
			String outputFilePath) throws URISyntaxException, MalformedURLException, IOException, OMEncodingException  {
		counter=0;
		IObservationCollection result = null;

		FeatureCollection<SimpleFeatureType, SimpleFeature> foiCollection = getFCollectionFromShpFile(new File(shpFilePath).toURL(), props.getFeatClassName());
		Map<String,SpatialSamplingFeature> sf4featureID=new HashMap<String,SpatialSamplingFeature>(foiCollection.size());
		FeatureIterator<SimpleFeature> features = foiCollection.features();

		/*
		 * TODO workaround because Geotools seems to add 1 to the FID when read in;
		 * so, if number starts with 1
		 */
		while (features.hasNext()) {
			SimpleFeature feature = features.next();

			/*
			 * Getting FID
			 *
			 * TODO workaround because Geotools seems to add 1 to the FID when read in;
			 */
			String fidPrefix = props.getFeatClassName()+".";
			String textFID = feature.getID().replace(fidPrefix,"");
			int id = new Integer(textFID).intValue()-1;
			textFID = fidPrefix+id;
			Object geom = feature.getDefaultGeometry();
			SpatialSamplingFeature sf = null;
			// create samplingFeature
			if (geom instanceof Geometry) {
				sf = ShapeFileConverterUtil.createSamplingFeature(textFID, (Geometry) geom);
			}
			sf4featureID.put(textFID, sf);
			//TODO only encode feature once and then use xlink:href to reference the feature
		}


		//TODO currently only one procedure per
		String procID = props.getProcPrefix()+props.getProcId();
		String obsProp = props.getObsPropsPrefix()+props.getObsProps().get(0);
		List<String> uncertaintyTypes = props.getUncertaintyType();
		String obsType = props.getObsPropsType();

		// decide if uncertainties are available or if provided data is not certain
		if(uncertaintyTypes.get(0).equals("certain")||uncertaintyTypes.get(0).equals("")){
			if (obsType.equals("double")) {
				result = new MeasurementCollection();
			}
		}else{
			result = new UncertaintyObservationCollection();
		}

		// read CSV File
		CSVReader reader = new CSVReader(new FileReader(props.getOmPropsFilePath()));
		String[] nextLine;
		Map<String,Integer> pos4ColumnName = null;
		int obsCounter =0;
	    while ((nextLine = reader.readNext()) != null) {

	    	obsCounter++;
	    	//if first row, initialize mapping between column names and column position
	    	if (pos4ColumnName==null){
	    		pos4ColumnName = new HashMap<String, Integer>(nextLine.length);
	    		for (int i=0;i<nextLine.length;i++){
	    			pos4ColumnName.put(nextLine[i], new Integer(i));
	    		}
	    	}

	    	else {
		    	//parse featureID
		    	String featureID = props.getFeatClassName()+"."+nextLine[pos4ColumnName.get("fid")];
		    	SpatialSamplingFeature ssf = sf4featureID.get(featureID);

		    	//retrieve phenomenonTime
	    	  	String phenTime = nextLine[pos4ColumnName.get(props.getPhenTimeColName())].trim();
	    	  	TimeObject to = ShapeFileConverterUtil.parsePhenTime(phenTime);

	    	  	// for certain data
	    	  	if(result instanceof MeasurementCollection){
	    	  		String value = nextLine[pos4ColumnName.get(props.getCertainDataColName())].trim();
	    	  		MeasureResult r = new MeasureResult(Double.parseDouble(value), props.getUom());
	    	  		Measurement obs = new Measurement(to,to,new URI(procID),new URI(obsProp),ssf,r);
	    	  		result.addObservation(obs);
	    	  	}

	    	  	//check if MultivariateNormalDistribution parameters are set, then parse uncertainty and add observations
	    	  	if (uncertaintyTypes.contains("MultivariateNormalDistribution")&&props.getMultivarNormalMeanColName()!=null&&props.getMultivarNormalCovarianceColName()!=null){
	    	  		String means = nextLine[pos4ColumnName.get(props.getMultivarNormalMeanColName())].trim();
	    	  		String covariances = nextLine[pos4ColumnName.get(props.getMultivarNormalCovarianceColName())].trim();
	    	  		double[] meanDoubles = createDoubles(means);
	    	  		CovarianceMatrix cm = createCovarianceMatrix(meanDoubles.length,covariances);
	    	  		MultivariateNormalDistribution mgd = new MultivariateNormalDistribution(meanDoubles,cm);
	    	  		UncertaintyResult ur = new UncertaintyResult(mgd);
	    	  		ur.setUnitOfMeasurement(props.getUom());
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}

	    	  	//check if NormalDistribution parameters are set, then also create uncertainty type and create new observation
	    	  	if (uncertaintyTypes.contains("NormalDistribution")&&props.getNormalMeanColName()!=null&&props.getNormalVarianceColName()!=null){
	    	  		Double mean = new Double(nextLine[pos4ColumnName.get(props.getNormalMeanColName())]);
	    	  		Double var = new Double(nextLine[pos4ColumnName.get(props.getNormalVarianceColName())]);
	    	  		NormalDistribution gd = new NormalDistribution(mean.doubleValue(),var.doubleValue());
	    	  		UncertaintyResult ur = new UncertaintyResult(gd);
	    	  		ur.setUnitOfMeasurement(props.getUom());
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}
	    	  //check if NormalDistribution parameters are set, then also create uncertainty type and create new observation
	    	  if (uncertaintyTypes.contains("LogNormalDistribution")&&props.getLogNormalMeanColName()!=null&&props.getLogNormalVarianceColName()!=null){
	    	  		Double mean = new Double(nextLine[pos4ColumnName.get(props.getLogNormalMeanColName())]);
	    	  		Double var = new Double(nextLine[pos4ColumnName.get(props.getLogNormalVarianceColName())]);
	    	  		LogNormalDistribution gd = new LogNormalDistribution(mean.doubleValue(),var.doubleValue());
	    	  		UncertaintyResult ur = new UncertaintyResult(gd);
	    	  		ur.setUnitOfMeasurement(props.getUom());
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  }
	    	}
	    }

	    reader.close();
	    long maxMem = Runtime.getRuntime().maxMemory();
	    long freeMem = Runtime.getRuntime().freeMemory();
        System.out.println("Remaining Heap Size: " + freeMem + " of max size: " + maxMem);
	    StaxObservationEncoder encoder = new StaxObservationEncoder();
		File out = new File(outputFilePath);
		encoder.encodeObservationCollection(result, out);
//        CSVEncoder encoder = new CSVEncoder();
//        File out = new File(outputFilePath);
//        encoder.encodeObservationCollection(result,out);



		// TODO write to file

	}




	/**
	 * helper method for reading featureCollection for passed feature type from SHP file
	 *
	 * @param filePath
	 * 			path of SHP file
	 * @param featureClassName
	 * 			name of feature class
	 * @return Returns feature collection
	 * @throws IOException
	 * 			if reading feature collection from SHP file fails
	 */
	private FeatureCollection<SimpleFeatureType,SimpleFeature> getFCollectionFromShpFile(URL filePath, String featureClassName) throws IOException{
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		ShapefileDataStore store = new ShapefileDataStore(filePath);
		source = store.getFeatureSource(props.getFeatClassName());
		FeatureCollection<SimpleFeatureType,SimpleFeature> features = source.getFeatures();
		store.dispose();
		return features;
	}

}
