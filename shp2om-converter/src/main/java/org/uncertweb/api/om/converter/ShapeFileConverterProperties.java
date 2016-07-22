package org.uncertweb.api.om.converter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	
	
	public enum FILETYPE{
		dbf,csv,shp
	};
	
	private FILETYPE fileType;
	

	private String shpFilePath;
	private String omPropsFilePath;
	private String outFilePath;

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
	private List<String> uncertaintyTypes;
	private String normalMeanColName;
	private String normalVarianceColName;
	private String multivarNormalMeanColName;
	private String multivarNormalCovarianceColName;
	private String logNormalMeanColName;
	private String logNormalVarianceColName;
	private String certainDataColName;

	/**
	 * constructor; reads in config file and creates constants
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws Exception
	 * 			if properties file shp2om is not found or if some mandatory properties are missing
	 */
	public ShapeFileConverterProperties() throws FileNotFoundException, IOException  {
		Properties props = new Properties();
		props.load(new FileInputStream("src/main/resources/shp2om.props"));
		this.initialize(props);
	}

	public ShapeFileConverterProperties(String propsFilePath) throws FileNotFoundException, IOException  {
		Properties props = new Properties();
		props.load(new FileInputStream(propsFilePath));
		this.initialize(props);
	}
	
	private void initialize(Properties props) throws FileNotFoundException, IOException {
		shpFilePath = props.getProperty("SHPPATH");
		omPropsFilePath = props.getProperty("OMFILEPATH");
		outFilePath = props.getProperty("OUTFILEPATH");
		fileType = FILETYPE.valueOf(props.getProperty("FILETYPE"));
		featClassName=props.getProperty("FEATCLASSNAME");
		procId = props.getProperty("PROCID");
		procColName = props.getProperty("PROCCOL");
		obsProps = parsePropsList(props.getProperty("OBSPROPS"));
		obsPropsType = props.getProperty("OBSPROPSTYPE");
		phenTime = ShapeFileConverterUtil.parsePhenTime(props.getProperty("PHENTIME"));
		procPrefix = props.getProperty("PROCPREFIX");
		obsPropsPrefix = props.getProperty("PHENPREFIX");
		foiPrefix = props.getProperty("FOIPREFIX");
		uncertaintyTypes = parsePropsList(props.getProperty("UNTYPES"));
		normalMeanColName = props.getProperty("UNTYPE.NORMALDISTRIBUTION.MEAN");
		normalVarianceColName = props.getProperty("UNTYPE.NORMALDISTRIBUTION.VARIANCE");
		logNormalMeanColName = props.getProperty("UNTYPE.LOGNORMALDISTRIBUTION.MEAN");
		logNormalVarianceColName = props.getProperty("UNTYPE.LOGNORMALDISTRIBUTION.VARIANCE");
		multivarNormalMeanColName = props.getProperty("UNTYPE.MULTIVARIATENORMALDISTRIBUTION.MEANS");
		multivarNormalCovarianceColName = props.getProperty("UNTYPE.MULTIVARIATENORMALDISTRIBUTION.COVARIANCE");
		certainDataColName = props.getProperty("UNTYPE.CERTAIN");
		uom = props.getProperty("UOM");
		phenTimeColName = props.getProperty("PHENTIMECOL");
		resultTimeColName = props.getProperty("RESULTTIMECOL");
	}
	
	
	/**
	 * helper method for parsing a list of properties from file
	 * 
	 * @param obsPropsString
	 *            comma seperated list of
	 * @return 
	 * @throws IOException 
	 */
	private List<String> parsePropsList(String obsPropsString) throws IOException{
		if (obsPropsString != null && !obsPropsString.equals("")) {
			List<String> obsPropsList = new ArrayList<String>();
			String[] obsProps = obsPropsString.split(",");
			for (String obsProp : obsProps) {
				obsPropsList.add(obsProp);
			}
			return obsPropsList;
		} else {
			throw new IOException(
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
	public List<String> getUncertaintyType() {
		return uncertaintyTypes;
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
		result.add(this.normalMeanColName);
		result.add(this.normalVarianceColName);
		result.add(this.phenTimeColName);
		result.add(this.resultTimeColName);
		result.add(this.multivarNormalCovarianceColName);
		result.add(this.multivarNormalMeanColName);
		result.add(this.procColName);
		return result;
	}

	
	public FILETYPE getFileType() {
		return fileType;
	}

	public String getShpFilePath() {
		return shpFilePath;
	}

	public String getOmPropsFilePath() {
		return omPropsFilePath;
	}

	/**
	 * @return the outFilePath
	 */
	public String getOutFilePath() {
		return outFilePath;
	}

	/**
	 * @return the uncertaintyTypes
	 */
	public List<String> getUncertaintyTypes() {
		return uncertaintyTypes;
	}

	/**
	 * @return the normalMeanColName
	 */
	public String getNormalMeanColName() {
		return normalMeanColName;
	}

	/**
	 * @return the normalVarianceColName
	 */
	public String getNormalVarianceColName() {
		return normalVarianceColName;
	}

	/**
	 * @return the logNormalMeanColName
	 */
	public String getLogNormalMeanColName() {
		return logNormalMeanColName;
	}

	/**
	 * @return the logNormalVarianceColName
	 */
	public String getLogNormalVarianceColName() {
		return logNormalVarianceColName;
	}

	/**
	 * @return the multivarNormalMeanColName
	 */
	public String getMultivarNormalMeanColName() {
		return multivarNormalMeanColName;
	}

	/**
	 * @return the multivarNormalCovarianceColName
	 */
	public String getMultivarNormalCovarianceColName() {
		return multivarNormalCovarianceColName;
	}
	
	/**
	 * 
	 * @return the certainDataColName
	 */
	public String getCertainDataColName(){
		return certainDataColName;
	}
}
