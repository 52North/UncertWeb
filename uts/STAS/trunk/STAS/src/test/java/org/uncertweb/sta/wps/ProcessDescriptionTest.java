package org.uncertweb.sta.wps;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.server.IAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.wps.testutils.ProcessTester;

public class ProcessDescriptionTest {
	private static final Logger log = LoggerFactory.getLogger(ProcessDescriptionTest.class);
	
	@Test
	public void testProcessDescription() throws ClassNotFoundException, IOException {
		for (IAlgorithm a : ProcessTester.getRepository().getAlgorithms()) {
			String id = a.getDescription().getIdentifier().getStringValue();
			Assert.assertTrue(id, a.processDescriptionIsValid());
			log.info("{}'s ProcessDescription is valid.",id);
//			a.getDescription().save(new File("/home/auti/"+id+".xml"), STANamespace.defaultOptions());
		}
	}
}
