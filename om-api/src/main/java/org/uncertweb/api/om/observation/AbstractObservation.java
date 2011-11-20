package org.uncertweb.api.om.observation;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.uncertweb.api.gml.Identifier;
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

	protected Identifier identifier;
	protected Envelope boundedBy;
	protected TimeObject phenomenonTime;
	protected TimeObject resultTime;
	protected TimeObject validTime;
	protected URI procedure;
	protected URI observedProperty;
	protected SpatialSamplingFeature featureOfInterest;
	protected Map<String, Object> parameters = new HashMap<String, Object>();

	/**
	 * Data quality uncertainty result is an optional property of an
	 * Observation. While it is placed here for better usability, the schema
	 * intends to provide it as resultQuality > AbstractDQ_Element > result >
	 * AbstractDQ_Result
	 */
	protected DQ_UncertaintyResult[] resultQuality;

	
	
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
	public AbstractObservation(TimeObject phenomenonTime, TimeObject resultTime,
			URI procedure, URI observedProperty,
			SpatialSamplingFeature featureOfInterest, IResult result) {
		setPhenomenonTime(phenomenonTime);
		setResultTime(resultTime);
		setProcedure(procedure);
		setObservedProperty(observedProperty);
		setFeatureOfInterest(featureOfInterest);
		setResult(result);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 *            identifier of observation
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
	public AbstractObservation(Identifier identifier, Envelope boundedBy, TimeObject phenomenonTime,
			TimeObject resultTime, TimeObject validTime, URI procedure,
			URI observedProperty, SpatialSamplingFeature featureOfInterest,
			DQ_UncertaintyResult[] resultQuality, IResult result) {
		this(phenomenonTime, resultTime, procedure, observedProperty, featureOfInterest,result);
		setIdentifier(identifier);
		setBoundedBy(boundedBy);
		setValidTime(validTime);
		setResultQuality(resultQuality);
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
	public abstract void setResult(IResult result) throws IllegalArgumentException;

	///////////////////////////////////////////////////
	// getters and setters
	/**
	 * @return the gmlId
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * @param gmlId the gmlId to set
	 */
	public void setIdentifier(Identifier identifier) {
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

	/**
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	/**
	 * @param id the name of the parameter
	 * @return the parameter with the specified name
	 */
	public Object getParameter(String key) {
		return parameters.get(key);
	}

	/**
	 * @param key the key of the parameter
	 * @param value the value of the parameter
	 */
	public void addParameter(String key, Object value) { 
		this.parameters.put(key, value);
	}
	
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * returns the name of observation
	 * 
	 * @return
	 */
	public abstract String getName();
	
}
