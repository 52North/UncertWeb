package org.uncertweb.api.om.io.test;

import junit.framework.TestCase;

import org.uncertweb.api.om.GeneralTimeInstant;
import org.uncertweb.api.om.TimeObject;

/**
 * TestCase for CSV Observation Encoder
 * 
 * @author staschc
 *
 */
public class GeneralTimeTestCase extends TestCase {
	
	public void testGeneralTime() throws Exception {
		String testString = "M03D4h14m30";
		GeneralTimeInstant t = new GeneralTimeInstant(testString);
		System.out.println(t.toString());
		TimeObject to = new TimeObject(testString);
		System.out.println(to.toString());
		TimeObject to1 = new TimeObject("2008-11-12T15:00:23Z");
		System.out.println(to1.toString());
	}
}