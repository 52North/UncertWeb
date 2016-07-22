package org.uncertweb.ems.data.exposure;

import java.util.ArrayList;
import org.uncertweb.ems.util.ExposureModelConstants;

/**
 * Data container for exposure value realisations with metadata about the source type and the pollutant
 * @author LydiaGerharz
 *
 */
public class ExposureValue {

	// store information about source (indoor, outdoor)
	private String type;
	
	// store realisations of concentration
	private double[] vals;
	
	// store information about pollutant
	private String pollutant;
	private String uom;
	
	public ExposureValue(double[] exposureValues, String type, String pollutant, String uom){
		setExposureValues(exposureValues);
		setType(type);
		setPollutant(pollutant);
	}
	
	// get and set for exposure values
	public void setExposureValues(double[] exposureValues){
		vals = exposureValues;
	}
	
	public double[] getExposureValues(){
		return vals;
	}
	
	// get and set for source type
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
	
	// get and set for pollutant
	public void setPollutant(String pollutant){
		this.pollutant = pollutant;
	}
	
	public String getPollutant(){
		return pollutant;
	}
	
	// get and set for uom
	public void setUom(String uom){
		this.uom = uom;
	}
		
	public String getUom(){
		return uom;
	}
}
