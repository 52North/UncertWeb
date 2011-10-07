package org.uncertweb.sta.wps;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;


/**
 * tests the {@link AggregationProcessConfiguration}
 * 
 * @author staschc
 *
 */
public class AggregationProcessConfigurationTest extends TestCase{

	@Before
	public void setUp() {
	}
	
	@Test
	public void testAggregationProcessConfiguration(){
		AggregationServiceConfiguration config = AggregationServiceConfiguration.getInstance();
		assertEquals(config.getAllProcessIdentifiers().size(),1);
		//TODO add further assertions
	}
}
