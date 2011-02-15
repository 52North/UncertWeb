package org.uncertweb.sta.wps;

import javax.xml.namespace.QName;

import net.opengis.gml.TimePeriodType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.GetObservationDocument.GetObservation.EventTime;
import net.opengis.sos.x10.ResponseModeType;

import org.apache.xmlbeans.XmlCursor;
import org.joda.time.DateTime;
import org.junit.Test;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.method.aggregation.ArithmeticMeanAggregation;
import org.uncertweb.sta.wps.method.grouping.spatial.NoSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.IgnoreTimeGrouping;
import org.uncertweb.sta.wps.testutils.ProcessTester;


public class NoSpatialIgnoreTimeTest {

	private static final String OFFERING = "PM10";
	private static final String OBSERVED_PROPERTY = "http://giv-genesis.uni-muenster.de:8080/SOR/REST/phenomenon/OGC/Concentration[PM10]";
	private static final String SOURCE_SOS = "http://giv-uw.uni-muenster.de:8080/AQE/sos";
	private static final String DESTINATION_SOS = "http://localhost:8080/sos/sos";
	private static final DateTime BEGIN_DATE = TimeUtils.parseDateTime("2001-01-01T00:00:00.000+00:00");
	private static final DateTime END_DATE = TimeUtils.parseDateTime("2001-01-02T00:00:00.000+00:00");

	
	
	private static GetObservationDocument buildGetObservationDocument(String offering, String obsProp, DateTime begin, DateTime end) {
		GetObservationDocument request = GetObservationDocument.Factory.newInstance();
		GetObservation getObs = request.addNewGetObservation();
		getObs.setOffering(offering);
		getObs.setService(Constants.SOS_SERVICE_NAME);
		getObs.setVersion(Constants.SOS_SERVICE_VERSION);
		getObs.setResultModel(Constants.MEASUREMENT_RESULT_MODEL);
		getObs.setResponseFormat(Constants.SOS_OBSERVATION_OUTPUT_FORMAT);
		getObs.setResponseMode(ResponseModeType.INLINE);
		getObs.addNewObservedProperty().setStringValue(obsProp);
		BinaryTemporalOpType btot = BinaryTemporalOpType.Factory.newInstance();
		btot.addNewPropertyName();
		XmlCursor cursor = btot.newCursor();
		cursor.toChild(new QName("http://www.opengis.net/ogc", "PropertyName"));
		cursor.setTextValue("om:SamplingTime");
		cursor.dispose();
        TimePeriodType xb_timePeriod = TimePeriodType.Factory.newInstance();
        xb_timePeriod.addNewBeginPosition().setStringValue(TimeUtils.format(begin));
        xb_timePeriod.addNewEndPosition().setStringValue(TimeUtils.format(end));
		btot.setTimeObject(xb_timePeriod);
		EventTime eventTime = getObs.addNewEventTime();
		eventTime.setTemporalOps(btot);
		cursor = eventTime.newCursor();
		cursor.toChild(new QName("http://www.opengis.net/ogc", "temporalOps"));
		cursor.setName(new QName("http://www.opengis.net/ogc", "TM_Equals"));
		cursor.toChild(new QName("http://www.opengis.net/gml", "_TimeObject"));
		cursor.setName(new QName("http://www.opengis.net/gml", "TimePeriod"));
		cursor.dispose();
		return request;
	}
	
	public static void main(String[] args) throws Exception {
//		System.out.println(buildGetObservationDocument(OFFERING, OBSERVED_PROPERTY, BEGIN_DATE, END_DATE).xmlText(Namespace.defaultOptions()));
		new NoSpatialIgnoreTimeTest().test();
	}
	
	@Test
	public void test() throws Exception {
		ProcessTester t = new ProcessTester();
		t.selectAlgorithm(NoSpatialGrouping.class, IgnoreTimeGrouping.class);
		t.setSpatialAggregationMethod(ArithmeticMeanAggregation.class);
		t.setTemporalAggregationMethod(ArithmeticMeanAggregation.class);
		t.setSosSourceUrl(SOURCE_SOS);
		t.setSosDestinationUrl(DESTINATION_SOS);
		t.setGroupByObservedProperty(true);
		t.setSosRequest(buildGetObservationDocument(OFFERING, OBSERVED_PROPERTY, BEGIN_DATE, END_DATE));

		
		ObservationCollection oc = t.execute().getOutput();
		System.out.println(t.getReferenceOutput().xmlText(Namespace.defaultOptions()));
		ProcessTester.print(oc, "/home/auti/response.xml");
	}
}
