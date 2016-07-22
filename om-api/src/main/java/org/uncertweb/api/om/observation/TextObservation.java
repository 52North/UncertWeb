package org.uncertweb.api.om.observation;

import java.net.URI;

import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Text observation contains string result and inherits other properties from AbstractObservation
 * 
 * @author Kiesow, staschc
 *
 */
public class TextObservation extends AbstractObservation {
	
	/** measure result of the observation */
	private TextResult result;
	
	/**type name of this observation*/
	public static final String NAME = "OM_TextObservation";
	
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
	public TextObservation(TimeObject phenomenonTime, TimeObject resultTime,
			URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest, TextResult result) {
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
	 *            (optional) result qualities as UncertaintyResults
	 * @throws Exception
	 */
	public TextObservation(Identifier identifier, Envelope boundedBy,
			TimeObject phenomenonTime, TimeObject resultTime,
			TimeObject validTime, URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality, TextResult result) {
		super(identifier, boundedBy, phenomenonTime, resultTime, validTime,
				procedure, observedProperty, featureOfInterest, resultQuality,
				result);
	}

	@Override
	public TextResult getResult() {
		return result;
	}

	@Override
	public void setResult(IResult result) throws IllegalArgumentException {
		if(result instanceof TextResult){
			this.result = (TextResult)result;
		}
		else throw new IllegalArgumentException("Result type of TextObservation has to be Text!");
		
	}

	@Override
	public String getName() {
		return NAME;
	}
}
