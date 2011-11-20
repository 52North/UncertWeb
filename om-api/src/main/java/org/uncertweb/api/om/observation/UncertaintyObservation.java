package org.uncertweb.api.om.observation;

import java.net.URI;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Boolean observation contains uncertainty result and inherits other properties from AbstractObservation
 * 
 * @author Kiesow, staschc
 *
 */
public class UncertaintyObservation extends AbstractObservation{

	
	/** uncertainty result of the observation */
	private UncertaintyResult result;
	
	/**type name of this observation*/
	public static final String NAME = "OM_UncertaintyObservation";
	
	
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
	public UncertaintyObservation(TimeObject phenomenonTime,
			TimeObject resultTime, URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest, UncertaintyResult result) {
		super(phenomenonTime, resultTime, procedure, observedProperty,
				featureOfInterest, result);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
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
	public UncertaintyObservation(Identifier identifier, Envelope boundedBy,
			TimeObject phenomenonTime, TimeObject resultTime,
			TimeObject validTime, URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality, UncertaintyResult result) {
		super(identifier, boundedBy, phenomenonTime, resultTime, validTime,
				procedure, observedProperty, featureOfInterest, resultQuality,
				result);
	}

	@Override
	public UncertaintyResult getResult() {
		return result;
	}

	@Override
	public void setResult(IResult result) throws IllegalArgumentException {
		if(result instanceof UncertaintyResult){
			this.result = (UncertaintyResult)result;
		}
		else throw new IllegalArgumentException("Result type of UncertaintyObservation has to be Uncertainty!");
		
	}

	@Override
	public String getName() {
		return NAME;
	}
}
