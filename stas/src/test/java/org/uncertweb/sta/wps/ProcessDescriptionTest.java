/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.server.IAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.sta.wps.testutils.ProcessTester;

public class ProcessDescriptionTest {

	private static final Logger log = LoggerFactory
			.getLogger(ProcessDescriptionTest.class);

	@Test
	public void testProcessDescription() throws ClassNotFoundException,
			IOException {
		for (IAlgorithm a : ProcessTester.getRepository().getAlgorithms()) {
			String id = a.getDescription().getIdentifier().getStringValue();
			Assert.assertTrue(id, a.processDescriptionIsValid());
			log.info("{}'s ProcessDescription is valid.", id);
			// a.getDescription()
			// .save(new File("/home/auti/" + id + ".xml"),
			// Namespace.defaultOptions());
		}
	}
}
