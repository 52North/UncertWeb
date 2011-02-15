package org.uncertweb.sta.wps;

import org.junit.Before;
import org.junit.Test;
import org.n52.wps.server.AlgorithmParameterException;
import org.uncertweb.sta.wps.method.grouping.spatial.IgnoreSpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.temporal.IgnoreTimeGrouping;
import org.uncertweb.sta.wps.testutils.ProcessTester;

public class InputFailureTest {
	ProcessTester p = null;
	
	@Before
	public void setUp() {
		p = new ProcessTester();
	}
	
	@Test(expected=AlgorithmParameterException.class)
	public void noObservationCollection() {
		p.selectAlgorithm(IgnoreSpatialGrouping.class, IgnoreTimeGrouping.class);
		p.execute();
	}
	
}
