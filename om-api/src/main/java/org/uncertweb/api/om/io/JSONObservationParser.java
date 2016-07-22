package org.uncertweb.api.om.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.JSONParser;
import org.uncertweb.api.gml.Identifier;
import org.uncertweb.api.gml.io.JSONGeometryDecoder;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.TimeObject;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.api.om.result.BooleanResult;
import org.uncertweb.api.om.result.CategoryResult;
import org.uncertweb.api.om.result.IntegerResult;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.result.ReferenceResult;
import org.uncertweb.api.om.result.TextResult;
import org.uncertweb.api.om.result.UncertaintyResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * parser for parsing observations according to the UncertWeb observation profile encoded in JSON
 *
 * @author staschc
 *
 */
public class JSONObservationParser implements IObservationParser{

	@Override
	public AbstractObservation parseObservation(String jsonObs) throws OMParsingException {
		AbstractObservation result = null;
		JSONObject obs;
		try {
			obs = new JSONObject(jsonObs);

		JSONObject jobs =null;
		String typeName =null;

		////////////////////////
		//check type and retrieve observation JSON object
		//obs is measurement
		if (obs.has(Measurement.NAME)){
			typeName=Measurement.NAME;
			jobs = obs.getJSONObject(Measurement.NAME);
		}
		else if (obs.has(BooleanObservation.NAME)){
			typeName=BooleanObservation.NAME;
			jobs=obs.getJSONObject(BooleanObservation.NAME);
		}
		else if (obs.has(CategoryObservation.NAME)){
			typeName=CategoryObservation.NAME;
			jobs=obs.getJSONObject(CategoryObservation.NAME);
		}
		else if (obs.has(DiscreteNumericObservation.NAME)){
			typeName=DiscreteNumericObservation.NAME;
			jobs=obs.getJSONObject(DiscreteNumericObservation.NAME);
		}
		else if (obs.has(ReferenceObservation.NAME)){
			typeName=ReferenceObservation.NAME;
			jobs=obs.getJSONObject(ReferenceObservation.NAME);
		}
		else if (obs.has(TextObservation.NAME)){
			typeName=TextObservation.NAME;
			jobs=obs.getJSONObject(TextObservation.NAME);
		}
		else if (obs.has(UncertaintyObservation.NAME)){
			typeName=UncertaintyObservation.NAME;
			jobs=obs.getJSONObject(UncertaintyObservation.NAME);
		}



		else{
			throw new OMParsingException("Observation type is not supported by JSON parser!");
		}

		/////////////////////////////
		//parse common properties
		Identifier identifier =null;
		if (jobs.has("identifier")){
			identifier = parseIdentifier(jobs.getJSONObject("identifier"));
		}
		TimeObject phenomenonTime = parseTime(jobs.getJSONObject("phenomenonTime"));
		TimeObject resultTime = parseTime(jobs.getJSONObject("resultTime"));
		TimeObject validTime = null;
		if (jobs.has("validTime")){
			validTime = parseTime(jobs.getJSONObject("validTime"));
		}
		URI procedureURI = new URI(jobs.getString("procedure"));
		URI obsPropURI = new URI(jobs.getString("observedProperty"));
		JSONObject jfoi = jobs.getJSONObject("featureOfInterest").getJSONObject("SF_SpatialSamplingFeature");
		SpatialSamplingFeature foi = parseSamplingFeature(jfoi);
		DQ_UncertaintyResult[] resultQuality = null;
		if (jobs.has("resultQuality")){
			resultQuality = parseResultQuality(jobs.getJSONArray("resultQuality"));
		}

		//parse results
		if (typeName.equals(Measurement.NAME)){
			JSONObject jresult = jobs.getJSONObject("result");
			String uom = jresult.getString("uom");
			double value = jresult.getDouble("value");
			MeasureResult mresult = new MeasureResult(value,uom);
			result = new Measurement(identifier,null,phenomenonTime,resultTime,validTime,procedureURI,obsPropURI,foi,resultQuality,mresult);
		}
		else if (typeName.equals(BooleanObservation.NAME)){
			BooleanResult bresult = new BooleanResult(jobs.getBoolean("result"));
			result = new BooleanObservation(identifier,null,phenomenonTime,resultTime,validTime,procedureURI,obsPropURI,foi,resultQuality,bresult);
		}
		else if (typeName.equals(CategoryObservation.NAME)){
			JSONObject jresult = jobs.getJSONObject("result");
			String codeSpace = jresult.getString("codeSpace");
			String value = jresult.getString("value");
			CategoryResult cresult = new CategoryResult(value,codeSpace);
			result = new CategoryObservation(identifier,null,phenomenonTime,resultTime,validTime,procedureURI,obsPropURI,foi,resultQuality,cresult);
		}

		else if (typeName.equals(ReferenceObservation.NAME)){
			JSONObject jresult = jobs.getJSONObject("result");
			String href = jresult.getString("href");
			String role = jresult.getString("role");
			ReferenceResult rresult = new ReferenceResult(href,role);
			result = new ReferenceObservation(identifier,null,phenomenonTime,resultTime,validTime,procedureURI,obsPropURI,foi,resultQuality,rresult);
		}

		else if (typeName.equals(DiscreteNumericObservation.NAME)){
			IntegerResult iresult = new IntegerResult(new BigInteger(jobs.getString("result")));
			result = new DiscreteNumericObservation(identifier,null,phenomenonTime,resultTime,validTime,procedureURI,obsPropURI,foi,resultQuality,iresult);
		}
		else if (typeName.equals(TextObservation.NAME)){
			TextResult tresult = new TextResult(jobs.getString("result"));
			result = new TextObservation(identifier,null,phenomenonTime,resultTime,validTime,procedureURI,obsPropURI,foi,resultQuality,tresult);
		}
		else if (typeName.equals(UncertaintyObservation.NAME)){
			JSONObject juncertainty = jobs.getJSONObject("result");
			IUncertainty uncertainty = new JSONParser().parse(juncertainty.getJSONObject("value").toString());
			UncertaintyResult uresult = new UncertaintyResult(uncertainty);
			result = new UncertaintyObservation(identifier,null,phenomenonTime,resultTime,validTime,procedureURI,obsPropURI,foi,resultQuality,uresult);
		}

		} catch (JSONException e) {
			throw new OMParsingException(e);
		} catch (UncertaintyParserException e) {
			throw new OMParsingException(e);
		} catch (URISyntaxException e) {
			throw new OMParsingException(e);
		}

		return result;

	}


	@Override
	public IObservationCollection parseObservationCollection(String jsonObsCol) throws OMParsingException {
		JSONObject job = null;
		try {
			job = new JSONObject(jsonObsCol);

		//BooleanObservationCollection
		if (job.has(BooleanObservationCollection.NAME)){
			JSONArray obsArray = job.getJSONArray(BooleanObservationCollection.NAME);
			List<BooleanObservation> resultArray = new ArrayList<BooleanObservation>(obsArray.length());
			for (int i=0;i<obsArray.length();i++){
				resultArray.add((BooleanObservation)parseObservation(obsArray.getJSONObject(i).toString()));
			}
			return new BooleanObservationCollection(resultArray);
		}
		//CategoryObservaationCollection
		else if (job.has(CategoryObservationCollection.NAME)){
			JSONArray obsArray = job.getJSONArray(CategoryObservationCollection.NAME);
			List<CategoryObservation> resultArray = new ArrayList<CategoryObservation>(obsArray.length());
			for (int i=0;i<obsArray.length();i++){
				resultArray.add((CategoryObservation)parseObservation(obsArray.getJSONObject(i).toString()));
			}
			return new CategoryObservationCollection(resultArray);
		}
		//DiscreteNumericObservation
		else if (job.has(DiscreteNumericObservationCollection.NAME)){
			JSONArray obsArray = job.getJSONArray(DiscreteNumericObservationCollection.NAME);
			List<DiscreteNumericObservation> resultArray = new ArrayList<DiscreteNumericObservation>(obsArray.length());
			for (int i=0;i<obsArray.length();i++){
				resultArray.add((DiscreteNumericObservation)parseObservation(obsArray.getJSONObject(i).toString()));
			}
			return new DiscreteNumericObservationCollection(resultArray);
		}
		//MeasurementCollection
		else if (job.has(MeasurementCollection.NAME)){
			JSONArray obsArray = job.getJSONArray(MeasurementCollection.NAME);
			List<Measurement> resultArray = new ArrayList<Measurement>(obsArray.length());
			for (int i=0;i<obsArray.length();i++){
				resultArray.add((Measurement)parseObservation(obsArray.getJSONObject(i).toString()));
			}
			return new MeasurementCollection(resultArray);
		}
		//ReferenceObservationCollection
		else if (job.has(ReferenceObservationCollection.NAME)){
			JSONArray obsArray = job.getJSONArray(ReferenceObservationCollection.NAME);
			List<ReferenceObservation> resultArray = new ArrayList<ReferenceObservation>(obsArray.length());
			for (int i=0;i<obsArray.length();i++){
				resultArray.add((ReferenceObservation)parseObservation(obsArray.getJSONObject(i).toString()));
			}
			return new ReferenceObservationCollection(resultArray);
		}
		//TextObservationCollection
		else if (job.has(TextObservationCollection.NAME)){
			JSONArray obsArray = job.getJSONArray(TextObservationCollection.NAME);
			List<TextObservation> resultArray = new ArrayList<TextObservation>(obsArray.length());
			for (int i=0;i<obsArray.length();i++){
				resultArray.add((TextObservation)parseObservation(obsArray.getJSONObject(i).toString()));
			}
			return new TextObservationCollection(resultArray);
		}
		//UncertaintyObservationCollection
		else if (job.has(UncertaintyObservationCollection.NAME)){
			JSONArray obsArray = job.getJSONArray(UncertaintyObservationCollection.NAME);
			List<UncertaintyObservation> resultArray = new ArrayList<UncertaintyObservation>(obsArray.length());
			for (int i=0;i<obsArray.length();i++){
				resultArray.add((UncertaintyObservation)parseObservation(obsArray.getJSONObject(i).toString()));
			}
			return new UncertaintyObservationCollection(resultArray);
		}

		//type of collection not supported
		else {
			throw new OMParsingException("ObservationCollection Type is not supported by JSONParser!");
		}

		} catch (Exception e) {
			throw new OMParsingException(e);
		}
	}

	@Override
	public IObservationCollection parse(String jsonString) throws OMParsingException {
		JSONObject job = null;
		IObservationCollection result = null;
		try {
			job = new JSONObject(jsonString);


		//if string is an collection, invoke method for parsing observations
		if (job.has(BooleanObservationCollection.NAME)||
				job.has(CategoryObservationCollection.NAME)||
				job.has(DiscreteNumericObservationCollection.NAME)||
				job.has(MeasurementCollection.NAME)||
				job.has(ReferenceObservationCollection.NAME)||
				job.has(TextObservationCollection.NAME)||
				job.has(UncertaintyObservationCollection.NAME)){
			result = parseObservationCollection(jsonString);
		}
		//observation is single observation
		else if (job.has(Measurement.NAME)){
			List<Measurement> members = new ArrayList<Measurement>();
			members.add((Measurement)parseObservation(jsonString));
			result = new MeasurementCollection(members);
		}
		else if (job.has(BooleanObservation.NAME)){
			List<BooleanObservation> members = new ArrayList<BooleanObservation>();
			members.add((BooleanObservation)parseObservation(jsonString));
			result = new BooleanObservationCollection(members);
		}
		else if (job.has(CategoryObservation.NAME)){
			List<CategoryObservation> members = new ArrayList<CategoryObservation>();
			members.add((CategoryObservation)parseObservation(jsonString));
			result = new CategoryObservationCollection(members);
		}
		else if (job.has(DiscreteNumericObservation.NAME)){
			List<DiscreteNumericObservation> members = new ArrayList<DiscreteNumericObservation>();
			members.add((DiscreteNumericObservation)parseObservation(jsonString));
			result = new DiscreteNumericObservationCollection(members);
		}
		else if (job.has(ReferenceObservation.NAME)){
			List<ReferenceObservation> members = new ArrayList<ReferenceObservation>();
			members.add((ReferenceObservation)parseObservation(jsonString));
			result = new ReferenceObservationCollection(members);
		}
		else if (job.has(TextObservation.NAME)){
			List<TextObservation> members = new ArrayList<TextObservation>();
			members.add((TextObservation)parseObservation(jsonString));
			result = new TextObservationCollection(members);
		}
		else if (job.has(UncertaintyObservation.NAME)){
			List<UncertaintyObservation> members = new ArrayList<UncertaintyObservation>();
			members.add((UncertaintyObservation)parseObservation(jsonString));
			result = new UncertaintyObservationCollection(members);
		}

		} catch (Exception e) {
			throw new OMParsingException("Error while creating JSONObject from input string!",e);
		}

		return result;
	}

	/**
	 * parses a JSON representation of an resultQuality array
	 *
	 * @param jsonArray
	 * 			array containing the resultQuality elements
	 * @return Returns internal representation of resultQuality (currently ONLY containinig uncertainties!)
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private DQ_UncertaintyResult[] parseResultQuality(JSONArray jsonArray) throws JSONException{
		DQ_UncertaintyResult[] result = null;
		if (jsonArray!=null){
			result = new DQ_UncertaintyResult[jsonArray.length()];
			for (int i=0;i<jsonArray.length();i++){
				JSONObject jobject = jsonArray.getJSONObject(i);
				String uom = jobject.getString("uom");
				JSONArray values = jobject.getJSONArray("values");
				IUncertainty[] uncertainties = new IUncertainty[values.length()];
				for (int j=0;j<values.length();j++){
					JSONObject juncertainty = values.getJSONObject(j);
					try {
						uncertainties[j] = new JSONParser().parse(juncertainty.toString());
					} catch (UncertaintyParserException e) {
						throw new IllegalArgumentException(e.getMessage());
					}
				}
				result[i]=new DQ_UncertaintyResult(uncertainties,uom);
			}
		}
		return result;
	}


	/**
	 * helper method for identifier in JSON representation
	 *
	 * @param jsonObject
	 * 			identifier encoded in JSON
	 * @return Returns internal representation of identifier
	 * @throws URISyntaxException
	 * 			if parsing of codespace fails
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private Identifier parseIdentifier(JSONObject jsonObject) throws URISyntaxException, JSONException {
		URI codeSpace = new URI(jsonObject.getString("codeSpace"));
		String value = jsonObject.getString("value");
		return new Identifier(codeSpace, value);
	}


	/**
	 * helper method for parsing a spatial sampling feature in JSON representation
	 *
	 * @param jfoi
	 * 			spatial sampling feature encoded in JSON
	 * @return Returns internal representation of spatial sampling feature
	 * @throws IllegalArgumentException
	 * 			if parsing fails
	 * @throws JSONException
	 * 			if parsing fails
	 * @throws URISyntaxException
	 */
	private SpatialSamplingFeature parseSamplingFeature(JSONObject jfoi) throws IllegalArgumentException, JSONException, URISyntaxException {
		Geometry geom;
		try {
			geom = new JSONGeometryDecoder().parseUwGeometry(jfoi.getString("shape"));
		} catch (XmlException e) {
			throw new IllegalArgumentException(e);
		}
		String sampledFeature = null;
		if (jfoi.has("sampledFeature")) {
			sampledFeature = jfoi.getString("sampledFeature");
		}
		Identifier identifier =null;
		if (jfoi.has("identifier")){
			identifier = parseIdentifier(jfoi.getJSONObject("identifier"));
		}
		return new SpatialSamplingFeature(identifier, sampledFeature, geom);
	}

	/**
	 * helper method for parsing a time object encoded in JSON
	 *
	 * @param jsonObject
	 * 			time object encoded in JSON
	 * @return Returns internal representation of timeobject
	 * @throws JSONException
	 * 			if parsing fails
	 */
	public TimeObject parseTime(JSONObject jsonObject) throws JSONException {

		//time is TimePeriod, has to be checked first, as it also contains TimeInstants.
		if (jsonObject.has("TimePeriod")) {
			JSONObject jtimePeriod = jsonObject.getJSONObject("TimePeriod");
			JSONObject jbegin = jtimePeriod.getJSONObject("begin").getJSONObject("TimeInstant");
			JSONObject jend = jtimePeriod.getJSONObject("end").getJSONObject("TimeInstant");
			TimeObject toBegin = parseTimeInstant(jbegin);
			TimeObject toEnd = parseTimeInstant(jend);
			return new TimeObject(toBegin.getDateTime(),toEnd.getDateTime());
		}

		//time is TimeInstant
		else {
			JSONObject jti = jsonObject.getJSONObject("TimeInstant");
			return parseTimeInstant(jti);
		}
	}

	/**
	 * helper method for parsing a time instant encoded in JSON
	 *
	 * @param jinstant
	 * 			time instant encoded in JSON
	 * @return Returns internal representation of time instant
	 * @throws JSONException
	 * 			if parsing fails
	 */
	private TimeObject parseTimeInstant(JSONObject jinstant) throws JSONException {
		return new TimeObject(jinstant.getString("timePosition"));
	}


	@Override
	public IObservationCollection parseObservationCollection(InputStream in)
			throws OMParsingException {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(in, writer);
		} catch (IOException e) {
			throw new OMParsingException("error while converting input to string!");
		}
		String inputString = writer.toString();
		return this.parseObservationCollection(inputString);
	}

}
