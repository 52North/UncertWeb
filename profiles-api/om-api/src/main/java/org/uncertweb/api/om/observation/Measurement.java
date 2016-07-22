package org.uncertweb.api.om.observation;

import java.net.URI;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Measurement contains double result and inherits other properties from AbstractObservation
 * 
 * @author Kiesow, staschc
 *
 */
public class Measurement extends AbstractObservation {

	/** measure result of the observation */
	private MeasureResult result;
	
	/**type name of this observation*/
	public static final String NAME = "OM_Measurement";
	
	/**
	 * Constructor with mandatory attributes
	 * 
	 * @param phenomenonTime
	 *            phenomenon time property
	 * @param resultTime
	 *            result time property
	 * @param procedure
	 *            procedure property
	 * @param observedProperty
	 *            observed property property
	 * @param featureOfInterest
	 *            feature of interest property
	 * @param result
	 *            result
	 * @throws Exception 
	 */
	public Measurement(TimeObject phenomenonTime, TimeObject resultTime,
			URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest, MeasureResult result) throws Exception{
		super(phenomenonTime,resultTime,procedure,observedProperty,featureOfInterest);
		setResult(result);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 *            identifier of the observation
	 * @param boundedBy
	 *            (optional) spatial and temporal extent
	 * @param phenomenonTime
	 *            phenomenon time property
	 * @param resultTime
	 *            result time property
	 * @param validTime
	 *            (optional) valid time property
	 * @param procedure
	 *            procedure property
	 * @param observedProperty
	 *            observed property property
	 * @param featureOfInterest
	 *            feature of interest property
	 * @param result
	 *            result
	 * @param resultQuality
	 * 			  (optional) result qualities as UncertaintyResults
	 * @throws Exception 
	 */
	public Measurement(Identifier identifier, Envelope boundedBy, TimeObject phenomenonTime,
			TimeObject resultTime, TimeObject validTime, URI procedure,
			URI observedProperty, SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality, MeasureResult result){
		super(phenomenonTime,resultTime,procedure,observedProperty,featureOfInterest);
		setIdentifier(identifier);
		setBoundedBy(boundedBy);
		setValidTime(validTime);
		setResultQuality(resultQuality);
		setResult(result);
	}

	@Override
	public MeasureResult getResult() {
		return result;
	}

	@Override
	public void setResult(IResult result) throws IllegalArgumentException {
		if(result instanceof MeasureResult){
			this.result = (MeasureResult)result;
		}
		else throw new IllegalArgumentException("Result type of MeasurementObservation has to be Measurement!");
		
	}

	@Override
	public String getName() {
		return NAME;
	}
}
