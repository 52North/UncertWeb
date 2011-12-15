package org.uncertweb.api.om.observation;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.uncertml.IUncertainty;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.CategoryResult;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Abstract super class for all observation types
 * 
 * @author Kiesow, staschc
 *
 */
public abstract class AbstractObservation {
	public static class Builder {

		private Identifier identifier;
		private Envelope boundedBy;
		private TimeObject phenomenonTime;
		private TimeObject resultTime;
		private TimeObject validTime;
		private URI procedure;
		private URI observedProperty;
		private SpatialSamplingFeature featureOfInterest;
		private IResult result;
		private DQ_UncertaintyResult[] resultQuality;

		public static Builder instance() {
			return new Builder();
		}

		public Builder withIdentifier(URI codeSpace, String identifier) {
			return withIdentifier(new Identifier(codeSpace, identifier));
		}

		public Builder withIdentifier(Identifier i) {
			this.identifier = i;
			return this;
		}

		public Builder withBoundedBy(Envelope e) {
			this.boundedBy = e;
			return this;
		}

		public Builder withPhenomenonTime(DateTime t) {
			return withPhenomenonTime(new TimeObject(t));
		}

		public Builder withPhenomenonTime(Interval t) {
			return withPhenomenonTime(new TimeObject(t));
		}

		public Builder withPhenomenonTime(DateTime b, DateTime e) {
			return withPhenomenonTime(new TimeObject(b, e));
		}

		public Builder withPhenomenonTime(TimeObject t) {
			this.phenomenonTime = t;
			return this;
		}

		public Builder withPhenomenonAndResultTime(DateTime t) {
			return withPhenomenonTime(t).withResultTime(t);
		}

		public Builder withPhenomenonAndResultTime(Interval t) {
			return withPhenomenonTime(t).withResultTime(t);
		}

		public Builder withPhenomenonAndResultTime(DateTime b, DateTime e) {
			return withPhenomenonTime(b, e).withResultTime(b, e);
		}

		public Builder withPhenomenonAndResultTime(TimeObject t) {
			return withPhenomenonTime(t).withResultTime(t);
		}

		public Builder withResultTime(DateTime t) {
			return withResultTime(new TimeObject(t));
		}

		public Builder withResultTime(Interval t) {
			return withResultTime(new TimeObject(t));
		}

		public Builder withResultTime(DateTime b, DateTime e) {
			return withResultTime(new TimeObject(b, e));
		}

		public Builder withResultTime(TimeObject t) {
			this.resultTime = t;
			return this;
		}

		public Builder withValidTime(DateTime t) {
			return withValidTime(new TimeObject(t));
		}

		public Builder withValidTime(Interval t) {
			return withValidTime(new TimeObject(t));
		}

		public Builder withValidTime(DateTime b, DateTime e) {
			return withValidTime(new TimeObject(b, e));
		}

		public Builder withValidTime(TimeObject t) {
			this.validTime = t;
			return this;
		}

		public Builder withProcedure(String p) {
			return withProcedure(URI.create(p));
		}

		public Builder withProcedure(URI p) {
			this.procedure = p;
			return this;
		}

		public Builder withObservedProperty(String p) {
			return withObservedProperty(URI.create(p));
		}

		public Builder withObservedProperty(URI o) {
			this.observedProperty = o;
			return this;
		}

		public Builder withFeatureOfInterest(SpatialSamplingFeature sff) {
			this.featureOfInterest = sff;
			return this;
		}

		public Builder withResult(double value, String unit) {
			return withResult(new MeasureResult(value, unit));
		}

		public Builder withResult(String value, String codeSpace) {
			return withResult(new CategoryResult(value, codeSpace));
		}

		public Builder withResult(BigInteger value) {
			return withResult(new IntegerResult(value));
		}

		public Builder withResult(boolean bool) {
			return withResult(new BooleanResult(bool));
		}

		public Builder withResult(String text) {
			return withResult(new TextResult(text));
		}

		public Builder withResult(IUncertainty u) {
			return withResult(u, null);
		}

		public Builder withResult(IUncertainty u, String uom) {
			return withResult(new UncertaintyResult(u, uom));
		}

		public Builder withResult(URI href, String role) {
			return withResult(new ReferenceResult(href.toString(), role));
		}

		public Builder withResult(IResult r) {
			this.result = r;
			return this;
		}

		public Builder withResultQuality(DQ_UncertaintyResult[] ur) {
			this.resultQuality = ur;
			return this;
		}

		public Builder withResultQuality(DQ_UncertaintyResult ur) {
			return withResultQuality(new DQ_UncertaintyResult[] { ur });
		}

		public AbstractObservation build() {
			Validate.notNull(phenomenonTime, "PhenomenonTime must not be null.");
			Validate.notNull(resultTime, "ResultTime must not be null.");
			Validate.notNull(procedure, "Procedure must not be null.");
			Validate.notNull(observedProperty, "ObservedProperty must not be null.");
			Validate.notNull(featureOfInterest, "FeatureOfInterest must not be null.");
			Validate.notNull(result, "Result must not be null.");

			AbstractObservation ao = null;

			if (result instanceof MeasureResult) {
				ao = new Measurement(identifier, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality,
						(MeasureResult) result);
			} else if (result instanceof BooleanResult) {
				ao = new BooleanObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						(BooleanResult) result);
			} else if (result instanceof CategoryResult) {
				ao = new CategoryObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						(CategoryResult) result);
			} else if (result instanceof IntegerResult) {
				ao = new DiscreteNumericObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						(IntegerResult) result);
			} else if (result instanceof ReferenceResult) {
				ao = new ReferenceObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						(ReferenceResult) result);
			} else if (result instanceof TextResult) {
				ao = new TextObservation(identifier, boundedBy, phenomenonTime,
						resultTime, validTime, procedure, observedProperty,
						featureOfInterest, resultQuality, (TextResult) result);
			} else if (result instanceof UncertaintyResult) {
				ao = new UncertaintyObservation(identifier, boundedBy,
						phenomenonTime, resultTime, validTime, procedure,
						observedProperty, featureOfInterest, resultQuality,
						(UncertaintyResult) result);
			}
			return ao;
		}
	}
	

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
