package org.uncertweb.api.om.converter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.uncertml.distribution.continuous.GaussianDistribution;
import org.uncertml.distribution.multivariate.MultivariateGaussianDistribution;
import org.uncertml.statistic.CovarianceMatrix;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.io.XBObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

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
	private int multiGeomCounter = 0;

	/** properties read from shp2om.properties file */
	private ShapeFileConverterProperties props;

	/**
	 * constructor
	 * 
	 * @throws Exception
	 *             if initialisation of properties from shp2om.properties file
	 *             failed
	 */
	public ShapeFileConverter() throws Exception {
		props = new ShapeFileConverterProperties();
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
	 * @throws Exception
	 * 
	 */
	public void convertSHPnDBF2OM(String shpFilePath, String dbfFilePath,
			String outputFilePath) throws Exception {
		counter=0;

		IObservationCollection result = new UncertaintyObservationCollection();

		// extract feature class from feature source
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		try {
			ShapefileDataStore store = new ShapefileDataStore(new URL(
					shpFilePath));
			source = store.getFeatureSource(props.getFeatClassName());
		} catch (Exception e) {
			throw new Exception(
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
				sf = createSamplingFeature(id, (Geometry) geom);
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
		String uncertaintyType = props.getUncertaintyType();

	      while( (rowObjects = reader.nextRecord()) != null) {
	    	  	//retrieve featureID of row
	    	  	String featureID = props.getFeatClassName()+"."+((Double)rowObjects[0]).intValue();
	    	  	SpatialSamplingFeature ssf = sf4featureID.get(featureID);
	    	  	
	    	  	//retrieve phenomenonTime
	    	  	String phenTime = ((String) rowObjects[number4fieldName.get(props.getPhenTimeColName())]).trim();
	    	  	TimeObject to = ShapeFileConverterUtil.parsePhenTime(phenTime);
	    	  	
	    	  	//check if multivariate Gaussian parameters are set, then parse uncertainty and add observations
	    	  	if (props.getMultivarGaussianMeanColName()!=null&&props.getMultivarGaussianCovarianceColName()!=null){
	    	  		String means = ((String) rowObjects[number4fieldName.get(props.getMultivarGaussianMeanColName())]).trim();
	    	  		String covariances = ((String) rowObjects[number4fieldName.get(props.getMultivarGaussianCovarianceColName())]).trim();
	    	  		double[] meanDoubles = createDoubles(means);
	    	  		CovarianceMatrix cm = createCovarianceMatrix(meanDoubles.length,covariances);
	    	  		MultivariateGaussianDistribution mgd = new MultivariateGaussianDistribution(meanDoubles,cm);
	    	  		UncertaintyResult ur = new UncertaintyResult(mgd);
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}
	    	  	
	    	  	//check if parameters of Gaussian Distribution are set, then also create uncertainty type and create new observation
	    	  	if (props.getGaussianMeanColName()!=null&&props.getGaussianVarianceColName()!=null){
	    	  		Double mean = ((Double) rowObjects[number4fieldName.get(props.getGaussianMeanColName())]);
	    	  		Double var = ((Double) rowObjects[number4fieldName.get(props.getGaussianVarianceColName())]);
	    	  		GaussianDistribution gd = new GaussianDistribution(mean.doubleValue(),var.doubleValue());
	    	  		UncertaintyResult ur = new UncertaintyResult(gd);
	    	  		UncertaintyObservation obs = new UncertaintyObservation(to,to,new URI(procID),new URI(obsProp),ssf,ur);
	    	  		result.addObservation(obs);
	    	  	}
	    	  	
	    	}

	      XBObservationEncoder encoder = new XBObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(result));
	

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

	private CovarianceMatrix createCovarianceMatrix(int dim, String covariances) throws Exception {
		covariances = covariances.trim();
		String[] elements = covariances.split(" ");
		int elementsCount = dim*dim;
		if (elements.length!=elementsCount){
			throw new Exception("Covariance matrix has to contain nxn elements for n means!!");
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
		}
		// read shapefile
		ShapefileDataStore store = new ShapefileDataStore(new URL(shpFilePath));

		// extract feature class from feature source
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		try {
			source = store.getFeatureSource(props.getFeatClassName());
		} catch (Exception e) {
			throw new Exception(
					"Error while extracting feature class from shapefile: "
							+ e.getMessage());
		}

		// reading features
		FeatureCollection<SimpleFeatureType, SimpleFeature> foiCollection = source
				.getFeatures();
		FeatureIterator<SimpleFeature> features = foiCollection.features();
		while (features.hasNext()) {
			SimpleFeature feature = features.next();

			// get GML ID
			String id = feature.getID();
			Object geom = feature.getDefaultGeometry();

			SpatialSamplingFeature sf = null;
			// create samplingFeature
			if (geom instanceof Geometry) {
				sf = createSamplingFeature(id, (Geometry) geom);
			}

			// retrieve phenomenonTime
			TimeObject to = props.getPhenTime();

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

			}
		}

		XBObservationEncoder encoder = new XBObservationEncoder();
		System.out.println(encoder.encodeObservationCollection(result));
	}

	/**
	 * helper method for creating SpatialSamplingFeature from featureID and JTS
	 * geometry
	 * 
	 * @param id
	 *            gml id of the geometry
	 * @param geom
	 *            JTS geometry of the sampling feature
	 * @return POJO representation of the SamplingFeature
	 * @throws Exception
	 *             if parsing fails
	 */
	private SpatialSamplingFeature createSamplingFeature(String id,
			Geometry geom) throws Exception {
		SpatialSamplingFeature sf = null;
		int srid = geom.getSRID();
		if (geom instanceof MultiLineString) {
			MultiLineString mls = (MultiLineString) geom;
			int size = mls.getNumGeometries();
			LineString[] lsArray = new LineString[size];
			for (int i = 0; i < size; i++) {
				lsArray[i] = new GeometryFactory().createLineString(((LineString) mls
						.getGeometryN(i)).getCoordinateSequence());
				multiGeomCounter++;
			}
			MultiLineString gmlLineString =  new GeometryFactory().createMultiLineString(lsArray);
			Identifier identifier = new Identifier(new URI("http://www.uncertweb.org"),id);
			sf = new SpatialSamplingFeature(identifier,null, gmlLineString);
		}
		// TODO add further geometry types
		return sf;
	}

}
