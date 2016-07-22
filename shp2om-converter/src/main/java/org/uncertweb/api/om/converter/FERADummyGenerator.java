package org.uncertweb.api.om.converter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.uncertml.IUncertainty;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * class for generating dummy data using the shp files Dummy_soils.shp and Dummy_fields.shp
 *
 * @author staschc
 *
 */
public class FERADummyGenerator {

	///Shapefile constants
	private final String SHP_PATH="E:/uncertwebWorkspace/shape-om-converter/etc/";
	private final String SHP_FILE_FIELDS="Dummy_fields.shp";
	private final String FEATURE_CLASS_FIELDS="Dummy_fields";
	private final int SRID = 27700;

	//observation property constants
	private final String OBS_PROP_YIELD="urn:ogc:def:phenomenon:OGC:cropYield";
	private final String PROC_ID_AQUACROP="http://www.uncertweb.org/models/aquacrop";
	private final int START_YEAR = 2000;
	private final int END_YEAR = 2004;

	//properties of the yield distribution
	private final double YIELD_MEAN=80.0;
	private final double YIELD_SD=20.0;

	//Target file properties
	private final String TARGET_FILE_PATH = SHP_PATH+"yield_om_"+System.currentTimeMillis()+".xml";


	private Collection<TimeObject> years;

	public FERADummyGenerator(){
		years = initializeYears();
	}

	public void generateYieldObservationRealisations(int numberOfRealisations) throws IOException, URISyntaxException, OMEncodingException{


		UncertaintyObservationCollection obsCol = new UncertaintyObservationCollection();

		///////////////
		// extract features from shapefile
		String shpFilePath = SHP_PATH + SHP_FILE_FIELDS;
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		ShapefileDataStore store =null;
		try {
			store = new ShapefileDataStore(new File(
					shpFilePath).toURL());
			source = store.getFeatureSource(FEATURE_CLASS_FIELDS);
		} catch (Exception e) {
			throw new IOException(
					"Error while extracting feature class from shapefile: "
							+ e.getMessage());
		}

		FeatureCollection<SimpleFeatureType, SimpleFeature> foiCollection = source
				.getFeatures();


		//////////////////
		// iterate over features to create observations
		FeatureIterator<SimpleFeature> features = foiCollection.features();
		while (features.hasNext()) {
			SimpleFeature feature = features.next();

			// get GML ID
			String id = feature.getID();
			Object geom = feature.getDefaultGeometry();
			SpatialSamplingFeature sf = null;
			// create samplingFeature
			if (geom instanceof Geometry) {
				Geometry jtsGeom = (Geometry) geom;
				jtsGeom.setSRID(SRID);
				sf = ShapeFileConverterUtil.createSamplingFeature(id, jtsGeom);

				//create an uncertainty observation containing the realisations for each year
				Iterator<TimeObject> iter = this.years.iterator();
				while (iter.hasNext()){
					IUncertainty realisations = generateYieldRealisations(YIELD_MEAN, YIELD_SD, numberOfRealisations);
					TimeObject year = iter.next();
					UncertaintyObservation uncertObs = new UncertaintyObservation(year,year,new URI(PROC_ID_AQUACROP), new URI(OBS_PROP_YIELD),sf,new UncertaintyResult(realisations));
					obsCol.addObservation(uncertObs);
				}
			}
		}

		//encode observation collection to target file
		StaxObservationEncoder encoder = new StaxObservationEncoder();
		File outputFile = new File(TARGET_FILE_PATH);
		encoder.encodeObservationCollection(obsCol,outputFile);
	}


	/**
	 * helper method that generates realisations for yields
	 *
	 * @param numberOfRealisations
	 * 			number of realisations for each year
	 * @return Returns ContinuousRealisation containing the realisations
	 */
	private IUncertainty generateYieldRealisations(double mean, double variance, int numberOfRealisations){
		double[] values = new double[numberOfRealisations];
		int i=0;
		while (i<numberOfRealisations){
			values[i]=generateYield(mean,variance);
			i++;
		}
		return new ContinuousRealisation(values);
	}


	/**
	 * helper method that generates random sample from a Gaussian distribution
	 *
	 * @param mean
	 * 			mean of the Gaussian Distribution
	 * @param variance
	 * 			variance of the Gaussian Distribuion
	 * @return
	 * 		sample
	 */
	private double generateYield(double mean, double variance){
		Random generator = new Random();
		return mean+generator.nextGaussian()*variance;
	}

	private Collection<TimeObject> initializeYears() {
		int year = this.START_YEAR;
		Collection<TimeObject> yearsTmp = new ArrayList<TimeObject>();
		while (year <= this.END_YEAR){
			String beginISOString = year + "-01-01T00:00:00+01:00";
			String endISOString = year + "-12-31T23:59:59+01:00";
			yearsTmp.add(new TimeObject(beginISOString,endISOString));
			year++;
		}
		return yearsTmp;
	}


	public static void main(String[] args) {
		try {
			new FERADummyGenerator().generateYieldObservationRealisations(100);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OMEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
