package org.uncertweb.api.om.io;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.JSONEncoder;
import org.uncertweb.api.gml.io.JSONGeometryEncoder;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.CategoryResult;
import org.uncertweb.api.om.result.IResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

/**
 * encoder for UncertWeb O&M observations to JSON
 * 
 * @author staschc
 *
 */
public class JSONObservationEncoder implements IObservationEncoder{

	
	@Override
	public String encodeObservation(AbstractObservation obs)
			throws IllegalArgumentException, XmlException,
			UnsupportedUncertaintyTypeException, UncertaintyEncoderException, JSONException {
		String jsonString;
		JSONStringer writer = new JSONStringer();
		encodeObservation(writer,obs);
		jsonString=writer.toString();
		return jsonString;
	}

	

	@Override
	public String encodeObservationCollection(IObservationCollection obsCol)
			throws IllegalArgumentException, XmlException,
			UnsupportedUncertaintyTypeException, UncertaintyEncoderException, JSONException {
		JSONStringer writer = new JSONStringer();
		writer.object();
		writer.key(obsCol.getTypeName());
		List<? extends AbstractObservation> obsList = obsCol.getObservations();
			writer.array();
		for (AbstractObservation obs:obsList){
			encodeObservation(writer,obs);
		}
		writer.endArray();
		writer.endObject();
		return writer.toString();
	}
	
	/**
	 * encodes an observation to JSON
	 * 
	 * @param writer
	 * @param obs
	 * @throws JSONException
	 */
	private void encodeObservation(JSONStringer writer, AbstractObservation obs) throws JSONException {
		writer.object();
		writer.key(obs.getName());
		writer.object();
		
		//encode identifier, if present
		if (obs.getIdentifier()!=null){
			writer.key("identifier");
			writer.object();
			writer.key("codeSpace");
			writer.value(obs.getIdentifier().getCodeSpace());
			writer.key("value");
			writer.value(obs.getIdentifier().getIdentifier());
			writer.endObject();
		}
		//encode PhenomenonTime
		writer.key("phenomenonTime");
		encodeTime(writer,obs.getPhenomenonTime());
		//encode ResultTime
		writer.key("resultTime");
		encodeTime(writer,obs.getPhenomenonTime());
		if (obs.getValidTime()!=null){
			writer.key("validTime");
			encodeTime(writer,obs.getPhenomenonTime());
		}
		//encode observed property
		writer.key("observedProperty");
		writer.value(obs.getObservedProperty().toString());
		
		//encode procedure
		writer.key("procedure");
		writer.value(obs.getProcedure().toString());
		
		//encode foi
		encodeSamplingFeature(writer,obs.getFeatureOfInterest());
		
		//encode resultQuality
		if (obs.getResultQuality()!=null){
			encodeResultQuality(writer,obs.getResultQuality());
		}
		
		encodeResult(writer,obs.getResult());
		writer.endObject();
		writer.endObject();
	}



	private void encodeResult(JSONStringer writer, IResult result) throws JSONException {
		writer.key("result");
		if (result instanceof BooleanResult){
			writer.value(result.getValue());
		}
		else if (result instanceof CategoryResult){
			writer.object();
			writer.key("codeSpace");
			writer.value(((CategoryResult) result).getCodeSpace());
			writer.key("value");
			writer.value(((CategoryResult) result).getCategoryValue());
			writer.endObject();
		}
		else if (result instanceof IntegerResult){
			writer.value(((IntegerResult) result).getIntegerValue());
		}
		else if (result instanceof MeasureResult){
			writer.object();
			writer.key("uom");
			writer.value(((MeasureResult) result).getUnitOfMeasurement());
			writer.key("value");
			writer.value(((MeasureResult) result).getMeasureValue());
			writer.endObject();
		}
		else if (result instanceof ReferenceResult){
			writer.object();
			
			//TODO add further elements
			writer.key("href");
			writer.value(((ReferenceResult) result).getHref());
			writer.key("role");
			writer.value(((ReferenceResult) result).getRole());
			writer.endObject();
		}
		else if (result instanceof TextResult){
			writer.value(((TextResult) result).getTextValue());
		}
		else if (result instanceof UncertaintyResult){
			writer.object();
			writer.key("uom");
			writer.value(((UncertaintyResult)result).getUnitOfMeasurement());
			writer.key("value");
			JSONObject jsonUncertainty = new JSONObject(new JSONEncoder().encode(((UncertaintyResult) result).getUncertaintyValue()));
			writer.value(jsonUncertainty);
			writer.endObject();
		}
	}



	private void encodeResultQuality(JSONStringer writer,
			DQ_UncertaintyResult[] resultQuality) throws JSONException {
		writer.key("resultQuality");
		writer.array();
		for (int i=0;i<resultQuality.length;i++){
			writer.object();
			writer.key("uom");
			writer.value(resultQuality[i].getUom());
			writer.key("values");
			IUncertainty[] values = resultQuality[i].getValues();
			writer.array();
			for (int j=0;j<values.length;j++){
				//writer.value(new JSONEncoder().encode(values[j]));
				
				JSONObject jsonUncertainty = new JSONObject(new JSONEncoder().encode(values[j]));
				writer.value(jsonUncertainty);
			}
			writer.endArray();
			writer.endObject();
		}
		writer.endArray();
	}



	private void encodeSamplingFeature(JSONStringer writer,
			SpatialSamplingFeature foi) throws JSONException {
		writer.key("featureOfInterest");
		writer.object();
		writer.key("SF_SpatialSamplingFeature");
		writer.object();
		//TODO encapsulate in method encodeIdentifier()
		if (foi.getIdentifier()!=null){
		writer.key("identifier");
			writer.object();
			writer.key("codeSpace");
			writer.value(foi.getIdentifier().getCodeSpace().toString());
			writer.key("value");
			writer.value(foi.getIdentifier().getIdentifier());
			writer.endObject();
		}
		writer.key("type");
		writer.value(foi.getFeatureType());
		writer.key("sampledFeature");
		writer.value(foi.getSampledFeature());
		writer.key("shape");
		new JSONGeometryEncoder().encodeGeometry(writer, foi.getShape());
		writer.endObject();
		writer.endObject();
	}



	private void encodeTime(JSONStringer writer, TimeObject time) throws JSONException {
		writer.object();
		if (time.getDateTime()!=null){
			writer.key("TimeInstant");
			writer.object();
			writer.key("timePosition");
			writer.value(time.getDateTime().toString());
			writer.endObject();
		}
		else if (time.getInterval()!=null){
			writer.key("TimePeriod");
			writer.object();
			writer.key("begin");
			writer.object();
			writer.key("TimeInstant");
			writer.object();
			writer.key("timePosition");
			writer.value(time.getInterval().getStart().toString());
			writer.endObject();
			writer.endObject();
			writer.key("end");
			writer.object();
			writer.key("TimeInstant");
			writer.object();
			writer.key("timePosition");
			writer.value(time.getInterval().getEnd().toString());
			writer.endObject();
			writer.endObject();
			writer.endObject();
		}
		writer.endObject();
	}



	

}