package org.uncertweb.api.om.observation;

import java.net.URI;

import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Reference observation contains reference as result and inherits other properties from AbstractObservation
 * 
 * @author Kiesow, staschc
 *
 */
public class ReferenceObservation extends AbstractObservation{

	/** measure result of the observation */
	private ReferenceResult result;
	
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
	public ReferenceObservation(String gmlId, TimeObject phenomenonTime, TimeObject resultTime,
			URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest, ReferenceResult result){
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
	public ReferenceObservation(String gmlId, Envelope boundedBy, TimeObject phenomenonTime,
			TimeObject resultTime, TimeObject validTime, URI procedure,
			URI observedProperty, SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality, ReferenceResult result){
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
	public ReferenceResult getResult() {
		return result;
	}

	@Override
	public void setResult(IResult result) throws Exception {
		if(result instanceof ReferenceResult){
			this.result = (ReferenceResult)result;
		}
		else throw new Exception("Result type of ReferenceObservation has to be Reference!");
		
	}
	
}
