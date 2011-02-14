package org.uncertweb.api.om.converter;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.uncertweb.api.om.TimeObject;

/**
 * 
 * class encapsulates properties read from shp2om.props file
 * 
 * @author staschc
 * 
 */
public class ShapeFileConverterProperties {

	private String featClassName;
	private String procId;
	private String procColName;
	private List<String> obsProps;
	private String obsPropsType;
	private String geomType;
	private TimeObject phenTime;

	private String uom;

	private String procPrefix;
	private String foiPrefix;
	private String obsPropsPrefix;
	
	private String phenTimeColName;
	private String resultTimeColName;
	private String uncertaintyType;
	private String gaussianMeanColName;
	private String gaussianVarianceColName;
	private String multivarGaussianMeanColName;
	private String multivarGaussianCovarianceColName;

	/**
	 * constructor; reads in config file and creates constants
	 * 
	 * @throws Exception
	 * 			if properties file shp2om is not found or if some mandatory properties are missing
	 */
	public ShapeFileConverterProperties() throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream("src/main/resources/shp2om.props"));
		featClassName=props.getProperty("FEATCLASSNAME");
		procId = props.getProperty("PROCID");
		procColName = props.getProperty("PROCCOL");
		obsProps = parseObsProps(props.getProperty("OBSPROPS"));
		obsPropsType = props.getProperty("OBSPROPSTYPE");
		phenTime = ShapeFileConverterUtil.parsePhenTime(props.getProperty("PHENTIME"));
		procPrefix = props.getProperty("PROCPREFIX");
		obsPropsPrefix = props.getProperty("PHENPREFIX");
		foiPrefix = props.getProperty("FOIPREFIX");
		uncertaintyType = props.getProperty("UNTYPE");
		gaussianMeanColName = props.getProperty("UNTYPE.GAUSSIANDISTRIBUTION.MEAN");
		gaussianVarianceColName = props.getProperty("UNTYPE.GAUSSIANDISTRIBUTION.VARIANCE");
		multivarGaussianMeanColName = props.getProperty("UNTYPE.MULTIVARIANTGAUSSIANDISTRIBUTION.MEANS");
		multivarGaussianCovarianceColName = props.getProperty("UNTYPE.MULTIVARIANTGAUSSIANDISTRIBUTION.COVARIANCE");
		uom = props.getProperty("UOM");
		phenTimeColName = props.getProperty("PHENTIMECOL");
		resultTimeColName = props.getProperty("RESULTTIMECOL");
	}

	/**
	 * helper method for parsing the observed properties from properties file
	 * 
	 * @param obsPropsString
	 *            comma seperated list of
	 * @return
	 * @throws Exception
	 */
	private List<String> parseObsProps(String obsPropsString) throws Exception {
		if (obsPropsString != null && !obsPropsString.equals("")) {
			List<String> obsPropsList = new ArrayList<String>();
			String[] obsProps = obsPropsString.split(",");
			for (String obsProp : obsProps) {
				obsPropsList.add(obsProp);
			}
			return obsPropsList;
		} else {
			throw new Exception(
					"OBSPROPS property has to be set in config file!");
		}
	}
	
	

	/**
	 * @return the featClassName
	 */
	public String getFeatClassName() {
		return featClassName;
	}

	/**
	 * @return the procId
	 */
	public String getProcId() {
		return procId;
	}

	/**
	 * @return the procColName
	 */
	public String getProcColName() {
		return procColName;
	}

	/**
	 * @return the obsProps
	 */
	public List<String> getObsProps() {
		return obsProps;
	}

	/**
	 * @return the geomType
	 */
	public String getGeomType() {
		return geomType;
	}

	/**
	 * @return the phenTime
	 */
	public TimeObject getPhenTime() {
		return phenTime;
	}

	/**
	 * @return the procPrefix
	 */
	public String getProcPrefix() {
		return procPrefix;
	}

	/**
	 * @return the foiPrefix
	 */
	public String getFoiPrefix() {
		return foiPrefix;
	}

	/**
	 * @return the obsPropsPrefix
	 */
	public String getObsPropsPrefix() {
		return obsPropsPrefix;
	}

	/**
	 * @return the gaussianMeanColName
	 */
	public String getGaussianMeanColName() {
		return gaussianMeanColName;
	}

	/**
	 * @return the gaussianVarianceColName
	 */
	public String getGaussianVarianceColName() {
		return gaussianVarianceColName;
	}

	/**
	 * @return the obsPropsType
	 */
	public String getObsPropsType() {
		return obsPropsType;
	}

	/**
	 * @return the uom
	 */
	public String getUom() {
		return uom;
	}

	/**
	 * 
	 * @return the uncertainty type used in the result of observations
	 */
	public String getUncertaintyType() {
		return uncertaintyType;
	}

	/**
	 * @return the phenTimeColName
	 */
	public String getPhenTimeColName() {
		return phenTimeColName;
	}

	/**
	 * @return the resultTimeColName
	 */
	public String getResultTimeColName() {
		return resultTimeColName;
	}
	
	/**
	 * returns the names of fields which are read by the converter from the DBF file
	 * 
	 * @return list containing the names of fields which are read by the converter from the DBF file
	 */
	public List<String> getFieldNames(){
		List<String> result = new ArrayList<String>();
		result.add(this.gaussianMeanColName);
		result.add(this.gaussianVarianceColName);
		result.add(this.phenTimeColName);
		result.add(this.resultTimeColName);
		result.add(this.multivarGaussianCovarianceColName);
		result.add(this.multivarGaussianMeanColName);
		result.add(this.procColName);
		return result;
	}

	/**
	 * @return the multivarGaussianMeanColName
	 */
	public String getMultivarGaussianMeanColName() {
		return multivarGaussianMeanColName;
	}

	/**
	 * @return the multivarGaussianCovarianceColName
	 */
	public String getMultivarGaussianCovarianceColName() {
		return multivarGaussianCovarianceColName;
	}
}
