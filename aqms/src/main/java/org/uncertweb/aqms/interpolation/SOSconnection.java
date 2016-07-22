package org.uncertweb.aqms.interpolation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.namespace.QName;

import net.opengis.gml.TimePeriodType;
import net.opengis.gml.TimePositionType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.GetObservationDocument.GetObservation.EventTime;


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.uncertweb.api.netcdf.NetcdfUWFile;
import org.uncertweb.api.netcdf.NetcdfUWFileWriteable;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;
import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.StaxObservationEncoder;
import org.uncertweb.api.om.io.XBObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.aqms.austal.AustalModelRun;
import org.uncertweb.aqms.overlay.TotalConcentration;
import org.uncertweb.om.io.v1.XBv1ObservationParser;

import ucar.nc2.NetcdfFile;

import com.vividsolutions.jts.geom.Coordinate;



public class SOSconnection {
	private static String serviceURL;
	private static String SRS_NAME = "urn:ogc:def:crs:EPSG::4326";
	private static String SERVICE_NAME = "SOS";
	private static String SERVICE_VERSION = "1.0.0";
	private static String OBSERVED_PROPERTY = "urn:ogc:def:phenomenon:OGC:1.0.30:PM10";
	private static String OFFERING = "PM10";
	private static String RESPONSE_FORMAT = "text/xml;subtype=&quot;om/1.0.0&quot;";

	public SOSconnection(String url){
		serviceURL = url;
	}

	/**
	 * Method to get Capabilities of the SOS. Can be used to retrieve offering and
	 * fid information for getObservation request.
	 */
	public void getCapabilities(){

	}

	/**
	 * Method to retrieve observations from the SOS for a defined time period.
	 * @param startDate
	 * @param endDate
	 * @return ObservationCollection
	 */
	public IObservationCollection getObservationAll(String startDate, String endDate){

		// build request document
		GetObservationDocument doc = GetObservationDocument.Factory.newInstance();

		// getObservation request
		GetObservation getObs = doc.addNewGetObservation();

		// set parameters
		getObs.setSrsName(SRS_NAME);
		getObs.setService(SERVICE_NAME);
		getObs.setVersion(SERVICE_VERSION);
		getObs.addObservedProperty(OBSERVED_PROPERTY);
		getObs.setOffering(OFFERING);

		// create Time Period
		BinaryTemporalOpType binTempOp = BinaryTemporalOpType.Factory.newInstance();
		binTempOp.addNewPropertyName();
        XmlCursor cursor = binTempOp.newCursor();
        cursor.toChild(new QName("http://www.opengis.net/ogc", "PropertyName"));
        cursor.setTextValue("om:samplingTime");

        // TimePeriod
        TimePeriodType timePeriod = TimePeriodType.Factory.newInstance();
        TimePositionType beginPosition = timePeriod.addNewBeginPosition();
        beginPosition.setStringValue(startDate);
        TimePositionType endPosition = timePeriod.addNewEndPosition();
        endPosition.setStringValue(endDate);
        binTempOp.setTimeObject(timePeriod);

		//<eventTime>
		EventTime eventTime = getObs.addNewEventTime();
		eventTime.setTemporalOps(binTempOp);

		// rename elements:
        cursor = eventTime.newCursor();
        //<ogc:TM_During>
        cursor.toChild(new QName("http://www.opengis.net/ogc", "temporalOps"));
        cursor.setName(new QName("http://www.opengis.net/ogc", "TM_During"));
        //<gml:TimePeriod>
        cursor.toChild(new QName("http://www.opengis.net/gml", "_TimeObject"));
        cursor.setName(new QName("http://www.opengis.net/gml", "TimePeriod"));

        // set response format
      	getObs.setResponseFormat("text/xml;subtype=&quot;om/1.0.0&quot;");

      	// set result model
      	QName resultModel = new QName("http://www.opengis.net/om/1.0", "Measurement", "om");
		getObs.setResultModel(resultModel);

		// workaround with character replacement for responseFormat and resultModel
		// TODO: find better solution for char replacement
		// remove amp; in the String
		String r1 = doc.toString();
		String r2 = r1.replace("amp;", "");

		// replace ns with om
		String r3 = r2.replace("ns:Measurement", "om:Measurement");
		String request = r3.replace("xmlns:ns", "xmlns:om");
		//System.out.println(request);

		 OutputStreamWriter wr = null;

		 try {
			// connect to SOS
			URL url = new URL(serviceURL);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/xml");

	        // send request
	        wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(request);
	        wr.flush();

	        // get response
	        InputStream in = conn.getInputStream();
	        XmlObject xml = XmlObject.Factory.parse(in);
	        IObservationCollection iobs = processResponse(xml);

	        //XmlObject xml = XmlObject.Factory.parse(in);
            //IObservationCollection iobs = new XBObservationParser().parseObservationCollection(xml);

	        in.close();
	        wr.close();
	        System.out.println("SOS request successfully finished!");
	        return iobs;
	        }
	        catch (Exception e) {
	        	e.printStackTrace();
	            return null;
	        }
	}

	/**
	 * Method to process SOS response. Returns IObservationCollection
	 * @param xml
	 * @param write
	 * @return IObservationCollection
	 * @throws IOException
	 */

    protected IObservationCollection processResponse(XmlObject xml) throws IOException {
            if (xml instanceof ObservationCollectionDocument){
            	// parse IObservationCollection
            	try {
            		IObservationCollection obsCol = new XBv1ObservationParser().parse(xml.xmlText());
        			return obsCol;
            	} catch (OMParsingException e) {
					e.printStackTrace();
					return null;
				}
            }
            else if (xml instanceof ExceptionReportDocument) {
            	System.out.println(xml.toString());
                return null;
            }

            else {
                return null;
            }

    }

    /***
     * Method to extract spatial information and measurements from the observation collection.
     * These are written as tables to be read by R for the interpolation.
     * @param iobs
     * @return Rtable
     */
//    public HashMap<DateTime, HashMap<String, Double[]>> obsCollection2Table(IObservationCollection iobs){
//    	HashMap<DateTime, HashMap<String, Double[]>> obsTS = new HashMap<DateTime, HashMap<String, Double[]>>();
//
//    	// loop through observations
//    	for (AbstractObservation obs : iobs.getObservations()) {
//    		// If it is a measurements object, it is easier
//    		String fid = obs.getFeatureOfInterest().getIdentifier().getIdentifier();
//    		Coordinate c = obs.getFeatureOfInterest().getShape().getCoordinate();
//
//			// get result values
//    		Double res = (Double) obs.getResult().getValue();
//    		Double[] vals = {c.x,c.y,res};
//
//    		// get sampling time
//    		DateTime st = obs.getResultTime().getDateTime();
//
//    		// if it's a new date add it to the list
//    		if(!obsTS.containsKey(st)){
//    			HashMap<String, Double[]> station = new HashMap<String,Double[]>();
//    			station.put(fid, vals);
//    			obsTS.put(st, station);
//    		}else{
//    			HashMap<String, Double[]> stat = obsTS.get(st);
//    			stat.put(fid, vals);
//    			obsTS.put(st, stat);
//    			// get existing map and add the current station values
////    			HashMap<String, Double[]> station = obsTS.get(st);
////    			station.put(fid, vals);
////    			obsTS.put(st, station);
//    		}
//
//    		// TODO: get coordinate reference system
//
//    	}
//
//    	return obsTS;
//    }
//

}
