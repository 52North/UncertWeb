package org.uncertweb.aqms.austal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.geotools.feature.FeatureCollection;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class AustalProperties {

	// Austal parameters
	private String gx, gy, dd, nx, ny, qs, epsg, z0;
	private List<String> otherParameters, ha;
	private String pollutant;
	private Point centralPoint;
	
	public AustalProperties(String propsFilePath) throws FileNotFoundException, IOException{
		Properties props = new Properties();
		props.load(new FileInputStream(propsFilePath));
		gx = props.getProperty("GX");
		gy = props.getProperty("GY");
		dd = props.getProperty("DD");
		nx = props.getProperty("NX");
		ny = props.getProperty("NY");
		qs = props.getProperty("QS");
		epsg = props.getProperty("EPSG");
		z0 = props.getProperty("Z0");
		pollutant = props.getProperty("POLLUTANT");
		otherParameters = parsePropsList(props.getProperty("MODEL.PARAMS"));
		ha = parsePropsList(props.getProperty("HA"));
		centralPoint = createCentralPoint();
	}
	
	/**
	 * helper method for parsing a list of properties from file
	 * 
	 * @param obsPropsString
	 *            comma seperated list of
	 * @return 
	 * @throws IOException 
	 */
	private List<String> parsePropsList(String obsPropsString){
		if (obsPropsString != null && !obsPropsString.equals("")) {
			List<String> obsPropsList = new ArrayList<String>();
			String[] obsProps = obsPropsString.split(",");
			for (String obsProp : obsProps) {
				obsPropsList.add(obsProp);
			}
			return obsPropsList;
		} else {
			return null;
		}
	}
	
	private Point createCentralPoint(){
		PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);
		GeometryFactory geomFac = new GeometryFactory(pMod, Integer.parseInt(epsg));
		double gxD = Double.parseDouble(gx);
		double gyD = Double.parseDouble(gy);
		Coordinate coord = new Coordinate(gxD, gyD);											
		Point p = geomFac.createPoint(coord);
		return(p);
	}
	
	//TODO implement featurecollection
	private Point createCentralPointFeatureCollection(){
		Coordinate coord = new Coordinate(Double.parseDouble(gx), Double.parseDouble(gy));											
		PrecisionModel pMod = new PrecisionModel(PrecisionModel.FLOATING);
		GeometryFactory geomFac = new GeometryFactory(pMod, Integer.parseInt(epsg));
		Point p = geomFac.createPoint(coord);
		return(p);
	}
	
	/**
	 * @return the gx parameter
	 */
	public String getGX() {
		return gx;
	}
	
	/**
	 * @return the gy parameter
	 */
	public String getGY() {
		return gy;
	}
		
	/**
	 * @return the dd parameter
	 */
	public String getDD() {
		return dd;
	}
	
	/**
	 * @return the nx parameter
	 */
	public String getNX() {
		return nx;
	}
	
	/**
	 * @return the ny parameter
	 */
	public String getNY() {
		return ny;
	}
	
	/**
	 * @return the qs parameter
	 */
	public String getQS() {
		return qs;
	}
	
	/**
	 * @return the z0 parameter
	 */
	public String getZ0() {
		return z0;
	}
	
	/**
	 * @return the epsg parameter
	 */
	public String getEPSG() {
		return epsg;
	}
	
	/**
	 * @return the pollutant parameter
	 */
	public String getPollutant() {
		return pollutant;
	}
	
	/**
	 * @return the other parameters
	 */
	public List<String> getOtherParameters() {
		return otherParameters;
	}
	
	public Point getCentralPoint() {
		return centralPoint;
	}
}
