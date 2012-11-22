package org.uncertweb.api.om.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.uncertml.IUncertainty;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.distribution.multivariate.MultivariateNormalDistribution;
import org.uncertml.sample.ContinuousRealisation;
import org.uncertml.statistic.Probability;
import org.uncertweb.api.om.DQ_UncertaintyResult;
import org.uncertweb.api.om.OMConstants.Columns;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.result.MeasureResult;
import org.uncertweb.api.om.sampling.SpatialSamplingFeature;
import org.uncertweb.api.om.OMConstants;

import au.com.bytecode.opencsv.CSVWriter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * encoder for writing O&M in plain CSV in the following table structure:
 * 
 * PhenomenonTime WKTGeometry ObservedProperty Procedure Result UncertaintyType.param1 UncertaintyType.param2, etc.
 * 
 * ATTENTION: It is assumed that all observations have the same uncertainties (or no uncertainties) in the collection!!
 * 
 * @author staschc
 *
 */
public class CSVEncoder implements IObservationEncoder{
	

	private boolean isCollection=false;
	private Map<String,Integer> columnNumber4UncertaintyColName;
	

	@Override
	public synchronized String encodeObservationCollection(IObservationCollection obsCol)
			throws OMEncodingException {
		isCollection = true;
		StringWriter writer = new StringWriter();
		encodeObservationCollection(obsCol,writer);
		String result = writer.toString();
		writer.flush();
		try {
			writer.close();
		} catch (IOException e) {
			throw new OMEncodingException("Error while encoding observations to CSV file: "+e.getLocalizedMessage());
		}
		isCollection=false;
		return result;
	}

	@Override
	public synchronized void encodeObservationCollection(IObservationCollection obsCol,
			File f) throws OMEncodingException {
		try {
			isCollection = true;
			FileOutputStream fos = new FileOutputStream(f);
			encodeObservationCollection(obsCol,fos);
			isCollection=false;
		} catch (FileNotFoundException e) {
			throw new OMEncodingException("Error while encoding observations to CSV file: "+e.getLocalizedMessage());
		}
		
	}

	@Override
	public synchronized void encodeObservationCollection(IObservationCollection obsCol,
			OutputStream out) throws OMEncodingException {
		isCollection = true;
		OutputStreamWriter osWriter = new OutputStreamWriter(out);
		encodeObservationCollection(obsCol,osWriter);
		isCollection=false;
	}

	@Override
	public synchronized void encodeObservationCollection(IObservationCollection obsCol,
			Writer writer) throws OMEncodingException {
		
		isCollection = true;
		
		//initialize CSVEncoer
		CSVWriter encoder = new CSVWriter(writer);
				
		List<? extends AbstractObservation> obs = obsCol.getObservations();
		AbstractObservation o = obsCol.getObservations().get(0);
		// for realisations find the observations with longest list of realisations
		if(o instanceof UncertaintyObservation && ((UncertaintyObservation)o).getResult().getUncertaintyValue() instanceof ContinuousRealisation){
			for (int i=0;i<obs.size();i++){
				int currLength = ((ContinuousRealisation)((UncertaintyObservation)obs.get(i)).getResult().getUncertaintyValue()).getValues().size();
				if(currLength>((ContinuousRealisation)((UncertaintyObservation)o).getResult().getUncertaintyValue()).getValues().size())
					o = obs.get(i);
			}
		}
		
		//write columnnames
		encoder.writeNext(getColumnNames(o));
		for (int i=0;i<obs.size();i++){
			encoder.writeNext(getLine4Obs(obs.get(i)));
		}
		isCollection=false;
		try {
			encoder.flush();
			encoder.close();
		} catch (IOException e) {
			throw new OMEncodingException("Error while encoding observations to CSV file: "+e.getLocalizedMessage());
		}
	}

	@Override
	public synchronized String encodeObservation(AbstractObservation obs)
			throws OMEncodingException {
		StringWriter writer = new StringWriter();
		encodeObservation(obs,writer);
		String result = writer.toString();
		writer.flush();
		try {
			writer.close();
		} catch (IOException e) {
			throw new OMEncodingException("Error while encoding observations to CSV file: "+e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public synchronized void encodeObservation(AbstractObservation obs, File f)
			throws OMEncodingException {
		try {
			FileOutputStream fos = new FileOutputStream(f);
			encodeObservation(obs,fos);
		} catch (FileNotFoundException e) {
			throw new OMEncodingException("Error while encoding observations to CSV file: "+e.getLocalizedMessage());
		}
	}

	@Override
	public synchronized void encodeObservation(AbstractObservation obs, OutputStream out)
			throws OMEncodingException {
		OutputStreamWriter osWriter = new OutputStreamWriter(out);
		encodeObservation(obs,osWriter);
	}

	@Override
	public synchronized void encodeObservation(AbstractObservation obs, Writer writer)
			throws OMEncodingException {
		CSVWriter encoder = new CSVWriter(writer);
		
		//write columnnames
		if (!isCollection){
			encoder.writeNext(getColumnNames(obs));
		}
		//write observation values
		encoder.writeNext(getLine4Obs(obs));
		try {
			encoder.flush();
			encoder.close();
		} catch (IOException e) {
			throw new OMEncodingException("Error while encoding observations to CSV file: "+e.getLocalizedMessage());
		}
	}
	
	public String[] getColumnNames(AbstractObservation obs){
		ArrayList<String> columns = new ArrayList<String>();
		columns.add(Columns.PHEN_TIME);
		columns.add(Columns.FOI_IDENTIFIER);
		columns.add(Columns.WKT_GEOM);
		columns.add(Columns.SRID);
		columns.add(Columns.OBS_PROP);
		columns.add(Columns.PROCEDURE);
		columns.add(Columns.RESULT);
		
		IUncertainty uncertainty = null;
		if (obs instanceof UncertaintyObservation){
			uncertainty = ((UncertaintyObservation)obs).getResult().getUncertaintyValue();
		}
		else if(obs.getResultQuality()!=null){
			DQ_UncertaintyResult[] rqArray = obs.getResultQuality();
			if (rqArray.length==1){
				uncertainty = rqArray[0].getValues()[0];
			}
		}
		if (uncertainty!=null && uncertainty instanceof NormalDistribution){
			this.columnNumber4UncertaintyColName = new HashMap<String,Integer>();
			columns.add(Columns.CN_ND_MEAN);
			this.columnNumber4UncertaintyColName.put(Columns.CN_ND_MEAN, columns.indexOf(Columns.CN_ND_MEAN));
			columns.add(Columns.CN_ND_VAR);
			this.columnNumber4UncertaintyColName.put(Columns.CN_ND_VAR, columns.indexOf(Columns.CN_ND_VAR));
		}
		else if (uncertainty!=null && uncertainty instanceof MultivariateNormalDistribution){
			this.columnNumber4UncertaintyColName = new HashMap<String,Integer>();
			columns.add(Columns.CN_ND_MEAN);
			this.columnNumber4UncertaintyColName.put(Columns.CN_ND_MEAN, columns.indexOf(Columns.CN_ND_MEAN));
			columns.add(Columns.CN_ND_VAR);
			this.columnNumber4UncertaintyColName.put(Columns.CN_ND_VAR, columns.indexOf(Columns.CN_ND_VAR));
		}
		else if(uncertainty!=null && uncertainty instanceof ContinuousRealisation){
			this.columnNumber4UncertaintyColName = new HashMap<String,Integer>();
			for(int i=0; i<((ContinuousRealisation)uncertainty).getValues().size(); i++){
				columns.add(Columns.CN_REALISATION+i);
				this.columnNumber4UncertaintyColName.put(Columns.CN_REALISATION+i, columns.indexOf(Columns.CN_REALISATION+i));				
			}
		}
		else if(uncertainty!=null && uncertainty instanceof Probability){
			this.columnNumber4UncertaintyColName = new HashMap<String,Integer>();
			for(int i=0; i<((ContinuousRealisation)uncertainty).getValues().size(); i++){
				columns.add(Columns.CN_REALISATION+i);
				this.columnNumber4UncertaintyColName.put(Columns.CN_REALISATION+i, columns.indexOf(Columns.CN_REALISATION+i));				
			}
		}
		
		else{
			this.columnNumber4UncertaintyColName = new HashMap<String,Integer>();
		}
		String[] result = new String[columns.size()];
		columns.toArray(result);
		return result;
	}
	
	
	private String[] getLine4Obs(AbstractObservation obs){
		int totalSize = Columns.NUMBER_OF_COLUMNS + this.columnNumber4UncertaintyColName.size();
		String[] result = new String[totalSize];
		
		//set phenomenonTime
		String timeString = "";
		if(obs.getPhenomenonTime().isInterval()){
			timeString = obs.getPhenomenonTime().getInterval().getStart().toString()+"/"+
					obs.getPhenomenonTime().getInterval().getEnd().toString();
		}
		else{
			timeString = obs.getPhenomenonTime().getDateTime().toString();
		}
		result[0] = timeString;
		
		//set geometry
		SpatialSamplingFeature foi = obs.getFeatureOfInterest();
		Geometry geom = obs.getFeatureOfInterest().getShape();
		result[1] = foi.getIdentifier().toIdentifierString();
		result[2] = new WKTWriter().write(geom);
		result[3] = ""+geom.getSRID();
		
		//set observed property
		result[4] = obs.getObservedProperty().toASCIIString();
		
		//set procedure
		result[5] = obs.getProcedure().toASCIIString();
		
		//set resultValue
		if (obs instanceof Measurement){
			result[6] = ""+((MeasureResult)obs.getResult()).getMeasureValue();
			//TODO hack for supporting only one uncertainty per observation; might need to be updated
			IUncertainty uncertainty = obs.getResultQuality()[0].getValues()[0];
			if (uncertainty!=null && uncertainty instanceof NormalDistribution){
				int meanPos = this.columnNumber4UncertaintyColName.get(Columns.CN_ND_MEAN);
				int varPos = this.columnNumber4UncertaintyColName.get(Columns.CN_ND_VAR);
				result[meanPos]=""+((NormalDistribution)uncertainty).getMean().get(0);
				result[varPos]=""+((NormalDistribution)uncertainty).getVariance().get(0);
			}
		}
		else if (obs instanceof UncertaintyObservation){
			IUncertainty uncertainty = ((UncertaintyObservation)obs).getResult().getUncertaintyValue();
			if(uncertainty!=null && uncertainty instanceof ContinuousRealisation){
				for(int i=0; i<((ContinuousRealisation)uncertainty).getValues().size(); i++){
					int rPos = this.columnNumber4UncertaintyColName.get(Columns.CN_REALISATION+i);
					result[rPos]=""+((ContinuousRealisation)uncertainty).getValues().get(i);
				}
			}else if(uncertainty!=null && uncertainty instanceof NormalDistribution){
				int meanPos = this.columnNumber4UncertaintyColName.get(Columns.CN_ND_MEAN);
				int varPos = this.columnNumber4UncertaintyColName.get(Columns.CN_ND_VAR);
				result[meanPos]=""+((NormalDistribution)uncertainty).getMean().get(0);
				result[varPos]=""+((NormalDistribution)uncertainty).getVariance().get(0);}		
		} else if(obs instanceof CategoryObservation){
			result[6] = obs.getResult().getValue().toString();
		}
		else {
			throw new RuntimeException("CSVEncoder currently only supports Measurements and UncertaintyObservations");
		}
		return result;
		
	}

	

}

