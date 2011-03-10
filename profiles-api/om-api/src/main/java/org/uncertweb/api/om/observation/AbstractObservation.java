package org.uncertweb.api.om.observation;

import java.net.URI;

import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Abstract super class for all observation types
 * 
 * @author Kiesow, staschc
 *
 */
public abstract class AbstractObservation {

	private String identifier;
	private Envelope boundedBy;
	private TimeObject phenomenonTime;
	private TimeObject resultTime;
	private TimeObject validTime;
	private URI procedure;
	private URI observedProperty;
	private SpatialSamplingFeature featureOfInterest;

	/**
	 * Data quality uncertainty result is an optional property of an
	 * Observation. While it is placed here for better usability, the schema
	 * intends to provide it as resultQuality > AbstractDQ_Element > result >
	 * AbstractDQ_Result
	 */
	private DQ_UncertaintyResult[] resultQuality;

	/**
	 * Constructor with mandatory attributes
	 * 
	 * @param identifier
	 *            observation identifier
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
	public AbstractObservation(String identifier, TimeObject phenomenonTime, TimeObject resultTime,
			URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest) {

		this.identifier = identifier;
		this.phenomenonTime = phenomenonTime;
		this.resultTime = resultTime;
		this.procedure = procedure;
		this.observedProperty = observedProperty;
		this.featureOfInterest = featureOfInterest;
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
	 */
	public AbstractObservation(String identifier, Envelope boundedBy, TimeObject phenomenonTime,
			TimeObject resultTime, TimeObject validTime, URI procedure,
			URI observedProperty, SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality) {
		this(identifier, phenomenonTime, resultTime, procedure, observedProperty,
				featureOfInterest);

		this.boundedBy = boundedBy;
		this.validTime = validTime;
		this.resultQuality = resultQuality;
	}
	
	///////////////////////////////////////////////////
	//abstract Methods
	/**
	 * @return Returns the result of the observation
	 */
	public abstract IResult getResult();

	/**
	 * 
	 * @param result
	 * 			the result of the observation
	 * @throws Exception
	 * 			if the type of the result does not match the type defined by the Observation type
	 */
	public abstract void setResult(IResult result) throws Exception;

	///////////////////////////////////////////////////
	// getters and setters
	/**
	 * @return the gmlId
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param gmlId the gmlId to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the boundedBy
	 */
	public Envelope getBoundedBy() {
		return boundedBy;
	}

	/**
	 * @param boundedBy the boundedBy to set
	 */
	public void setBoundedBy(Envelope boundedBy) {
		this.boundedBy = boundedBy;
	}

	/**
	 * @return the phenomenonTime
	 */
	public TimeObject getPhenomenonTime() {
		return phenomenonTime;
	}

	/**
	 * @param phenomenonTime the phenomenonTime to set
	 */
	public void setPhenomenonTime(TimeObject phenomenonTime) {
		this.phenomenonTime = phenomenonTime;
	}

	/**
	 * @return the resultTime
	 */
	public TimeObject getResultTime() {
		return resultTime;
	}

	/**
	 * @param resultTime the resultTime to set
	 */
	public void setResultTime(TimeObject resultTime) {
		this.resultTime = resultTime;
	}

	/**
	 * @return the validTime
	 */
	public TimeObject getValidTime() {
		return validTime;
	}

	/**
	 * @param validTime the validTime to set
	 */
	public void setValidTime(TimeObject validTime) {
		this.validTime = validTime;
	}

	/**
	 * @return the procedure
	 */
	public URI getProcedure() {
		return procedure;
	}

	/**
	 * @param procedure the procedure to set
	 */
	public void setProcedure(URI procedure) {
		this.procedure = procedure;
	}

	/**
	 * @return the observedProperty
	 */
	public URI getObservedProperty() {
		return observedProperty;
	}

	/**
	 * @param observedProperty the observedProperty to set
	 */
	public void setObservedProperty(URI observedProperty) {
		this.observedProperty = observedProperty;
	}

	/**
	 * @return the featureOfInterest
	 */
	public SpatialSamplingFeature getFeatureOfInterest() {
		return featureOfInterest;
	}

	/**
	 * @param featureOfInterest the featureOfInterest to set
	 */
	public void setFeatureOfInterest(SpatialSamplingFeature featureOfInterest) {
		this.featureOfInterest = featureOfInterest;
	}

	/**
	 * @return the resultQuality
	 */
	public DQ_UncertaintyResult[] getResultQuality() {
		return resultQuality;
	}

	/**
	 * @param resultQuality the resultQuality to set
	 */
	public void setResultQuality(DQ_UncertaintyResult[] resultQuality) {
		this.resultQuality = resultQuality;
	}

	
	
}
