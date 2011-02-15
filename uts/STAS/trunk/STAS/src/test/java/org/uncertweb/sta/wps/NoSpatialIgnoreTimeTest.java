package org.uncertweb.sta.wps;

import org.joda.time.DateTime;
import org.junit.Test;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.intamap.utils.TimeUtils;
import org.uncertweb.sta.wps.method.aggregation.ArithmeticMeanAggregation;
import org.uncertweb.sta.wps.method.grouping.spatial.NoSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.IgnoreTimeGrouping;
import org.uncertweb.sta.wps.testutils.ProcessTester;


public class NoSpatialIgnoreTimeTest {

	private static final String BEGIN_DATE = "2001-01-01T00:00:00.000+00:00";
	private static final String DURATION = "P2D";
	private static final String OFFERING = "PM10";
	private static final String OBSERVED_PROPERTY = "http://giv-genesis.uni-muenster.de:8080/SOR/REST/phenomenon/OGC/Concentration[PM10]";
	private static final String SOURCE_SOS = "http://giv-uw.uni-muenster.de:8080/AQE/sos";
	private static final String DESTINATION_SOS = "http://localhost:8080/sos/sos";
	
	
	private static final String PRINT_FILE = "/home/auti/response.xml";
	
	@Test
	public void test() throws Exception {
		ProcessTester t = new ProcessTester();
		t.selectAlgorithm(NoSpatialGrouping.class, IgnoreTimeGrouping.class);
		t.setSpatialAggregationMethod(ArithmeticMeanAggregation.class);
		t.setTemporalAggregationMethod(ArithmeticMeanAggregation.class);
		t.setSosSourceUrl(SOURCE_SOS);
		t.setSosDestinationUrl(DESTINATION_SOS);
		t.setGroupByObservedProperty(true);
		DateTime b = TimeUtils.parseDateTime(BEGIN_DATE);
		DateTime e = b.plus(TimeUtils.parsePeriod(DURATION));
		t.setSosRequest(OFFERING, OBSERVED_PROPERTY, b, e);

		
		ObservationCollection oc = t.execute().getOutput();
		System.out.println(t.getReferenceOutput().xmlText(Namespace.defaultOptions()));
		ProcessTester.print(oc, PRINT_FILE);
	}

	public static void main(String[] args) throws Exception {
		new NoSpatialIgnoreTimeTest().test();
	}
}
