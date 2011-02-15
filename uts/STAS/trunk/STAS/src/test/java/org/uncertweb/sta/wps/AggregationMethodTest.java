package org.uncertweb.sta.wps;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.uncertweb.intamap.om.Observation;
import org.uncertweb.intamap.om.ObservationCollection;
import org.uncertweb.sta.wps.method.aggregation.ArithmeticMeanAggregation;
import org.uncertweb.sta.wps.method.aggregation.MedianAggregation;
import org.uncertweb.sta.wps.method.aggregation.SumAggregation;
import org.uncertweb.sta.wps.method.grouping.spatial.IgnoreSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.IgnoreTimeGrouping;
import org.uncertweb.sta.wps.testutils.ObservationFactory;
import org.uncertweb.sta.wps.testutils.ProcessTester;

public class AggregationMethodTest {
	private ProcessTester p = null;

	
	@Before
	public void setUp() {
		p = new ProcessTester();
		p.selectAlgorithm(IgnoreSpatialGrouping.class, IgnoreTimeGrouping.class);
//		p.observationCollection(buildOC());
	}
	
	@SuppressWarnings("unused")
	private ObservationCollection buildOC() {
		LinkedList<Observation> obs = new LinkedList<Observation>();
		ObservationFactory f = ObservationFactory.getInstance();
		DateTime begin = new DateTime();
		long minute = 60*1000;
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 1), 1, 5d + Math.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 2), 2, 5d + Math.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 3), 3, 5d + Math.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 4), 4, 5d + Math.random(), 52d + Math.random()));
		obs.add(f.createObservation(PROCESS, begin.plus(minute * 5), 10, 5d + Math.random(), 52d + Math.random()));
		return new ObservationCollection(obs);
	}

	private static final String PROCESS = "urn:ogc:object:sensor:test";

	@Test
	public void meanTest() throws XmlException {
		p.setSpatialAggregationMethod(ArithmeticMeanAggregation.class);
		p.setTemporalAggregationMethod(ArithmeticMeanAggregation.class);
		p.execute(); 
		ObservationCollection oc = p.getOutput();
		assertEquals(1, oc.size());
		assertEquals(4, oc.get(0).getResult(), Double.MIN_VALUE);
	}

	@Test
	public void medianTest() throws XmlException {
		p.setSpatialAggregationMethod(MedianAggregation.class);
		p.setTemporalAggregationMethod(MedianAggregation.class);
		p.execute();
		ObservationCollection oc = p.getOutput();
		assertEquals(1, oc.size());
		assertEquals(3, oc.get(0).getResult(), Double.MIN_VALUE);
	}

	@Test
	public void sumTest() {
		p.setSpatialAggregationMethod(SumAggregation.class);
		p.setTemporalAggregationMethod(SumAggregation.class);
		p.execute();
		ObservationCollection oc = p.getOutput();
		assertEquals(1, oc.size());
		assertEquals(20, oc.get(0).getResult(), Double.MIN_VALUE);
	}
}
