package org.uncertweb.sta.wps;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * tests the {@link AggregationAlgorithmRepository}
 * 
 * @author staschc
 *
 */
public class AggregationAlgorithmRepositoryTest extends TestCase {
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void testAggregationProcessConfiguration(){
		AggregationAlgorithmRepository config = new AggregationAlgorithmRepository();
		//assertEquals(config.getAllProcessIdentifiers().size(),1);
		//TODO add further assertions
	}
}
