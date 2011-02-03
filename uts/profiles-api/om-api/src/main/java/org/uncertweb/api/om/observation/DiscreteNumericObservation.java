package org.uncertweb.api.om.observation;

import java.net.URI;

import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Discrete observation contains BigInteger result and inherits other properties from AbstractObservation
 * 
 * @author Kiesow, staschc
 *
 */
public class DiscreteNumericObservation extends AbstractObservation{

	/** integer result of the observation */
	private IntegerResult result;
	
	/**
	 * Constructor with mandatory attributes
	 * 
	 * @param gmlId
	 *            gml id attribute
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
	public DiscreteNumericObservation(String gmlId, TimeObject phenomenonTime, TimeObject resultTime,
			URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest, IntegerResult result){
		super(gmlId,phenomenonTime,resultTime,procedure,observedProperty,featureOfInterest);
		try {
			setResult(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param gmlId
	 *            gml id attribute
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
	public DiscreteNumericObservation(String gmlId, Envelope boundedBy, TimeObject phenomenonTime,
			TimeObject resultTime, TimeObject validTime, URI procedure,
			URI observedProperty, SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality, IntegerResult result){
		super(gmlId,phenomenonTime,resultTime,procedure,observedProperty,featureOfInterest);

		setBoundedBy(boundedBy);
		setValidTime(validTime);
		setResultQuality(resultQuality);
		try {
			setResult(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IntegerResult getResult() {
		return result;
	}

	@Override
	public void setResult(IResult result) throws Exception {
		if(result instanceof IntegerResult){
			this.result = (IntegerResult)result;
		}
		else throw new Exception("Result type of DiscreteNumericObservation has to be Integer!");
		
	}

}
