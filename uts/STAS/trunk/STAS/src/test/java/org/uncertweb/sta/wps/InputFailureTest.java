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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.wps.server.AlgorithmParameterException;
import org.uncertweb.sta.wps.method.grouping.impl.ConvexHullGrouping;
import org.uncertweb.sta.wps.method.grouping.impl.OneContainingTimeRangeGrouping;
import org.uncertweb.sta.wps.testutils.ProcessTester;

public class InputFailureTest {

	ProcessTester p = null;

	@Before
	public void setUp() {
		p = new ProcessTester();
	}

	@Ignore
	@Test(expected = AlgorithmParameterException.class)
	public void noObservationCollection() {
		p.selectAlgorithm(ConvexHullGrouping.class, OneContainingTimeRangeGrouping.class);
		p.execute();
	}

}
