package org.uncertweb.api.om.observation;

import java.net.URI;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.CategoryResult;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

public class CategoryObservation extends AbstractObservation{
	
	/** measure result of the observation */
	private CategoryResult result;
	
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
	 */
	public CategoryObservation(Identifier identifier, TimeObject phenomenonTime, TimeObject resultTime,
			URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest, CategoryResult result){
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
	public CategoryObservation(Identifier identifier, Envelope boundedBy, TimeObject phenomenonTime,
			TimeObject resultTime, TimeObject validTime, URI procedure,
			URI observedProperty, SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality, CategoryResult result){
		
		super(phenomenonTime,resultTime,procedure,observedProperty,featureOfInterest);
		setIdentifier(identifier);
		setBoundedBy(boundedBy);
		setValidTime(validTime);
		setResultQuality(resultQuality);
		setResult(result);
	}

	@Override
	public CategoryResult getResult() {
		return result;
	}

	@Override
	public void setResult(IResult result) throws IllegalArgumentException {
		if(result instanceof CategoryResult){
			this.result = (CategoryResult)result;
		}
		else throw new IllegalArgumentException("Result type of CategoryObservation has to be CategoryResult!");
		
	}
}
